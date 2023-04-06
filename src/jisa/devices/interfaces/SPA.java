package jisa.devices.interfaces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
    default List<Class<? extends Instrument>> getSubInstrumentTypes() {
        return List.of(SMU.class, VMeter.class, VSource.class, Switch.class);
    }

    @Override
    default <I extends Instrument> List<I> getSubInstruments(Class<I> type) {

        List<I> list = new ArrayList<>();

        if (type.isAssignableFrom(SMU.class)) {
            list.addAll((Collection<? extends I>) getSMUChannels());
        }

        if (type.isAssignableFrom(VMeter.class)) {
            list.addAll((Collection<? extends I>) getVMeterChannels());
        }

        if (type.isAssignableFrom(VSource.class)) {
            list.addAll((Collection<? extends I>) getVSourceChannels());
        }

        if (type.isAssignableFrom(Switch.class)) {
            list.addAll((Collection<? extends I>) getSwitchChannels());
        }

        return list;

    }

    @Override
    default <I extends Instrument> I getSubInstrument(Class<I> type, int index) {
        return getSubInstruments(type).get(index);
    }

}
