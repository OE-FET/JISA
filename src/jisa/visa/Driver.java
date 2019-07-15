package jisa.visa;

import jisa.addresses.Address;
import jisa.addresses.StrAddress;

public interface Driver {

    /**
     * Attempts to open a connection to the instrument at the given address. Returns a Connection object if successful.
     *
     * @param address Address of the instrument to open
     *
     * @return Connection object representing newly opened connection
     *
     * @throws VISAException If it goes wrong
     */
    Connection open(Address address) throws VISAException;

    /**
     * Use this driver to find instruments.
     *
     * @return Array of found instrument addresses.
     *
     * @throws VISAException If it goes wrong
     */
    StrAddress[] search() throws VISAException;

}
