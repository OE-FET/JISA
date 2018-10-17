package JISA.VISA;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

public class NIGPIBDriver extends GPIBDriver {

    protected static NIGPIBNativeInterface lib;

    public static void init() throws VISAException {

        try {
            if (OS_NAME.contains("win")) {
                libName = "ni4882";
                lib = (NIGPIBNativeInterface) Native.loadLibrary(libName, NIGPIBNativeInterface.class);
            } else if (OS_NAME.contains("linux")) {
                libName = "gpib";
                lib = (NIGPIBNativeInterface) Native.loadLibrary(libName, NIGPIBNativeInterface.class);
            } else {
                System.err.println("This system is not yet supported!");
                System.exit(1);
            }
        } catch (UnsatisfiedLinkError e) {
            lib = null;
        }

        if (lib == null) {
            System.out.println("NI-GPIB driver not loaded.");
            throw new VISAException("Could not load GPIB library");
        }

        System.out.println("NI-GPIB driver loaded.");

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
