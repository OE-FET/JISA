package jisa.devices;

public class LibraryInitialisationException extends DeviceException {

    public LibraryInitialisationException(String libraryName, String instrumentName, String errorMessage) {
        super("Unable to connect to %s due to an error occurring when initialising the required library \"%s\": %s", instrumentName, libraryName, errorMessage);
    }

}
