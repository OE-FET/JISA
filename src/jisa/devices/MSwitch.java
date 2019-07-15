package jisa.devices;

import jisa.addresses.Address;
import jisa.Util;

import java.io.IOException;

public interface MSwitch extends Switch {

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

    default Switch getChannel(int channel) throws DeviceException {

        checkChannel(channel);

        return new Switch() {
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
