package jisa.devices;

import jisa.addresses.Address;
import jisa.enums.AMode;
import jisa.enums.Source;
import jisa.enums.TType;
import jisa.enums.Terminals;
import jisa.experiment.IVPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Class to combine multiple SMUs into a single virtual SMU with multiple channels.
 */
public class SMUCluster implements MCSMU {

    private ArrayList<SMU> devices = new ArrayList<>();

    /**
     * Creates an SMUCluster using the given SMU objects.
     *
     * @param smus SMUs to combine
     *
     * @throws IOException Upon communications error
     */
    public SMUCluster(SMU... smus) {

        for (SMU s : smus) {

            if (s instanceof MCSMU) {
                add((MCSMU) s);
            } else {
                add(s);
            }

        }
    }

    /**
     * Add an SMU to the cluster.
     *
     * @param device SMU to add
     */
    public void add(SMU device) {
        devices.add(device);
    }

    /**
     * Add one channel from an MCSMU to the cluster
     *
     * @param device  The MCSMU
     * @param channel Channel number to add
     *
     * @throws DeviceException If the channel does not exist
     */
    public void add(MCSMU device, int channel) throws DeviceException {
        devices.add(device.getChannel(channel));
    }

    /**
     * Add all channels from an MCSMU to the cluster
     *
     * @param device MCSMU to add
     */
    public void add(MCSMU device) {
        for (SMU d : devices) {
            add(d);
        }
    }

    @Override
    public double getVoltage(int channel) throws DeviceException, IOException {
        if (devices.size() <= channel) {
            throw new DeviceException("Channel does not exist!");
        }
        return devices.get(channel).getVoltage();
    }

    @Override
    public double getCurrent(int channel) throws DeviceException, IOException {
        if (devices.size() <= channel) {
            throw new DeviceException("Channel does not exist!");
        }
        return devices.get(channel).getCurrent();
    }

    @Override
    public void setVoltage(int channel, double voltage) throws DeviceException, IOException {
        if (devices.size() <= channel) {
            throw new DeviceException("Channel does not exist!");
        }
        devices.get(channel).setVoltage(voltage);
    }

    @Override
    public void setCurrent(int channel, double current) throws DeviceException, IOException {
        if (devices.size() <= channel) {
            throw new DeviceException("Channel does not exist!");
        }
        devices.get(channel).setCurrent(current);
    }

    @Override
    public void turnOn(int channel) throws DeviceException, IOException {
        if (devices.size() <= channel) {
            throw new DeviceException("Channel does not exist!");
        }
        devices.get(channel).turnOn();
    }

    @Override
    public void turnOff(int channel) throws DeviceException, IOException {
        if (devices.size() <= channel) {
            throw new DeviceException("Channel does not exist!");
        }
        devices.get(channel).turnOff();
    }

    @Override
    public boolean isOn(int channel) throws DeviceException, IOException {
        if (devices.size() <= channel) {
            throw new DeviceException("Channel does not exist!");
        }
        return devices.get(channel).isOn();
    }

    @Override
    public void setSource(int channel, Source source) throws DeviceException, IOException {
        if (devices.size() <= channel) {
            throw new DeviceException("Channel does not exist!");
        }
        devices.get(channel).setSource(source);
    }

    @Override
    public Source getSource(int channel) throws DeviceException, IOException {
        if (devices.size() <= channel) {
            throw new DeviceException("Channel does not exist!");
        }
        return devices.get(channel).getSource();
    }

    @Override
    public void setBias(int channel, double level) throws DeviceException, IOException {
        if (devices.size() <= channel) {
            throw new DeviceException("Channel does not exist!");
        }
        devices.get(channel).setBias(level);
    }

    @Override
    public double getSourceValue(int channel) throws DeviceException, IOException {
        if (devices.size() <= channel) {
            throw new DeviceException("Channel does not exist!");
        }
        return devices.get(channel).getSourceValue();
    }

    @Override
    public double getMeasureValue(int channel) throws DeviceException, IOException {
        if (devices.size() <= channel) {
            throw new DeviceException("Channel does not exist!");
        }
        return devices.get(channel).getMeasureValue();
    }

    @Override
    public int getNumChannels() {
        return devices.size();
    }

    @Override
    public void useFourProbe(int channel, boolean fourProbes) throws DeviceException, IOException {
        if (devices.size() <= channel) {
            throw new DeviceException("Channel does not exist!");
        }
        devices.get(channel).useFourProbe(fourProbes);
    }

    @Override
    public boolean isUsingFourProbe(int channel) throws DeviceException, IOException {
        if (devices.size() <= channel) {
            throw new DeviceException("Channel does not exist!");
        }
        return devices.get(channel).isUsingFourProbe();
    }

    @Override
    public void setAverageMode(int channel, AMode mode) throws DeviceException, IOException {
        if (devices.size() <= channel) {
            throw new DeviceException("Channel does not exist!");
        }
        devices.get(channel).setAverageMode(mode);
    }

