package jisa.devices;

public interface Channel<T> {

    String getChannelName();

    Class<T> getChannelType();

}
