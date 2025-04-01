package jisa.visa;

import com.sun.jna.Library;
import com.sun.jna.Native;
import jisa.Util;
import jisa.devices.DeviceException;
import jisa.devices.Instrument;
import jisa.devices.LibraryInitialisationException;
import jisa.devices.LibraryNotFoundException;
import org.reflections.Reflections;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Abstract base class for instruments that use native libraries for communications.
 */
public abstract class NativeDevice implements Instrument {

    private final static List<WeakReference<NativeDevice>> opened    = new LinkedList<>();
    private final static Map<Class, Library>               libraries = new HashMap<>();

    static {

        Util.addShutdownHook(() -> {

            for (WeakReference<NativeDevice> reference : opened) {

                NativeDevice device = reference.get();

                try {
                    if (device != null) { device.close(); }
                } catch (Throwable ignored) {
                    // Ignored
                }

            }

        });

    }

    private final String name;

    public NativeDevice(String name) {
        this.name = name;
        opened.add(new WeakReference<>(this));
    }

    /**
     * Returns an instance of the specified native library. If it has already been loaded via this method,
     * then a cached instance will be returned to prevent multiple instances of the library being
     * linked.
     *
     * @param libraryInterface Class of the interface used to represent the library.
     * @param libraryName      Name of the library (i.e., X means X.dll or libX.so etc).
     * @param <I>              The interface used to represent the library.
     *
     * @return Object representing the library.
     *
     * @throws DeviceException If the library cannot be found or fails to initialise.
     */
    public <I extends NativeLibrary> I findLibrary(Class<I> libraryInterface, String libraryName) throws DeviceException {

        // If it's already been loaded, return cached instance.
        if (libraries.containsKey(libraryInterface)) {
            return (I) libraries.get(libraryInterface);
        }

        I loaded = getNewLibraryInstance(libraryInterface, libraryName);

        libraries.put(libraryInterface, loaded);

        return loaded;

    }

    /**
     * Returns a new instance of the specified native library. Does not use any caching --- a new instance will be
     * returned each time this is called. You probably want to use findLibrary(...) instead.
     *
     * @param libraryInterface Class of the interface used to represent the library.
     * @param libraryName      Name of the library (i.e., X means X.dll or libX.so etc).
     * @param <I>              The interface used to represent the library.
     *
     * @return Object representing the library.
     *
     * @throws DeviceException If the library cannot be found or fails to initialise.
     */
    public <I extends NativeLibrary> I getNewLibraryInstance(Class<I> libraryInterface, String libraryName) throws DeviceException {

        try {

            I loaded = Native.load(libraryName, libraryInterface);

            try {
                loaded.initialise();
            } catch (Throwable e) {
                throw new LibraryInitialisationException(libraryName, name, e.getMessage());
            }

            return loaded;

        } catch (Throwable e) {
            throw new LibraryNotFoundException(libraryName, name);
        }

    }

    public static List<NativeDevice> search() {

        Reflections                        reflection = new Reflections("jisa");
        Set<Class<? extends NativeDevice>> classes    = reflection.getSubTypesOf(NativeDevice.class);

        List<NativeDevice> found = new LinkedList<>();

        for (Class<? extends NativeDevice> c : classes) {

            if (!Modifier.isAbstract(c.getModifiers())) {

                try {
                    Method search = c.getMethod("find");
                    found.addAll((List<NativeDevice>) search.invoke(null));
                } catch (Throwable ignored) { }

            }

        }

        return found;

    }

    public abstract String getIDN() throws IOException, DeviceException;

    @Override
    public abstract void close() throws IOException, DeviceException;

    public interface Initialiser<T extends Library> {
        void initialise(T library) throws Exception;
    }

}
