package jisa.devices;

import java.io.IOException;
import java.util.List;

public interface MultiChannel<T> {

    int getNumChannels();

    String getChannelName(int channelNumber);

    List<T> getChannels();

    T getChannel(int channelNumber) throws IOException, DeviceException;

    Class<T> getChannelType();

}
