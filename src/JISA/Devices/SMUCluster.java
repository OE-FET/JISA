package JISA.Devices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class SMUCluster extends MCSMU {

    private ArrayList<SMU> devices = new ArrayList<>();

    public SMUCluster(SMU... smus) throws IOException {
        super();
        devices.addAll(Arrays.asList(smus));
    }

    public void add(SMU device) {
        devices.add(device);
    }

    public void add(MCSMU device, int channel) throws DeviceException {
        devices.add(device.getChannel(channel));
    }

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

    public SMU.DataPoint[] doSweep(int channel, Source source, double[] values, long delay, boolean symmetric, ProgressMonitor onUpdate) throws DeviceException, IOException {
        return devices.get(channel).doSweep(source, values, delay, symmetric, onUpdate);
    }

    public Iterator<SMU> iterator() {
        return devices.iterator();
    }

}
