package jisa.devices.preamp;

import jisa.Util;
import jisa.devices.DeviceException;
import jisa.devices.Instrument;
import jisa.devices.ParameterList;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public interface VPreAmp extends Instrument {

    static String getDescription() {
        return "Voltage Pre-Amplifier";
    }

    static void addParameters(VPreAmp inst, Class target, ParameterList parameters) {

        parameters.addValue("Gain", inst::getGain, 1.0, inst::setGain);

        List<? extends Input> inputs;
        try {
            inputs = inst.getInputs();
        } catch (Throwable e) {
            inputs = List.of(new Input<>(null, false, 0.0, "UNKNOWN"));
        }

        parameters.addChoice("Input", inst::getInput, inputs.get(0), inst::setInput, inputs.toArray(Input[]::new));
        parameters.addChoice("Coupling", inst::getCoupling, Coupling.AC, inst::setCoupling, Coupling.values());
        parameters.addValue("Filter Roll-Off [dB/oct]", inst::getFilterRollOff, 6.0, inst::setFilterRollOff);
        parameters.addOptional("Low-Pass Filter [Hz]", false, 3.0, v -> inst.setLowPassFrequency(0.0), inst::setLowPassFrequency);
        parameters.addOptional("High-Pass Filter [Hz]", false, 3.0, v -> inst.setHighPassFrequency(0.0), inst::setHighPassFrequency);

    }


    /**
     * Sets the gain to the given value, or as close to it as possible.
     *
     * @param gain Gain value
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void setGain(double gain) throws IOException, DeviceException;

    /**
     * Sets the source terminal to use. Available options can be queried by use of getInputs().
     *
     * @param source Source terminal configuration
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void setInput(Input source) throws IOException, DeviceException;

    /**
     * Returns a list of available input configurations for use on the pre-amplifier.
     *
     * @return List of available inputs
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    List<Input> getInputs() throws IOException, DeviceException;

    /**
     * Finds the input configuration that is either differential or not (depending on the supplied boolean)
     * and has the lowest input impedance that is at least that specified.
     *
     * @param differential Should this method find a differential input configuration?
     * @param minImpedance The minimum impedance the found input should have.
     *
     * @return Found configuration.
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon no suitable configuration being found.
     */
    default Input findInput(boolean differential, double minImpedance) throws IOException, DeviceException {

        return getInputs().stream().filter(i -> i.isDifferential() == differential && i.getInputImpedance() >= minImpedance)
                          .min(Comparator.comparingDouble(Input::getInputImpedance))
                          .orElseThrow(() -> new DeviceException("No input found matching those specifications."));

    }

    /**
     * Finds the greatest-impedance input configuration that is either differential or not depending on the supplied boolean.
     *
     * @param differential Should it be differential?
     *
     * @return Found configuration.
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon no suitable configuration being found.
     */
    default Input findInput(boolean differential) throws IOException, DeviceException {

        return getInputs().stream().filter(i -> i.isDifferential() == differential)
                          .max(Comparator.comparingDouble(Input::getInputImpedance))
                          .orElseThrow(() -> new DeviceException("No input found matching those specifications."));
    }

    /**
     * Finds the lowest-impedance input configuration that at least has the specified minimum impedance.
     *
     * @param minImpedance Minimum input impedance the found input should have.
     *
     * @return Found configuration.
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon no suitable configuration being found.
     */
    default Input findInput(double minImpedance) throws IOException, DeviceException {

        return getInputs().stream().filter(i -> i.getInputImpedance() >= minImpedance)
                          .min(Comparator.comparingDouble(Input::getInputImpedance))
                          .orElseThrow(() -> new DeviceException("No input found matching those specifications."));

    }

    /**
     * Sets the coupling mode of the pre-amplifier.
     *
     * @param mode Coupling mode: GROUND, DC or AC.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void setCoupling(Coupling mode) throws IOException, DeviceException;

    /**
     * Sets the frequency cut-off for the pre-amp's high-pass filter. Disabled if set to zero.
     *
     * @param frequency Frequency cut-off, in Hz.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void setHighPassFrequency(double frequency) throws IOException, DeviceException;

    /**
     * Returns the frequency cut-off for the pre-amp's high-pass filter. Disabled if set to zero.
     *
     * @return Frequency cut-off, in Hz.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    double getHighPassFrequency() throws IOException, DeviceException;

    /**
     * Sets the frequency cut-off for the pre-amp's low-pass filter. Disabled if set to zero.
     *
     * @param frequency Frequency cut-off, in Hz.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void setLowPassFrequency(double frequency) throws IOException, DeviceException;

    /**
     * Returns the frequency cut-off for the pre-amp's low-pass filter. Disabled if set to zero.
     *
     * @return Frequency cut-off, in Hz.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    double getLowPassFrequency() throws IOException, DeviceException;

    /**
     * Sets the filter roll-off value in db/decade (or as close to it as one can get).
     *
     * @param dbLevel Roll-off (db/decade)
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void setFilterRollOff(double dbLevel) throws IOException, DeviceException;

    /**
     * Returns the gain value of the amplifier currently.
     *
     * @return Gain value
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    double getGain() throws IOException, DeviceException;

    /**
     * Returns the source terminal configuration currently being used.
     *
     * @return Source terminal configuration
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    Input getInput() throws IOException, DeviceException;

    /**
     * Returns the coupling configuration currently being used.
     *
     * @return Coupling configuration
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    Coupling getCoupling() throws IOException, DeviceException;

    /**
     * Returns the filter roll-off value currently being used in db/decade
     *
     * @return Filter roll off (db/decade)
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    double getFilterRollOff() throws IOException, DeviceException;

    boolean isInverting() throws IOException, DeviceException;

    void setInverting(boolean inverting) throws IOException, DeviceException;

    class Input<T> {

        private final T       value;
        private final boolean differential;
        private final double  inputImpedance;
        private final String  name;

        public Input(T value, boolean differential, double inputImpedance, String name) {
            this.value          = value;
            this.differential   = differential;
            this.inputImpedance = inputImpedance;
            this.name           = name;
        }

        public T getValue() {
            return value;
        }

        public boolean isDifferential() {
            return differential;
        }

        public double getInputImpedance() {
            return inputImpedance;
        }

        public String getName() {
            return name;
        }

        public String toString() {
            return String.format("%s (%s, %s)", getName(), Util.toSIUnits(getInputImpedance(), "Ω"), isDifferential() ? "Differential" : "Single");
        }

    }

    /**
     * Enumeration of electronic coupling modes (ie AC, DC, or GROUND)
     */
    enum Coupling {
        AC,
        DC,
        GROUND
    }
}
