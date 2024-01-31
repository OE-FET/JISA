package jisa.visa.exceptions;

import jisa.addresses.Address;
import jisa.visa.drivers.Driver;

import java.util.Map;
import java.util.stream.Collectors;

public class ConnectionFailedException extends VISAException {

    private final Address                address;
    private final Map<Driver, Exception> errors;

    public ConnectionFailedException(Address address, Map<Driver, Exception> errors) {

        super(
            "Could not open \"%s\" using any available, compatible driver. The following errors were given:%n%s",
            address.toString(),
            errors.entrySet().stream()
                  .map(e -> String.format(
                      "* %s: %s",
                      e.getKey().getClass().getSimpleName(),
                      e.getValue().getMessage()
                  ))
                  .collect(Collectors.joining("\n"))
        );

        this.address = address;
        this.errors  = Map.copyOf(errors);

    }

    public Address getAddress() {
        return address;
    }

    public Map<Driver, Exception> getErrors() {
        return errors;
    }

}
