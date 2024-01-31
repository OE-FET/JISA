package jisa.visa.exceptions;

import jisa.addresses.Address;
import jisa.visa.drivers.Driver;

public class IncompatibleAddressException extends VISAException {

    private final Address address;
    private final Driver  driver;

    public IncompatibleAddressException(Address address, Driver driver) {

        super("The address \"%s\" (%s Address) is not compatible with %s.", address.toString(), address.getTypeName(), driver.getClass().getSimpleName());

        this.address = address;
        this.driver  = driver;

    }

}
