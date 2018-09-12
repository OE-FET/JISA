package JISA.Devices;

import java.io.IOException;

public interface SMU {

    public double getVoltage() throws IOException;
    public double getCurrent() throws IOException;
    public void setVoltage(double voltage) throws IOException;
    public void setCurrent(double current) throws IOException;
    public void turnOn() throws IOException;
    public void turnOff() throws IOException;
    public void setSource(Source source) throws IOException;
    public Source getSource() throws IOException;

    public enum Source {
        VOLTAGE,
        CURRENT
    }

}
