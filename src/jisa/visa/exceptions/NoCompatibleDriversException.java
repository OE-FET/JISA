package jisa.visa.exceptions;

import jisa.addresses.Address;

public class NoCompatibleDriversException extends VISAException {

    private final Address address;

    public NoCompatibleDriversException(Address address) {
        super("No drivers available that support connecting to %s", address.toString());
        this.address = address;
    }

    public Address getAddress() {
        return address;
    }

}
