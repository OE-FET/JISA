package jisa.devices.interfaces;

import kotlin.jvm.JvmClassMappingKt;
import kotlin.reflect.KClass;

import java.util.List;
import java.util.stream.Collectors;

public interface MultiInstrument {

    /**
     * Returns a list of all sub-instruments contained within this instrument
     *
     * @return List of sub-instruments
     */
    List<? extends Instrument> getSubInstruments();

    /**
     * Returns a list of the classes of sub-instruments this instrument contains.
     *
     * @return List of sub-instrument classes
     */
    default List<Class<? extends Instrument>> getSubInstrumentTypes() {
        return getSubInstruments().stream().map(Instrument::getClass).distinct().collect(Collectors.toUnmodifiableList());
    }

    /**
     * Returns a list of all sub-instruments that can be considered to be of the given class, contained in this instrument.
     *
     * @param type Type of sub-instrument
     * @param <I>  Instrument class
     *
     * @return List of matching sub-instruments
     */
    default <I extends Instrument> List<I> get(Class<I> type) {
        return (List<I>) getSubInstruments().stream().filter(i -> type.isAssignableFrom(i.getClass())).collect(Collectors.toUnmodifiableList());
    }

    /**
     * Returns a list of all sub-instruments that can be considered to be of the given class, contained in this instrument.
     *
     * @param type Type of sub-instrument
     * @param <I>  Instrument class
     *
     * @return List of matching sub-instruments
     */
    default <I extends Instrument> List<I> getSubInstruments(Class<I> type) {
        return get(type);
    }

    /**
     * Returns a list of all sub-instruments that can be considered to be of the given (Kotlin) class, contained in this instrument.
     *
     * @param type Type of sub-instrument
     * @param <I>  Instrument class
     *
     * @return List of matching sub-instruments
     */
    default <I extends Instrument> List<I> get(KClass<I> type) {
        return get(JvmClassMappingKt.getJavaClass(type));
    }

    /**
     * Returns the nth (indexed from 0) sub-instrument that can be considered to be of the given class contained in this instrument.
     *
     * @param type Type of sub-instrument
     * @param n    Index
     * @param <I>  Instrument class
     *
     * @return Found instrument
     */
    default <I extends Instrument> I get(Class<I> type, int n) {
        return (I) getSubInstruments().stream().filter(i -> type.isAssignableFrom(i.getClass())).skip(n).findFirst().orElse(null);
    }

    /**
     * Returns the nth (indexed from 0) sub-instrument that can be considered to be of the given class contained in this instrument.
     *
     * @param type Type of sub-instrument
     * @param n    Index
     * @param <I>  Instrument class
     *
     * @return Found instrument
     */
    default <I extends Instrument> I getSubInstrument(Class<I> type, int n) {
        return get(type, n);
    }

    /**
     * Returns the nth (indexed from 0) sub-instrument that can be considered to be of the given (Kotlin) class contained in this instrument.
     *
     * @param type Type of sub-instrument
     * @param n    Index
     * @param <I>  Instrument class
     *
     * @return Found instrument
     */
    default <I extends Instrument> I get(KClass<I> type, int n) {
        return get(JvmClassMappingKt.getJavaClass(type), n);
    }

    /**
     * Returns whether this instrument contains any sub-instruments that match the given type.
     *
     * @param type Sub-instrument type
     *
     * @return Does it contain any?
     */
    default boolean contains(Class<? extends Instrument> type) {
        return getSubInstrumentTypes().stream().anyMatch(type::isAssignableFrom);
    }

}
