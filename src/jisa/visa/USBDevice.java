package jisa.visa;

import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.Instrument;
import jisa.visa.drivers.USBDriver;
import org.usb4java.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class USBDevice implements Instrument {

    private final Context      context;
    private final DeviceHandle handle = new DeviceHandle();


    public USBDevice(int vendor, int product, String serial, int conf, int iface, int alt) throws IOException, DeviceException {

        int result;

        USBDriver.initialise();
        this.context = USBDriver.context;

        DeviceList deviceList = new DeviceList();
        result = LibUsb.getDeviceList(context, deviceList);

        if (result < 0) {
            throw new IOException(String.format("Error acquiring USB device list: %s", LibUsb.strError(result)));
        }

        Device found = null;

        for (Device device : deviceList) {

            DeviceDescriptor descriptor = new DeviceDescriptor();
            result = LibUsb.getDeviceDescriptor(device, descriptor);

            if (result < 0) {
                continue;
            }

            if (((int) descriptor.idVendor() & 0xffff) == vendor && ((int) descriptor.idProduct() & 0xffff) == product) {
                found = device;
                break;
            }

        }

        LibUsb.freeDeviceList(deviceList, false);

        if (found == null) {
            throw new IOException("No connected device found with that vendorID and productID.");
        }

        result = LibUsb.open(found, handle);

        if (result < 0) {
            throw new IOException(String.format("Error opening USB device: %s", LibUsb.strError(result)));
        }

        result = LibUsb.resetDevice(handle);

        if (result < 0) {
            throw new IOException(String.format("Error resetting USB device: %s", LibUsb.strError(result)));
        }

        if (conf >= 0) {

            result = LibUsb.setConfiguration(handle, conf);

            if (result < 0) {
                throw new IOException(String.format("Error setting USB configuration (c: %d): %s", conf, LibUsb.strError(result)));
            }

        }

        if (iface < 0) {
            DeviceDescriptor descriptor = new DeviceDescriptor();
            LibUsb.getDeviceDescriptor(found, descriptor);

        }

        if (LibUsb.kernelDriverActive(handle, iface) == 1) {

            result = LibUsb.detachKernelDriver(handle, iface);

            if (result < 0) {
                throw new IOException(String.format("Error detaching kernel driver: %s", LibUsb.strError(result)));
            }

        }

        result = LibUsb.claimInterface(handle, iface);

        if (result < 0) {
            throw new IOException(String.format("Error claiming USB interface (i: %d): %s", iface, LibUsb.strError(result)));
        }

        if (alt >= 0) {

            result = LibUsb.setInterfaceAltSetting(handle, iface, alt);

            if (result < 0) {
                throw new IOException(String.format("Error setting interface alt setting (c: %d, i: %d, a: %d): %s", conf, iface, alt, LibUsb.strError(result)));
            }

        }

    }

    public void controlSend(byte type, byte request, short wValue, short wIndex, byte[] data, long timeout) throws IOException {

        ByteBuffer buffer = ByteBuffer.allocateDirect(data.length);
        buffer.rewind().put(data);

        int result = LibUsb.controlTransfer(handle, type, request, wValue, wIndex, buffer, timeout);

        if (result < 0) {
            throw new IOException(String.format("Error performing control transfer out: %s", LibUsb.strError(result)));
        }

    }

    public byte[] controlReceive(byte type, byte request, short wValue, short wIndex, int bufferSize, long timeout) throws IOException {

        ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);
        buffer.rewind();

        int result = LibUsb.controlTransfer(handle, type, request, wValue, wIndex, buffer, timeout);

        if (result < 0) {
            throw new IOException(String.format("Error performing control transfer in: %s", LibUsb.strError(result)));
        }

        return buffer.rewind().array();

    }

    public Endpoint getEndpoint(byte id) {
        return new Endpoint(id);
    }

    @Override
    public String getIDN() throws IOException, DeviceException {
        return "";
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public void close() throws IOException, DeviceException {

    }

    @Override
    public Address getAddress() {
        return null;
    }

    public class Endpoint {

        private final byte id;

        public Endpoint(byte id) {
            this.id = id;
        }

        public byte getID() {
            return id;
        }

        public void bulkSend(byte[] data, long timeout) throws IOException {

            ByteBuffer buffer    = ByteBuffer.allocateDirect(data.length);
            IntBuffer  intBuffer = IntBuffer.allocate(1);

            buffer.rewind().put(data);

            int result = LibUsb.bulkTransfer(handle, id, buffer, intBuffer, timeout);

            if (result < 0) {
                throw new IOException(String.format("Error encountered with bulk transfer out (endpoint %d): %s", id, LibUsb.strError(result)));
            }

            if (intBuffer.get(0) != data.length) {
                throw new IOException(String.format("Incorrect number of bits transmitted (endpoint %d, sent %d, expected %d)!", id, intBuffer.get(0), data.length));
            }

        }

        public byte[] bulkReceive(int bufferSize, long timeout) throws IOException {

            ByteBuffer buffer    = ByteBuffer.allocateDirect(bufferSize);
            IntBuffer  intBuffer = IntBuffer.allocate(1);

            int result = LibUsb.bulkTransfer(handle, id, buffer, intBuffer, timeout);

            if (result < 0) {
                throw new IOException(String.format("Error encountered with bulk transfer in (endpoint %d): %s", id, LibUsb.strError(result)));
            }

            if (intBuffer.get(0) >= bufferSize) {
                throw new IOException(String.format("Buffer overflow (endpoint %d, buffer %d, received %d)!", id, bufferSize, intBuffer.get(0)));
            }

            byte[] data = new byte[intBuffer.get(0)];


            buffer.rewind();
            for (int i = 0; i < data.length; i++) {
                data[i] = buffer.get();
            }

            return data;

        }

    }

}
