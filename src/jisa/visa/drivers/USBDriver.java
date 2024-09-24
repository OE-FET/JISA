package jisa.visa.drivers;

import jisa.Util;
import jisa.addresses.Address;
import jisa.addresses.USBAddress;
import jisa.addresses.USBRawAddress;
import jisa.addresses.USBTMCAddress;
import jisa.visa.connections.Connection;
import jisa.visa.exceptions.VISAException;
import org.usb4java.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class USBDriver implements Driver {

    private static final Context context     = new Context();
    private static       boolean initialised = false;

    private static void initialise() throws VISAException {

        if (!initialised) {

            int result = LibUsb.init(context);

            if (result < 0) {
                throw new VISAException("Error initialising libUSB context.");
            } else {
                initialised = true;
            }

        }

    }

    private static boolean matches(DeviceDescriptor descriptor, USBAddress address) {

        return ((int) descriptor.idVendor() & 0xffff) == address.getVendorID()
            && ((int) descriptor.idProduct() & 0xffff) == address.getProductID();

    }

    private static boolean isTMC(InterfaceDescriptor descriptor) {
        return (descriptor.bInterfaceClass() == (byte) 0xFE) && (descriptor.bInterfaceSubClass() == (byte) 0x03);
    }

    private static boolean isRaw(InterfaceDescriptor descriptor) {
        return (descriptor.bInterfaceClass() == (byte) 0xFF) && (descriptor.bInterfaceSubClass() == (byte) 0xFF);
    }

    private static boolean isIsoBulk(EndpointDescriptor eDesc, boolean in) {

        if ((eDesc.bmAttributes() & LibUsb.TRANSFER_TYPE_MASK) != LibUsb.TRANSFER_TYPE_BULK) {
            return false;
        }

        if (in) {
            return (eDesc.bEndpointAddress() & LibUsb.ENDPOINT_DIR_MASK) == LibUsb.ENDPOINT_IN;
        } else {
            return (eDesc.bEndpointAddress() & LibUsb.ENDPOINT_IN) == LibUsb.ENDPOINT_OUT;
        }

    }

    @Override
    public Connection open(Address address) throws VISAException {

        if (!(address instanceof USBAddress)) {
            throw new VISAException("USB driver can only open USB (Raw or TMC) connections!");
        }

        int result;

        initialise();

        USBAddress rawAddress = (USBAddress) address;
        DeviceList list       = new DeviceList();

        result = LibUsb.getDeviceList(context, list);

        if (result < 0) {
            throw new VISAException("Error acquiring USB device list: %s", LibUsb.strError(result));
        }

        Device           device           = null;
        DeviceDescriptor deviceDescriptor = null;

        for (Device d : list) {

            DeviceDescriptor descriptor = new DeviceDescriptor();
            result = LibUsb.getDeviceDescriptor(d, descriptor);

            if (result < 0) {
                continue;
            }

            if (matches(descriptor, rawAddress)) {
                device           = d;
                deviceDescriptor = descriptor;
                break;
            }

        }

        LibUsb.freeDeviceList(list, false);

        if (device == null) {
            throw new VISAException("Could not find specified USB device.");
        }

        byte  nConfigs = deviceDescriptor.bNumConfigurations();
        byte  configID = -1;
        byte  iFaceID  = -1;
        byte  iFaceInd = -1;
        byte  altID    = -1;
        byte  endInID  = -1;
        byte  endOutID = -1;
        short maxPkt   = -1;

        search:
        for (byte i = 0; i < nConfigs; i++) {

            ConfigDescriptor configDescriptor = new ConfigDescriptor();
            result = LibUsb.getConfigDescriptor(device, i, configDescriptor);

            if (result < 0) {
                continue;
            }

            for (Interface iFace : configDescriptor.iface()) {

                for (InterfaceDescriptor iDesc : iFace.altsetting()) {

                    if ((address instanceof USBTMCAddress && isTMC(iDesc)) || (address instanceof USBRawAddress && isRaw(iDesc))) {

                        EndpointDescriptor endIn = Arrays.stream(iDesc.endpoint())
                                                         .filter(e -> isIsoBulk(e, true))
                                                         .findFirst()
                                                         .orElse(null);

                        EndpointDescriptor endOut = Arrays.stream(iDesc.endpoint())
                                                          .filter(e -> isIsoBulk(e, false))
                                                          .findFirst()
                                                          .orElse(null);

                        if (endIn != null && endOut != null) {

                            configID = configDescriptor.bConfigurationValue();
                            iFaceID  = iDesc.bInterfaceNumber();
                            iFaceInd = iDesc.iInterface();
                            altID    = iDesc.bAlternateSetting();
                            endInID  = endIn.bEndpointAddress();
                            endOutID = endOut.bEndpointAddress();
                            maxPkt   = endOut.wMaxPacketSize();

                            break search;

                        }


                    }

                }

            }

            LibUsb.freeConfigDescriptor(configDescriptor);

        }

        if (configID < 0 || iFaceID < 0 || altID < 0) {
            throw new VISAException("Unable to find USB interface in specified device.");
        }

        DeviceHandle handle = new DeviceHandle();

        result = LibUsb.open(device, handle);

        if (result < 0) {
            throw new VISAException("Error opening USB device: %s", LibUsb.strError(result));
        }

        result = LibUsb.resetDevice(handle);

        if (result < 0) {
            throw new VISAException("Error resetting USB device: %s", LibUsb.strError(result));
        }

        if (LibUsb.kernelDriverActive(handle, iFaceID) == 1) {

            result = LibUsb.detachKernelDriver(handle, iFaceID);

            if (result < 0) {
                throw new VISAException("Error detaching kernel driver: %s", LibUsb.strError(result));
            }

        }

        result = LibUsb.setConfiguration(handle, configID);

        if (result < 0) {
            throw new VISAException("Error setting USB device configuration: %s", LibUsb.strError(result));
        }

        result = LibUsb.claimInterface(handle, iFaceID);

        if (result < 0) {
            throw new VISAException("Error claiming USB interface (c: %d, i: %d): %s", configID, iFaceID, LibUsb.strError(result));
        }

        result = LibUsb.setInterfaceAltSetting(handle, iFaceID, altID);

        if (result < 0) {
            throw new VISAException("Error setting interface alt setting (c: %d, i: %d, a: %d): %s", configID, iFaceID, altID, LibUsb.strError(result));
        }

        if (address instanceof USBRawAddress) {
            return new USBConnection(handle, endInID, endOutID, maxPkt, iFaceID, iFaceInd);
        } else if (address instanceof USBTMCAddress) {
            return new USBTMCConnection(handle, endInID, endOutID, maxPkt, iFaceID, iFaceInd);
        } else {
            throw new VISAException("Unexpected type of USB address.");
        }

    }

    @Override
    public List<Address> search() {

        try {
            initialise();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }

        DeviceList    list      = new DeviceList();
        List<Address> addresses = new LinkedList<>();

        LibUsb.getDeviceList(context, list);

        for (Device device : list) {

            DeviceDescriptor descriptor = new DeviceDescriptor();

            LibUsb.getDeviceDescriptor(device, descriptor);

            byte         nConf  = descriptor.bNumConfigurations();
            DeviceHandle handle = new DeviceHandle();
            int          result = LibUsb.open(device, handle);

            String serialNumber;

            if (result < 0) {
                serialNumber = "";
            } else {
                serialNumber = LibUsb.getStringDescriptor(handle, descriptor.iSerialNumber());
                LibUsb.close(handle);
            }


            for (byte i = 0; i < nConf; i++) {

                ConfigDescriptor config = new ConfigDescriptor();
                LibUsb.getConfigDescriptor(device, i, config);

                if (Arrays.stream(config.iface()).flatMap(in -> Arrays.stream(in.altsetting())).anyMatch(USBDriver::isTMC)) {
                    addresses.add(new USBTMCAddress(descriptor.idVendor() & 0xffff, descriptor.idProduct() & 0xffff, serialNumber));
                    break;

                } else if (Arrays.stream(config.iface()).flatMap(in -> Arrays.stream(in.altsetting())).anyMatch(USBDriver::isRaw)) {
                    addresses.add(new USBRawAddress(descriptor.idVendor() & 0xffff, descriptor.idProduct() & 0xffff, serialNumber));
                }

                LibUsb.freeConfigDescriptor(config);

            }

        }

        return addresses;

    }

    @Override
    public boolean worksWith(Address address) {
        return address instanceof USBAddress;
    }

    @Override
    public void reset() throws VISAException {

    }

    private static class USBConnection implements jisa.visa.connections.USBConnection {

        protected final DeviceHandle handle;
        protected final byte         bulkIn;
        protected final byte         bulkOut;
        protected final short        maxPkt;
        protected final int          iFace;
        protected final int          iFaceInd;
        protected       int          timeOut = 500;
        protected       Charset      charset = StandardCharsets.UTF_8;

        private USBConnection(DeviceHandle device, byte bulkIn, byte bulkOut, short maxPkt, int iFace, int iFaceInd) {
            this.handle   = device;
            this.bulkIn   = bulkIn;
            this.bulkOut  = bulkOut;
            this.maxPkt   = maxPkt;
            this.iFace    = iFace;
            this.iFaceInd = iFaceInd;
        }

        @Override
        public void writeBytes(byte[] bytes) throws VISAException {

            ByteBuffer outBuf = ByteBuffer.allocateDirect(bytes.length);
            IntBuffer  outNum = IntBuffer.allocate(1);

            outBuf.put(bytes);
            outBuf.rewind();

            int result = LibUsb.bulkTransfer(handle, bulkOut, outBuf, outNum, timeOut);

            if (result < 0) {
                System.err.printf("Unable to send USB data (endpoint " + bulkOut + "): %s%n", LibUsb.strError(result));
                throw new VISAException("Unable to send USB data (endpoint " + bulkOut + "): %s", LibUsb.strError(result));
            }

            if (outNum.get(0) != bytes.length) {
                throw new VISAException("USB: Expecting to send %d bytes, but %d were sent instead.", bytes.length, outNum.get(0));
            }

        }

        @Override
        public void clear() throws VISAException {

            int result = LibUsb.resetDevice(handle);

            if (result < 0) {
                throw new VISAException("Error resetting USB device.");
            }

        }

        @Override
        public void setEncoding(Charset charset) {
            this.charset = charset;
        }

        @Override
        public Charset getEncoding() {
            return charset;
        }

        @Override
        public byte[] readBytes(int bufferSize) throws VISAException {

            bufferSize = Math.max(1, Math.min(bufferSize, maxPkt));

            ByteBuffer inBuf  = ByteBuffer.allocateDirect(bufferSize);
            IntBuffer  inNum  = IntBuffer.allocate(1);
            int        result = LibUsb.bulkTransfer(handle, bulkIn, inBuf, inNum, timeOut);

            if (result < 0) {
                throw new VISAException("Unable to receive USB data: %s", LibUsb.strError(result));
            }
            byte[] data = new byte[inNum.get(0)];


            inBuf.rewind();
            for (int i = 0; i < data.length; i++) {
                data[i] = inBuf.get();
            }

            return data;

        }

        @Override
        public void setReadTerminator(long terminator) throws VISAException {

        }

        @Override
        public void setTimeout(int duration) throws VISAException {
            this.timeOut = duration;
        }

        @Override
        public void close() throws VISAException {

            try {

                int error = LibUsb.releaseInterface(handle, iFace);

                if (error < 0) {
                    throw new VISAException("Unable to release interface: %s", LibUsb.strError(error));
                }

            } finally {
                LibUsb.close(handle);
            }

        }

    }

    private static class USBTMCConnection extends USBConnection {

        private static final byte RESERVED               = 0x00;
        private static final byte DEV_DEP_MSG_OUT        = 0x01;
        private static final byte REQUEST_DEV_DEP_MSG_IN = 0x02;
        private static final byte DEV_DEP_MSG_IN         = 0x02;

        private       byte    bTag = 0;
        private final boolean usb488;
        private final boolean ren;
        private final boolean trigger;

        protected ByteBuffer createBuffer(int capacity) {
            return ByteBuffer.allocate(capacity).order(ByteOrder.LITTLE_ENDIAN);
        }

        private USBTMCConnection(DeviceHandle device, byte bulkIn, byte bulkOut, short maxPkt, int iFace, int iFaceInd) throws VISAException {

            super(device, bulkIn, bulkOut, maxPkt, iFace, iFaceInd);

            Util.sleep(500);

            ByteBuffer ret = ByteBuffer.allocateDirect(24);

            // Determine USB-TMC capabilities
            LibUsb.controlTransfer(
                handle,
                (byte) (LibUsb.RECIPIENT_INTERFACE | LibUsb.REQUEST_TYPE_CLASS | LibUsb.ENDPOINT_IN),
                (byte) 0x7,
                (short) 0x0000,
                (short) 0x0000,
                ret,
                timeOut
            );

            byte capabilities = ret.get(14);

            usb488  = (capabilities & 4) != 0;
            ren     = (capabilities & 2) != 0;
            trigger = (capabilities & 1) != 0;

            ret = ByteBuffer.allocateDirect(1);

            // If it is capable of asserting REN (Remote ENable), then do so
            if (ren) {
                LibUsb.controlTransfer(
                    handle,
                    (byte) (LibUsb.RECIPIENT_INTERFACE | LibUsb.REQUEST_TYPE_CLASS | LibUsb.ENDPOINT_IN),
                    (byte) 0xA0,
                    (short) 0x0001,
                    (short) 0x0000,
                    ret,
                    timeOut
                );

            }

            if (ret.rewind().get() != (byte) 0x01) {
                throw new VISAException("Failed to assert REN!");
            }

        }

        private byte getBTag() {
            bTag = (byte) ((bTag % 255) + 1);
            return bTag;
        }

        private byte invert(byte tag) {
            return (byte) ((~tag) % 255);
        }

        @Override
        public void writeBytes(byte[] bytes) throws VISAException {

            int begin    = 0;
            int end      = 0;
            int length   = bytes.length;
            int size     = 0;
            int total    = 0;
            int capacity = 0;

            while (end < length) {

                begin    = end;
                end      = Math.min(length, end + (int) maxPkt - 12);
                size     = end - begin;
                total    = 12 + size;
                capacity = ((total + 3) / 4) * 4; // Packets must be a multiple of 4 in length, so round up

                byte bTag     = getBTag();
                int  dataSize = end - begin;

                ByteBuffer toSend = createBuffer(capacity);

                toSend.put(DEV_DEP_MSG_OUT)
                      .put(bTag)
                      .put(invert(bTag))
                      .put(RESERVED)
                      .putInt(size)
                      .put(end >= length ? (byte) 0x01 : (byte) 0x00)
                      .put(RESERVED)
                      .put(RESERVED)
                      .put(RESERVED);

                for (int i = begin; i < end; i++) {
                    toSend.put(bytes[i]);
                }

                super.writeBytes(toSend.rewind().array());

            }

        }

        @Override
        public byte[] readBytes(int bufferSize) throws VISAException {

            bufferSize = Math.min(bufferSize, maxPkt - 12);

            byte       bTag   = getBTag();
            ByteBuffer toSend = createBuffer(12);

            toSend.put(REQUEST_DEV_DEP_MSG_IN)  // Bulk-Out Header
                  .put(bTag)
                  .put(invert(bTag))
                  .put(RESERVED)
                  .putInt(bufferSize)           // Max bytes to return
                  .put((byte) 0)                // TermChar settings
                  .put((byte) 0)                // TermChar value
                  .put(RESERVED)
                  .put(RESERVED);

            super.writeBytes(toSend.rewind().array());

            byte[] response = super.readBytes(bufferSize + 24);

            if (response[0] != DEV_DEP_MSG_IN) {
                throw new VISAException("(USB-TMC) Unexpected message header in response to read request.");
            }

            int     transferSize = ByteBuffer.wrap(response, 4, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
            boolean eom          = (response[8] & 0x01) != 0;
            byte[]  data         = new byte[transferSize];

            System.arraycopy(response, 12, data, 0, transferSize);

            return data;

        }
    }

}