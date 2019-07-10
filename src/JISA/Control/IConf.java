package JISA.Control;

import JISA.Devices.Instrument;

public interface IConf<T extends Instrument> {

    T get();

}
