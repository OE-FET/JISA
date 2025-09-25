package jisa.devices;

import jisa.addresses.Address;
import jisa.control.SRunnable;
import jisa.devices.features.Feature;
import kotlin.jvm.JvmClassMappingKt;
import kotlin.reflect.KClass;
import org.apache.commons.lang3.ClassUtils;
import org.reflections.Reflections;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Interface for defining the base functionality of all instruments.
 */
public interface Instrument {

    static String getDescription(Class<? extends Instrument> clazz) {

        try {
            return clazz.getDeclaredMethod("getDescription").invoke(null).toString();
        } catch (Exception e) {
            return getName(clazz);
        }

    }

    static String getName(Class<? extends Instrument> clazz) {

        Class<?> enclosingClass = clazz.getEnclosingClass();

        if (enclosingClass == null) {
            return clazz.getSimpleName();
        } else {
            return String.format("%s.%s", enclosingClass.getSimpleName(), clazz.getSimpleName());
        }

    }

    /**
     * Returns an identifying String of the instrument.
     *
     * @return Identifying String
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    String getIDN() throws IOException, DeviceException;

    /**
     * Returns the name of the instrument or channel.
     *
     * @return Name
     */
    String getName();

    /**
     * Closes the connection to the instrument.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    void close() throws IOException, DeviceException;

    /**
     * Returns any Address object used to connect to this instrument.
     *
     * @return Address object, null if none
     */
    Address getAddress();

    /**
     * Sets the timeout for read/write operations to this instrument (if applicable).
     *
     * @param msec Timeout, in milliseconds
     *
     * @throws IOException Upon communications error
     */
    default void setTimeout(int msec) throws IOException {

    }

    default Object getLockObject() {
        return this;
    }

    /**
     * Returns a list of all instrument parameters defined by its base type(s) --- i.e., configuration parameters
     * common to all instruments of the same type. This may depend on what the instrument is intended to be used as,
     * for instance an SMU being used as a VMeter will return an extra configuration option to set current to zero.
     *
     * @param target The target class that ths instrument is going to be used as.
     *
     * @return List of base instrument parameters.
     */
    default void addBaseParameters(Class<?> target, ParameterList parameters) {

        Reflections                 reflections = new Reflections("jisa.devices");
        Class<? extends Instrument> thisClass   = getClass();

        List<Class<?>> interfaces = ClassUtils.getAllInterfaces(getClass());

        Collections.reverse(interfaces);

        interfaces.stream().filter(Instrument.class::isAssignableFrom).forEach(type -> {

            try {

                type.getMethod(
                    "addParameters",
                    type,
                    Class.class,
                    ParameterList.class
                ).invoke(null, this, target, parameters);

            } catch (Throwable ignored) { }

        });

    }

    /**
     * Returns a list of all instrument-specific parameters --- i.e., configuration parameters specific to just this
     * make/model of instrument.
     *
     * @param target The target class that this instrument is going to be used as.
     *
     * @return List of instrument-specific configuration parameters.
     */
    default void addInstrumentParameters(Class<?> target, ParameterList parameters) {

    }

    /**
     * Returns a list of all configuration parameters from extra features this instrument implements.
     *
     * @param target The target class that this instrument is going to be used as (e.g., an SMU might be used as a VMeter).
     *
     * @return List of feature configuration parameters.
     */
    default void addFeatureParameters(Class<?> target, ParameterList parameters) {
        Feature.addFeatureParameters(this, target, parameters);
    }

    /**
     * Returns all configuration parameters for this instrument, for given target usage type. For instance if a
     * measurement routine is to use a multimeter (IVMeter) simply as a voltmeter (VMeter), then options regarding
     * current measurements will be omitted etc.
     *
     * @param target Target instrument type.
     *
     * @return List of parameters.
     */
    default List<Parameter<?>> getAllParameters(Class<?> target) {

        ParameterList parameters = new ParameterList();

        addBaseParameters(target, parameters);
        addFeatureParameters(target, parameters);
        addInstrumentParameters(target, parameters);

        return parameters;

    }

    /**
     * Returns all configuration parameters for this instrument.
     *
     * @return List of parameters
     */
    default List<Parameter<?>> getAllParameters() {
        return getAllParameters(getClass());
    }

