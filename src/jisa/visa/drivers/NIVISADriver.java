package jisa.visa.drivers;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import jisa.visa.VISANativeInterface;
import jisa.visa.exceptions.VISAException;

public class NIVISADriver extends VISADriver {

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

            if (Platform.isWindows()) {
                libName   = Platform.is64Bit() ? "nivisa64" : "visa32";
                libStatic = Native.loadLibrary(libName, VISANativeInterface.class);
            } else if (Platform.isLinux() || Platform.isMac()) {
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
