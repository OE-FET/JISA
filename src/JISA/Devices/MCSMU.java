package JISA.Devices;

import JISA.Addresses.InstrumentAddress;
import JISA.Experiment.IVPoint;
import JISA.Experiment.MCIVPoint;
import JISA.Experiment.ResultList;
import JISA.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Semaphore;

public abstract class MCSMU extends SMU implements Iterable<SMU> {

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
     * Sets whether the SMU should apply source using FORCE probes and measure using separate SENSE probes or whether is should
     * do both with the FORCE probes on the specified channel.
     *
     * @param channel    Channel number
     * @param fourProbes Should it use all four probes?
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract void useFourProbe(int channel, boolean fourProbes) throws DeviceException, IOException;

    /**
     * Sets whether the SMU should apply source using FORCE probes and measure using separate SENSE probes or whether is should
     * do both with the FORCE probes on the first channel.
     *
     * @param fourProbes Should it use all four probes?
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public void useFourProbe(boolean fourProbes) throws DeviceException, IOException {
        useFourProbe(0, fourProbes);
    }

    /**
     * Returns whether the device is currently configured to use all four probes on the specified channel.
     *
     * @param channel Channel number
     * @return Are all probes to be used?
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract boolean isUsingFourProbe(int channel) throws DeviceException, IOException;

    /**
     * Returns whether the device is currently configured to use all four probes on the first channel.
     *
     * @return Are all probes to be used?
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public boolean isUsingFourProbe() throws DeviceException, IOException {
        return isUsingFourProbe(0);
    }

    public abstract void setAverageMode(int channel, AMode mode) throws DeviceException, IOException;

    public void setAverageMode(AMode mode) throws DeviceException, IOException {
        setAverageMode(0, mode);
    }

    public abstract void setAverageCount(int channel, int count) throws DeviceException, IOException;

    public void setAverageCount(int count) throws DeviceException, IOException {
        setAverageCount(0, count);
    }

    public abstract int getAverageCount(int channel) throws DeviceException, IOException;

    public int getAverageCount() throws DeviceException, IOException {
        return getAverageCount(0);
    }

    public abstract AMode getAverageMode(int channel) throws DeviceException, IOException;

    public AMode getAverageMode() throws DeviceException, IOException {
        return getAverageMode(0);
    }

    /**
     * Sets the range of allowed values for the quantity being sourced by the SMU on the given channel.
     * A value of n indicates a range of -n to +n.
     *
     * @param channel Channel number
     * @param value   Range value, in Volts or Amps
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract void setSourceRange(int channel, double value) throws DeviceException, IOException;

    public void setSourceRange(double value) throws DeviceException, IOException {
        setSourceRange(0, value);
    }

    /**
     * Returns the range of allowed values for the quantity being sourced by the SMU on the given channel.
     * A value of n indicates a range of -n to +n.
     *
     * @param channel Channel number
     * @return Range value, in Volts or Amps
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract double getSourceRange(int channel) throws DeviceException, IOException;

    public double getSourceRange() throws DeviceException, IOException {
        return getSourceRange(0);
    }

    public abstract void useAutoSourceRange(int channel) throws DeviceException, IOException;

    public void useAutoSourceRange() throws DeviceException, IOException {
        useAutoSourceRange(0);
    }

    public abstract boolean isSourceRangeAuto(int channel) throws DeviceException, IOException;

    public boolean isSourceRangeAuto() throws DeviceException, IOException {
        return isSourceRangeAuto(0);
    }

    /**
     * Sets the range of allowed values for the quantity being measured by the SMU on the given channel.
     *
     * @param channel Channel number
     * @param value   Range value, in Volts or Amps
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract void setMeasureRange(int channel, double value) throws DeviceException, IOException;

    public void setMeasureRange(double value) throws DeviceException, IOException {
        setMeasureRange(0, value);
    }


    /**
     * Returns the range of allowed values for the quantity being measured by the SMU on the given channel.
     *
     * @param channel Channel number
     * @return Range value, in Volts or Amps
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public abstract double getMeasureRange(int channel) throws DeviceException, IOException;

    public double getMeasureRange() throws DeviceException, IOException {
        return getMeasureRange(0);
    }

    public abstract void useAutoMeasureRange(int channel) throws DeviceException, IOException;

    public void useAutoMeasureRange() throws DeviceException, IOException {
        useAutoMeasureRange(0);
    }

    public abstract boolean isMeasureRangeAuto(int channel) throws DeviceException, IOException;

    public boolean isMeasureRangeAuto() throws DeviceException, IOException {
        return isMeasureRangeAuto(0);
    }

    public abstract void setVoltageRange(int channel, double value) throws DeviceException, IOException;

    public void setVoltageRange(double value) throws DeviceException, IOException {
        setVoltageRange(0, value);
    }

    public abstract double getVoltageRange(int channel) throws DeviceException, IOException;

    public double getVoltageRange() throws DeviceException, IOException {
        return getVoltageRange(0);
    }

    public abstract void useAutoVoltageRange(int channel) throws DeviceException, IOException;

    public void useAutoVoltageRange() throws DeviceException, IOException {
        useAutoMeasureRange(0);
    }

    public abstract boolean isVoltageRangeAuto(int channel) throws DeviceException, IOException;

    public boolean isVoltageRangeAuto() throws DeviceException, IOException {
        return isMeasureRangeAuto(0);
    }

    public abstract void setCurrentRange(int channel, double value) throws DeviceException, IOException;

    public void setCurrentRange(double value) throws DeviceException, IOException {
        setCurrentRange(0, value);
    }

    public abstract double getCurrentRange(int channel) throws DeviceException, IOException;

    public double getCurrentRange() throws DeviceException, IOException {
        return getCurrentRange(0);
    }

    public abstract void useAutoCurrentRange(int channel) throws DeviceException, IOException;

    public void useAutoCurrentRange() throws DeviceException, IOException {
        useAutoMeasureRange(0);
    }

    public abstract boolean isCurrentRangeAuto(int channel) throws DeviceException, IOException;

    public boolean isCurrentRangeAuto() throws DeviceException, IOException {
        return isMeasureRangeAuto(0);
    }

    public abstract void setOutputLimit(int channel, double value) throws DeviceException, IOException;

    public void setOutputLimit(double value) throws DeviceException, IOException {
        setOutputLimit(0, value);
    }

    public abstract double getOutputLimit(int channel) throws DeviceException, IOException;

    public double getOutputLimit() throws DeviceException, IOException {
        return getOutputLimit(0);
    }

    public abstract void setVoltageLimit(int channel, double value) throws DeviceException, IOException;

    public void setVoltageLimit(double value) throws DeviceException, IOException {
        setVoltageLimit(0, value);
    }

    public abstract double getVoltageLimit(int channel) throws DeviceException, IOException;

    public double getVoltageLimit() throws DeviceException, IOException {
        return getVoltageLimit(0);
    }

    public abstract void setCurrentLimit(int channel, double value) throws DeviceException, IOException;

    public void setCurrentLimit(double value) throws DeviceException, IOException {
        setCurrentLimit(0, value);
    }

    public abstract double getCurrentLimit(int channel) throws DeviceException, IOException;

    public double getCurrentLimit() throws DeviceException, IOException {
        return getCurrentLimit(0);
    }

    public abstract void setIntegrationTime(int channel, double time) throws DeviceException, IOException;

    public void setIntegrationTime(double time) throws DeviceException, IOException {
        setIntegrationTime(0, time);
    }

    public abstract double getIntegrationTime(int channel) throws DeviceException, IOException;

    public double getIntegrationTime() throws DeviceException, IOException {
        return getIntegrationTime(0);
    }

    public IVPoint getIVPoint(int channel) throws DeviceException, IOException {
        return new IVPoint(getVoltage(channel), getCurrent(channel));
    }

    public IVPoint getIVPoint() throws DeviceException, IOException {
        return getIVPoint(0);
    }

    public MCIVPoint getMCIVPoint() throws DeviceException, IOException {
        MCIVPoint point = new MCIVPoint();

        for (int i = 0; i < getNumChannels(); i++) {
            point.addChannel(i, new IVPoint(getVoltage(i), getCurrent(i)));
        }

        return point;

    }

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

        try {
            return new VirtualSMU(channel);
        } catch (Exception e) {
            return null;
        }

    }

    /**
     * Performs a linear sweep of either VOLTAGE or CURRENT, returning the V-I data points as an array of MCIVPoint objects
     *
     * @param channel   Channel number to do sweep on
     * @param source    VOLTAGE or CURRENT
     * @param min       Minimum source value
     * @param max       Maximum source value
     * @param numSteps  Number of steps in sweep
     * @param delay     Amount of time, in milliseconds, to wait before taking each measurement
     * @param symmetric Should we sweep back to starting point after sweeping forwards?
     * @return Array of MCIVPoint objects containing I-V data points
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public IVPoint[] doLinearSweep(int channel, Source source, double min, double max, int numSteps, long delay, boolean symmetric) throws DeviceException, IOException {
        return doLinearSweep(channel, source, min, max, numSteps, delay, symmetric, (i, point) -> {
        });
    }

    /**
     * Performs a linear sweep of either VOLTAGE or CURRENT, returning the V-I data points as an array of MCIVPoint objects
     * whilst allowing you to keep track of the sweep's progress via a ProgressMonitor object.
     *
     * @param channel   Channel number to do sweep on
     * @param source    VOLTAGE or CURRENT
     * @param min       Minimum source value
     * @param max       Maximum source value
     * @param numSteps  Number of steps in sweep
     * @param delay     Amount of time, in milliseconds, to wait before taking each measurement
     * @param symmetric Should we sweep back to starting point after sweeping forwards?
     * @param onUpdate  Method to run each time a new measurement is completed
     * @return Array of MCIVPoint objects containing I-V data points
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public IVPoint[] doLinearSweep(int channel, Source source, double min, double max, int numSteps, long delay, boolean symmetric, ProgressMonitor onUpdate) throws DeviceException, IOException {

        return doSweep(
                channel,
                source,
                Util.makeLinearArray(min, max, numSteps),
                delay,
                symmetric,
                onUpdate
        );

    }

    public IVPoint[] doLinearSweep(Source source, double min, double max, int numSteps, long delay, boolean symmetric, ProgressMonitor onUpdate) throws DeviceException, IOException {
        return doLinearSweep(0, source, min, max, numSteps, delay, symmetric, onUpdate);
    }

    /**
     * Performs a logarithmic sweep of either VOLTAGE or CURRENT, returning V-I data points as an array of MCIVPoint objects
     * whilst allowing you to keep track of the sweep's progress via a ProgressMonitor object.
     *
     * @param channel   Channel number to do sweep on
     * @param source    VOLTAGE or CURRENT
     * @param min       Minimum source value
     * @param max       Maximum source value
     * @param numSteps  Number of steps in sweep
     * @param delay     Amount of time, in milliseconds, to wait before taking each measurement
     * @param symmetric Should we sweep back to starting point after sweeping forwards?
     * @param onUpdate  Method ot run each time a new measurement is completed
     * @return Array of MCIVPoint objects containing V-I data points
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public IVPoint[] doLogarithmicSweep(int channel, Source source, double min, double max, int numSteps, long delay, boolean symmetric, ProgressMonitor onUpdate) throws DeviceException, IOException {

        return doSweep(
                channel,
                source,
                Util.makeLogarithmicArray(min, max, numSteps),
                delay,
                symmetric,
                onUpdate
        );

    }

    public IVPoint[] doLogarithmicSweep(Source source, double min, double max, int numSteps, long delay, boolean symmetric, ProgressMonitor onUpdate) throws DeviceException, IOException {
        return doLogarithmicSweep(0, source, min, max, numSteps, delay, symmetric, onUpdate);
    }

    /**
     * Performs a logarithmic sweep of either VOLTAGE or CURRENT, returning V-I data points as an array of MCIVPoint objects
     * whilst allowing you to keep track of the sweep's progress via a ProgressMonitor object.
     *
     * @param channel   Channel number to do sweep on
     * @param source    VOLTAGE or CURRENT
     * @param values    Array of values to use in the sweep
     * @param delay     Amount of time, in milliseconds, to wait before taking each measurement
     * @param symmetric Should we sweep back to starting point after sweeping forwards?
     * @param onUpdate  Method ot run each time a new measurement is completed
     * @return Array of MCIVPoint objects containing V-I data points
     * @throws DeviceException Upon incompatibility with device
     * @throws IOException     Upon communications error
     */
    public IVPoint[] doSweep(int channel, Source source, double[] values, long delay, boolean symmetric, ProgressMonitor onUpdate) throws DeviceException, IOException {

        if (symmetric) {
            values = Util.symArray(values);
        }

        turnOff(channel);
        setSource(channel, source);
        setBias(channel, values[0]);

        int                i      = 0;
        ArrayList<IVPoint> points = new ArrayList<>();

        turnOn();

        for (double b : values) {

            setBias(channel, b);
            try {
                Thread.sleep(delay);
            } catch (Exception e) {
                throw new DeviceException("Couldn't sleep!");
            }

            IVPoint point = new IVPoint(getVoltage(channel), getCurrent(channel));
            onUpdate.update(i, point);
            points.add(point);
            i++;

        }

        return points.toArray(new IVPoint[0]);

    }


