package jisa.devices;

import com.sun.jna.Platform;
import jisa.Util;
import jisa.visa.NativeDevice;

public class LibraryNotFoundException extends DeviceException {

    public LibraryNotFoundException(String libraryName, String instrumentName) {

        super(
                "Unable to connect to %s as the required library \"%s\" (i.e., %s.dll or lib%s.so) was not found. " +
                        "This could be because the library is not installed, is not in one of the locations JISA looks " +
                        "for libraries or you are trying to use a 32-bit library on a 64-bit system or vice versa.\n\n" +
                        "JISA tried looking for this library in the following locations:\n- %s\n- %s%s",
                instrumentName, libraryName, libraryName, libraryName,
                String.join("\n- ", NativeDevice.getSearchPaths(libraryName)),
                String.join("\n- ", System.getProperty("java.library.path").split(Platform.isWindows() ? ";" : ":")),
                Platform.isWindows() ? "\n- Recursive search of " + System.getenv("ProgramFiles") : ""
        );

    }

}
