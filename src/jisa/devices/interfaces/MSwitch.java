package jisa.devices.interfaces;

import jisa.Util;
import jisa.addresses.Address;
import jisa.devices.DeviceException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public interface MSwitch extends Switch, MultiInstrument {

    static String getDescription() {
        return "Multi-Channel Switch";
    }

    String getName(int channel);

    void turnOn(int channel) throws IOException, DeviceException;

    default void turnOn() throws IOException, DeviceException {

        for (int i = 0; i < getNumChannels(); i++) {
            turnOn(i);
        }

    }

    void turnOff(int channel) throws IOException, DeviceException;

    default void turnOff() throws IOException, DeviceException {

        for (int i = 0; i < getNumChannels(); i++) {
            turnOff(i);
        }

    }


    default void setOn(int channel, boolean on) throws IOException, DeviceException {

        if (on) {
            turnOn(channel);
        } else {
            turnOff(channel);
        }

    }

    boolean isOn(int channel) throws IOException, DeviceException;

    default boolean isOn() throws IOException, DeviceException {
        return isOn(0);
    }

    int getNumChannels();

    default void checkChannel(int channel) throws DeviceException {

        if (!Util.isBetween(channel, 0, getNumChannels()-1)) {
            throw new DeviceException("That is not a valid channel number.");
        }

    }

    default List<Instrument> getSubInstruments() {
        return getChannels().stream().collect(Collectors.toUnmodifiableList());
    }

    default List<Switch> getChannels() {

        ArrayList<Switch> switches = new ArrayList<>(getNumChannels());

        for (int i = 0; i < getNumChannels(); i++) {
            switches.add(getChannel(i));
        }

        return switches;

    }

    default Switch getChannel(int channel) {

        if (!Util.isBetween(channel, 0, getNumChannels()-1)) {
            return null;
        }

        return new Switch() {

            @Override
            public String getName() {
                return MSwitch.this.getName(channel);
            }

            @Override
            public void turnOn() throws IOException, DeviceException {
                MSwitch.this.turnOn(channel);
            }

            @Override
            public void turnOff() throws IOException, DeviceException {
                MSwitch.this.turnOff(channel);
            }

            @Override
            public boolean isOn() throws IOException, DeviceException {
                return MSwitch.this.isOn(channel);
            }

            @Override
            public String getIDN() throws IOException, DeviceException {
                return MSwitch.this.getIDN();
            }

            @Override
            public void close() throws IOException, DeviceException {
                MSwitch.this.close();
            }

            @Override
            public Address getAddress() {
                return MSwitch.this.getAddress();
            }
        };

    }

}
