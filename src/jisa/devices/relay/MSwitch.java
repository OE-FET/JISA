package jisa.devices.relay;

import jisa.Util;
import jisa.devices.Instrument;
import jisa.devices.MultiInstrument;

import java.util.List;

public interface MSwitch<T extends Switch> extends MultiInstrument {

    static String getDescription() {
        return "Multi-Channel Switch";
    }

    List<T> getSwitches();

    default List<? extends Instrument> getSubInstruments() {
        return getSwitches();
    }

    default void turnOff() { getSwitches().forEach(sw -> Util.runRegardless(sw::turnOff)); }

    default void turnOn() { getSwitches().forEach(sw -> Util.runRegardless(sw::turnOn)); }

    default int getOnCount() { return (int) getSwitches().stream().filter(sw -> { try { return sw.isOn(); } catch (Exception e) { return false; } }).count(); }

}
