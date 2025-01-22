package jisa.devices.features;

import jisa.control.Sync;
import jisa.devices.DeviceException;
import jisa.devices.ParameterList;

import java.io.IOException;

public interface TemperatureControlled extends Feature {
    
    static void addParameters(TemperatureControlled instrument, Class<?> target, ParameterList parameters) {

        try {

            parameters.addOptional(
                "Temperature Control [K]",
                instrument.isTemperatureControlEnabled(),
                instrument.getTemperatureControlTarget(),
                T -> instrument.setTemperatureControlEnabled(false),
                T -> {
                    instrument.setTemperatureControlEnabled(true);
                    instrument.setTemperatureControlTarget(T);
                }
            );

        } catch (Exception e) {

            parameters.addOptional(
                "Temperature Control [K]",
                false,
                298.0,
                T -> instrument.setTemperatureControlEnabled(false),
                T -> {
                    instrument.setTemperatureControlEnabled(true);
                    instrument.setTemperatureControlTarget(T);
                }
            );

        }
        
    }

    /**
     * Enables or disables temperature control for this instrument.
     *
     * @param enabled Enabled?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    void setTemperatureControlEnabled(boolean enabled) throws IOException, DeviceException;

    /**
     * Returns whether temperature is currently enabled for this instrument.
     *
     * @return Enabled?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    boolean isTemperatureControlEnabled() throws IOException, DeviceException;

    /**
     * Sets the set-point for the temperature controller on this instrument (if configurable).
     *
     * @param targetTemperature The target temperature to reach, in Kelvin.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    void setTemperatureControlTarget(double targetTemperature) throws IOException, DeviceException;

    /**
     * Returns the set-point for the temperature controller on this instrument.
     *
     * @return Temperature set-point, in Kelvin.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    double getTemperatureControlTarget() throws IOException, DeviceException;

    /**
     * Returns the current temperature of this instrument.
     *
     * @return Current temperature, in Kelvin.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    double getControlledTemperature() throws IOException, DeviceException;

    /**
     * Returns whether the temperature control on this instrument has stabilised or not.
     *
     * @return Is it stable?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    boolean isTemperatureControlStable() throws IOException, DeviceException;

    /**
     * Waits for the temperature of this instrument's temperature controller to stabilise before continuing.
     *
     * @throws IOException          Upon communications error
     * @throws DeviceException      Upon device compatibility error
     * @throws InterruptedException Upon waiting being interrupted by a thread interrupt
     */
    default void waitForTemperatureControlStable() throws IOException, DeviceException, InterruptedException {
        Sync.waitForCondition(i -> isTemperatureControlStable(), 1000);
    }

}
