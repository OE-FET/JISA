package JISA.VISA;

import JISA.Devices.DeviceException;
import JISA.Devices.Instrument;
import com.sun.jna.Library;
import com.sun.jna.Native;

import java.io.IOException;

public abstract class NativeDevice<I extends Library> implements Instrument {

    protected I      lib;
    protected String name;

    public NativeDevice(String libraryName, Class<I> libraryInterface) throws IOException {

        name = libraryName;

        try {
            lib = Native.loadLibrary(libraryName, libraryInterface);
        } catch (Throwable e) {
            throw new IOException(String.format("Unable to load library \"%s\":\n\n%s", name, e.getMessage()));
        }

    }

    public abstract String getIDN() throws IOException;

    @Override
    public abstract void close() throws IOException, DeviceException;

}
