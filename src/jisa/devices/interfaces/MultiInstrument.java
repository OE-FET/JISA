package jisa.devices.interfaces;

import jisa.devices.DeviceException;

import java.io.IOException;
import java.util.List;

public interface MultiInstrument {

    List<Class<? extends Instrument>> getMultiTypes();

    <I extends Instrument> List<I> getSubInstruments(Class<I> type) throws IOException, DeviceException;

    <I extends Instrument> I getSubInstrument(Class<I> type, int index) throws IOException, DeviceException;

}
