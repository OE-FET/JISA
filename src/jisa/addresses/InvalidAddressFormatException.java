package jisa.addresses;

public class InvalidAddressFormatException extends Exception {

    public InvalidAddressFormatException(String attemptedAddress, String type) {
        super(String.format("The supplied address: \"%s\" is not a valid %s address", attemptedAddress, type));
    }

}
