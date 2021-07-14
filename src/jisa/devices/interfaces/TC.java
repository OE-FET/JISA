package jisa.devices.interfaces;

import jisa.control.Synch;
import jisa.devices.DeviceException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Abstract class to define the standard functionality of temperature controllers
 */
public interface TC extends PID, TMeter {

    public static String getDescription() {
        return "Temperature Controller";
    }


    @Override
    default String getInputName() throws IOException, DeviceException {
        return "Temperature";
    }

    @Override
    default String getInputUnits() throws IOException, DeviceException {
        return "K";
    }

    @Override
    default double getSetPoint() throws IOException, DeviceException {
        return getTargetTemperature();
    }

    @Override
    default void setSetPoint(double value) throws IOException, DeviceException {
        setTargetTemperature(value);
    }

    @Override
    default double getInputValue() throws IOException, DeviceException {
        return getTemperature();
    }

    @Override
    default double getOutputValue() throws IOException, DeviceException {
        return getHeaterPower();
    }

    @Override
    default void setOutputValue(double value) throws IOException, DeviceException {
        setHeaterPower(value);
    }

    @Override
    default double getOutputRange() throws IOException, DeviceException {
        return getHeaterRange();
    }

    @Override
    default void setOutputRange(double range) throws IOException, DeviceException {
        setHeaterRange(range);
    }

    @Override
    default void useAutoOutput() throws IOException, DeviceException {
        useAutoHeater();
    }

    @Override
    default boolean isUsingAutoOutput() throws IOException, DeviceException {
        return isUsingAutoHeater();
    }

    /**
     * Returns the temperature measured by the controller.
     *
     * @return Temperature, in Kelvin
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    double getTemperature() throws IOException, DeviceException;

    default void setTemperature(double temperature) throws IOException, DeviceException {
        setTargetTemperature(temperature);
    }

    /**
     * Returns the target temperature set on the controller.
     *
     * @return Target temperature, in Kelvin
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    double getTargetTemperature() throws IOException, DeviceException;

    /**
     * Sets the target temperature of the temperature controller.
     *
     * @param temperature Target temperature, in Kelvin
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void setTargetTemperature(double temperature) throws IOException, DeviceException;

    /**
     * Returns the maximum rate at which the temperature controller is set to allow temperature to change.
     *
     * @return Maximum rate, in Kelvin per minute
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    double getTemperatureRampRate() throws IOException, DeviceException;

    /**
     * Sets the maximum rate at which the temperature controller allows temperature to change.
     *
     * @param kPerMin Maximum rate, in Kelvin per minute
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void setTemperatureRampRate(double kPerMin) throws IOException, DeviceException;

    /**
     * Returns the heater output power percentage.
     *
     * @return Heater power, percentage of max
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    double getHeaterPower() throws IOException, DeviceException;

    /**
     * Sets the heater to be operated manually with the specified power output percentage
     *
     * @param powerPCT Output power (percentage of max)
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void setHeaterPower(double powerPCT) throws IOException, DeviceException;

    /**
     * Returns the gas flow in whatever units the controller uses
     *
     * @return Gas flow (arbitrary units)
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    double getFlow() throws IOException, DeviceException;

    /**
     * Sets the gas flow to be controlled manually with the specified output
     *
     * @param outputPCT Output
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void setFlow(double outputPCT) throws IOException, DeviceException;

    /**
     * Sets the heater to be operated automatically
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void useAutoHeater() throws IOException, DeviceException;

    /**
     * Returns whether the heater is currently operating automatically or manually
     *
     * @return Automatic?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    boolean isUsingAutoHeater() throws IOException, DeviceException;

    /**
     * Sets the gas flow to be controlled automatically
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void useAutoFlow() throws IOException, DeviceException;

    /**
     * Returns whether the gas flow is currently controlled automatically or manually
     *
     * @return Automatic?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    boolean isUsingAutoFlow() throws IOException, DeviceException;

    double getHeaterRange() throws IOException, DeviceException;

    void setHeaterRange(double rangePCT) throws IOException, DeviceException;

    default TMeter asThermometer() {
        return this;
    }

    /**
     * Halts the current thread until the temperature has stabilised to the specified value within the given percentage
     * margin for at least the given amount of time.
     *
     * @param temperature Target temperature, in Kelvin
     * @param pctMargin   Percentage margin
     * @param time        Amount of time to be considered stable, in milliseconds
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    default void waitForStableTemperature(double temperature, double pctMargin, long time) throws IOException, DeviceException, InterruptedException {

        Synch.waitForStableTarget(
            this::getTemperature,
            temperature,
            pctMargin,
            1000,
            time
        );

    }

    default List<Parameter<?>> getConfigurationParameters(Class<?> target) {

        LinkedList<Parameter<?>> parameters = new LinkedList<>();

        parameters.add(new Parameter<>(
            "PID Settings",
            new TableQuantity(new String[]{"Min T [K]", "Max T [K]", "P", "I", "D", "Heater Range [%]"}, List.of(
                List.of(0.0, 1000.0, 70.0, 30.0, 0.0, 100.0)
            )),
            q -> {

                List<List<Double>> values = q.getValue();

                if (values.size() == 0) {
                    values.add(List.of(0.0, 1000.0, 70.0, 30.0, 0.0, 100.0));
                }

                if (values.size() < 2) {

                    useAutoPID(false);
                    setPIDValues(values.get(0).get(2), values.get(0).get(3), values.get(0).get(4));
                    setHeaterRange(values.get(0).get(5));

                } else {

                    PID.Zone[] zones = values
                        .stream().map(r -> new PID.Zone(r.get(0), r.get(1), r.get(2), r.get(3), r.get(4), r.get(5)))
                        .toArray(PID.Zone[]::new);

                    setAutoPIDZones(zones);
                    useAutoPID(true);

                }

            }

        ));

        parameters.add(new Parameter<>("Ramp Rate [K/min]", 0.0, this::setTemperatureRampRate));

        parameters.addAll(TMeter.super.getConfigurationParameters(target));

        return parameters;

    }

}
