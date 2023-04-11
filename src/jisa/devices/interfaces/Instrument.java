package jisa.devices.interfaces;

import jisa.addresses.Address;
import jisa.devices.DeviceException;
import kotlin.jvm.JvmClassMappingKt;
import kotlin.reflect.KClass;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Interface for defining the base functionality of all instruments.
 */
public interface Instrument {

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

    default List<Parameter<?>> getConfigurationParameters(Class<?> target) {
        return new LinkedList<>();
    }

    default List<Parameter<?>> getConfigurationParameters(KClass<?> target) {
        return getConfigurationParameters(JvmClassMappingKt.getJavaClass(target));
    }

    interface Setter<S> {

        void set(S value) throws IOException, DeviceException;

    }

    class Parameter<S> {

        private final String    name;
        private final S         defaultValue;
        private final Setter<S> setter;
        private final List<S>   options;

        public Parameter(String name, S defaultValue, Setter<S> setter, S... options) {
            this.name         = name;
            this.defaultValue = defaultValue;
            this.setter       = setter;
            this.options      = options.length > 0 ? List.of(options) : Collections.emptyList();
        }

        public String getName() {
            return name;
        }

        public void set(S value) throws IOException, DeviceException {
            setter.set(value);
        }

        public S getDefaultValue() {
            return defaultValue;
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
