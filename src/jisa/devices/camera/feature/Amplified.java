package jisa.devices.camera.feature;

import jisa.devices.DeviceException;
import jisa.devices.ParameterList;
import jisa.devices.features.Feature;

import java.io.IOException;
import java.util.List;

public interface Amplified extends Feature {

    static void addParameters(Amplified inst, Class<?> target, ParameterList params) {

        List<? extends Amplifier> amps  = inst.getAmplifiers();
        List<Double>              gains = inst.getAmplifierGains();

        if (!amps.isEmpty()) {
            params.addChoice("Amplifier", "Type", inst::getAmplifier, Amplifier.NONE, inst::setAmplifier, amps.toArray(Amplifier[]::new));
        }

        if (!gains.isEmpty() && gains.get(0) == -1.0) {
            params.addValue("Amplifier", "Gain", inst::getAmplifierGain, 1.0, inst::setAmplifierGain);
        } else if (!gains.isEmpty()) {
            params.addChoice("Amplifier", "Gain", inst::getAmplifierGain, 1.0, inst::setAmplifierGain, gains.toArray(Double[]::new));
        }

    }

    /**
     * Sets the gain to use when amplifying the readout of frames from this camera.
     * If only discrete options are available, the closest value is select.
     *
     * @param gain The gain to use, in dB.
     * @throws DeviceException Upon device incompatibility error.
     * @throws IOException     Upon communications error.
     */
    void setAmplifierGain(double gain) throws DeviceException, IOException;

    /**
     * Returns the gain used when amplifying the readout of frames from this camera.
     *
     * @throws DeviceException Upon device incompatibility error.
     * @throws IOException     Upon communications error.
     * @returns The gain being used, in dB.
     */
    double getAmplifierGain() throws DeviceException, IOException;


    /**
     * Returns a list of all available gain settings for the camera's preamplifier. A list containing only -1 indicates
     * a continuous range. An empty list indicates no configurable gain.
     *
     * @return List of values
     */
    default List<Double> getAmplifierGains() {
        return List.of(-1.0);
    }

    /**
     * Sets which amplifier to use on this camera's output (if multiple choices exist).
     *
     * @param amplifier The amplifier to use.
     * @throws DeviceException Upon device or compatibility error.
     * @throws IOException     Upon communications error.
     */
    default void setAmplifier(Amplifier amplifier) throws DeviceException, IOException {
        /* do nothing */
    }

    /**
     * Returns the amplifier the camera is using on its output (if multiple choices exist).
     *
     * @return The amplifier in use.
     * @throws DeviceException Upon device or compatibility error.
     * @throws IOException     Upon communications error.
     */
    default Amplifier getAmplifier() throws DeviceException, IOException {
        return Amplifier.NONE;
    }

    /**
     * Returns a list of amplifiers that can be used on this camera's output. If there is no choice, this will return
     * an empty list.
     *
     * @return List of available amplifiers.
     */
    default List<? extends Amplifier> getAmplifiers() {
        return List.of();
    }

    class Amplifier {

        public static final Amplifier NONE = new Amplifier(-1, "None");

        private final int    index;
        private final String name;

        public Amplifier(int index, String name) {
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
            return getName();
        }

    }

}
