package jisa.visa.exceptions;

import jisa.addresses.Address;
import jisa.visa.drivers.Driver;

public class NoDeviceException extends VISAException {

    private final Address address;
    private final Driver  driver;

    public NoDeviceException(Address address, Driver driver) {

        super("No device found at \"%s\" using \"%s\".", address.toString(), driver.getClass().getSimpleName());

        this.address = address;
        this.driver  = driver;

    }

    /**
     * Returns the address object that resulted in the failed connection.
     *
     * @return Address object
     */
    public Address getAddress() {
        return address;
    }

    /**
     * Returns the Driver object that was unable to connect.
     *
     * @return Driver object
     */
    public Driver getDriver() {
        return driver;
    }

}
