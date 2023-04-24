package jisa.devices.interfaces;

import kotlin.jvm.JvmClassMappingKt;
import kotlin.reflect.KClass;

import java.util.List;
import java.util.stream.Collectors;

public interface MultiInstrument {

    List<Instrument> getSubInstruments();

    default List<Class<? extends Instrument>> getSubInstrumentTypes() {
        return getSubInstruments().stream().map(Instrument::getClass).distinct().collect(Collectors.toUnmodifiableList());
    }

    default <I extends Instrument> List<I> get(Class<I> type) {
        return (List<I>) getSubInstruments().stream().filter(i -> type.isAssignableFrom(i.getClass())).collect(Collectors.toUnmodifiableList());
    }

    default <I extends Instrument> List<I> get(KClass<I> type) {
        return get(JvmClassMappingKt.getJavaClass(type));
    }

    default <I extends Instrument> I get(Class<I> type, int n) {
        return (I) getSubInstruments().stream().filter(i -> type.isAssignableFrom(i.getClass())).skip(n).findFirst().orElse(null);
    }

    default <I extends Instrument> I get(KClass<I> type, int n) {
        return get(JvmClassMappingKt.getJavaClass(type), n);
    }

    default boolean contains(Class<? extends Instrument> type) {
        return getSubInstruments().stream().anyMatch(i -> type.isAssignableFrom(i.getClass()));
    }

}