    @Override
    public void setAverageCount(int channel, int count) throws DeviceException, IOException {
        if (devices.size() <= channel) {
            throw new DeviceException("Channel does not exist!");
        }
        devices.get(channel).setAverageCount(count);
    }

    @Override
    public int getAverageCount(int channel) throws DeviceException, IOException {
        if (devices.size() <= channel) {
            throw new DeviceException("Channel does not exist!");
        }
        return devices.get(channel).getAverageCount();
    }

    @Override
    public AMode getAverageMode(int channel) throws DeviceException, IOException {
        if (devices.size() <= channel) {
            throw new DeviceException("Channel does not exist!");
        }
        return devices.get(channel).getAverageMode();
    }

    @Override
    public void setSourceRange(int channel, double value) throws DeviceException, IOException {
        checkChannel(channel);
        devices.get(channel).setSourceRange(value);
    }

    @Override
    public double getSourceRange(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return devices.get(channel).getSourceRange();
    }

    @Override
    public void useAutoSourceRange(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        devices.get(channel).useAutoSourceRange();
    }

    @Override
    public boolean isAutoRangingSource(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return devices.get(channel).isAutoRangingSource();
    }

    @Override
    public void setMeasureRange(int channel, double value) throws DeviceException, IOException {
        checkChannel(channel);
        devices.get(channel).setMeasureRange(value);
    }

    @Override
    public double getMeasureRange(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return devices.get(channel).getMeasureRange();
    }

    @Override
    public void useAutoMeasureRange(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        devices.get(channel).useAutoMeasureRange();
    }

    @Override
    public boolean isAutoRangingMeasure(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return devices.get(channel).isAutoRangingMeasure();
    }

    @Override
    public void setVoltageRange(int channel, double value) throws DeviceException, IOException {
        checkChannel(channel);
        devices.get(channel).setVoltageRange(value);
    }

    @Override
    public double getVoltageRange(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return devices.get(channel).getVoltageRange();
    }

    @Override
    public void useAutoVoltageRange(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        devices.get(channel).useAutoVoltageRange();
    }

    @Override
    public boolean isAutoRangingVoltage(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return devices.get(channel).isAutoRangingVoltage();
    }

    @Override
    public void setCurrentRange(int channel, double value) throws DeviceException, IOException {
        checkChannel(channel);
        devices.get(channel).setCurrentRange(value);
    }

    @Override
    public double getCurrentRange(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return devices.get(channel).getCurrentRange();
    }

    @Override
    public void useAutoCurrentRange(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        devices.get(channel).useAutoCurrentRange();
    }

    @Override
    public boolean isAutoRangingCurrent(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return devices.get(channel).isAutoRangingCurrent();
    }

    @Override
    public void setOutputLimit(int channel, double value) throws DeviceException, IOException {
        checkChannel(channel);
        devices.get(channel).setOutputLimit(value);
    }

    @Override
    public double getOutputLimit(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return devices.get(channel).getOutputLimit();
    }

    @Override
    public void setVoltageLimit(int channel, double value) throws DeviceException, IOException {
        checkChannel(channel);
        devices.get(channel).setVoltageLimit(value);
    }

    @Override
    public double getVoltageLimit(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return devices.get(channel).getVoltageLimit();
    }

    @Override
    public void setCurrentLimit(int channel, double value) throws DeviceException, IOException {
        checkChannel(channel);
        devices.get(channel).setCurrentLimit(value);
    }

    @Override
    public double getCurrentLimit(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return devices.get(channel).getCurrentLimit();
    }

    @Override
    public void setIntegrationTime(int channel, double time) throws DeviceException, IOException {
        checkChannel(channel);
        devices.get(channel).setIntegrationTime(time);
    }

    @Override
    public double getIntegrationTime(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return devices.get(channel).getIntegrationTime();
    }

    @Override
    public TType getTerminalType(int channel, Terminals terminals) throws DeviceException, IOException {
        checkChannel(channel);
        return devices.get(channel).getTerminalType(terminals);
    }

    @Override
    public void setTerminals(int channel, Terminals terminals) throws DeviceException, IOException {
        checkChannel(channel);
        devices.get(channel).setTerminals(terminals);
    }

    @Override
    public Terminals getTerminals(int channel) throws DeviceException, IOException {
        checkChannel(channel);
        return devices.get(channel).getTerminals();
    }

    @Override
    public void setOffMode(int channel, OffMode mode) throws DeviceException, IOException {

    }

    @Override
    public OffMode getOffMode(int channel) throws DeviceException, IOException {
        return null;
    }

    public IVPoint[] doSweep(int channel, Source source, double[] values, long delay, boolean symmetric, ProgressMonitor onUpdate) throws DeviceException, IOException {
        return devices.get(channel).doSweep(source, values, delay, symmetric, onUpdate);
    }

    public Iterator<SMU> iterator() {
        return devices.iterator();
    }

    @Override
    public String getIDN() throws IOException {
        return null;
    }

    @Override
    public void close() throws IOException, DeviceException {

    }

    @Override
    public Address getAddress() {
        return null;
    }
}
