package jisa.visa.exceptions;

import jisa.visa.drivers.Driver;

public class DriverSpecificException extends VISAException {

    private final Driver driver;

    public DriverSpecificException(Driver driver, String message) {
        super("%s error: \"%s\".", driver.getClass().getSimpleName(), message);
        this.driver = driver;
    }

    public Driver getDriver() {
        return driver;
    }

}
