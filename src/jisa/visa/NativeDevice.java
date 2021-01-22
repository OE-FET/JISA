package jisa.visa;

import com.sun.jna.Library;
import com.sun.jna.Native;
import jisa.Util;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.Instrument;
import org.reflections.Reflections;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public abstract class NativeDevice<I extends Library> implements Instrument {

    private final static List<WeakReference<NativeDevice<?>>> opened = new LinkedList<>();

    static {

        Util.addShutdownHook(() -> {

            for (WeakReference<NativeDevice<?>> reference : opened) {

                NativeDevice<?> device = reference.get();

                try {
                    if (device != null) device.close();
                } catch (Exception ignored) {
                    // Ignored
                }

            }

        });

    }

    protected I      lib;
    protected String name;

    public NativeDevice(String libraryName, Class<I> libraryInterface) throws IOException {

        name = libraryName;

        try {
            lib = Native.loadLibrary(libraryName, libraryInterface);
        } catch (Throwable e) {
            throw new IOException(String.format("Unable to load library \"%s\":\n\n%s", name, e.getMessage()));
        }

        opened.add(new WeakReference<>(this));

    }

    public NativeDevice(String libraryName, I library) {

        name = libraryName;
        lib  = library;

        opened.add(new WeakReference<>(this));
    }

    public static List<NativeDevice<?>> search() {

        Reflections                        reflection = new Reflections("jisa");
        Set<Class<? extends NativeDevice>> classes    = reflection.getSubTypesOf(NativeDevice.class);

        List<NativeDevice<?>> found = new LinkedList<>();

        for (Class<? extends NativeDevice> c : classes) {

            if (!Modifier.isAbstract(c.getModifiers())) {

                try {
                    Method search = c.getMethod("find");
                    found.addAll((List<NativeDevice<?>>) search.invoke(null));
                } catch (Throwable ignored) {
                }

            }

        }

        return found;

    }

    public abstract String getIDN() throws IOException, DeviceException;

    @Override
    public abstract void close() throws IOException, DeviceException;


}
