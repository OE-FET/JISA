package jisa.devices.interfaces;

import jisa.devices.DeviceException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Abstract class to define the standard functionality of temperature controllers
 */
public interface TC extends PID, MultiInstrument {

    static String getDescription() {
        return "Temperature Controller";
    }

    interface TMeter extends Input, jisa.devices.interfaces.TMeter {

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

        default double getPower() throws IOException, DeviceException {
            return getValue();
        }

    }

    interface Loop extends PID.Loop {

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

        default List<TMeter> getAvailableThermometers() {

            return getAvailableInputs().stream()
                                       .filter(i -> i instanceof TMeter)
                                       .map(i -> (TMeter) i)
                                       .collect(Collectors.toUnmodifiableList());

        }

        default List<Heater> getAvailableHeaters() {

            return getAvailableOutputs().stream()
                                       .filter(i -> i instanceof Heater)
                                       .map(i -> (Heater) i)
                                       .collect(Collectors.toUnmodifiableList());

        }

    }



    List<? extends Loop> getLoops();

    default Loop getLoop(int index) {
        return getLoops().get(index);
    }

    abstract class ZonedLoop extends PID.ZonedLoop implements Loop {

    }

    default List<? extends TMeter> getThermometers() {

        return getInputs().stream()
                          .filter(i -> i instanceof TMeter)
                          .map(i -> (TMeter) i)
                          .collect(Collectors.toList());

    }

    default List<? extends Heater> getHeaters() {

        return getInputs().stream()
                          .filter(i -> i instanceof Heater)
                          .map(i -> (Heater) i)
                          .collect(Collectors.toList());

    }

    default List<Class<? extends Instrument>> getSubInstrumentTypes() {
        return List.of(TMeter.class, Heater.class, Loop.class, Input.class, Output.class, PID.Loop.class);
    }

    default <I extends Instrument> List<I> getSubInstruments(Class<I> type) {

        if (jisa.devices.interfaces.TMeter.class.isAssignableFrom(type)) {
            return getThermometers().stream().map(i -> (I) i).collect(Collectors.toUnmodifiableList());
        } else if (Loop.class.isAssignableFrom(type)) {
            return getLoops().stream().map(i -> (I) i).collect(Collectors.toUnmodifiableList());
        } else {
            return Collections.emptyList();
        }

    }

    default <I extends Instrument> I getSubInstrument(Class<I> type, int index) {

        try {
            return getSubInstruments(type).get(index);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }

    }

}
