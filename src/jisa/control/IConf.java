package jisa.control;

import jisa.devices.Instrument;

public interface IConf<T extends Instrument> {

    T get();

}
