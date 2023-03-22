package jisa.visa.drivers;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import jisa.visa.VISAException;
import jisa.visa.VISANativeInterface;

public class NIVISADriver extends VISADriver {

    private static final String              OS_NAME = System.getProperty("os.name").toLowerCase();
    private static       VISANativeInterface libStatic;
    private static       String              libName;

    public NIVISADriver() throws VISAException {
        super();
    }

    @Override
    protected VISANativeInterface lib() {
        return libStatic;
    }

    protected void initialise() throws VISAException {

        if (libStatic != null) {
            return;
        }

        try {

            if (OS_NAME.contains("win")) {
                libName   = Platform.is64Bit() ? "nivisa64" : "nivisa32";
                libStatic = Native.loadLibrary(libName, VISANativeInterface.class);
            } else if (OS_NAME.contains("linux") || OS_NAME.contains("mac")) {
                libName   = "visa";
                libStatic = Native.loadLibrary(libName, VISANativeInterface.class);
            } else {
                throw new VISAException("Platform not yet supported!");
            }

        } catch (UnsatisfiedLinkError e) {
            libStatic = null;
        }

        if (libStatic == null) {
            throw new VISAException("Could not load NI-VISA library");
        }

    }


}
