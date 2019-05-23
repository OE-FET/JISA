package JISA.Devices;

import JISA.Addresses.Address;
import org.reflections.Reflections;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Interface for defining the base functionality of all instruments.
 */
public interface Instrument {

    /**
     * Returns an identifying String of the instrument.
     *
     * @return Identifying String
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    String getIDN() throws IOException, DeviceException;

    /**
     * Closes the connection to the instrument.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void close() throws IOException, DeviceException;

    /**
     * Returns any Address object used to connect to this instrument.
     *
     * @return Address object, null if none
     */
    Address getAddress();

    /**
     * Sets the timeout for read/write operations to this instrument (if applicable).
     *
     * @param msec Timeout, in milliseconds
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    default void setTimeout(int msec) throws IOException {

    }

    /**
     * Makes sure the connection closes when destroyed.
     */
    default void finalise() {

        try {
            close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
