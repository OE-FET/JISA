package jisa.devices.interfaces;

import jisa.addresses.Address;
import jisa.devices.DeviceException;

import java.io.IOException;

/**
 * Dummy interface used to mark subclasses of instruments so they don't get picked up by Connectors
 */
public interface SubInstrument<T extends Instrument> extends Instrument {

    default void close() throws IOException, DeviceException { }

    default Address getAddress() {
        return getParentInstrument().getAddress();
    }

    default String getIDN() throws IOException, DeviceException {
        return getParentInstrument().getIDN();
    }

    T getParentInstrument();

}
