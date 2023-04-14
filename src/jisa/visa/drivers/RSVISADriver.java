package jisa.visa.drivers;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import jisa.visa.VISAException;
import jisa.visa.VISANativeInterface;

public class RSVISADriver extends VISADriver {

    private static       VISANativeInterface libStatic = null;
    private static       String              libName;

    public RSVISADriver() throws VISAException {
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
                libName   = "RsVisa32";
                libStatic = Native.loadLibrary(libName, VISANativeInterface.class);
            } else if (Platform.isLinux() || Platform.isMac()) {
                libName   = "rsvisa";
                libStatic = Native.loadLibrary(libName, VISANativeInterface.class);
            } else {
                throw new VISAException("Platform not yet supported!");
            }

        } catch (UnsatisfiedLinkError e) {
            libStatic = null;
        }

        if (libStatic == null) {
            throw new VISAException("Could not load RS-VISA library");
        }

    }


}
