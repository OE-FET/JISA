package JISA.Devices;

import java.io.IOException;

public interface SMU {

    public double getVoltage() throws DeviceException, IOException;

    public double getCurrent() throws DeviceException, IOException;

    public void setVoltage(double voltage) throws DeviceException, IOException;

    public void setCurrent(double current) throws DeviceException, IOException;

    public void turnOn() throws DeviceException, IOException;

    public void turnOff() throws DeviceException, IOException;

    public boolean isOn() throws DeviceException, IOException;

    public void setSource(Source source) throws DeviceException, IOException;

    public Source getSource() throws DeviceException, IOException;

    public DataPoint[] performLinearSweep(Source source, double min, double max, int numSteps, long delay) throws DeviceException, IOException;

    public enum Source {
        VOLTAGE,
        CURRENT
    }

    public class DataPoint {
        public double voltage;
        public double current;
    }

}
