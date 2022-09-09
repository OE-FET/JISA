package jisa.devices.relay;

import jisa.Util;
import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.MSwitch;
import jisa.devices.interfaces.Switch;
import jisa.visa.VISADevice;
import jisa.visa.connections.Connection;
import jisa.visa.connections.SerialConnection;

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

        Connection connection = getConnection();

        if (connection instanceof SerialConnection) {
            ((SerialConnection) connection).setSerialParameters(9600, 8, SerialConnection.Parity.NONE, SerialConnection.Stop.BITS_10);
        }

        setTimeout(2000);

        setReadTerminator(LF_TERMINATOR);
        setWriteTerminator("\n");
        addAutoRemove("\r");
        addAutoRemove("\n");

        Util.sleep(1500); // Need to wait for Arduino to initialise serial routines.

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
    public String getChannelName(int channelNumber) {
        return String.format("Relay %d", channelNumber + 1);
    }

    @Override
    public List<Switch> getChannels() {

        int numChannels = getNumChannels();

        List<Switch> list = new ArrayList<>(numChannels);

        for (int cn = 0; cn < numChannels; cn++) {

            try {
                list.add(getChannel(cn));
            } catch (DeviceException ignored) { }

        }

        return list;

    }

    @Override
    public String getChannelName() {
        return "All Channels";
    }

    @Override
    public Class<Switch> getChannelClass() {
        return Switch.class;
    }
}
