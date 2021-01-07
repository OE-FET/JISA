package jisa.devices;

public interface Output<T> {

    String getOutputName();

    Class<T> getOutputType();

}
