package jisa.visa;

import jisa.addresses.Address;
import jisa.addresses.StrAddress;
import jisa.addresses.USBAddress;

import javax.usb.*;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.List;

public class USBTMCDriver implements Driver {

    private static UsbHub rootHub = null;

    public static void init() throws UsbException {

        UsbServices services = UsbHostManager.getUsbServices();

        rootHub = services.getRootUsbHub();

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
        return new StrAddress[0];
    }

    @Override
    public boolean worksWith(Address address) {
        return address.getType() == Address.Type.USB;
    }

    public static class USBTMCConnection implements Connection {

        public final static byte DEV_DEP_MSG_OUT = (byte) 1;
        public final static byte DEV_DEP_MSG_IN  = (byte) 2;


        private final UsbInterface usbInterface;
        private final UsbPipe      bulkOut;
        private final UsbPipe      bulkIn;
        private final UsbPipe      interrupt;
        private       byte         messageID = (byte) 0;

        public USBTMCConnection(UsbInterface usbInterface) throws UsbException {

            usbInterface.claim();

            bulkOut   = usbInterface.getUsbEndpoint((byte) 1).getUsbPipe();
            bulkIn    = usbInterface.getUsbEndpoint((byte) 2).getUsbPipe();
            interrupt = usbInterface.getUsbEndpoint((byte) 3).getUsbPipe();

            if (!bulkOut.isOpen()) bulkOut.open();
            if (!bulkIn.isOpen()) bulkIn.open();
            if (!interrupt.isOpen()) interrupt.open();

            this.usbInterface = usbInterface;

        }

        @Override
        public synchronized void writeBytes(byte[] bytes) throws VISAException {

            int        sendSize   = 12 + bytes.length;
            int        extraBytes = ((sendSize % 4) == 0) ? sendSize : 4 - (sendSize % 4);
            byte[]     message    = new byte[sendSize + extraBytes];
            ByteBuffer buffer     = ByteBuffer.allocate(4);
            buffer.putInt(bytes.length);

            message[0]  = DEV_DEP_MSG_OUT;
            message[1]  = messageID;
            message[2]  = (byte) ~messageID;
            message[3]  = 0x00;
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

            byte[]     message = new byte[12];
            ByteBuffer buffer  = ByteBuffer.allocate(4);
            buffer.putInt(bufferSize);

            message[0]  = DEV_DEP_MSG_OUT;
            message[1]  = messageID;
            message[2]  = (byte) ~messageID;
            message[3]  = 0x00;
            buffer.get(message, 4, 4);
            message[8]  = 0x00;
            message[9]  = 0x00;
            message[10] = 0x00;
            message[11] = 0x00;

            try {

                int sent = bulkOut.syncSubmit(message);

                if (sent != 12) {
                    throw new VISAException("Sent bytes do not add up.");
                }

            } catch (UsbException e) {

                throw new VISAException(e.getMessage());

            }

            messageID++;

            try {

                byte[] response = new byte[bufferSize];
                int    received = bulkIn.syncSubmit(response);

                if (!(response[0] == DEV_DEP_MSG_IN && response[1] == message[1])) {
                    throw new VISAException("Improper response from device.");
                }

                int    dataSize = ByteBuffer.wrap(response, 4, 4).getInt();
                byte[] data     = new byte[dataSize];

                System.arraycopy(response, 12, data, 0, dataSize);

                return data;

            } catch (UsbException e) {

                throw new VISAException(e.getMessage());

            }

        }

        @Override
        public void setEOI(boolean set) throws VISAException {

        }

        @Override
        public void setEOS(long terminator) throws VISAException {

        }

        @Override
        public void setTMO(int duration) throws VISAException {

        }

        @Override
        public void setSerial(int baud, int data, Parity parity, StopBits stop, Flow flow) throws VISAException {

        }

        @Override
        public void close() throws VISAException {

            try {
                usbInterface.release();
            } catch (UsbException e) {
                throw new VISAException(e.getMessage());
            }

        }

    }

}
