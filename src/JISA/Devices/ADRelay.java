package JISA.Devices;

import JISA.Addresses.Address;
import JISA.Util;
import JISA.VISA.Connection;
import JISA.VISA.SerialDriver;
import JISA.VISA.VISADevice;

import java.io.IOException;

public class ADRelay extends VISADevice implements MSwitch {

    private final int NUM_CHANNELS;

    public ADRelay(Address address) throws IOException, DeviceException {
        super(address);
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
