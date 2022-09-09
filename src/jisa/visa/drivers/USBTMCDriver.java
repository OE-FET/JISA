package jisa.visa.drivers;

import jisa.addresses.Address;
import jisa.addresses.USBAddress;
import jisa.visa.VISAException;
import jisa.visa.connections.Connection;
import jisa.visa.connections.USBConnection;
import org.usb4java.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Implements a bulk transfer I/O driver that uses usb4java to communicate with a USB Device
 * using the Usb4Java Library.
 * <p>
 * See: http://usb4java.org, and http://usb4java.org/apidocs/index.html for more info
 * <p>
 * http://libusb.sourceforge.net/api-1.0/
 */

public class USBTMCDriver implements Driver {

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
        boolean  matches;
        try
        {
            matches = descriptor.idVendor() == address.getManufacturer()
                    && descriptor.idProduct() == address.getModel()
                    && (address.getSerialNumber() == null || descriptor.iSerialNumber() == Integer.parseInt(address.getSerialNumber()));
        }
        catch (NumberFormatException e) {
            matches = false; // no match
        }

        return matches;

    }

    private static boolean isTMC(InterfaceDescriptor descriptor) {
        return (descriptor.bInterfaceClass() == (byte) 0xFE) && (descriptor.bInterfaceSubClass() == (byte) 0x03);
    }

    @Override
    public Connection open(Address address) throws VISAException {

        int result;

        initialise();

        USBAddress usbAddress = address.toUSBAddress();

        if (usbAddress == null) {
            throw new VISAException("This driver can only open USB-TMC connections.");
        }

        DeviceList list = new DeviceList();

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

            if (matches(descriptor, usbAddress)) {
                device           = d;
                deviceDescriptor = descriptor;
                break;
            }

        }

        LibUsb.freeDeviceList(list, false);

        if (device == null) {
            throw new VISAException("Could not find specified USB-TMC device.");
        }

        byte  nConfigs = deviceDescriptor.bNumConfigurations();
        byte  configID = -1;
        byte  iFaceID  = -1;
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

                    if (isTMC(iDesc)) {

                        EndpointDescriptor endIn = Arrays.stream(iDesc.endpoint())
                                                         .filter(e -> ((e.bmAttributes() & 0x03) != 0) && ((e.bEndpointAddress() & 0x80) != 0))
                                                         .findFirst()
                                                         .orElse(null);

                        EndpointDescriptor endOut = Arrays.stream(iDesc.endpoint())
                                                          .filter(e -> ((e.bmAttributes() & 0x03) != 0) && ((e.bEndpointAddress() & 0x80) == 0))
                                                          .findFirst()
                                                          .orElse(null);

                        if (endIn != null && endOut != null) {

                            configID = configDescriptor.iConfiguration();
                            iFaceID  = iDesc.bInterfaceNumber();
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
            throw new VISAException("Unable to find USB-TMC interface in specified device.");
        }

        DeviceHandle handle = new DeviceHandle();

        result = LibUsb.open(device, handle);

        if (result < 0) {
            throw new VISAException("Error opening USB device: %s", LibUsb.strError(result));
        }

        result = LibUsb.setConfiguration(handle, configID);

        if (result < 0) {
            throw new VISAException("Error setting USB device configuration: %s", LibUsb.strError(result));
        }

        if (LibUsb.kernelDriverActive(handle, iFaceID) == 1) {

            result = LibUsb.detachKernelDriver(handle, iFaceID);

            if (result < 0) {
                throw new VISAException("Error detaching kernel driver: %s", LibUsb.strError(result));
            }

        }

        result = LibUsb.claimInterface(handle, iFaceID);

        if (result < 0) {
            throw new VISAException("Error claiming USB interface (c: %d, i: %d): %s", configID, iFaceID, LibUsb.strError(result));
        }

        result = LibUsb.setInterfaceAltSetting(handle, iFaceID, altID);

        if (result < 0) {
            throw new VISAException("Error setting interface alt setting (c: %d, i: %d, a: %d): %s", configID, iFaceID, altID, LibUsb.strError(result));
        }

        return new USBTMCConnection(handle, endInID, endOutID, maxPkt, iFaceID);

    }

    @Override
    public List<Address> search() throws VISAException {

        initialise();

        DeviceList    list      = new DeviceList();
        List<Address> addresses = new LinkedList<>();

        LibUsb.getDeviceList(context, list);

        for (Device device : list) {

            DeviceDescriptor descriptor = new DeviceDescriptor();

            LibUsb.getDeviceDescriptor(device, descriptor);

            byte nConf = descriptor.bNumConfigurations();

            for (byte i = 0; i < nConf; i++) {

                ConfigDescriptor config = new ConfigDescriptor();
                if (LibUsb.getConfigDescriptor(device, i, config) == 0) //http://usb4java.org/apidocs/org/usb4java/LibUsb.html#getConfigDescriptor-org.usb4java.Device-byte-org.usb4java.ConfigDescriptor-
                {
                    if (Arrays.stream(config.iface()).flatMap(in -> Arrays.stream(in.altsetting())).anyMatch(USBTMCDriver::isTMC)) {

                        addresses.add(new USBAddress(descriptor.idVendor(), descriptor.idProduct()));
                        break;
                    }
                }

            }

        }

        return addresses;

    }

    @Override
    public boolean worksWith(Address address) {
        return address.getType() == Address.Type.USB;
    }

    private static class USBTMCConnection implements USBConnection {

        private final DeviceHandle handle;
        private final byte         bulkIn;
        private final byte         bulkOut;
        private final short        maxPkt;
        private final int          iFace;
        private       int          timeOut = 500;
        private       Charset      charset = StandardCharsets.UTF_8;

        private USBTMCConnection(DeviceHandle device, byte bulkIn, byte bulkOut, short maxPkt, int iFace) {
            this.handle  = device;
            this.bulkIn  = bulkIn;
            this.bulkOut = bulkOut;
            this.maxPkt  = maxPkt;
            this.iFace   = iFace;
        }

        @Override
        public void writeBytes(byte[] bytes) throws VISAException {

            ByteBuffer outBuf = BufferUtils.allocateByteBuffer(bytes.length);

            outBuf.put(bytes);

            IntBuffer outNum = IntBuffer.allocate(1);
            int       result = LibUsb.bulkTransfer(handle, bulkOut, outBuf, outNum, timeOut);

            if (result < 0) {
                throw new VISAException("Unable to send USB-TMC data: %d", result);
            }

        }

        @Override
        public void clear() throws VISAException {

            int result = LibUsb.resetDevice(handle);

            if (result < 0) {
                throw new VISAException("Error resetting USB-TMC device.");
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

            ByteBuffer inBuf = ByteBuffer.allocateDirect(maxPkt * 2).order(ByteOrder.LITTLE_ENDIAN);
            IntBuffer  inNum = IntBuffer.allocate(1);
            int        result;
            int        retry = 3;

            do {

                if ((result = LibUsb.bulkTransfer(handle, bulkIn, inBuf, inNum, timeOut)) >= 0) {

                    if (inBuf.hasArray()) {

                        return inBuf.array();

                    } else {

                        int    cnt  = inNum.get(0);
                        int    cap  = inBuf.capacity();
                        byte[] data = new byte[cnt];

                        for (int ii = 0; ii < cnt && ii < cap; ii++) {
                            data[ii] = inBuf.get();
                        }

                        inBuf.clear();

                        return data;

                    }

                }

            } while (result == LibUsb.ERROR_TIMEOUT && --retry > 0);

            throw new VISAException("Unable to receive USB-TMC data: %d", result);

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

}