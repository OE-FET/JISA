package JPIB;

import java.io.IOException;

public interface SMU {

    public double getVoltage() throws IOException;
    public double getCurrent() throws IOException;
    public void setVoltage(double voltage) throws IOException;
    public void setCurrent(double current) throws IOException;
    public void turnOn() throws IOException;
    public void turnOff() throws IOException;


}
