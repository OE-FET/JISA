package jisa.devices.interfaces;

import jisa.devices.DeviceException;

import java.io.IOException;
import java.util.List;

/**
 * Abstract class defining the standard interface for controller Multiple-Channel SMUs.
 */
public interface MCSMU extends MultiInstrument {

    static String getDescription() {
        return "Multi-Channel Source Measure Unit";
    }

    /**
     * Returns a list of all SMU channels in this multi-channel SMU.
     *
     * @return List of SMU channels
     */
    List<SMU> getSMUChannels();

    /**
     * Returns a list of all SMU channels in this multi-channel SMU.
     * Alias for backwards compatibility.
     *
     * @return List of SMU channels
     */
    default List<SMU> getChannels() {
        return getSMUChannels();
    }

    default SMU getSMUChannel(int index) {
        return getSMUChannels().get(index);
    }

    default SMU getChannel(int index) {
        return getSMUChannel(index);
    }

    @Override
    default List<? extends Instrument> getSubInstruments() {
        return getSMUChannels();
    }

    /**
     * Turn off all channels.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device incompatibility
     */
    default void turnOffAll() throws IOException, DeviceException {

        for (SMU smu : getSMUChannels()) {
            smu.turnOff();
        }

    }

}
