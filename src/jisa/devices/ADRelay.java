package jisa.devices;

import jisa.addresses.Address;
import jisa.Util;
import jisa.visa.Connection;
import jisa.visa.VISADevice;

import java.io.IOException;

public class ADRelay extends VISADevice implements MSwitch {

    private final int NUM_CHANNELS;

    public ADRelay(Address address) throws IOException, DeviceException {
        super(address);
        setSerialParameters(9600, 8, Connection.Parity.NONE, Connection.StopBits.ONE, Connection.Flow.NONE);
        setReadTerminationCharacter(LF_TERMINATOR);
        setRemoveTerminator("\r\n");
        setTerminator("\n");

        Util.sleep(1500);

        String idn = getIDN().trim();

        if (!idn.equals("Arduino Controlled Relay")) {
            throw new DeviceException("Device at \"%s\" is not an Arduino Controlled Relay.", address.toString());
        }

        NUM_CHANNELS = queryInt("NCHANS?");

    }

    @Override
    public void turnOn(int channel) throws IOException, DeviceException {
        checkChannel(channel);
        write("OUTP %d 1", channel + 1);
    }

    @Override
    public void turnOff(int channel) throws IOException, DeviceException {
        checkChannel(channel);
        write("OUTP %d 0", channel + 1);
    }

    @Override
    public boolean isOn(int channel) throws IOException, DeviceException {
        checkChannel(channel);
        return queryInt("OUTP? %d", channel + 1) == 1;
    }

    @Override
    public int getNumChannels() {
        return NUM_CHANNELS;
    }
}