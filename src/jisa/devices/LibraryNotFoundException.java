package jisa.devices;

public class LibraryNotFoundException extends DeviceException {

    public LibraryNotFoundException(String libraryName, String instrumentName) {

        super(
            "Unable to connect to %s as the required library \"%s\" (i.e., %s.dll or lib%s.so) was not found. " +
                "This could be because the library is not installed, is not in the system path (i.e., /usr/lib or C:\\Windows\\System), " +
                "or you are trying to use a 32-bit library on a 64-bit system or vice versa.",
            instrumentName, libraryName, libraryName, libraryName
        );

    }

}
