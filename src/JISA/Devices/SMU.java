package JISA.Devices;

import JISA.Addresses.InstrumentAddress;
import JISA.Experiment.IVPoint;
import JISA.Experiment.ResultList;
import JISA.Util;
import JISA.VISA.VISADevice;

import java.io.IOException;
import java.util.ArrayList;
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
     * Returns the number of terminals that can be used on the SMU.
     *
     * @return Number of terminals
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public int getNumTerminals() throws DeviceException, IOException {
        return 1;
    }

    /**
     * Sets which set of terminals should be used on the SMU.
     *
     * @param terminalNumber Index of terminal set, starting at 0
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public void setTerminals(int terminalNumber) throws DeviceException, IOException {

    }

    /**
     * Returns the index of the set of terminals currently being used on the SMU.
     *
     * @return Index of terminal set
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public int getTerminals() throws DeviceException, IOException {
        return 0;
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
        return doLinearSweep(source, min, max, numSteps, delay, symmetric, (i, p) -> {
            list.addData(p.voltage, p.current);
        });
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
        return doLogarithmicSweep(source, min, max, numSteps, delay, symmetric, (i, p) -> {
            list.addData(p.voltage, p.current);
        });
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
        IVPoint                  point   = null;
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

        final ArrayList<IVPoint> points    = new ArrayList<>();
        IVPoint                  point1    = null;
        IVPoint                  point2    = null;
        long                     lastTime  = 0;
        Updater                  updater   = new Updater(points, onUpdate);

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
        return doSweep(source, values, delay, symmetric, (i, p) -> {
            list.addData(p.voltage, p.current);
        });
    }

    /**
     * Enumeration of source modes
     */
    public enum Source {
        VOLTAGE,
        CURRENT
    }


    /**
     * Structure for defining what to do on each update
     */
    public interface ProgressMonitor {

        void update(int i, IVPoint point) throws IOException, DeviceException;

    }

}
