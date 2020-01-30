package jisa.visa;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;

public class AgilentVISADriver extends NIVISADriver {

    private static       VISANativeInterface lib;
    private static final String              OS_NAME          = System.getProperty("os.name").toLowerCase();
    private static       String              libName;
    private static final String              responseEncoding = "UTF8";
    private static final long                VISA_ERROR       = 0x7FFFFFFF;
    private static final int                 _VI_ERROR        = -2147483648;
    private static final int                 VI_SUCCESS       = 0;
    private static final int                 VI_NULL          = 0;
    private static final int                 VI_TRUE          = 1;
    private static final int                 VI_FALSE         = 0;
    private static       NativeLong          visaResourceManagerHandle;

    public static void init() throws VISAException {

        try {
            if (OS_NAME.contains("win")) {
                libName = "agvisa32";
                lib     = Native.loadLibrary(libName, VISANativeInterface.class);
            } else if (OS_NAME.contains("linux") || OS_NAME.contains("mac")) {
                libName = "visa";
                lib     = Native.loadLibrary(libName, VISANativeInterface.class);
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
