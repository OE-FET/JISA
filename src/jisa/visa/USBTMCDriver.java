package jisa.visa;

import jisa.Util;
import jisa.addresses.Address;
import jisa.addresses.StrAddress;
import jisa.addresses.USBAddress;

import javax.usb.*;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class USBTMCDriver implements Driver {

    private static UsbHub rootHub = null;

    public static void init() throws VISAException {

        try {
            rootHub = UsbHostManager.getUsbServices().getRootUsbHub();
        } catch (Exception e) {
            e.printStackTrace();
            throw new VISAException(e.getMessage());
        }

    }

    @Override
    public Connection open(Address address) throws VISAException {

        if (address.getType() != Address.Type.USB) {
            throw new VISAException("The USB-TMC driver can only open USB-TMC connections.");
        }

        USBAddress usbAddress = address.toUSBAddress();
        short      vendorID   = (short) usbAddress.getManufacturer();
        short      productID  = (short) usbAddress.getModel();
        byte       intfce     = (byte) usbAddress.getInterfaceNumber();
        String     serial     = usbAddress.getSerialNumber();

        try {

            UsbDevice device = findDevice(rootHub, vendorID, productID, serial);

            if (device == null) {
                throw new VISAException("No USB device with that descriptor was found.");
            }

            UsbConfiguration configuration = device.getActiveUsbConfiguration();
            UsbInterface     usbInterface  = null;
            if (intfce == -1) {

                usbInterface = ((List<UsbInterface>) configuration.getUsbInterfaces())
                    .stream()
                    .filter(intf -> {
                        UsbInterfaceDescriptor iDesc = intf.getUsbInterfaceDescriptor();
                        boolean                clss  = iDesc.bInterfaceClass() == (byte) 0xFE;
                        boolean                scls  = iDesc.bInterfaceSubClass() == (byte) 0x03;
                        return clss && scls;
                    }).findFirst().orElse(null);

            } else {
                usbInterface = configuration.getUsbInterface(intfce);
                UsbInterfaceDescriptor iDesc = usbInterface.getUsbInterfaceDescriptor();
                boolean                clss  = iDesc.bInterfaceClass() == (byte) 0xFE;
                boolean                scls  = iDesc.bInterfaceSubClass() == (byte) 0x03;
                if (!(clss && scls)) {
                    usbInterface = null;
                }
            }

            if (usbInterface == null) {
                throw new VISAException("No USB-TMC interface was found on that device.");
            }

            return new USBTMCConnection(usbInterface);

        } catch (Exception e) {
            throw new VISAException(e.getMessage());
        }

    }

    private UsbDevice findDevice(UsbHub hub, short vendorID, short productID, String serial) throws UnsupportedEncodingException, UsbException {

        for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices()) {

            UsbDeviceDescriptor descriptor = device.getUsbDeviceDescriptor();

            if (descriptor.idVendor() == vendorID
                && descriptor.idProduct() == productID
                && device.getSerialNumberString().trim().equals(serial.trim())) {

                return device;

            }

            if (device.isUsbHub()) {

                device = findDevice((UsbHub) device, vendorID, productID, serial);

                if (device != null) {
                    return device;
                }

            }

        }

        return null;

    }

    @Override
    public StrAddress[] search() throws VISAException {

        return findDevices(rootHub)
            .stream()
            .map(USBAddress::fromUSBDevice)
            .map(USBAddress::toStrAddress)
            .toArray(StrAddress[]::new);

    }

    private List<UsbDevice> findDevices(UsbHub hub) {

        List<UsbDevice> devices = new LinkedList<>();

        for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices()) {

            devices.add(device);

            if (device.isUsbHub()) {
                devices.addAll(findDevices((UsbHub) device));
            }

        }

        return devices;

    }

    @Override
    public boolean worksWith(Address address) {
        return address.getType() == Address.Type.USB;
    }

    private interface USBFrame {

        void loadByteArray(byte[] array);

        void appendByteArray(byte[] array);

        byte[] getByteArray();

        byte[] getData();

        void setData(byte[] data);

    }

    public static class USBTMCConnection implements Connection {

        public final static byte DEV_DEP_MSG_OUT = (byte) 1;
        public final static byte DEV_DEP_MSG_IN  = (byte) 2;

        private final UsbInterface usbInterface;
        private final UsbPipe      bulkOut;
        private final UsbPipe      bulkIn;
        private       byte         messageID           = (byte) 0;
        private       boolean      useEOI              = true;
        private       byte[]       terminationSequence = "\n".getBytes();
        private       int          timeOut             = 200;

        public USBTMCConnection(UsbInterface usbInterface) throws UsbException {

            usbInterface.claim();

            bulkOut = usbInterface.getUsbEndpoint((byte) 1).getUsbPipe();
            bulkIn  = usbInterface.getUsbEndpoint((byte) 2).getUsbPipe();

            if (!bulkOut.isOpen()) { bulkOut.open(); }
            if (!bulkIn.isOpen()) { bulkIn.open(); }

            this.usbInterface = usbInterface;

        }

        @Override
        public synchronized void writeBytes(byte[] bytes) throws VISAException {

            int        sendSize   = 12 + bytes.length;
            int        extraBytes = ((sendSize % 4) == 0) ? sendSize : 4 - (sendSize % 4);
            byte[]     message    = new byte[sendSize + extraBytes];
            ByteBuffer buffer     = ByteBuffer.allocate(4);
            buffer.putInt(bytes.length);

            message[0] = DEV_DEP_MSG_OUT;
            message[1] = (byte) (messageID & 256);
            message[2] = (byte) ~(messageID & 256);
            message[3] = 0x00;
            buffer.get(message, 4, 4);
            message[8]  = 0x01;
            message[9]  = 0x00;
            message[10] = 0x00;
            message[11] = 0x00;

            System.arraycopy(bytes, 0, message, 12, bytes.length);

            try {
                bulkOut.syncSubmit(message);
            } catch (UsbException e) {
                throw new VISAException(e.getMessage());
            } finally {
                messageID++;
            }

        }

        @Override
        public synchronized byte[] readBytes(int bufferSize) throws VISAException {

            byte[]     request = new byte[12];
            ByteBuffer size    = ByteBuffer.allocate(4);
            size.putInt(bufferSize);

            request[0]  = DEV_DEP_MSG_OUT;
            request[1]  = (byte) (messageID & 256);
            request[2]  = (byte) ~(messageID & 256);
            request[3]  = 0x00;
            size.get(request, 4, 4);
            request[8]  = 0x00;
            request[9]  = 0x00;
            request[10] = 0x00;
            request[11] = 0x00;

            try {

                int sent = bulkOut.syncSubmit(request);

                if (sent != 12) {
                    throw new VISAException("Sent bytes do not add up.");
                }

            } catch (UsbException e) {

                throw new VISAException(e.getMessage());

            }

            messageID++;

            ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
            long       start  = System.currentTimeMillis();
            int        count  = 0;


            try {

                byte[]  response = new byte[bufferSize];
                int     received = bulkIn.syncSubmit(response);
                boolean eom      = (response[8] == 0x01);

                if (!(response[0] == DEV_DEP_MSG_IN && response[1] == request[1])) {
                    throw new VISAException("Improper response from device.");
                }

                int dataSize = ByteBuffer.wrap(response, 4, 4).getInt();

                buffer.put(response, 12, dataSize);

                count += dataSize;

            } catch (UsbException e) {

                throw new VISAException(e.getMessage());

            }


            return Util.trimArray(buffer.array());

        }

        @Override
        public void setEOI(boolean set) throws VISAException {
            useEOI = set;
        }

        @Override
        public void setEOS(long character) throws VISAException {

            ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
            buffer.putLong(character);

            int pos = 0;

            for (int i = 0; i < Long.BYTES; i++) {
                if (buffer.get(i) > 0) {
                    pos = i;
                    break;
                }
            }

            terminationSequence = new byte[Long.BYTES - pos];
            System.arraycopy(buffer.array(), pos, terminationSequence, 0, terminationSequence.length);

        }

        @Override
        public void setTMO(int duration) throws VISAException {
            timeOut = duration;
        }

        @Override
        public void close() throws VISAException {

            try {
                bulkIn.close();
                bulkOut.close();
                usbInterface.release();
            } catch (UsbException e) {
                throw new VISAException(e.getMessage());
            }

        }

    }

    private static class USBTMCOutData implements USBFrame {

        private static int tagValue = 0x00;

        private byte   id;
        private byte[] data;

        public USBTMCOutData(byte[] data) {

            this.id   = (byte) ((tagValue++ % 0xFF) + 1);
            this.data = data;

        }

        @Override
        public void loadByteArray(byte[] array) {

            id = array[1];
            int size = ByteBuffer.wrap(array, 4, 4).getInt();
            data = new byte[size];
            System.arraycopy(array, 12, data, 0, size);

        }

        @Override
        public void appendByteArray(byte[] array) {

            int    size    = ByteBuffer.wrap(array, 4, 4).getInt();
            byte[] newData = new byte[data.length + size];
            System.arraycopy(array, 12, newData, data.length, size);
            data = newData;

        }

        @Override
        public byte[] getByteArray() {

            int    length    = data.length + 12;
            int    alignment = (4 - (length % 4)) % 4;
            byte[] array     = new byte[length + alignment];

            Arrays.fill(array, (byte) 0x00);

            array[0] = 0x01;
            array[1] = id;
            array[2] = (byte) ~id;
            array[3] = 0x00;
            System.arraycopy(ByteBuffer.allocate(4).putInt(data.length).array(), 0, array, 4, 4);
            array[8] = 0x01;
            System.arraycopy(data, 0, array, 12, data.length);

            return array;

        }

        @Override
        public byte[] getData() {
            return data;
        }

        @Override
        public void setData(byte[] data) {
            this.data = data;
        }
    }

}
