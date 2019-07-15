package jisa.visa;

import jisa.devices.DeviceException;
import jisa.devices.Instrument;
import com.sun.jna.Library;
import com.sun.jna.Native;
import org.reflections.Reflections;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public abstract class NativeDevice<I extends Library> implements Instrument {

    protected I      lib;
    protected String name;

    public static List<NativeDevice> search() {

        Reflections                        reflection = new Reflections("jisa");
        Set<Class<? extends NativeDevice>> classes    = reflection.getSubTypesOf(NativeDevice.class);

        List<NativeDevice> found = new LinkedList<>();

        for (Class<? extends NativeDevice> c : classes) {

            if (!Modifier.isAbstract(c.getModifiers())) {

                try {
                    Method search = c.getMethod("find");
                    found.addAll((List<NativeDevice>) search.invoke(null));
                } catch (Throwable ignored) {
                }

            }

        }

        return found;

    }

    public NativeDevice(String libraryName, Class<I> libraryInterface) throws IOException {

        name = libraryName;

        try {
            lib = Native.loadLibrary(libraryName, libraryInterface);
        } catch (Throwable e) {
            throw new IOException(String.format("Unable to load library \"%s\":\n\n%s", name, e.getMessage()));
        }

    }

    public NativeDevice(String libraryName, Class<I> libraryInterface, I library) {
        name = libraryName;
        lib = library;
    }

    public abstract String getIDN() throws IOException, DeviceException;

    @Override
    public abstract void close() throws IOException, DeviceException;

    public void finalize() {

        try {
            close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
