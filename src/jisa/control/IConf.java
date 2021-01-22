package jisa.control;

import jisa.devices.interfaces.Instrument;

public interface IConf<T extends Instrument> {

    T get();

}
