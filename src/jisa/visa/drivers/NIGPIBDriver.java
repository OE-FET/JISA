package jisa.visa.drivers;

import com.sun.jna.Native;
import jisa.visa.VISAException;

public class NIGPIBDriver extends GPIBDriver {

    protected static NIGPIBNativeInterface lib;

    public NIGPIBDriver() throws VISAException {
        super();
    }

    protected void initialise() throws VISAException {

        try {
            if (OS_NAME.contains("win")) {
                libName = "ni4882";
                lib = Native.loadLibrary(libName, NIGPIBNativeInterface.class);
            } else if (OS_NAME.contains("linux")) {
                libName = "gpib";
                lib = Native.loadLibrary(libName, NIGPIBNativeInterface.class);
            } else {
                throw new VISAException("Platform not yet supported!");
            }
        } catch (UnsatisfiedLinkError e) {
            lib = null;
        }

        if (lib == null) {
            throw new VISAException("Could not load GPIB library");
        }

        try {
            lib.Ibsta();
        } catch (UnsatisfiedLinkError e) {
            throw new VISAException("Could not link to global var methods.");
        }

    }

    protected int Ibsta() {
        return lib.Ibsta();
    }

    protected int Iberr() {
        return lib.Iberr();
    }

    protected int Ibcnt() {
        return lib.Ibcnt();
    }

}
