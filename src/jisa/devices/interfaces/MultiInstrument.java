package jisa.devices.interfaces;

import kotlin.jvm.JvmClassMappingKt;
import kotlin.reflect.KClass;

import java.util.List;
import java.util.stream.Collectors;

public interface MultiInstrument {

    /**
     * Returns a list of all the different classes of sub-instruments that this instrument contains.
     *
     * @return List of sub-instrument classes
     */
    List<Class<? extends Instrument>> getSubInstrumentTypes();

    /**
     * Checks whether this instrument contains any sub-instruments that can be considered to be of the specified type
     *
     * @param type Type to check
     *
     * @return Does it contain any?
     */
    default boolean contains(Class<? extends Instrument> type) {
        return getSubInstruments().stream().anyMatch(s -> type.isAssignableFrom(s.getClass()));
    }

    default boolean contains(KClass<? extends Instrument> type) {
        return contains(JvmClassMappingKt.getJavaClass(type));
    }

    /**
     * Returns a list of all sub-instruments this instrument contains that can be considered to be of the given instrument type.
     *
     * @param type The class object of sub-instrument to return
     * @param <I>  Instrument class
     *
     * @return List of sub-instruments matching the given class
     */
    <I extends Instrument> List<I> get(Class<I> type);

    /**
     * Returns a list of all sub-instruments this instrument contains that can be considered to be of the given instrument type.
     *
     * @param type The (Kotlin) class object of sub-instrument to return
     * @param <I>  Instrument class
     *
     * @return List of sub-instruments matching the given class
     */
    default <I extends Instrument> List<I> get(KClass<I> type) {
        return get(JvmClassMappingKt.getJavaClass(type));
    }

    default <I extends Instrument> List<I> getSubInstruments(Class<I> type) {
        return get(type);
    }

    default <I extends Instrument> I getSubInstrument(Class<I> type, int n) {
        return get(type).get(n);
    }

    /**
     * Returns a list of all sub-instruments contained in this instrument.
     *
     * @return List of all sub-instruments
     */
    default List<? extends Instrument> getSubInstruments() {

        return getSubInstrumentTypes()
            .stream()
            .flatMap(t -> get(t).stream())
            .distinct()
            .collect(Collectors.toUnmodifiableList());

    }

}
