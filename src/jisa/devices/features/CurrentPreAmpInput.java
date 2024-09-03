package jisa.devices.features;

import jisa.devices.DeviceException;
import jisa.devices.ParameterList;

import java.io.IOException;

public interface CurrentPreAmpInput extends Feature {

    static void addParameters(CurrentPreAmpInput instrument, Class<?> target, ParameterList parameters) {

        parameters.addValue("Use Current Input", instrument::isCurrentInputEnabled, false, instrument::setCurrentInputEnabled);
        parameters.addValue("Current Input Gain [V/A]", instrument::getCurrentGain, 1e-6, instrument::setCurrentGain);
        parameters.addValue("Current Range [A]", instrument::getCurrentRange, 1e-6, instrument::setCurrentRange);

    }

    /**
     * Sets whether this instrument should use its current pre-amp for input.
     *
     * @param enabled Use current pre-amp?
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon device compatibility error.
     */
    void setCurrentInputEnabled(boolean enabled) throws IOException, DeviceException;

    /**
     * Returns whether this instrument is using its current pre-amp for input.
     *
     * @return Using current pre-amp?
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon device compatibility error.
     */
    boolean isCurrentInputEnabled() throws IOException, DeviceException;

    /**
     * Sets the current-to-voltage gain of this instrument's input pre-amp.
     *
     * @param voltsPerAmp Gain, in volts per amp.
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon device compatibility error.
     */
    void setCurrentGain(double voltsPerAmp) throws IOException, DeviceException;

    /**
     * Returns the current-to-voltage gain of this instrument's input pre-amp.
     *
     * @return Gain, in volts per amp.
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon device compatibility error.
     */
    double getCurrentGain() throws IOException, DeviceException;

    /**
     * Sets the current measurement range of the instrument.
     *
     * @param currentRange Range, in Amps.
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon device compatibility error.
     */
    void setCurrentRange(double currentRange) throws IOException, DeviceException;

    /**
     * Returns the current measurement range currently being used by the instrument.
     *
     * @return Range, in Amps.
     *
     * @throws IOException     Upon communications error.
     * @throws DeviceException Upon device compatibility error.
     */
    double getCurrentRange() throws IOException, DeviceException;

}
