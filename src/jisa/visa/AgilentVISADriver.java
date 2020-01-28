package jisa.visa;

import com.sun.jna.Native;

public class AgilentVISADriver extends NIVISADriver {

    public static void init() throws VISAException {

        try {
            if (OS_NAME.contains("win")) {
                libName = "agvisa32";
                lib = Native.loadLibrary(libName, VISANativeInterface.class);
            } else if (OS_NAME.contains("linux") || OS_NAME.contains("mac")) {
                libName = "visa";
                lib = Native.loadLibrary(libName, VISANativeInterface.class);
            } else {
                throw new VISAException("Platform not yet supported!");
            }
        } catch (UnsatisfiedLinkError e) {
            lib = null;
        }

        if (lib == null) {
            throw new VISAException("Could not load VISA library");
        }

        // Attempt to get a resource manager handle
        try {
            visaResourceManagerHandle = getResourceManager();
        } catch (VISAException e) {
            throw new VISAException("Could not get resource manager");
        }

    }

}
