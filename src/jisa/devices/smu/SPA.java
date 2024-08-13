package jisa.devices.smu;

import jisa.devices.Instrument;
import jisa.devices.MultiInstrument;
import jisa.devices.meter.VMeter;
import jisa.devices.relay.Switch;
import jisa.devices.source.VSource;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface SPA<A extends SMU, B extends VMeter, C extends VSource, D extends Switch> extends Instrument, MultiInstrument {

    static String getDescription() {
        return "Semiconductor Parameter Analyser";
    }

    /**
     * Returns a list of all SMU sub-instruments.
     *
     * @return List of all SMU units in the SPA.
     */
    List<A> getSMUChannels();

    /**
     * Returns the SMU sub-instrument of the SPA with the given index.
     *
     * @param index Index to get
     *
     * @return SMU subunit of given index
     */
    default A getSMUChannel(int index) {
        return getSMUChannels().get(index);
    }

    List<B> getVMeterChannels();

    default B getVMeterChannel(int index) {
        return getVMeterChannels().get(index);
    }

    List<C> getVSourceChannels();

    default C getVSourceChannel(int index) {
        return getVSourceChannels().get(index);
    }

    List<D> getSwitchChannels();

    default D getSwitchChannel(int index) {
        return getSwitchChannels().get(index);
    }

    @Override
    default List<? extends Instrument> getSubInstruments() {

        return Stream.concat(
            Stream.concat(getSwitchChannels().stream(), getVMeterChannels().stream()),
            Stream.concat(getVSourceChannels().stream(), getSMUChannels().stream())
        ).collect(Collectors.toUnmodifiableList());

    }
}
