package jisa.visa.drivers;

import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Platform;
import jisa.visa.exceptions.VISAException;

public class LinuxGPIBDriver extends GPIBDriver {

    protected static       GPIBNativeInterface lib     = null;
    protected static       String              libName;

    public LinuxGPIBDriver() throws VISAException {
        super();
    }

    protected void initialise() throws VISAException {

        if (lib != null) {
            return;
        }

        try {

            if (Platform.isLinux()) {
                libName = "gpib";
                lib     = Native.loadLibrary(libName, GPIBNativeInterface.class);
            } else {
                throw new VISAException("Platform not supported.");
            }

        } catch (UnsatisfiedLinkError e) {
            lib = null;
        }

        if (lib == null) {
            throw new VISAException("Could not load Linux-GPIB library");
        }

        try {
            NativeLibrary nLib = NativeLibrary.getInstance(libName);
            lib.ibsta.setPointer(nLib.getGlobalVariableAddress("ibsta"));
            lib.iberr.setPointer(nLib.getGlobalVariableAddress("iberr"));
            lib.ibcnt.setPointer(nLib.getGlobalVariableAddress("ibcnt"));
        } catch (Exception | Error e) {
            throw new VISAException("Could not link global variables");
        }

    }

    @Override
    protected GPIBNativeInterface lib() {
        return lib;
    }

    @Override
    protected int getIBSTA() {
        return lib().ibsta.getValue();
    }

    @Override
    protected int getIBERR() {
        return lib.iberr.getValue();
    }

    @Override
    protected int getIBCNT() {
        return lib.ibcnt.getValue();
    }
}