    default <I> void ifImplements(Class<I> target, InstrumentAcceptor<I> action) throws IOException, DeviceException, InterruptedException {

        if (target.isAssignableFrom(this.getClass())) {
            action.accept((I) this);
        }

    }

    default <I> void ifImplements(Class<I> target, InstrumentAcceptor<I> action, SRunnable otherwise) throws IOException, DeviceException, InterruptedException {

        if (target.isAssignableFrom(this.getClass())) {

            action.accept((I) this);

        } else {

            try {
                otherwise.run();
            } catch (IOException | DeviceException | InterruptedException e) {
                throw e;
            } catch (Exception e) {
                throw new DeviceException(e.getMessage());
            }

        }

    }

    default <I> void ifImplements(KClass<I> target, InstrumentAcceptor<I> action) throws IOException, DeviceException, InterruptedException {
        ifImplements(JvmClassMappingKt.getJavaObjectType(target), action);
    }

    default <I> void ifImplements(KClass<I> target, InstrumentAcceptor<I> action, SRunnable otherwise) throws IOException, DeviceException, InterruptedException {
        ifImplements(JvmClassMappingKt.getJavaObjectType(target), action, otherwise);
    }

    interface InstrumentAcceptor<I> {
        void accept(I instrument) throws IOException, DeviceException, InterruptedException;
    }

    interface Setter<S> {
        void set(S value) throws IOException, DeviceException;
    }

    interface Getter<S> {
        S get() throws IOException, DeviceException;
    }

    class Parameter<S> {

        private final String    name;
        private final S         defaultValue;
        private final Setter<S> setter;
        private final List<S>   options;
        private final Getter<S> getter;

        public Parameter(String name, S defaultValue, Setter<S> setter, S... options) {
            this(name, defaultValue, setter, null, options);
        }

        public Parameter(String name, S defaultValue, Setter<S> setter, Getter<S> getter, S... options) {
            this.name         = name;
            this.defaultValue = defaultValue;
            this.setter       = setter;
            this.getter       = getter;
            this.options      = options.length > 0 ? List.of(options) : Collections.emptyList();
        }

        public String getName() {
            return name;
        }

        public Parameter<S> copy(String newName) {
            return new Parameter<>(newName, defaultValue, setter, getter, (S[]) options.toArray());
        }

        public void set(S value) throws IOException, DeviceException {
            setter.set(value);
        }

        public S getDefaultValue() {
            return defaultValue;
        }

        public S getCurrentValue() throws IOException, DeviceException {

            if (getter == null) {
                return null;
            }

            return getter.get();
        }

        public boolean hasGetter() {
            return getter != null;
        }

        public boolean isChoice() {
            return !options.isEmpty();
        }

        public List<S> getChoices() {
            return options;
        }

    }

    class AutoQuantity<S> {

        private final boolean auto;
        private final S       otherwise;

        public AutoQuantity(boolean auto, S otherwise) {
            this.auto      = auto;
            this.otherwise = otherwise;
        }

        public boolean isAuto() {
            return auto;
        }

        public S getValue() {
            return otherwise;
        }

        public boolean equals(Object o) {

            if (this == o) {
                return true;
            }

            return (o instanceof AutoQuantity) && (((AutoQuantity<?>) o).isAuto() == isAuto()) && ((AutoQuantity<?>) o).getValue().equals(getValue());

        }

    }

    class OptionalQuantity<S> {

        private final boolean used;
        private final S       otherwise;

        public OptionalQuantity(boolean used, S otherwise) {
            this.used      = used;
            this.otherwise = otherwise;
        }

        public boolean isUsed() {
            return used;
        }

        public S getValue() {
            return otherwise;
        }

        public boolean equals(Object o) {

            if (this == o) {
                return true;
            }

            return (o instanceof OptionalQuantity) && (((OptionalQuantity<?>) o).isUsed() == isUsed()) && (((OptionalQuantity<?>) o).getValue().equals(getValue()));

        }

    }

    class TableQuantity {

        private final String[]           columns;
        private final List<List<Double>> value;

        public TableQuantity(String[] columns, List<List<Double>> value) {
            this.columns = columns;
            this.value   = value;
        }

        public String[] getColumns() {
            return columns;
        }

        public List<List<Double>> getValue() {
            return value;
        }

    }

}
