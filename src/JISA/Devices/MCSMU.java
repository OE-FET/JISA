package JISA.Devices;

import JISA.Addresses.InstrumentAddress;
import JISA.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class MCSMU extends SMU implements Iterable<SMU> {

    public MCSMU() {
    }

    public MCSMU(InstrumentAddress address) throws IOException {
        super(address);
    }

    /**
     * Returns the voltage of the specified channel
     *
     * @param channel Channel number
     * @return Voltage, in Volts
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    public abstract double getVoltage(int channel) throws DeviceException, IOException;


    /**
     * Returns the voltage of the first channel
     *
     * @return Voltage, in Volts
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    public double getVoltage() throws DeviceException, IOException {
        return getVoltage(0);
    }

    /**
     * Returns the current of the specified channel
     *
     * @param channel Channel number
     * @return Current, in Amps
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    public abstract double getCurrent(int channel) throws DeviceException, IOException;

    /**
     * Returns the current of the first channel
     *
     * @return Current, in Amps
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    public double getCurrent() throws DeviceException, IOException {
        return getCurrent(0);
    }

    /**
     * Sets the specified channel to source a the given voltage (when turned on)
     *
     * @param channel Channel number
     * @param voltage Voltage to source, in Volts
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    public abstract void setVoltage(int channel, double voltage) throws DeviceException, IOException;

    /**
     * Sets the first channel to source a the given voltage (when turned on)
     *
     * @param voltage Voltage to source, in Volts
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    public void setVoltage(double voltage) throws DeviceException, IOException {
        setVoltage(0, voltage);
    }

    /**
     * Sets the specified channel to source a the given current (when turned on)
     *
     * @param channel Channel number
     * @param current Current to source, in Amps
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    public abstract void setCurrent(int channel, double current) throws DeviceException, IOException;

    /**
     * Sets the first channel to source a the given current (when turned on)
     *
     * @param current Current to source, in Amps
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    public void setCurrent(double current) throws DeviceException, IOException {
        setCurrent(0, current);
    }

    /**
     * Enables output on the specified channel
     *
     * @param channel Channel number
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    public abstract void turnOn(int channel) throws DeviceException, IOException;

    /**
     * Enables output on the first channel
     *
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    public void turnOn() throws DeviceException, IOException {
        turnOn(0);
    }

    /**
     * Disables output on the specified channel
     *
     * @param channel Channel number
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    public abstract void turnOff(int channel) throws DeviceException, IOException;

    /**
     * Disables output on the first channel
     *
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    public void turnOff() throws DeviceException, IOException {
        turnOff(0);
    }

    /**
     * Returns whether the specified channel currently has its output enabled
     *
     * @param channel Channel number
     * @return Is it enabled?
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    public abstract boolean isOn(int channel) throws DeviceException, IOException;

    /**
     * Returns whether the first channel currently has its output enabled
     *
     * @return Is it enabled?
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    public boolean isOn() throws DeviceException, IOException {
        return isOn(0);
    }

    /**
     * Sets the source mode of the specified channel
     *
     * @param channel Channel number
     * @param source  VOLTAGE or CURRENT
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    public abstract void setSource(int channel, Source source) throws DeviceException, IOException;

    /**
     * Sets the source mode of the first channel
     *
     * @param source VOLTAGE or CURRENT
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    public void setSource(Source source) throws DeviceException, IOException {
        setSource(0, source);
    }

    /**
     * Returns the source mode of the specified channel
     *
     * @param channel Channel number
     * @return Source mode (VOLTAGE or CURRENT)
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    public abstract Source getSource(int channel) throws DeviceException, IOException;

    /**
     * Returns the source mode of the first channel
     *
     * @return Source mode (VOLTAGE or CURRENT)
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    public Source getSource() throws DeviceException, IOException {
        return getSource(0);
    }

    /**
     * Sets the level of whichever quantity is being sourced on the specified channel
     *
     * @param channel Channel number
     * @param level   Volts or Amps
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    public abstract void setBias(int channel, double level) throws DeviceException, IOException;

    /**
     * Sets the level of whichever quantity is being sourced on the first channel
     *
     * @param level Volts or Amps
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    public void setBias(double level) throws DeviceException, IOException {
        setBias(0, level);
    }

    /**
     * Returns the value of whichever quantity is being sourced on the specified channel
     *
     * @param channel Channel number
     * @return Voltage or Current, in Volts or Amps
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    public abstract double getSourceValue(int channel) throws DeviceException, IOException;

    /**
     * Returns the value of whichever quantity is being sourced on the first channel
     *
     * @return Voltage or Current, in Volts or Amps
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    public double getSourceValue() throws DeviceException, IOException {
        return getSourceValue(0);
    }

    /**
     * Returns the value of whichever quantity is being measured on the specified channel
     *
     * @param channel Channel number
     * @return Voltage or Current, in Volts or Amps
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    public abstract double getMeasureValue(int channel) throws DeviceException, IOException;

    /**
     * Returns the value of whichever quantity is being measured on the first channel
     *
     * @return Voltage or Current, in Volts or Amps
     * @throws DeviceException Upon device compatibility error
     * @throws IOException     Upon communications error
     */
    public double getMeasureValue() throws DeviceException, IOException {
        return getMeasureValue(0);
    }

    /**
     * Returns the number of channels this SMU has.
     *
     * @return Number of channels
     */
    public abstract int getNumChannels();

    /**
     * Returns a virtual SMU object to control the specified channel of the MCSMU
     *
     * @param channel Channel number
     * @return Virtual SMU
     * @throws DeviceException If channel does not exist
     */
    public SMU getChannel(int channel) throws DeviceException {

        if (channel >= getNumChannels()) {
            throw new DeviceException("This SMU does not have that channel!");
        }

        return new VirtualSMU(channel);

    }

    /**
     * Performs a linear sweep of either VOLTAGE or CURRENT, returning the V-I data points as an array of DataPoint objects
     *
     * @param channel  Channel number to do sweep on
     * @param source   VOLTAGE or CURRENT
     * @param min      Minimum source value
     * @param max      Maximum source value
     * @param numSteps Number of steps in sweep
     * @param delay    Amount of time, in milliseconds, to wait before taking each measurement
     * @param symmetric Should we sweep back to starting point after sweeping forwards?
     * @return Array of DataPoint objects containing I-V data points
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public SMU.DataPoint[] doLinearSweep(int channel, Source source, double min, double max, int numSteps, long delay, boolean symmetric) throws DeviceException, IOException {
        return doLinearSweep(channel, source, min, max, numSteps, delay, symmetric, (i, point) -> {
        });
    }

    /**
     * Performs a linear sweep of either VOLTAGE or CURRENT, returning the V-I data points as an array of DataPoint objects
     * whilst allowing you to keep track of the sweep's progress via a ProgressMonitor object.
     *
     * @param channel  Channel number to do sweep on
     * @param source   VOLTAGE or CURRENT
     * @param min      Minimum source value
     * @param max      Maximum source value
     * @param numSteps Number of steps in sweep
     * @param delay    Amount of time, in milliseconds, to wait before taking each measurement
     * @param symmetric Should we sweep back to starting point after sweeping forwards?
     * @param onUpdate Method to run each time a new measurement is completed
     * @return Array of DataPoint objects containing I-V data points
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public SMU.DataPoint[] doLinearSweep(int channel, Source source, double min, double max, int numSteps, long delay, boolean symmetric, ProgressMonitor onUpdate) throws DeviceException, IOException {

        return doSweep(
                channel,
                source,
                Util.makeLinearArray(min, max, numSteps),
                delay,
                symmetric,
                onUpdate
        );

    }

    public SMU.DataPoint[] doLinearSweep(Source source, double min, double max, int numSteps, long delay, boolean symmetric, ProgressMonitor onUpdate) throws DeviceException, IOException {
        return doLinearSweep(0, source, min, max, numSteps, delay, symmetric, onUpdate);
    }

    /**
     * Performs a logarithmic sweep of either VOLTAGE or CURRENT, returning V-I data points as an array of DataPoint objects
     * whilst allowing you to keep track of the sweep's progress via a ProgressMonitor object.
     *
     * @param channel  Channel number to do sweep on
     * @param source   VOLTAGE or CURRENT
     * @param min      Minimum source value
     * @param max      Maximum source value
     * @param numSteps Number of steps in sweep
     * @param delay    Amount of time, in milliseconds, to wait before taking each measurement
     * @param symmetric Should we sweep back to starting point after sweeping forwards?
     * @param onUpdate Method ot run each time a new measurement is completed
     * @return Array of DataPoint objects containing V-I data points
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public SMU.DataPoint[] doLogarithmicSweep(int channel, Source source, double min, double max, int numSteps, long delay, boolean symmetric, ProgressMonitor onUpdate) throws DeviceException, IOException {

        return doSweep(
                channel,
                source,
                Util.makeLogarithmicArray(min, max, numSteps),
                delay,
                symmetric,
                onUpdate
        );

    }

    public SMU.DataPoint[] doLogarithmicSweep(Source source, double min, double max, int numSteps, long delay, boolean symmetric, ProgressMonitor onUpdate) throws DeviceException, IOException {
        return doLogarithmicSweep(0, source, min, max, numSteps, delay, symmetric, onUpdate);
    }

    /**
     * Performs a logarithmic sweep of either VOLTAGE or CURRENT, returning V-I data points as an array of DataPoint objects
     * whilst allowing you to keep track of the sweep's progress via a ProgressMonitor object.
     *
     * @param channel  Channel number to do sweep on
     * @param source   VOLTAGE or CURRENT
     * @param values   Array of values to use in the sweep
     * @param delay    Amount of time, in milliseconds, to wait before taking each measurement
     * @param symmetric Should we sweep back to starting point after sweeping forwards?
     * @param onUpdate Method ot run each time a new measurement is completed
     * @return Array of DataPoint objects containing V-I data points
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public SMU.DataPoint[] doSweep(int channel, Source source, double[] values, long delay, boolean symmetric, ProgressMonitor onUpdate) throws DeviceException, IOException {

        if (symmetric) {
            values = Util.symArray(values);
        }

        turnOff(channel);
        setSource(channel, source);
        setBias(channel, values[0]);

        int                      i      = 0;
        ArrayList<SMU.DataPoint> points = new ArrayList<>();

        turnOn();

        for (double b : values) {

            setBias(channel, b);
            try {
                Thread.sleep(delay);
            } catch (Exception e) {
                throw new DeviceException("Couldn't sleep!");
            }

            SMU.DataPoint point = new SMU.DataPoint(getVoltage(channel), getCurrent(channel));
            onUpdate.update(i, point);
            points.add(point);
            i++;

        }

        return points.toArray(new SMU.DataPoint[0]);

    }


    public SMU.DataPoint[] doSweep(Source source, double[] values, long delay, boolean symmetric, ProgressMonitor onUpdate) throws DeviceException, IOException {
        return doSweep(0, source, values, delay, symmetric, onUpdate);
    }

    /**
     * Creates an MCSMU.Sweep object allowing the configuration and execution of a multi-channel sweep
     *
     * @return Sweep
     */
    public Sweep createMultiSweep() {

        Sweep sweep = new Sweep() {

            private ArrayList<DataPoint> results = new ArrayList<>();

            @Override
            public DataPoint[] run() throws IOException, DeviceException {
                results.clear();
                step(0, new HashMap<>());
                return results.toArray(new DataPoint[0]);
            }

            private void step(int step, HashMap<Integer, SMU.DataPoint> points) throws IOException, DeviceException {

                Config conf = sweeps.get(step);

                if (step < sweeps.size() - 1) {
                    doSweep(
                            conf.channel,
                            conf.source,
                            conf.values,
                            conf.delay,
                            conf.symmetric,
                            (n, p) -> {
                                points.put(conf.channel, p);
                                step(step + 1, points);
                            }
                    );
                } else {

                    doSweep(
                            conf.channel,
                            conf.source,
                            conf.values,
                            conf.delay,
                            conf.symmetric,
                            (n, p) -> {
                                DataPoint pnt = new DataPoint();
                                pnt.addAll(points);
                                pnt.addChannel(conf.channel, p);
                                results.add(pnt);
                            }
                    );
                }

            }

        };

        return sweep;

    }

    /**
     * Structure to hold I-V data from a multi-channel sweep
     */
    public class DataPoint {

        private HashMap<Integer, SMU.DataPoint> channels = new HashMap<>();

        public void addChannel(int channel, SMU.DataPoint point) {
            channels.put(channel, point);
        }

        public void addAll(Map<Integer, SMU.DataPoint> c) {
            channels.putAll(c);
        }

        public SMU.DataPoint getChannel(int channel) {
            return channels.getOrDefault(channel, null);
        }

    }

    /**
     * Class for controlling an MCSMU channel as if it were a separate SMU
     */
    public class VirtualSMU extends SMU {

        private int channel;

        public VirtualSMU(int channel) {
            this.channel = channel;
        }

        @Override
        public double getVoltage() throws DeviceException, IOException {
            return MCSMU.this.getVoltage(channel);
        }

        @Override
        public double getCurrent() throws DeviceException, IOException {
            return MCSMU.this.getCurrent(channel);
        }

        @Override
        public void setVoltage(double voltage) throws DeviceException, IOException {
            MCSMU.this.setVoltage(channel, voltage);
        }

        @Override
        public void setCurrent(double current) throws DeviceException, IOException {
            MCSMU.this.setCurrent(channel, current);
        }

        @Override
        public void turnOn() throws DeviceException, IOException {
            MCSMU.this.turnOn(channel);
        }

        @Override
        public void turnOff() throws DeviceException, IOException {
            MCSMU.this.turnOff(channel);
        }

        @Override
        public boolean isOn() throws DeviceException, IOException {
            return MCSMU.this.isOn(channel);
        }

        @Override
        public void setSource(Source source) throws DeviceException, IOException {
            MCSMU.this.setSource(channel, source);
        }

        @Override
        public Source getSource() throws DeviceException, IOException {
            return MCSMU.this.getSource(channel);
        }

        @Override
        public void setBias(double level) throws DeviceException, IOException {
            MCSMU.this.setBias(channel, level);
        }

        @Override
        public double getSourceValue() throws DeviceException, IOException {
            return MCSMU.this.getSourceValue(channel);
        }

        @Override
        public double getMeasureValue() throws DeviceException, IOException {
            return MCSMU.this.getMeasureValue(channel);
        }

    }

    /**
     * Class for configuring then executing multi-channel sweeps
     */
    public abstract class Sweep {

        protected ArrayList<Config> sweeps = new ArrayList<>();

        protected class Config {

            public int      channel;
            public Source   source;
            public double[] values;
            public long     delay;
            public boolean  symmetric;

            public Config(int channel, Source source, double[] values, long delay, boolean symmetric) {

                this.channel = channel;
                this.source = source;
                this.values = values;
                this.delay = delay;
                this.symmetric = symmetric;

            }

        }

        public void addSweep(int channel, Source source, double[] values, long delay, boolean symmetric) {
            sweeps.add(new Config(channel, source, values, delay, symmetric));
        }

        public void addLinearSweep(int channel, Source source, double min, double max, int numSteps, long delay, boolean symmetric) {
            addSweep(
                    channel,
                    source,
                    Util.makeLinearArray(min, max, numSteps),
                    delay,
                    symmetric
            );
        }

        public void addLogarithmicSweep(int channel, Source source, double min, double max, int numSteps, long delay, boolean symmetric) {
            addSweep(
                    channel,
                    source,
                    Util.makeLogarithmicArray(min, max, numSteps),
                    delay,
                    symmetric
            );
        }

        public abstract DataPoint[] run() throws IOException, DeviceException;

    }

    public Iterator<SMU> iterator() {

        ArrayList<SMU> list = new ArrayList<>();
        for (int i = 0; i < getNumChannels(); i++) {
            try {
                list.add(getChannel(i));
            } catch (DeviceException e) {
            }
        }

        return list.iterator();

    }

}
