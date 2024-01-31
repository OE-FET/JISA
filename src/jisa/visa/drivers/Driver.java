package jisa.visa.drivers;

import jisa.addresses.Address;
import jisa.visa.connections.Connection;
import jisa.visa.exceptions.VISAException;

import java.util.List;

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
    List<? extends Address> search();

    /**
     * Checks whether this driver can be used to open a connection to a device with the given address.
     *
     * @param address The address to check.
     *
     * @return Is it compatible?
     */
    boolean worksWith(Address address);

    /**
     * Resets the driver. For instance, for VISA-based drivers this closes the current and opens a new resource manager.
     *
     * @throws VISAException If something goes wrong
     */
    void reset() throws VISAException;

}
