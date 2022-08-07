package jisa.devices.interfaces;

import jisa.devices.DeviceException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Abstract class to define the standard functionality of temperature controllers
 */
public interface TC extends PID, MultiInstrument, MultiChannel<TC.Loop>, MultiSensor<TMeter> {

    static String getDescription() {
        return "Temperature Controller";
    }

    interface Thermometer extends Input, TMeter {

        default double getValue() throws IOException, DeviceException {
            return getTemperature();
        }

        default void setRange(double range) throws IOException, DeviceException {
            setTemperatureRange(range);
        }

        @Override
        default String getSensorName() {
            return Input.super.getSensorName();
        }

        default double getRange() throws IOException, DeviceException {
            return getTemperatureRange();
        }

        default String getValueName() {
            return "Temperature";
        }

        default String getUnits() {
            return "K";
        }

    }

    interface Heater extends Output {

        default String getValueName() {
            return "Power";
        }

        default String getUnits() {
            return "%";
        }

    }

    interface Loop extends PID.Loop, Channel<Loop> {

        default Class<Loop> getChannelClass() {
            return Loop.class;
        }

        default String getChannelName() {

            try {
                return getName();
            } catch (Exception e) {
                return "Unknown Loop";
            }

        }

        void setSetPoint(double temperature) throws IOException, DeviceException;

        default void setTemperature(double temperature) throws IOException, DeviceException {
            setSetPoint(temperature);
        }

        double getSetPoint() throws IOException, DeviceException;

        default double getTemperature() throws IOException, DeviceException {
            return getInput().getValue();
        }

        default void waitForStableTemperature(double target, double pct, long msec) {
            waitForStableValue(target, pct, msec);
        }

        default List<Thermometer> getAvailableThermometers() {

            return getAvailableInputs().stream()
                                       .filter(i -> i instanceof Thermometer)
                                       .map(i -> (Thermometer) i)
                                       .collect(Collectors.toUnmodifiableList());

        }

        default List<Heater> getAvailableHeaters() {

            return getAvailableOutputs().stream()
                                       .filter(i -> i instanceof Heater)
                                       .map(i -> (Heater) i)
                                       .collect(Collectors.toUnmodifiableList());

        }

    }

    List<? extends Loop> getLoops() throws IOException, DeviceException;

    default Loop getLoop(int index) throws IOException, DeviceException {
        return getLoops().get(index);
    }

    abstract class ZonedLoop extends PID.ZonedLoop implements Loop {

    }

    default List<? extends Thermometer> getThermometers() throws IOException, DeviceException {

        return getInputs().stream()
                          .filter(i -> i instanceof Thermometer)
                          .map(i -> (Thermometer) i)
                          .collect(Collectors.toList());

    }

    default List<? extends Heater> getHeaters() throws IOException, DeviceException {

        return getInputs().stream()
                          .filter(i -> i instanceof Heater)
                          .map(i -> (Heater) i)
                          .collect(Collectors.toList());

    }

    default List<Class<? extends Instrument>> getMultiTypes() {

        return List.of(
            PID.Loop.class,
            TMeter.class
        );

    }

    default <I extends Instrument> List<I> getSubInstruments(Class<I> type) throws IOException, DeviceException {

        if (TMeter.class.isAssignableFrom(type)) {
            return getThermometers().stream().map(i -> (I) i).collect(Collectors.toUnmodifiableList());
        } else if (Loop.class.isAssignableFrom(type)) {
            return getLoops().stream().map(i -> (I) i).collect(Collectors.toUnmodifiableList());
        } else {
            return Collections.emptyList();
        }

    }

    default <I extends Instrument> I getSubInstrument(Class<I> type, int index) throws IOException, DeviceException {

        try {
            return getSubInstruments(type).get(index);
        } catch (IndexOutOfBoundsException e) {
            throw new DeviceException("No \"%s\" with index %d found.", type.getSimpleName(), index);
        }

    }

    @Override
    default int getNumChannels() {

        try {
            return getLoops().size();
        } catch (IOException | DeviceException e) {
            return 0;
        }

    }

    @Override
    default String getChannelName(int channelNumber) {

        try {
            return getLoop(channelNumber).getChannelName();
        } catch (IOException | DeviceException e) {
            return "Unknown Loop";
        }

    }

    @Override
    default List<Loop> getChannels() {

        try {
            return (List<Loop>) getLoops();
        } catch (IOException | DeviceException e) {
            return Collections.emptyList();
        }

    }

    @Override
    default Loop getChannel(int channelNumber) throws IOException, DeviceException {
        return getLoop(channelNumber);
    }

    @Override
    default Class<Loop> getChannelClass() {
        return Loop.class;
    }

    @Override
    default int getNumSensors() {

        try {
            return getThermometers().size();
        } catch (Exception e) {
            return 0;
        }

    }

    @Override
    default String getSensorName(int sensorNumber) {

        try {
            return getThermometers().get(0).getName();
        } catch (Exception e) {
            return "Name Unknown";
        }

    }

    @Override
    default List<TMeter> getSensors() {

        try {
            return getThermometers().stream().map(t -> (TMeter) t).collect(Collectors.toUnmodifiableList());
        } catch (Exception e) {
            return Collections.emptyList();
        }

    }

    @Override
    default TMeter getSensor(int sensorNumber) throws IOException, DeviceException {
        return getSensors().get(sensorNumber);
    }

    @Override
    default Class<TMeter> getSensorClass() {
        return TMeter.class;
    }

}
