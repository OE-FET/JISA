package jisa.devices.spectrometer;

import jisa.devices.*;
import jisa.devices.mux.Multiplexer;
import jisa.maths.Range;

import java.io.IOException;
import java.util.List;

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

    default List<Component> getSubInstruments() {
        return getComponents();
    }


    interface Component<S extends Spectrograph, D> extends SubInstrument<S> {

        static void addParameters(Component inst, Class target, ParameterList parameters) {

            try {

                List possibleValues = inst.getPossibleValues();

                if (possibleValues.isEmpty()) {
                    parameters.addValue("Components", inst.getName(), inst::getValue, null, inst::setValue);
                } else {
                    parameters.addChoice("Components", inst.getName(), (Getter) inst::getValue, null, (Setter) inst::setValue, possibleValues.toArray());
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

    class Grating {

        private final int    index;
        private final String name;
        private final double density;

        public Grating(int index, String name, double density) {
            this.index   = index;
            this.name    = name;
            this.density = density;
        }

        public int getIndex() {
            return index;
        }

        public String getName() {
            return name;
        }

        public double getDensity() {
            return density;
        }

        public String toString() {
            return getName();
        }

    }


}
