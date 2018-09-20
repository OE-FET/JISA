package JISA.Devices;

import JISA.Addresses.InstrumentAddress;
import sun.nio.cs.ext.MS874;

import java.io.IOException;

public abstract class MCSMU extends SMU {

    public MCSMU(InstrumentAddress address) throws IOException {
        super(address);
    }

    public abstract double getVoltage(int channel) throws DeviceException, IOException;

    public abstract double getCurrent(int channel) throws DeviceException, IOException;

    public abstract double setVoltage(int channel, double voltage) throws DeviceException, IOException;

    public abstract double setCurrent(int channel, double voltage) throws DeviceException, IOException;

    public abstract double turnOn(int channel) throws DeviceException, IOException;

    public abstract double turnOff(int channel) throws DeviceException, IOException;

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

}
