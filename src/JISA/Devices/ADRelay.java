package JISA.Devices;

import JISA.Addresses.Address;
import JISA.VISA.Connection;
import JISA.VISA.VISADevice;

import java.io.IOException;

public class ADRelay extends VISADevice implements MSwitch {

    private final int NUM_CHANNELS;

    public ADRelay(Address address) throws IOException, DeviceException {
        super(address);

        String idn = getIDN();

        if (!idn.trim().equals("Arduino Controlled Relay")) {
            throw new DeviceException("This is not an arduino controlled relay!");
        }

        NUM_CHANNELS = queryInt("NUMCHANS?");
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
