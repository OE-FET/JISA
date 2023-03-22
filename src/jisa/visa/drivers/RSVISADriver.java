package jisa.visa.drivers;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Platform;
import com.sun.jna.ptr.NativeLongByReference;
import jisa.Util;
import jisa.visa.VISAException;
import jisa.visa.VISANativeInterface;

public class RSVISADriver extends VISADriver {

    private static       VISANativeInterface libStatic = null;
    private static final String              OS_NAME   = System.getProperty("os.name").toLowerCase();
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

            if (OS_NAME.contains("win")) {
                libName   = "RsVisa32";
                libStatic = Native.loadLibrary(libName, VISANativeInterface.class);
            } else if (OS_NAME.contains("linux") || OS_NAME.contains("mac")) {
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
