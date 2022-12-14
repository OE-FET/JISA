package jisa.devices.interfaces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface SPA extends Instrument, MultiInstrument {

    public static String getDescription() {
        return "Semiconductor Parameter Analyser";
    }

    List<SMU> getSMUChannels();

    List<VMeter> getVMeterChannels();

    List<VSource> getVSourceChannels();

    List<Switch> getSwitchChannels();

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
