package jisa.devices.interfaces;

import jisa.addresses.Address;
import jisa.devices.DeviceException;

import java.io.IOException;
import java.util.List;

public interface LevelMeter extends Instrument, MultiChannel<LevelMeter>, Channel<LevelMeter> {

    public static String getDescription() {
        return "Level Meter";
    }

    default Class<LevelMeter> getChannelType() {
        return LevelMeter.class;
    }

    double getLevel(int channel) throws IOException, DeviceException;

    default double getLevel() throws IOException, DeviceException {
        return getLevel(0);
    }

    default String getChannelName() {
        return getChannelName(0);
    }

    default LevelMeter getChannel(int channelNo) {

        return new LevelMeter() {
            @Override
            public String getChannelName() {
                return LevelMeter.this.getChannelName(channelNo);
            }

            @Override
            public int getNumChannels() {
                return 1;
            }

            @Override
            public String getChannelName(int channelNumber) {
                return "Channel " + channelNo;
            }

            @Override
            public List<LevelMeter> getChannels() {
                return List.of(this);
            }

            @Override
            public double getLevel(int channel) throws IOException, DeviceException {
                return LevelMeter.this.getLevel(channelNo);
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
