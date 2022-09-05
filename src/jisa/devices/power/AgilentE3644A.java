package jisa.devices.power;

import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.DCPower;
import jisa.visa.VISADevice;
import jisa.visa.connections.Connection;
import jisa.visa.connections.GPIBConnection;

import java.io.IOException;

public class AgilentE3644A extends VISADevice implements DCPower {

    public static String getDescription() {
        return "Agilent E3644A";
    }

    public AgilentE3644A(Address address) throws IOException, DeviceException {

        super(address);

        setReadTerminator("\n");
        setWriteTerminator("\n");

        Connection connection = getConnection();

        if (connection instanceof GPIBConnection) {
            ((GPIBConnection) connection).setEOIEnabled(false);
        }

        if (!getIDN().contains(",E3644A,")) {
            throw new DeviceException("Instrument at \"%s\" is not an Agilent E3644A.", address.toString());
        }

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
        write("OUTPUT:STATE ON");
    }

    @Override
    public void turnOff() throws IOException, DeviceException {
        write("OUTPUT:STATE OFF");
    }

    @Override
    public boolean isOn() throws IOException, DeviceException {

        switch(queryInt("OUTPUT:STATE?")) {

            case 0:
                return false;

            case 1:
                return true;

            default:
                throw new IOException("Invalid response from Agilent E3644A.");

        }

    }

    @Override
    public void setVoltage(double voltage) throws IOException, DeviceException {

        if (voltage > 8.0) {
            write("VOLTAGE:RANGE HIGH");
        }

        write("VOLTAGE %e", voltage);
    }

    @Override
    public void setCurrent(double current) throws IOException, DeviceException {

        if (current > 4.0) {
            write("VOLTAGE:RANGE LOW");
        }

        write("CURRENT %e", current);

    }

    @Override
    public double getVoltage() throws IOException, DeviceException {
        return queryDouble("MEASURE:VOLTAGE?");
    }

    @Override
    public double getCurrent() throws IOException, DeviceException {
        return queryDouble("MEASURE:CURRENT?");
    }

    @Override
    public void setVoltageLimit(double limit) throws IOException, DeviceException {
        write("VOLTAGE:PROTECTION:LEVEL %e", limit);
    }

    @Override
    public double getVoltageLimit() throws IOException, DeviceException {
        return queryDouble("VOLTAGE:PROTECTION:LEVEL?");
    }

    @Override
    public void setCurrentLimit(double current) throws IOException, DeviceException
    {
        throw new DeviceException("Device not available");
    }
}
