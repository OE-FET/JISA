package jisa.devices;

import jisa.addresses.Address;

import java.io.IOException;

/**
 * Dummy interface used to mark subclasses of instruments, so they don't get picked up by Connectors
 */
public interface SubInstrument<Parent extends Instrument> extends Instrument {

    default void close() throws IOException, DeviceException {
    }

    default Address getAddress() {
        return getParentInstrument().getAddress();
    }

    default String getIDN() throws IOException, DeviceException {
        return getParentInstrument().getIDN();
    }

    /**
     * Returns the parent instrument to which this sub-instrument belongs.
     *
     * @return Parent instrument
     */
    Parent getParentInstrument();

}
