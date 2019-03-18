package JISA.VISA;

import JISA.Addresses.Address;
import JISA.Addresses.StrAddress;

public interface Driver {

    Connection open(Address address) throws VISAException;

    StrAddress[] search() throws VISAException;

}
