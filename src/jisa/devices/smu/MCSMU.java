package jisa.devices.smu;

import jisa.devices.DeviceException;
import jisa.devices.Instrument;
import jisa.devices.MultiInstrument;
import jisa.devices.ParameterList;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Abstract class defining the standard interface for Multiple-Channel SMUs.
 */
public interface MCSMU<T extends SMU> extends Instrument, MultiInstrument, Iterable<T> {

    static void addParameters(MCSMU<?> inst, Class<?> target, ParameterList parameters) {

        for (SMU smu : inst.getSMUs()) {

            for (Parameter<?> parameter : smu.getAllParameters()) {

                if (parameter.isGrouped()) {
                    parameters.add(parameter.copy(String.format("%s %s", smu.getName(), parameter.getGroup()), parameter.getName()));
                } else {
                    parameters.add(parameter.copy(smu.getName(), parameter.getName()));
                }

            }

        }


    }

    static String getDescription() {
        return "Multi-Channel Source Measure Unit";
    }

    /**
     * Returns a list of all SMU channels in this multi-channel SMU.
     *
     * @return List of SMU channels
     */
    List<T> getSMUs();

    /**
     * Returns a list of all SMU channels in this multi-channel SMU.
     * Alias for backwards compatibility.
     *
     * @return List of SMU channels
     */
    default List<T> getChannels() {
        return getSMUs();
    }

    default T getSMU(int index) {
        return getSMUs().get(index);
    }

    default T getChannel(int index) {
        return getSMU(index);
    }

    @Override
    default List<? extends Instrument> getSubInstruments() {
        return getSMUs();
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
        return getSMUs().iterator();
    }

    interface SMUAcceptor<T extends SMU> {

        void accept(T smu) throws DeviceException, IOException;

    }

}
