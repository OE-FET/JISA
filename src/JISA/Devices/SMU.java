package JISA.Devices;

import JISA.Addresses.InstrumentAddress;
import JISA.Util;
import JISA.VISA.VISADevice;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.util.ArrayList;

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
     * Performs a linear sweep of either VOLTAGE or CURRENT, returning the V-I data points as an array of DataPoint objects
     *
     * @param source   VOLTAGE or CURRENT
     * @param min      Minimum source value
     * @param max      Maximum source value
     * @param numSteps Number of steps in sweep
     * @param delay    Amount of time, in milliseconds, to wait before taking each measurement
     *
     * @return Array of DataPoint objects containing I-V data points
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public DataPoint[] performLinearSweep(Source source, double min, double max, int numSteps, long delay) throws DeviceException, IOException {
        return performLinearSweep(source, min, max, numSteps, delay, (i, point) -> {
        });
    }

    /**
     * Performs a linear sweep of either VOLTAGE or CURRENT, returning the V-I data points as an array of DataPoint objects
     * whilst allowing you to keep track of the sweep's progress via a ProgressMonitor object.
     *
     * @param source   VOLTAGE or CURRENT
     * @param min      Minimum source value
     * @param max      Maximum source value
     * @param numSteps Number of steps in sweep
     * @param delay    Amount of time, in milliseconds, to wait before taking each measurement
     * @param onUpdate Method to run each time a new measurement is completed
     *
     * @return Array of DataPoint objects containing I-V data points
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public DataPoint[] performLinearSweep(Source source, double min, double max, int numSteps, long delay, ProgressMonitor onUpdate) throws DeviceException, IOException {

        return performSweep(
                source,
                Util.makeLinearArray(min, max, numSteps),
                delay,
                onUpdate
        );

    }

    /**
     * Performs a logarithmic sweep of either VOLTAGE or CURRENT, returning V-I data points as an array of DataPoint objects.
     *
     * @param source   VOLTAGE or CURRENT
     * @param min      Minimum source value
     * @param max      Maximum source value
     * @param numSteps Number of steps in sweep
     * @param delay    Amount of time, in milliseconds, to wait before taking each measurement
     *
     * @return Array of DataPoint objects containing V-I data points
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public DataPoint[] performLogarithmicSweep(Source source, double min, double max, int numSteps, long delay) throws DeviceException, IOException {

        return performSweep(
                source,
                Util.makeLogarithmicArray(min, max, numSteps),
                delay,
                (i, p) -> {
                }
        );

    }

    /**
     * Performs a logarithmic sweep of either VOLTAGE or CURRENT, returning V-I data points as an array of DataPoint objects
     * whilst allowing you to keep track of the sweep's progress via a ProgressMonitor object.
     *
     * @param source   VOLTAGE or CURRENT
     * @param min      Minimum source value
     * @param max      Maximum source value
     * @param numSteps Number of steps in sweep
     * @param delay    Amount of time, in milliseconds, to wait before taking each measurement
     * @param onUpdate Method ot run each time a new measurement is completed
     *
     * @return Array of DataPoint objects containing V-I data points
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public DataPoint[] performLogarithmicSweep(Source source, double min, double max, int numSteps, long delay, ProgressMonitor onUpdate) throws DeviceException, IOException {

        return performSweep(
                source,
                Util.makeLogarithmicArray(min, max, numSteps),
                delay,
                onUpdate
        );

    }

    /**
     * Performs a logarithmic sweep of either VOLTAGE or CURRENT, returning V-I data points as an array of DataPoint objects
     * whilst allowing you to keep track of the sweep's progress via a ProgressMonitor object.
     *
     * @param source   VOLTAGE or CURRENT
     * @param values   Array of values to use in the sweep
     * @param delay    Amount of time, in milliseconds, to wait before taking each measurement
     * @param onUpdate Method ot run each time a new measurement is completed
     *
     * @return Array of DataPoint objects containing V-I data points
     *
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public DataPoint[] performSweep(Source source, double[] values, long delay, ProgressMonitor onUpdate) throws DeviceException, IOException {

        turnOff();
        setSource(source);
        setBias(values[0]);

        int                  i      = 0;
        ArrayList<DataPoint> points = new ArrayList<>();

        turnOn();

        for (double b : values) {

            setBias(b);
            try {
                Thread.sleep(delay);
            } catch (Exception e) {
                throw new DeviceException("Couldn't sleep!");
            }

            DataPoint point = new DataPoint(getVoltage(), getCurrent());
            onUpdate.update(i, point);
            points.add(point);
            i++;

        }

        return points.toArray(new DataPoint[0]);

    }

    public DataPoint[] performSweep(Source source, double[] values, long delay) throws IOException, DeviceException {
        return performSweep(source, values, delay, (i,p) -> {});
    }

    /**
     * Enumeration of source modes
     */
    public enum Source {
        VOLTAGE,
        CURRENT
    }

    /**
     * Class to contain voltage-current data points
     */
    public class DataPoint {
        public double voltage;
        public double current;

        public DataPoint(double V, double I) {
            voltage = V;
            current = I;
        }

    }

    /**
     * Structure for defining what to do on each update
     */
    public interface ProgressMonitor {

        public void update(int i, DataPoint point);

    }

}
