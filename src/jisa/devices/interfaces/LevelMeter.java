package jisa.devices.interfaces;

import jisa.addresses.Address;
import jisa.devices.DeviceException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public interface LevelMeter extends Instrument, MultiInstrument {

    static String getDescription() {
        return "Level Meter";
    }

    double getLevel(int channel) throws IOException, DeviceException;

    default double getLevel() throws IOException, DeviceException {
        return getLevel(0);
    }

    String getName(int i);

    default String getName() {
        return getName(0);
    }

    default LevelMeter getChannel(int channelNo) {

        return new LevelMeter() {

            public List<Instrument> getSubInstruments() {
                return Collections.emptyList();
            }

            @Override
            public double getLevel(int channel) throws IOException, DeviceException {
                return getLevel();
            }

            @Override
            public double getLevel() throws IOException, DeviceException {
                return LevelMeter.this.getLevel(channelNo);
            }

            @Override
            public String getName(int i) {
                return getName();
            }

            @Override
            public String getIDN() throws IOException, DeviceException {
                return LevelMeter.this.getIDN();
            }

            @Override
            public void close() throws IOException, DeviceException {
                LevelMeter.this.close();
            }

            @Override
            public Address getAddress() {
                return LevelMeter.this.getAddress();
            }

        };

    }

}
