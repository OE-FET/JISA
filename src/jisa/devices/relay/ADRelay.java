package jisa.devices.relay;

import jisa.Util;
import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.MSwitch;
import jisa.devices.interfaces.Switch;
import jisa.visa.VISADevice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ADRelay extends VISADevice implements MSwitch {

    public static String getDescription() {
        return "Arduino-Controlled Relay";
    }

    private final int NUM_CHANNELS;

    public ADRelay(Address address) throws IOException, DeviceException {

        super(address);

        configSerial(serial -> serial.setSerialParameters(9600, 8));

        setTimeout(2000);

        setReadTerminator("\n");
        setWriteTerminator("\n");
        addAutoRemove("\r");
        addAutoRemove("\n");

        Util.sleep(2000); // Need to wait for Arduino to initialise serial routines.

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

    @Override
    public String getName(int channelNumber) {
        return String.format("Relay %d", channelNumber + 1);
    }

    @Override
    public List<Switch> getChannels() {

        int numChannels = getNumChannels();

        List<Switch> list = new ArrayList<>(numChannels);

        for (int cn = 0; cn < numChannels; cn++) {

            list.add(getChannel(cn));

        }

        return list;

    }

}
