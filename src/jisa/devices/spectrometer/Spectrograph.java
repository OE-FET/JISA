package jisa.devices.spectrometer;

import jisa.devices.*;
import jisa.devices.mux.Multiplexer;
import jisa.maths.Range;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public interface Spectrograph extends Instrument, MultiInstrument {

    static void addParameters(Spectrograph inst, Class target, ParameterList parameters) {

        for (Component component : inst.getComponents()) {
            parameters.addAll(component.getAllParameters(target));
        }

    }

    /**
     * Returns a list of all the subcomponents of this spectrograph (like flippers/mirrors, irises etc).
     *
     * @return List of subcomponents.
     */
    List<Component> getComponents();

    default List<SwappableGrating> getSwappableGratings() {
        return getComponents().stream().filter(c -> c instanceof SwappableGrating).map(c -> (SwappableGrating) c).collect(Collectors.toList());
    }

    default List<Flipper> getFlippers() {
        return getComponents().stream().filter(c -> c instanceof Flipper).map(c -> (Flipper) c).collect(Collectors.toList());
    }

    default List<Iris> getIrises() {
        return getComponents().stream().filter(c -> c instanceof Iris).map(c -> (Iris) c).collect(Collectors.toList());
    }

    default List<AdjustableSlit> getAdjustableSlits() {
        return getComponents().stream().filter(c -> c instanceof AdjustableSlit).map(c -> (AdjustableSlit) c).collect(Collectors.toList());
    }

    default List<MotorMirror> getMotorMirrors() {
        return getComponents().stream().filter(c -> c instanceof MotorMirror).map(c -> (MotorMirror) c).collect(Collectors.toList());
    }

    default List<FilterWheel> getFilterWheels() {
        return getComponents().stream().filter(c -> c instanceof FilterWheel).map(c -> (FilterWheel) c).collect(Collectors.toList());
    }

    default List<Component> getSubInstruments() {
        return getComponents();
    }

    /**
     * Sets the state of the specified spectrograph component.
     *
     * @param component The component to change the state of.
     * @param state     The state to change it to.
     * @param <T>       The data type used to represent the state of the specified component.
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon device / compatibility error.
     */
    default <T> void setComponentState(Component<?, T> component, T state) throws IOException, DeviceException {

        if (component == null) {
            throw new DeviceException("The specified component was not found (null).");
        }

        if (!getComponents().contains(component)) {
            throw new DeviceException("This spectrometer does not have component %s.", component);
        }

        component.setValue(state);

    }

    /**
     * Returns the current state of the specified spectrograph component.
     *
     * @param component The component to query the state of.
     * @param <T>       The data type used to represent the state of the specified component.
     * @return The current state of the specified component.
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon device / compatibility error.
     */
    default <T> T getComponentState(Component<?, T> component) throws IOException, DeviceException {

        if (component == null) {
            throw new DeviceException("The specified component was not found (null).");
        }

        if (!getComponents().contains(component)) {
            throw new DeviceException("This spectrometer does not have component %s.", component);
        }

        return component.getValue();

    }

    interface Component<S extends Spectrograph, D> extends SubInstrument<S> {

        static void addParameters(Component inst, Class target, ParameterList parameters) {

            try {

                List possibleValues = inst.getPossibleValues();

                if (possibleValues.isEmpty()) {
                    parameters.addValue("Spectrograph Components", inst.getName(), inst::getValue, null, inst::setValue);
                } else {
                    parameters.addChoice("Spectrograph Components", inst.getName(), (Getter) inst::getValue, null, (Setter) inst::setValue, possibleValues.toArray());
                }

            } catch (Throwable ignored) {
            }

        }

        D getValue() throws IOException, DeviceException;

        void setValue(D value) throws IOException, DeviceException;

        List<D> getPossibleValues() throws IOException, DeviceException;

        default D getMin() throws IOException, DeviceException {
            return null;
        }

        default D getMax() throws IOException, DeviceException {
            return null;
        }

    }

    interface Flipper<S extends Spectrograph> extends Component<S, Integer>, Multiplexer {

        default List<Integer> getPossibleValues() throws IOException, DeviceException {
            return Range.count(0, getRouteCount() - 1).list();
        }

        default Integer getValue() throws IOException, DeviceException {
            return getRoute();
        }

        default void setValue(Integer value) throws IOException, DeviceException {
            setRoute(value);
        }

        default Integer getMin() throws IOException, DeviceException {
            return 0;
        }

        default Integer getMax() throws IOException, DeviceException {
            return getRouteCount() - 1;
        }

    }

    interface Iris<S extends Spectrograph> extends Component<S, Double> {

        Double getMin() throws IOException, DeviceException;

        Double getMax() throws IOException, DeviceException;

    }

    interface SwappableGrating<S extends Spectrograph> extends Component<S, Grating> {


    }

    interface AdjustableSlit<S extends Spectrograph> extends Component<S, Double> {

        Double getMin() throws IOException, DeviceException;

        Double getMax() throws IOException, DeviceException;

        default List<Double> getPossibleValues() throws IOException, DeviceException {
            return List.of();
        }

    }

    interface FilterWheel<S extends Spectrograph> extends Component<S, Filter> {

    }

    interface MotorMirror<S extends Spectrograph> extends Component<S, Integer> {

        Integer getMin() throws IOException, DeviceException;

        Integer getMax() throws IOException, DeviceException;

        default List<Integer> getPossibleValues() throws IOException, DeviceException {
            return List.of();
        }

    }

    class Grating {

        private final int    index;
        private final String name;

        public Grating(int index, String name) {
            this.index = index;
            this.name  = name;
        }

        public int getIndex() {
            return index;
        }

        public String getName() {
            return name;
        }

        public String toString() {
            return name;
        }

    }

    class Filter {

        private final int    index;
        private final String name;

        public Filter(int index, String name) {
            this.index = index;
            this.name  = name;
        }

        public int getIndex() {
            return index;
        }

        public String getName() {
            return name;
        }

        public String toString() {
            return name;
        }

    }


}
