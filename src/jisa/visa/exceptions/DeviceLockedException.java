package jisa.visa.exceptions;

import jisa.addresses.Address;
import jisa.visa.drivers.Driver;

public class DeviceLockedException extends VISAException {

    private final Address address;
    private final Driver  driver;

    public DeviceLockedException(Address address, Driver driver) {

        super("Cannot connect to \"%s\" using %s, as the resource at that location is already locked by something else.", address.toString(), driver.getClass().getSimpleName());

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
