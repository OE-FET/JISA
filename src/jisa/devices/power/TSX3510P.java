package jisa.devices.power;

import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.visa.VISADevice;

import java.io.IOException;

/**
 * TSX3510P driver contributed by @uoiah (https://github.com/uoiah)
 */
public class TSX3510P extends VISADevice implements DCPower {

    public static String getDescription() {
        return "AIM-TTI TSX3510P";
    }

    private boolean on = false;

    public TSX3510P(Address address) throws IOException, DeviceException {

        super(address);

        configGPIB(gpib -> gpib.setEOIEnabled(false));

        setWriteTerminator("\n");
        setReadTerminator(0xA);
        addAutoRemove("\n");
        addAutoRemove("^END");

        turnOff();

    }

    @Override
    public double getSetCurrent() throws DeviceException, IOException
    {
        throw new DeviceException("Not implemented");
    }

    @Override
    public double getSetVoltage() throws DeviceException, IOException
    {
        throw new DeviceException("Not implemented");
    }

    @Override
    public void turnOn() throws IOException, DeviceException {
        write("OP 1");
        on = true;
    }

    @Override
    public void turnOff() throws IOException, DeviceException {
        write("OP 0");
        on = false;
    }

    @Override
    public boolean isOn() throws IOException, DeviceException {
        return on;
    }

    @Override
    public void setVoltage(double voltage) throws IOException {
        write("V %e", voltage);
    }

    @Override
    public void setCurrent(double current) throws IOException {
        write("I %e", current);
    }

    @Override
    public double getVoltage() throws IOException {
        return Double.parseDouble(query("V?").substring(2));
    }

    @Override
    public double getCurrent() throws IOException {
        return Double.parseDouble(query("I?").substring(2));
    }

    public void setVoltageLimit(double voltage) throws IOException {
        write("OVP %e", voltage);
    }

    public double getVoltageLimit() throws IOException {
        return Double.parseDouble(query("OVP?").substring(4));
    }

    @Override
    public void setCurrentLimit(double current) throws IOException, DeviceException
    {
        throw new DeviceException("Device not available");
    }

    public void setDeltaI(double current) throws IOException {
        write("DELTAI %e", current);
    }

    public void increaseI() throws IOException {
        write("INCI");
    }

    public void decreaseI() throws IOException {
        write("DECI");
    }

}