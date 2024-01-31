package jisa.devices.relay;

import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.Switch;
import jisa.visa.VISADevice;
import jisa.visa.connections.SerialConnection;

import java.io.IOException;

public class MFF101 extends VISADevice implements Switch {

    public MFF101(Address address) throws IOException, DeviceException {

        super(address);

        configSerial(serial -> {

            serial.setSerialParameters(
                115200,
                8,
                SerialConnection.Parity.NONE,
                SerialConnection.Stop.BITS_10,
                SerialConnection.FlowControl.RTS_CTS
            );

            serial.clear();

        });

        // Flash the LED to indicate connection success
        writeBytes(new byte[]{0x23, 0x02, 0x00, 0x00, 0x21, 0x01});

    }

    @Override
    public void turnOn() throws IOException, DeviceException {
        writeBytes(new byte[]{0x6A, 0x04, 0x01, 0x01, 0x21, 0x01});
    }

    @Override
    public void turnOff() throws IOException, DeviceException {
        writeBytes(new byte[]{0x6A, 0x04, 0x01, 0x02, 0x21, 0x01});
    }

    @Override
    public boolean isOn() throws IOException, DeviceException {
        return false;
    }

}
