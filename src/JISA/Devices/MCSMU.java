package JISA.Devices;

import JISA.Addresses.InstrumentAddress;
import JISA.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public abstract class MCSMU extends SMU implements Iterable<SMU> {

    public MCSMU () {}

    public MCSMU(InstrumentAddress address) throws IOException {
        super(address);
    }

    public abstract double getVoltage(int channel) throws DeviceException, IOException;

    public abstract double getCurrent(int channel) throws DeviceException, IOException;

    public abstract void setVoltage(int channel, double voltage) throws DeviceException, IOException;

    public abstract void setCurrent(int channel, double current) throws DeviceException, IOException;

    public abstract void turnOn(int channel) throws DeviceException, IOException;

    public abstract void turnOff(int channel) throws DeviceException, IOException;

    public abstract boolean isOn(int channel) throws DeviceException, IOException;

    public abstract void setSource(int channel, Source source) throws DeviceException, IOException;

    public abstract Source getSource(int channel) throws DeviceException, IOException;

    public abstract void setBias(int channel, double level) throws DeviceException, IOException;

    public abstract double getSourceValue(int channel) throws DeviceException, IOException;

    public abstract double getMeasureValue(int channel) throws DeviceException, IOException;

    public abstract int getNumChannels();

    @Override
    public double getVoltage() throws DeviceException, IOException {
        return getVoltage(0);
    }

    @Override
    public double getCurrent() throws DeviceException, IOException {
        return getCurrent(0);
    }

    @Override
    public void setVoltage(double voltage) throws DeviceException, IOException {
        setVoltage(0, voltage);
    }

    @Override
    public void setCurrent(double current) throws DeviceException, IOException {
        setCurrent(0, current);
    }

    @Override
    public void turnOn() throws DeviceException, IOException {
        turnOn(0);
    }

    @Override
    public void turnOff() throws DeviceException, IOException {
        turnOff(0);
    }

    @Override
    public boolean isOn() throws DeviceException, IOException {
        return isOn(0);
    }

    @Override
    public void setSource(Source source) throws DeviceException, IOException {
        setSource(0, source);
    }

    @Override
    public Source getSource() throws DeviceException, IOException {
        return getSource(0);
    }

    @Override
    public void setBias(double level) throws DeviceException, IOException {
        setBias(0, level);
    }

    @Override
    public double getSourceValue() throws DeviceException, IOException {
        return getSourceValue(0);
    }

    @Override
    public double getMeasureValue() throws DeviceException, IOException {
        return getMeasureValue(0);
    }

    public SMU getChannel(int channel) throws DeviceException {

        if (channel >= getNumChannels()) {
            throw new DeviceException("This SMU does not have that channel!");
        }

        return new VirtualSMU(channel);

    }

    /**
     * Performs a linear sweep of either VOLTAGE or CURRENT, returning the V-I data points as an array of DataPoint objects
     *
     * @param channel  Channel number to perform sweep on
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
    public SMU.DataPoint[] performLinearSweep(int channel, Source source, double min, double max, int numSteps, long delay) throws DeviceException, IOException {
        return performLinearSweep(channel, source, min, max, numSteps, delay, (i, point) -> {
        });
    }

    /**
     * Performs a linear sweep of either VOLTAGE or CURRENT, returning the V-I data points as an array of DataPoint objects
     * whilst allowing you to keep track of the sweep's progress via a ProgressMonitor object.
     *
     * @param channel  Channel number to perform sweep on
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
    public SMU.DataPoint[] performLinearSweep(int channel, Source source, double min, double max, int numSteps, long delay, ProgressMonitor onUpdate) throws DeviceException, IOException {

        return performSweep(
                channel,
                source,
                Util.makeLinearArray(min, max, numSteps),
                delay,
                onUpdate
        );

    }

    /**
     * Performs a logarithmic sweep of either VOLTAGE or CURRENT, returning V-I data points as an array of DataPoint objects.
     *
     * @param channel  Channel number to perform sweep on
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
    public SMU.DataPoint[] performLogarithmicSweep(int channel, Source source, double min, double max, int numSteps, long delay) throws DeviceException, IOException {

        return performSweep(
                channel,
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
     * @param channel  Channel number to perform sweep on
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
    public SMU.DataPoint[] performLogarithmicSweep(int channel, Source source, double min, double max, int numSteps, long delay, ProgressMonitor onUpdate) throws DeviceException, IOException {

        return performSweep(
                channel,
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
     * @param channel  Channel number to perform sweep on
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
    public SMU.DataPoint[] performSweep(int channel, Source source, double[] values, long delay, ProgressMonitor onUpdate) throws DeviceException, IOException {

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

    public SMU.DataPoint[] performSweep(int channel, Source source, double[] values, long delay) throws IOException, DeviceException {
        return performSweep(source, values, delay, (i, p) -> {
        });
    }

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
    public SMU.DataPoint[] performLinearSweep(Source source, double min, double max, int numSteps, long delay) throws DeviceException, IOException {
        return performLinearSweep(0, source, min, max, numSteps, delay, (i, point) -> {
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
    public SMU.DataPoint[] performLinearSweep(Source source, double min, double max, int numSteps, long delay, ProgressMonitor onUpdate) throws DeviceException, IOException {

        return performSweep(
                0,
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
    public SMU.DataPoint[] performLogarithmicSweep(Source source, double min, double max, int numSteps, long delay) throws DeviceException, IOException {

        return performSweep(
                0,
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
    public SMU.DataPoint[] performLogarithmicSweep(Source source, double min, double max, int numSteps, long delay, ProgressMonitor onUpdate) throws DeviceException, IOException {

        return performSweep(
                0,
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
    public SMU.DataPoint[] performSweep(Source source, double[] values, long delay, ProgressMonitor onUpdate) throws DeviceException, IOException {
        return performSweep(0, source, values, delay, onUpdate);
    }

    public SMU.DataPoint[] performSweep(Source source, double[] values, long delay) throws IOException, DeviceException {
        return performSweep(source, values, delay, (i, p) -> {
        });
    }

    public Sweep createMultiSweep() {

        Sweep sweep = new Sweep() {

            private ArrayList<DataPoint> points = new ArrayList<>();

            @Override
            public DataPoint[] run() throws IOException, DeviceException {
                points.clear();
                step(0, new ArrayList<>(), new ArrayList<>());
                return points.toArray(new DataPoint[0]);
            }

            private void step(int step, ArrayList<Double> v, ArrayList<Double> i) throws IOException, DeviceException {

                Config conf = sweeps.get(step);

                if (step < sweeps.size() - 1) {
                    performSweep(
                            conf.channel,
                            conf.source,
                            conf.values,
                            conf.delay,
                            (n, p) -> {
                                v.add(p.voltage);
                                i.add(p.current);
                                step(step + 1, v, i);
                            }
                    );
                } else {

                    performSweep(
                            conf.channel,
                            conf.source,
                            conf.values,
                            conf.delay,
                            (n, p) -> {
                                ArrayList<Double> voltages = new ArrayList<>();
                                ArrayList<Double> currents = new ArrayList<>();
                                voltages.addAll(v);
                                voltages.add(p.voltage);
                                currents.addAll(i);
                                currents.add(p.current);
                                points.add(
                                        new DataPoint(
                                                voltages.toArray(new Double[0]),
                                                currents.toArray(new Double[0])
                                        )
                                );
                            }
                    );
                }

            }

        };

        return sweep;

    }

    public class DataPoint {

        public Double[] voltages;
        public Double[] currents;

        public DataPoint(Double[] v, Double[] i) {
            voltages = v;
            currents = i;
        }

    }

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

    public abstract class Sweep {

        protected ArrayList<Config> sweeps = new ArrayList<>();

        private class Config {

            public int      channel;
            public Source   source;
            public double[] values;
            public long     delay;

            public Config(int channel, Source source, double[] values, long delay) {

                this.channel = channel;
                this.source = source;
                this.values = values;
                this.delay = delay;

            }

        }

        public void addSweep(int channel, Source source, double[] values, long delay) {
            sweeps.add(new Config(channel, source, values, delay));
        }

        public void addLinearSweep(int channel, Source source, double min, double max, int numSteps, long delay) {
            addSweep(
                    channel,
                    source,
                    Util.makeLinearArray(min, max, numSteps),
                    delay
            );
        }

        public void addLogarithmicSweep(int channel, Source source, double min, double max, int numSteps, long delay) {
            addSweep(
                    channel,
                    source,
                    Util.makeLogarithmicArray(min, max, numSteps),
                    delay
            );
        }

        public abstract DataPoint[] run() throws IOException, DeviceException;

    }

    public Iterator<SMU> iterator() {

        ArrayList<SMU> list = new ArrayList<>();
        for (int i = 0; i < getNumChannels(); i ++) {
            try {
                list.add(getChannel(i));
            } catch (DeviceException e) {
            }
        }

        return list.iterator();

    }

}
