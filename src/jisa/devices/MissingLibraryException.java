package jisa.devices;

public class MissingLibraryException extends DeviceException {

    public MissingLibraryException(String libraryName, String instrumentName) {
        super("Unable to connect to %s as the required system library \"%s\" was not found (i.e., %s.dll or lib%s.so). Ensure that it is installed and that it is located in a system library path such as /usr/lib or C:\\Windows\\System etc.", instrumentName, libraryName, libraryName, libraryName);
    }

}
