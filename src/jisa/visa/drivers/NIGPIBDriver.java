package jisa.visa.drivers;

import com.sun.jna.Native;
import com.sun.jna.Platform;
import jisa.visa.VISAException;

public class NIGPIBDriver extends GPIBDriver {

    protected static NIGPIBNativeInterface lib = null;
    protected static String                libName;

    public NIGPIBDriver() throws VISAException {
        super();
    }

    protected void initialise() throws VISAException {

        try {

            if (Platform.isWindows()) {
                libName = "ni4882";
                lib     = Native.loadLibrary(libName, NIGPIBNativeInterface.class);
            } else if (Platform.isLinux()) {
                libName = "gpibapi";
                lib     = Native.loadLibrary(libName, NIGPIBNativeInterface.class);
            } else {
                throw new VISAException("Platform not yet supported.");
            }

        } catch (UnsatisfiedLinkError e) {
            lib = null;
        }

        if (lib == null) {
            throw new VISAException("Could not load NI-GPIB library");
        }

        try {
            lib.Ibsta();
        } catch (UnsatisfiedLinkError e) {
            throw new VISAException("Could not link to global var methods.");
        }

    }

    @Override
    protected NIGPIBNativeInterface lib() {
        return lib;
    }

    protected int getIBSTA() {
        return lib().Ibsta();
    }

    protected int getIBERR() {
        return lib().Iberr();
    }

    protected int getIBCNT() {
        return lib().Ibcnt();
    }

}
