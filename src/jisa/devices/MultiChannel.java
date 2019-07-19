package jisa.devices;

import java.io.IOException;
import java.util.List;

public interface MultiChannel<T> {

    int getNumChannels();

    List<T> getChannels() throws DeviceException, IOException;

    T getChannel(int channelNumber) throws IOException, DeviceException;

}
