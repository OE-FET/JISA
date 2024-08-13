package jisa.visa.exceptions;

import jisa.addresses.Address;
import jisa.visa.drivers.Driver;

public class InvalidAddressException extends VISAException {

    private final Address address;
    private final Driver  driver;

    public InvalidAddressException(Address address, Driver driver) {

        super("The address \"%s\" is not a valid %s address.", address.toString(), driver.getClass().getSimpleName());

        this.address = address;
        this.driver  = driver;

    }

    public Address getAddress() {
        return address;
    }

    public Driver getDriver() {
        return driver;
    }

}
