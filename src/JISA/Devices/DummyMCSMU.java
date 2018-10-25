package JISA.Devices;

import java.io.IOException;
import java.util.Random;

public class DummyMCSMU extends MCSMU {

    private Random    random  = new Random();
    private Double[]  current = {null, null, null, null};
    private Double[]  voltage = {null, null, null, null};
    private Source[]  mode    = {Source.VOLTAGE, Source.VOLTAGE, Source.VOLTAGE, Source.VOLTAGE};
    private boolean[] probes  = {true, true, true, true};
    private double[]  R       = {random.nextDouble() * 500, random.nextDouble() * 500, random.nextDouble() * 500, random.nextDouble() * 500};

    public DummyMCSMU() throws IOException {
        super(null);
    }

    @Override
    public double getVoltage(int channel) throws DeviceException, IOException {
        return voltage[channel] == null ? getCurrent(channel) * (R[channel] + (1 - 2 * random.nextDouble()) * 0.05 * R[channel]) : voltage[channel];
    }

    @Override
    public double getCurrent(int channel) throws DeviceException, IOException {
        return current[channel] == null ? getVoltage(channel) / (R[channel] + (1 - 2 * random.nextDouble()) * 0.05 * R[channel]) : current[channel];
    }

    @Override
    public void setVoltage(int channel, double voltage) throws DeviceException, IOException {
        this.voltage[channel] = voltage;
        this.current[channel] = null;
    }

    @Override
    public void setCurrent(int channel, double current) throws DeviceException, IOException {
        this.current[channel] = current;
        this.voltage[channel] = null;
    }

    @Override
    public void turnOn(int channel) throws DeviceException, IOException {

    }

    @Override
    public void turnOff(int channel) throws DeviceException, IOException {

    }

    @Override
    public boolean isOn(int channel) throws DeviceException, IOException {
        return true;
    }

    @Override
    public void setSource(int channel, Source source) throws DeviceException, IOException {
        mode[channel] = source;
    }

    @Override
    public Source getSource(int channel) throws DeviceException, IOException {
        return mode[channel];
    }

    @Override
    public void setBias(int channel, double level) throws DeviceException, IOException {

        switch (getSource(channel)) {

            case VOLTAGE:
                setVoltage(channel, level);
                break;

            case CURRENT:
                setCurrent(channel, level);
                break;

        }

    }

    @Override
    public double getSourceValue(int channel) throws DeviceException, IOException {

        switch (getSource(channel)) {

            case VOLTAGE:
                getVoltage(channel);
                break;

            case CURRENT:
                getCurrent(channel);
                break;

        }

        return getVoltage(channel);
    }

    @Override
    public double getMeasureValue(int channel) throws DeviceException, IOException {
        switch (getSource(channel)) {

            case VOLTAGE:
                getCurrent(channel);
                break;

            case CURRENT:
                getVoltage(channel);
                break;

        }

        return getVoltage(channel);
    }

    @Override
    public int getNumChannels() {
        return 4;
    }

    @Override
    public void useFourProbe(int channel, boolean fourProbes) throws DeviceException, IOException {
        probes[channel] = fourProbes;
    }

    @Override
    public boolean isUsingFourProbe(int channel) throws DeviceException, IOException {
        return probes[channel];
    }

    @Override
    public void setAverageMode(int channel, AMode mode) throws DeviceException, IOException {

    }

    @Override
    public void setAverageCount(int channel, int count) throws DeviceException, IOException {

    }

    @Override
    public int getAverageCount(int channel) throws DeviceException, IOException {
        return 0;
    }

    @Override
    public AMode getAverageMode(int channel) throws DeviceException, IOException {
        return null;
    }

}
