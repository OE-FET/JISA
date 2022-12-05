package jisa.devices.interfaces;

import java.util.List;

public interface MultiInstrument {

    List<Class<? extends Instrument>> getSubInstrumentTypes();

    <I extends Instrument> List<I> getSubInstruments(Class<I> type);

    <I extends Instrument> I getSubInstrument(Class<I> type, int index);

}
