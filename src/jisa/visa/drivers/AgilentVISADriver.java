package jisa.visa.drivers;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.NativeLongByReference;
import jisa.visa.VISAException;
import jisa.visa.VISANativeInterface;

public class AgilentVISADriver extends NIVISADriver {

    private static       VISANativeInterface libStatic;
    private static final String              OS_NAME          = System.getProperty("os.name").toLowerCase();
    private static       String              libName;
    private static final String              responseEncoding = "UTF8";
    private static final long                VISA_ERROR       = 0x7FFFFFFF;
    private static final int                 _VI_ERROR        = -2147483648;
    private static final int                 VI_SUCCESS       = 0;
    private static final int                 VI_NULL          = 0;
    private static final int                 VI_TRUE          = 1;
    private static final int                 VI_FALSE         = 0;
    private static       NativeLong          visaResourceManagerHandleStatic;

    public AgilentVISADriver() {
        lib = AgilentVISADriver.libStatic;
        visaResourceManagerHandle = AgilentVISADriver.visaResourceManagerHandleStatic;
    }

    public static void init() throws VISAException {

        try {
            if (OS_NAME.contains("win")) {
                AgilentVISADriver.libName = "agvisa32";
                AgilentVISADriver.libStatic     = Native.loadLibrary(AgilentVISADriver.libName, VISANativeInterface.class);
            } else if (OS_NAME.contains("linux") || OS_NAME.contains("mac")) {
                AgilentVISADriver.libName = "visa";
                AgilentVISADriver.libStatic     = Native.loadLibrary(AgilentVISADriver.libName, VISANativeInterface.class);
            } else {
                throw new VISAException("Platform not yet supported!");
            }
        } catch (UnsatisfiedLinkError e) {
            AgilentVISADriver.libStatic = null;
        }

        if (AgilentVISADriver.libStatic == null) {
            throw new VISAException("Could not load VISA library");
        }

        // Attempt to get a resource manager handle
        try {
            AgilentVISADriver.visaResourceManagerHandleStatic = getResourceManager();
        } catch (VISAException e) {
            throw new VISAException("Could not get resource manager");
        }

    }

    /**
     * Sets up the resource manager for this session. Used only internally.
     *
     * @return Resource manager handle.
     *
     * @throws VISAException When VISA does go gone screw it up
     */
    protected static NativeLong getResourceManager() throws VISAException {

        NativeLongByReference pViSession = new NativeLongByReference();
        NativeLong            visaStatus = AgilentVISADriver.libStatic.viOpenDefaultRM(pViSession);

        if (visaStatus.longValue() != VI_SUCCESS) {
            throw new VISAException("Error opening resource manager!");
        }
        return pViSession.getValue();

    }

}
