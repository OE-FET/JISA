package jisa.devices.interfaces;

import jisa.devices.DeviceException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Abstract class defining the standard interface for Multiple-Channel SMUs.
 */
public interface MCSMU<T extends SMU> extends Instrument, MultiInstrument, Iterable<T> {

    static String getDescription() {
        return "Multi-Channel Source Measure Unit";
    }

    /**
     * Returns a list of all SMU channels in this multi-channel SMU.
     *
     * @return List of SMU channels
     */
    List<T> getSMUChannels();

    /**
     * Returns a list of all SMU channels in this multi-channel SMU.
     * Alias for backwards compatibility.
     *
     * @return List of SMU channels
     */
    default List<T> getChannels() {
        return getSMUChannels();
    }

    default T getSMUChannel(int index) {
        return getSMUChannels().get(index);
    }

    default T getChannel(int index) {
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
        forEachSMU(SMU::turnOff);
    }

    default void forEachSMU(SMUAcceptor<T> forEach) throws IOException, DeviceException {

        for (T smu : this) {
            forEach.accept(smu);
        }

    }

    @NotNull
    @Override
    default Iterator<T> iterator() {
        return getSMUChannels().iterator();
    }

    interface SMUAcceptor<T extends SMU> {

        void accept(T smu) throws DeviceException, IOException;

    }

}
