package jisa.devices.interfaces;

import jisa.devices.DeviceException;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public interface MultiChannel<T extends Channel<T>> extends Iterable<T> {

    int getNumChannels();

    String getChannelName(int channelNumber);

    List<T> getChannels();

    T getChannel(int channelNumber) throws IOException, DeviceException;

    Class<T> getChannelClass();

    default Iterator<T> iterator() {
        return getChannels().iterator();
    }

}
