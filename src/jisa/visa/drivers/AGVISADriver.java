package jisa.visa.drivers;

import com.sun.jna.Native;
import jisa.visa.VISAException;
import jisa.visa.VISANativeInterface;

public class AGVISADriver extends VISADriver {

    private static       VISANativeInterface libStatic;
    private static final String              OS_NAME = System.getProperty("os.name").toLowerCase();
    private static       String              libName;

    public AGVISADriver() throws VISAException {
        super();
    }

    @Override
    protected VISANativeInterface lib() {
        return libStatic;
    }

    public static void init() throws VISAException {

        try {

            if (OS_NAME.contains("win")) {
                libName   = "agvisa32";
                libStatic = Native.loadLibrary(libName, VISANativeInterface.class);
            } else if (OS_NAME.contains("linux") || OS_NAME.contains("mac")) {
                libName   = "agvisa";
                libStatic = Native.loadLibrary(libName, VISANativeInterface.class);
            } else {
                throw new VISAException("Platform not yet supported!");
            }

        } catch (UnsatisfiedLinkError e) {
            libStatic = null;
        }

        if (libStatic == null) {
            throw new VISAException("Could not load AG-VISA library");
        }

    }

}