    public IVPoint[] doSweep(Source source, double[] values, long delay, boolean symmetric, ProgressMonitor onUpdate) throws DeviceException, IOException {
        return doSweep(0, source, values, delay, symmetric, onUpdate);
    }

    protected void checkChannel(int channel) throws DeviceException {
        if (!Util.isBetween(channel, 0, getNumChannels() - 1)) {
            throw new DeviceException("Channel %d does not exist for this SMU", channel);
        }
    }

    private static class Updater implements Runnable {

        private int                  i    = 0;
        private Semaphore            semaphore;
        private ArrayList<MCIVPoint> points;
        private MCUpdateHandler      onUpdate;
        private boolean              exit = false;

        public Updater(ArrayList<MCIVPoint> p, MCUpdateHandler o) {
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
                    onUpdate.onUpdate(i, points.get(i));
                    i++;
                } catch (Exception e) {
                    Util.exceptionHandler(e);
                }

            }

        }
    }

    /**
     * Creates an MCSMU.Sweep object allowing the configuration and execution of a multi-channel sweep
     *
     * @return Sweep
     */
    public Sweep createNestedSweep() {

        Sweep sweep = new Sweep() {

            private ArrayList<MCIVPoint> results = new ArrayList<>();
            private Updater updater;

            @Override
            public MCIVPoint[] run(MCUpdateHandler onUpdate) throws IOException, DeviceException {
                results.clear();
                updater = new Updater(results, onUpdate);
                (new Thread(updater)).start();
                step(0);
                updater.end();
                return results.toArray(new MCIVPoint[0]);
            }

            private void step(int step) throws IOException, DeviceException {

                if (step >= sweeps.size()) {

                    MCIVPoint point = new MCIVPoint();
                    for (int i = 0; i < getNumChannels(); i++) {
                        point.addChannel(i, new IVPoint(getVoltage(i), getCurrent(i)));
                    }

                    results.add(point);
                    updater.runUpdate();
                    return;

                }

                Config conf = sweeps.get(step);
                setSource(conf.channel, conf.source);
                setBias(conf.channel, conf.values[0]);
                turnOn(conf.channel);

                for (double val : conf.values) {

                    setBias(conf.channel, val);
                    Util.sleep(conf.delay);
                    step(step + 1);

                }

            }

        };

        return sweep;

    }

    public Sweep createComboSweep() {

        Sweep sweep = new Sweep() {

            private ArrayList<MCIVPoint> results = new ArrayList<>();
            private Updater updater;

            @Override
            public MCIVPoint[] run(MCUpdateHandler onUpdate) throws IOException, DeviceException {
                results.clear();

                if (sweeps.size() == 0) {
                    throw new DeviceException("No sweeps have been configured!");
                }

                int size = sweeps.get(0).values.length;

                if (size == 0) {
                    throw new DeviceException("Empty sweep!");
                }

                for (Config conf : sweeps) {

                    if (conf.values.length != size) {
                        throw new DeviceException("Each sweep must be of the same length!");
                    }

                    setSource(conf.channel, conf.source);
                    setBias(conf.channel, conf.values[0]);
                    turnOn(conf.channel);

                }

                updater = new Updater(results, onUpdate);
                (new Thread(updater)).start();

                MCIVPoint point;
                for (int i = 0; i < size; i++) {

                    for (Config conf : sweeps) {
                        setBias(conf.channel, conf.values[i]);
                        Util.sleep(conf.delay);
                    }

                    point = new MCIVPoint();
                    for (int j = 0; j < getNumChannels(); j++) {
                        point.addChannel(j, new IVPoint(getVoltage(j), getCurrent(j)));
                    }
                    results.add(point);
                    updater.runUpdate();

                }

                updater.end();
                return results.toArray(new MCIVPoint[0]);

            }

        };

        return sweep;

    }

    /**
     * Class for controlling an MCSMU channel as if it were a separate SMU
     */
    public class VirtualSMU extends SMU {

        private int channel;

        public VirtualSMU(int channel) throws IOException {
            super(null);
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

        @Override
        public void useFourProbe(boolean fourProbes) throws DeviceException, IOException {
            MCSMU.this.useFourProbe(channel, fourProbes);
        }

        @Override
        public boolean isUsingFourProbe() throws DeviceException, IOException {
            return MCSMU.this.isUsingFourProbe(channel);
        }

        @Override
        public void setAverageMode(AMode mode) throws DeviceException, IOException {
            MCSMU.this.setAverageMode(channel, mode);
        }

        @Override
        public void setAverageCount(int count) throws DeviceException, IOException {
            MCSMU.this.setAverageCount(channel, count);
        }

        @Override
        public AMode getAverageMode() throws DeviceException, IOException {
            return MCSMU.this.getAverageMode(channel);
        }

        @Override
        public int getAverageCount() throws DeviceException, IOException {
            return MCSMU.this.getAverageCount(channel);
        }

        @Override
        public void setSourceRange(double value) throws DeviceException, IOException {
            MCSMU.this.setSourceRange(channel, value);
        }

        @Override
        public double getSourceRange() throws DeviceException, IOException {
            return MCSMU.this.getSourceRange(channel);
        }

        @Override
        public void useAutoSourceRange() throws DeviceException, IOException {
            MCSMU.this.useAutoSourceRange(channel);
        }

        @Override
        public boolean isSourceRangeAuto() throws DeviceException, IOException {
            return MCSMU.this.isSourceRangeAuto(channel);
        }

        @Override
        public void setMeasureRange(double value) throws DeviceException, IOException {
            MCSMU.this.setMeasureRange(channel, value);
        }

        @Override
        public double getMeasureRange() throws DeviceException, IOException {
            return MCSMU.this.getMeasureRange(channel);
        }

        @Override
        public void useAutoMeasureRange() throws DeviceException, IOException {
            MCSMU.this.useAutoMeasureRange(channel);
        }

        @Override
        public boolean isMeasureRangeAuto() throws DeviceException, IOException {
            return MCSMU.this.isMeasureRangeAuto(channel);
        }

        @Override
        public void setVoltageRange(double value) throws DeviceException, IOException {
            MCSMU.this.setVoltageRange(channel, value);
        }

        @Override
        public double getVoltageRange() throws DeviceException, IOException {
            return MCSMU.this.getVoltageRange(channel);
        }

        @Override
        public void useAutoVoltageRange() throws DeviceException, IOException {
            MCSMU.this.useAutoVoltageRange(channel);
        }

        @Override
        public boolean isVoltageRangeAuto() throws DeviceException, IOException {
            return MCSMU.this.isVoltageRangeAuto(channel);
        }

        @Override
        public void setCurrentRange(double value) throws DeviceException, IOException {
            MCSMU.this.setCurrentRange(channel, value);
        }

        @Override
        public double getCurrentRange() throws DeviceException, IOException {
            return MCSMU.this.getCurrentRange(channel);
        }

        @Override
        public void useAutoCurrentRange() throws DeviceException, IOException {
            MCSMU.this.useAutoCurrentRange(channel);
        }

        @Override
        public boolean isCurrentRangeAuto() throws DeviceException, IOException {
            return MCSMU.this.isCurrentRangeAuto(channel);
        }

        @Override
        public void setOutputLimit(double value) throws DeviceException, IOException {
            MCSMU.this.setOutputLimit(channel, value);
        }

        @Override
        public double getOutputLimit() throws DeviceException, IOException {
            return MCSMU.this.getOutputLimit(channel);
        }

        @Override
        public void setVoltageLimit(double voltage) throws DeviceException, IOException {
            MCSMU.this.setVoltageLimit(channel, voltage);
        }

        @Override
        public double getVoltageLimit() throws DeviceException, IOException {
            return MCSMU.this.getVoltageLimit(channel);
        }

        @Override
        public void setCurrentLimit(double current) throws DeviceException, IOException {
            MCSMU.this.setCurrentLimit(channel, current);
        }

        @Override
        public double getCurrentLimit() throws DeviceException, IOException {
            return MCSMU.this.getCurrentLimit(channel);
        }

        @Override
        public void setIntegrationTime(double time) throws DeviceException, IOException {
            MCSMU.this.setIntegrationTime(channel, time);
        }

        @Override
        public double getIntegrationTime() throws DeviceException, IOException {
            return MCSMU.this.getIntegrationTime(channel);
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
                this.values = symmetric ? Util.symArray(values) : values;
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

        public abstract MCIVPoint[] run(MCUpdateHandler onUpdate) throws IOException, DeviceException;

        public MCIVPoint[] run() throws IOException, DeviceException {
            return run((i, p) -> {
            });
        }

        public MCIVPoint[] run(ResultList list) throws IOException, DeviceException {
            return run((n, p) -> {
                Double[] data = new Double[2 * getNumChannels()];
                for (int i = 0; i < getNumChannels(); i++) {
                    data[i * 2] = p.getChannel(i).voltage;
                    data[i * 2 + 1] = p.getChannel(i).current;
                }
                list.addData(data);
            });
        }

    }


    public ResultList createSweepList() {

        String[] titles = new String[getNumChannels() * 2];
        String[] units  = new String[getNumChannels() * 2];

        for (int i = 0; i < getNumChannels(); i++) {
            titles[i * 2] = String.format("Voltage %d", i);
            titles[i * 2 + 1] = String.format("Current %d", i);
            units[i * 2] = "V";
            units[i * 2 + 1] = "A";
        }

        ResultList list = new ResultList(titles);
        list.setUnits(units);

        return list;

    }

    public interface MCUpdateHandler {
        public void onUpdate(int count, MCIVPoint point) throws IOException, DeviceException;

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
