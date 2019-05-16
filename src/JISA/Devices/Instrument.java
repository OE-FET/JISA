package JISA.Devices;

import JISA.Addresses.Address;

import java.io.IOException;

public interface Instrument {

    String getIDN() throws IOException;

    void close() throws IOException, DeviceException;

    Address getAddress();

    default void setTimeout(int msec) throws IOException {

    }

    default void finalise() {

        try {
            close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
