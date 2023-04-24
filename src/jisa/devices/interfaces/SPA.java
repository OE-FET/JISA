package jisa.devices.interfaces;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface SPA extends Instrument, MultiInstrument {

    static String getDescription() {
        return "Semiconductor Parameter Analyser";
    }

    /**
     * Returns a list of all SMU sub-instruments.
     *
     * @return List of all SMU units in the SPA.
     */
    List<SMU> getSMUChannels();

    /**
     * Returns the SMU sub-instrument of the SPA with the given index.
     *
     * @param index Index to get
     *
     * @return SMU subunit of given index
     */
    default SMU getSMUChannel(int index) {
        return getSMUChannels().get(index);
    }

    List<VMeter> getVMeterChannels();

    default VMeter getVMeterChannel(int index) {
        return getVMeterChannels().get(index);
    }

    List<VSource> getVSourceChannels();

    default VSource getVSourceChannel(int index) {
        return getVSourceChannels().get(index);
    }

    List<Switch> getSwitchChannels();

    default Switch getSwitchChannel(int index) {
        return getSwitchChannels().get(index);
    }

    @Override
    default List<Instrument> getSubInstruments() {

        return Stream.concat(
            Stream.concat(getSwitchChannels().stream(), getVMeterChannels().stream()),
            Stream.concat(getVSourceChannels().stream(), getSMUChannels().stream())
        ).collect(Collectors.toUnmodifiableList());

    }
}
