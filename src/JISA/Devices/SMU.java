package JISA.Devices;

import JISA.Addresses.InstrumentAddress;
import JISA.Control.Returnable;
import JISA.Experiment.IVPoint;
import JISA.Experiment.ResultList;
import JISA.Util;
import JISA.VISA.VISADevice;
import org.apache.commons.math.stat.descriptive.rank.Median;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public abstract class SMU extends VISADevice {

    public SMU(InstrumentAddress address) throws IOException {
        super(address);
    }

    /**
     * Returns the voltage either being applied or measured by the SMU.
     *
     * @return Voltage value
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract double getVoltage() throws DeviceException, IOException;

    /**
     * Returns the current either being injected or measured by the SMU.
     *
     * @return Current value
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract double getCurrent() throws DeviceException, IOException;

    /**
     * Sets the voltage value to be applied by the SMU (switching to voltage source mode if not already)
     *
     * @param voltage Value to set
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract void setVoltage(double voltage) throws DeviceException, IOException;

    /**
     * Sets the current value to be applied by the SMU (switching to current source mode if not already)
     *
     * @param current Value to set
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract void setCurrent(double current) throws DeviceException, IOException;

    /**
     * Turns the output of the SMU on
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract void turnOn() throws DeviceException, IOException;

    /**
     * Turns the output of the SMU off
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract void turnOff() throws DeviceException, IOException;

    /**
     * Checks whether the output of the SMU is currently enabled
     *
     * @return Is the output on?
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract boolean isOn() throws DeviceException, IOException;

    /**
     * Sets the source mode of the SMU (VOLTAGE or CURRENT)
     *
     * @param source Source mode to set
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract void setSource(Source source) throws DeviceException, IOException;

    /**
     * Returns the current source mode of the SMU (VOLTAGE OR CURRENT)
     *
     * @return Source mode
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract Source getSource() throws DeviceException, IOException;

    /**
     * Sets the value for whichever parameter is currently being sourced
     *
     * @param level The level to set
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract void setBias(double level) throws DeviceException, IOException;

    /**
     * Returns the value of whichever parameter is set as source currently
     *
     * @return Value of source
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract double getSourceValue() throws DeviceException, IOException;

    /**
     * Returns the value of whichever parameter is set as measure currently
     *
     * @return Value of measure
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract double getMeasureValue() throws DeviceException, IOException;

    /**
     * Sets whether the SMU should apply source using FORCE probes and measure using separate SENSE probes or whether is should
     * do both with the FORCE probes.
     *
     * @param fourProbes Should it use all four probes?
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract void useFourProbe(boolean fourProbes) throws DeviceException, IOException;

    /**
     * Returns whether the device is currently configured to use all four probes.
     *
     * @return Are all probes to be used?
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract boolean isUsingFourProbe() throws DeviceException, IOException;

    /**
     * Sets the averaging mode of the SMU.
     *
     * @param mode Mode to use
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract void setAverageMode(AMode mode) throws DeviceException, IOException;

    /**
     * Sets how many measurements the SMU should average over.
     *
     * @param count Number of measurements
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract void setAverageCount(int count) throws DeviceException, IOException;

    /**
     * Sets both the averaging mode and count together.
     *
     * @param mode  Averaging mode
     * @param count Averaging count
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public void setAveraging(AMode mode, int count) throws DeviceException, IOException {
        setAverageMode(mode);
        setAverageCount(count);
    }

    /**
     * Returns the averaging mode of the SMU.
     *
     * @return Mode being used
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract AMode getAverageMode() throws DeviceException, IOException;

    /**
     * Returns the number of measurements used for averaging by the SMU.
     *
     * @return Number of measurements
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract int getAverageCount() throws DeviceException, IOException;

    /**
     * Sets the range of allowed values for the quantity being sourced by the SMU.
     * A value of n indicates a range of -n to +n.
     *
     * @param value Range value, in Volts or Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract void setSourceRange(double value) throws DeviceException, IOException;

    /**
     * Returns the range of allowed values for the quantity being sourced by the SMU.
     * A value of n indicates a range of -n to +n.
     *
     * @return Range value, in Volts or Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract double getSourceRange() throws DeviceException, IOException;

    /**
     * Sets the SMU to automatically determine the source range to use.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract void useAutoSourceRange() throws DeviceException, IOException;

    /**
     * Returns whether the SMU is set to automatically determine the source range to use.
     *
     * @return Automatic?
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract boolean isSourceRangeAuto() throws DeviceException, IOException;

    /**
     * Sets the range of allowed values for the quantity being measured by the SMU.
     *
     * @param value Range value, in Volts or Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract void setMeasureRange(double value) throws DeviceException, IOException;


    /**
     * Returns the range of allowed values for the quantity being measured by the SMU.
     *
     * @return Range value, in Volts or Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract double getMeasureRange() throws DeviceException, IOException;

    /**
     * Tells the SMU to automatically determine the range to use for the measured quantity.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract void useAutoMeasureRange() throws DeviceException, IOException;

    /**
     * Returns whether the SMU is currently determining its measure range automatically.
     *
     * @return Is it automatic?
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract boolean isMeasureRangeAuto() throws DeviceException, IOException;

    /**
     * Sets the range of allowed values for voltages being sourced or measured by the SMU.
     * A value of n indicates a range of -n to +n.
     *
     * @param value Range value, in Volts
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract void setVoltageRange(double value) throws DeviceException, IOException;

    /**
     * Returns the range of allowed values for voltages being sourced or measured by the SMU.
     * A value of n indicates a range of -n to +n.
     *
     * @return Range value, in Volts
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract double getVoltageRange() throws DeviceException, IOException;

    /**
     * Tells the SMU to automatically determine the range it uses for voltages.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract void useAutoVoltageRange() throws DeviceException, IOException;

    /**
     * Returns whether the SMU is automatically determining the range to use for voltages.
     *
     * @return Is it automatic?
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract boolean isVoltageRangeAuto() throws DeviceException, IOException;

    /**
     * Sets the range of allowed values for currents being sourced or measured by the SMU.
     *
     * @param value Range value, in Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract void setCurrentRange(double value) throws DeviceException, IOException;


    /**
     * Returns the range of allowed values for currents being sourced or measured by the SMU.
     *
     * @return Range value, inAmps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract double getCurrentRange() throws DeviceException, IOException;

    /**
     * Tells the SMU to automatically determine the range to use for current values.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract void useAutoCurrentRange() throws DeviceException, IOException;

    /**
     * Returns whether the SMU is currently determining its current range automatically.
     *
     * @return Is it automatic?
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract boolean isCurrentRangeAuto() throws DeviceException, IOException;

    /**
     * Sets the limit for the measured quantity (ie compliance value).
     *
     * @param value The limit to use, in Volts or Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract void setOutputLimit(double value) throws DeviceException, IOException;

    /**
     * Returns the limit for the measured quantity (compliance value).
     *
     * @return The limit, in Volts or Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract double getOutputLimit() throws DeviceException, IOException;

    /**
     * Sets the limit for voltages output when sourcing current (compliance value).
     *
     * @param voltage The limit, in Volts
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract void setVoltageLimit(double voltage) throws DeviceException, IOException;

    /**
     * Returns the limit on voltages output when sourcing current (compliance value).
     *
     * @return The limit, in Volts
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract double getVoltageLimit() throws DeviceException, IOException;

    /**
     * Sets the limit for currents output when sourcing voltage (compliance value).
     *
     * @param current The limit, in Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract void setCurrentLimit(double current) throws DeviceException, IOException;

    /**
     * Returns the limit for currents output when sourcing voltage (compliance value).
     *
     * @return The limit, in Amps
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract double getCurrentLimit() throws DeviceException, IOException;

    /**
     * Sets the integration time for each individual measurement, or closest over-estimate possible.
     *
     * @param time Integration time, in seconds
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract void setIntegrationTime(double time) throws DeviceException, IOException;

    /**
     * Returns the integration time used for each individual measurement.
     *
     * @return Integration time, in seconds
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract double getIntegrationTime() throws DeviceException, IOException;


    /**
     * Returns what type of connector is used for the given terminal.
     *
     * @param terminals Which terminal
     *
     * @return Terminal type (TRIAX, PHOENIX, BNC or BANANA)
     *
     * @throws DeviceException
     * @throws IOException
     */
    public abstract TType getTerminalType(Terminals terminals) throws DeviceException, IOException;

    /**
     * Sets which set of terminals should be used on the SMU.
     *
     * @param terminals Which type of terminals to use
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract void setTerminals(Terminals terminals) throws DeviceException, IOException;

    /**
     * Returns the type of the set of terminals currently being used on the SMU.
     *
     * @return The type of terminals being used
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract Terminals getTerminals() throws DeviceException, IOException;

    public abstract void setOffMode(OffMode mode) throws DeviceException, IOException;

    public abstract OffMode getOffMode() throws DeviceException, IOException;

    public abstract void setOffVoltageLimit(double limit) throws DeviceException, IOException;

    public abstract void setOffCurrentLimit(double limit) throws DeviceException, IOException;

    public void setOffLimits(double voltage, double current) throws DeviceException, IOException {
        setOffVoltageLimit(voltage);
        setOffCurrentLimit(current);
    }

    public abstract double getOffVoltageLimit() throws DeviceException, IOException;

    public abstract double getOffCurrentLimit() throws DeviceException, IOException;

    /**
     * Returns a combined voltage and current measurement.
     *
     * @return Voltage and Current
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public IVPoint getIVPoint() throws DeviceException, IOException {
        return new IVPoint(getVoltage(), getCurrent());
    }

    /**
     * Sets the value ranges for both voltage and current.
     *
     * @param voltageRange Range to use for voltage
     * @param currentRange Range to use for current
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public void setRanges(double voltageRange, double currentRange) throws DeviceException, IOException {
        setVoltageRange(voltageRange);
        setCurrentRange(currentRange);
    }

    /**
     * Tells the SMU to use auto-ranging for both current and voltage.
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public void useAutoRanges() throws DeviceException, IOException {
        useAutoVoltageRange();
        useAutoCurrentRange();
    }

    /**
     * Sets the limits (compliance values) for both voltage and current.
     *
     * @param voltageLimit Limit for voltage when not being sourced
     * @param currentLimit Limit for current when not being sourced
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public void setLimits(double voltageLimit, double currentLimit) throws DeviceException, IOException {
        setVoltageLimit(voltageLimit);
        setCurrentLimit(currentLimit);
    }

    /**
     * Performs a linear sweep of either VOLTAGE or CURRENT, returning the V-I data points as an array of IVPoint objects
     *
     * @param source    VOLTAGE or CURRENT
     * @param min       Minimum source value
     * @param max       Maximum source value
     * @param numSteps  Number of steps in sweep
     * @param delay     Amount of time, in milliseconds, to wait before taking each measurement
     * @param symmetric Should we sweep back to starting point after sweeping forwards?
     *
     * @return Array of IVPoint objects containing I-V data points
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public IVPoint[] doLinearSweep(Source source, double min, double max, int numSteps, long delay, boolean symmetric) throws DeviceException, IOException {
        return doLinearSweep(source, min, max, numSteps, delay, symmetric, (i, point) -> {
        });
    }

    /**
     * Performs a linear sweep of either VOLTAGE or CURRENT, returning the V-I data points as an array of IVPoint objects
     * whilst allowing you to keep track of the sweep's progress via a ProgressMonitor object.
     *
     * @param source    VOLTAGE or CURRENT
     * @param min       Minimum source value
     * @param max       Maximum source value
     * @param numSteps  Number of steps in sweep
     * @param delay     Amount of time, in milliseconds, to wait before taking each measurement
     * @param symmetric Should we sweep back to starting point after sweeping forwards?
     * @param onUpdate  Method to run each time a new measurement is completed
     *
     * @return Array of IVPoint objects containing I-V data points
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public IVPoint[] doLinearSweep(Source source, double min, double max, int numSteps, long delay, boolean symmetric, ProgressMonitor onUpdate) throws DeviceException, IOException {

        return doSweep(
                source,
                Util.makeLinearArray(min, max, numSteps),
                delay,
                symmetric,
                onUpdate
        );

    }

    /**
     * Performs a linear sweep of either VOLTAGE or CURRENT, returning the V-I data points as an array of IVPoint objects
     * and adding each point of data to the given ResultList object as it is acquired.
     *
     * @param source    VOLTAGE or CURRENT
     * @param min       Minimum source value
     * @param max       Maximum source value
     * @param numSteps  Number of steps in sweep
     * @param delay     Amount of time, in milliseconds, to wait before taking each measurement
     * @param symmetric Should we sweep back to starting point after sweeping forwards?
     * @param list      ResultList to update with data points column 0: Voltage, column 1: Current
     *
     * @return Array of IVPoint objects containing I-V data points
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public IVPoint[] doLinearSweep(Source source, double min, double max, int numSteps, long delay, boolean symmetric, ResultList list) throws DeviceException, IOException {
        return doLinearSweep(source, min, max, numSteps, delay, symmetric, (i, p) -> list.addData(p.voltage, p.current));
    }

    /**
     * Performs a logarithmic sweep of either VOLTAGE or CURRENT, returning V-I data points as an array of IVPoint objects.
     *
     * @param source    VOLTAGE or CURRENT
     * @param min       Minimum source value
     * @param max       Maximum source value
     * @param numSteps  Number of steps in sweep
     * @param delay     Amount of time, in milliseconds, to wait before taking each measurement
     * @param symmetric Should we sweep back to starting point after sweeping forwards?
     *
     * @return Array of IVPoint objects containing V-I data points
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public IVPoint[] doLogarithmicSweep(Source source, double min, double max, int numSteps, long delay, boolean symmetric) throws DeviceException, IOException {

        return doLogarithmicSweep(source, min, max, numSteps, delay, symmetric, (i, p) -> {
        });

    }

    /**
     * Performs a logarithmic sweep of either VOLTAGE or CURRENT, returning V-I data points as an array of IVPoint objects
     * whilst allowing you to keep track of the sweep's progress via a ProgressMonitor object.
     *
     * @param source    VOLTAGE or CURRENT
     * @param min       Minimum source value
     * @param max       Maximum source value
     * @param numSteps  Number of steps in sweep
     * @param delay     Amount of time, in milliseconds, to wait before taking each measurement
     * @param symmetric Should we sweep back to starting point after sweeping forwards?
     * @param onUpdate  Method ot run each time a new measurement is completed
     *
     * @return Array of IVPoint objects containing V-I data points
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public IVPoint[] doLogarithmicSweep(Source source, double min, double max, int numSteps, long delay, boolean symmetric, ProgressMonitor onUpdate) throws DeviceException, IOException {

        return doSweep(
                source,
                Util.makeLogarithmicArray(min, max, numSteps),
                delay,
                symmetric,
                onUpdate
        );

    }

    /**
     * Performs a linear sweep of either VOLTAGE or CURRENT, returning the V-I data points as an array of IVPoint objects
     * and adding each point of data to the given ResultList object as it is acquired.
     *
     * @param source    VOLTAGE or CURRENT
     * @param min       Minimum source value
     * @param max       Maximum source value
     * @param numSteps  Number of steps in sweep
     * @param delay     Amount of time, in milliseconds, to wait before taking each measurement
     * @param symmetric Should we sweep back to starting point after sweeping forwards?
     * @param list      ResultList to update with data points column 0: Voltage, column 1: Current
     *
     * @return Array of IVPoint objects containing I-V data points
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public IVPoint[] doLogarithmicSweep(Source source, double min, double max, int numSteps, long delay, boolean symmetric, ResultList list) throws DeviceException, IOException {
        return doLogarithmicSweep(source, min, max, numSteps, delay, symmetric, (i, p) -> list.addData(p.voltage, p.current));
    }

    private static class Updater implements Runnable {

        private int                i    = 0;
        private Semaphore          semaphore;
        private ArrayList<IVPoint> points;
        private ProgressMonitor    onUpdate;
        private boolean            exit = false;

        public Updater(ArrayList<IVPoint> p, ProgressMonitor o) {
            semaphore = new Semaphore(0);
            points = p;
            onUpdate = o;
        }

        public void runUpdate() {
            semaphore.release();
        }

        public void end() {
            exit = true;
            semaphore.release();
        }

        @Override
        public void run() {

            while (true) {
                try {
                    semaphore.acquire();
                } catch (InterruptedException ignored) {
                }

                if (exit) {
                    break;
                }

                try {
                    onUpdate.update(i, points.get(i));
                    i++;
                } catch (Exception e) {
                    Util.exceptionHandler(e);
                }

            }

        }
    }

    /**
     * Performs a sweep of either VOLTAGE or CURRENT using specific values, returning data as an array of IVPoint objects.
     *
     * @param source    VOLTAGE or CURRENT
     * @param values    Array of values to use in the sweep
     * @param delay     Amount of time, in milliseconds, to wait before taking each measurement
     * @param symmetric Should we sweep back to starting point after sweeping forwards?
     * @param onUpdate  Method to run each time a new measurement is completed (on separate thread)
     *
     * @return Array of IVPoint objects containing V-I data points
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public IVPoint[] doSweep(Source source, double[] values, long delay, boolean symmetric, ProgressMonitor onUpdate) throws DeviceException, IOException {

        final ArrayList<IVPoint> points  = new ArrayList<>();
        IVPoint                  point;
        Updater                  updater = new Updater(points, onUpdate);


        // Run updater on a new thread
        (new Thread(updater)).start();

        if (symmetric) {
            values = Util.symArray(values);
        }

        turnOff();
        setSource(source);
        setBias(values[0]);

        turnOn();
        for (double b : values) {

            setBias(b);
            try {
                Thread.sleep(delay);
            } catch (Exception e) {
                throw new DeviceException("Couldn't sleep!");
            }

            point = new IVPoint(getVoltage(), getCurrent());
            points.add(point);

            updater.runUpdate();

        }

        // Unblock updater thread so that it will end
        updater.end();

        return points.toArray(new IVPoint[0]);

    }

    public IVPoint[] doPulsedSweep(Source source, double baseLevel, double[] values, long delay, long timeOn, long timeOff, boolean symmetric, ProgressMonitor onUpdate) throws DeviceException, IOException {

        // The delay time must be smaller than the on and off time.
        if (delay > timeOn || delay > timeOff) {
            throw new DeviceException("Delay time cannot be larger than either time on or time off for a pulsed sweep.");
        }

        final ArrayList<IVPoint> points  = new ArrayList<>();
        IVPoint                  point1;
        IVPoint                  point2;
        long                     lastTime;
        Updater                  updater = new Updater(points, onUpdate);

        // If they want to go forwards then backwards, generate the extra data-points
        if (symmetric) {
            values = Util.symArray(values);
        }

        // Make sure the SMU is not outputting anything right now, set the source mode and initial bias
        turnOff();
        setSource(source);
        setBias(baseLevel);


        // Begin!
        turnOn();
        for (double b : values) {

            // Set output to the value of the current step and record the time
            setBias(b);
            lastTime = System.nanoTime();

            // Wait the specified time before measuring
            Util.sleep(delay);
            point1 = new IVPoint(getVoltage(), getCurrent());

            // Store measurement, trigger the onUpdate method
            points.add(point1);
            updater.runUpdate();

            // Work out how much time has passed and how much longer we need to wait before switching off
            lastTime = (System.nanoTime() - lastTime) / 1000;
            Util.sleep(Math.max(0, timeOn - lastTime));

            // "Switch off" by returning to base level, recording the current time
            setBias(baseLevel);
            lastTime = System.nanoTime();

            // Wait the specified time before measuring
            Util.sleep(delay);
            point2 = new IVPoint(getVoltage(), getCurrent());

            // Store measurement, trigger the onUpdate method
            points.add(point2);
            updater.runUpdate();

            // Work out how much time has passed and how much longer we need to wait before switching on again
            lastTime = System.nanoTime() - lastTime;
            Util.sleep(Math.max(0, timeOn - lastTime));

        }

        updater.end();

        return points.toArray(new IVPoint[0]);

    }

    /**
     * Performs a sweep of either VOLTAGE or CURRENT using specific values, returning data as an array of IVPoint objects.
     *
     * @param source    VOLTAGE or CURRENT
     * @param values    Array of values to use in the sweep
     * @param delay     Amount of time, in milliseconds, to wait before taking each measurement
     * @param symmetric Should we sweep back to starting point after sweeping forwards?
     *
     * @return Array of IVPoint objects containing V-I data points
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public IVPoint[] doSweep(Source source, double[] values, boolean symmetric, long delay) throws IOException, DeviceException {
        return doSweep(source, values, delay, symmetric, (i, p) -> {
        });
    }

    /**
     * Performs a sweep of either VOLTAGE or CURRENT using specific values, returning data as an array of IVPoint objects.
     *
     * @param source    VOLTAGE or CURRENT
     * @param values    Array of values to use in the sweep
     * @param delay     Amount of time, in milliseconds, to wait before taking each measurement
     * @param symmetric Should we sweep back to starting point after sweeping forwards?
     * @param list      ResultList object to update with measurements, column 0: Voltage, column 1: Current.
     *
     * @return Array of IVPoint objects containing V-I data points
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public IVPoint[] doSweep(Source source, double[] values, long delay, boolean symmetric, ResultList list) throws DeviceException, IOException {
        return doSweep(source, values, delay, symmetric, (i, p) -> list.addData(p.voltage, p.current));
    }

    public ResultList createSweepList() {
        ResultList list = new ResultList("Voltage", "Current");
        list.setUnits("V", "A");
        return list;
    }

    /**
     * Enumeration of source modes
     */
    public enum Source {
        VOLTAGE,
        CURRENT
    }

    public enum Terminals {
        FRONT,
        REAR
    }

    public enum TType {
        TRIAX,
        PHOENIX,
        BNC,
        BANANA,
        NONE
    }

    /**
     * Enumeration of averaging modes
     */
    public enum AMode {

        /**
         * No Averaging.
         */
        NONE,

        /**
         * Mean average, taking n data points each time.
         */
        MEAN_REPEAT,

        /**
         * Mean average, only taking one new data point each time and using the previous n-1 points.
         */
        MEAN_MOVING,

        /**
         * Median average, taking n data points each time.
         */
        MEDIAN_REPEAT,

        /**
         * Median average, only taking one new data point each time and using the previous n-1 points.
         */
        MEDIAN_MOVING

    }

    public enum OffMode {
        ZERO_V,
        ZERO_I,
        ZERO_AUTO,
        HIGH_IMPEDANCE
    }


    /**
     * Structure for defining what to do on each update
     */
    public interface ProgressMonitor {

        void update(int i, IVPoint point) throws IOException, DeviceException;

    }

    public interface Setupable {
        void run(int count) throws IOException, DeviceException;
    }

    protected interface ReadFilter {
        double getValue() throws IOException, DeviceException;

        void setCount(int count);

        int getCount();

        void clear();

        void setUp() throws IOException, DeviceException;

    }

    protected class MeanRepeatFilter implements ReadFilter {

        protected int                count = 1;
        protected Returnable<Double> value;
        protected Setupable          setUp;

        public MeanRepeatFilter(Returnable<Double> v, Setupable s) {
            value = v;
            setUp = s;
        }

        @Override
        public double getValue() throws IOException, DeviceException {

            double total = 0;

            for (int i = 0; i < count; i++) {
                total += value.get();
            }

            return total / ((double) count);

        }

        @Override
        public void setCount(int count) {
            this.count = count;
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public void clear() {

        }

        @Override
        public void setUp() throws IOException, DeviceException {
            setUp.run(getCount());
        }

    }

    protected class MeanMovingFilter implements ReadFilter {

        protected int                count = 1;
        protected Returnable<Double> value;
        protected ArrayList<Double>  queue = new ArrayList<>();
        protected Setupable          setUp;

        public MeanMovingFilter(Returnable<Double> v, Setupable s) {
            value = v;
            setUp = s;
        }

        @Override
        public double getValue() throws IOException, DeviceException {

            queue.subList(0, Math.max(0, queue.size() - (count - 1))).clear();

            int toFill = (count - queue.size());
            for (int i = 0; i < toFill; i++) {
                queue.add(value.get());
            }

            double total = 0;
            for (double v : queue) {
                total += v;
            }

            return total / ((double) queue.size());

        }

        @Override
        public void setCount(int count) {
            this.count = count;
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public void clear() {
            queue.clear();
        }

        @Override
        public void setUp() throws IOException, DeviceException {
            setUp.run(getCount());
        }
    }

    protected class MedianRepeatFilter implements ReadFilter {

        protected int                count  = 1;
        protected Returnable<Double> value;
        protected Median             median = new Median();
        protected Setupable          setUp;

        public MedianRepeatFilter(Returnable<Double> v, Setupable s) {
            value = v;
            setUp = s;
        }

        @Override
        public double getValue() throws IOException, DeviceException {

            double[] values = new double[count];

            for (int i = 0; i < count; i++) {
                values[i] = value.get();
            }

            return median.evaluate(values);

        }

        @Override
        public void setCount(int count) {
            this.count = count;
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public void clear() {

        }

        @Override
        public void setUp() throws IOException, DeviceException {
            setUp.run(getCount());
        }
    }

    protected class MedianMovingFilter implements ReadFilter {

        protected int                count  = 1;
        protected Returnable<Double> value;
        protected ArrayList<Double>  queue  = new ArrayList<>();
        protected Median             median = new Median();
        protected Setupable          setUp;

        public MedianMovingFilter(Returnable<Double> v, Setupable s) {
            value = v;
            setUp = s;
        }

        @Override
        public double getValue() throws IOException, DeviceException {

            queue.subList(0, Math.max(0, queue.size() - (count - 1))).clear();

            int toFill = (count - queue.size());
            for (int i = 0; i < toFill; i++) {
                queue.add(value.get());
            }

            double[] values = new double[count];

            for (int i = 0; i < count; i++) {
                values[i] = queue.get(i);
            }

            return median.evaluate(values);

        }

        @Override
        public void setCount(int count) {
            this.count = count;
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public void clear() {
            queue.clear();
        }

        @Override
        public void setUp() throws IOException, DeviceException {
            setUp.run(getCount());
        }
    }

    protected class BypassFilter implements ReadFilter {

        protected Returnable<Double> value;
        protected int                count = 1;
        protected Setupable          setUp;

        public BypassFilter(Returnable<Double> v, Setupable s) {
            value = v;
            setUp = s;
        }

        @Override
        public double getValue() throws IOException, DeviceException {
            return value.get();
        }

        @Override
        public void setCount(int count) {
            this.count = count;
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public void clear() {

        }

        @Override
        public void setUp() throws IOException, DeviceException {
            setUp.run(getCount());
        }
    }

}
