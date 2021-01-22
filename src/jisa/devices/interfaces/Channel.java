package jisa.devices.interfaces;

public interface Channel<T> {

    String getChannelName();

    Class<T> getChannelType();

}
