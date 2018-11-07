package JISA.VISA;

import JISA.Addresses.InstrumentAddress;
import JISA.Addresses.StrAddress;

public interface Driver {

    Connection open(InstrumentAddress address) throws VISAException;

    StrAddress[] search() throws VISAException;

}
