package jisa.visa.exceptions;

import jisa.addresses.Address;

public class NoCompatibleDriversException extends VISAException {

    public NoCompatibleDriversException(Address address) {
        super("No drivers available that support connecting to %s", address.toString());
    }

}
