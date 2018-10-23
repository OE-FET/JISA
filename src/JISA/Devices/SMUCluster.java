package JISA.Devices;

import JISA.Experiment.IVPoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Class to combine multiple SMUs into a single virtual SMU with multiple channels.
 */
public class SMUCluster extends MCSMU {

    private ArrayList<SMU> devices = new ArrayList<>();

    /**
     * Creates an SMUCluster using the given SMU objects.
     *
     * @param smus SMUs to combine
     *
     * @throws IOException Upon communications error
     */
    public SMUCluster(SMU... smus) throws IOException {
        super(null);
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

    @Override
    public void useAverage(int channel, boolean use) throws DeviceException, IOException {

    }

    @Override
    public boolean isAverageUsed(int channel) throws DeviceException, IOException {
        return false;
    }

    public IVPoint[] doSweep(int channel, Source source, double[] values, long delay, boolean symmetric, ProgressMonitor onUpdate) throws DeviceException, IOException {
        return devices.get(channel).doSweep(source, values, delay, symmetric, onUpdate);
    }

    public Iterator<SMU> iterator() {
        return devices.iterator();
    }

}
