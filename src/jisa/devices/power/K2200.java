package jisa.devices.power;

import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.DCPower;
import jisa.visa.VISADevice;

import java.io.IOException;

public class K2200 extends VISADevice implements DCPower {

    public static String getDescription() {
        return "Keithley 2200";
    }

    private static final String C_QUERY_VOLTAGE = "MEASURE:VOLTAGE:DC?";
    private static final String C_QUERY_CURRENT = "MEASURE:CURRENT:DC?";
    private static final String C_SET_VOLTAGE   = "VOLTAGE %f";
    private static final String C_SET_CURRENT   = "CURRENT %f";
    private static final String C_SET_OUTPUT    = "OUTPUT %s";
    private static final String C_QUERY_OUTPUT  = "OUTPUT?";
    private static final String C_SET_REMOTE    = "SYSTEM:REMOTE";
    private static final String OUTPUT_ON       = "1";
    private static final String OUTPUT_OFF      = "0";

    public K2200(Address address) throws IOException, DeviceException {

        super(address);

        manuallyClearReadBuffer();

        try {

            String[] idn = query("*IDN?").split(",");

            if (!idn[1].trim().equals("2200-30-5")) {
                throw new DeviceException("Device at address %s is not a Keithley 2200-30-5!", address.toString());
            }

            write(C_SET_REMOTE);

        } catch (IOException e) {

            throw new DeviceException("Device at address %s is not responding!", address.toString());

        }

    }

    @Override
    public double getSetCurrent() throws DeviceException, IOException {
        throw new DeviceException("Not implemented");
    }

    @Override
    public double getSetVoltage() throws DeviceException, IOException {
        throw new DeviceException("Not implemented");
    }

    public double getVoltage() throws IOException {
        return queryDouble(C_QUERY_VOLTAGE);
    }

    public double getCurrent() throws IOException {
        return queryDouble(C_QUERY_CURRENT);
    }

    @Override
    public void setVoltageLimit(double limit) throws IOException {

        if (limit > 0) {
            write("VOLT:PROP:STAT ON");
            write("VOLT:PROT %eV", limit);
        } else {
            write("VOLT:PROP:STAT OFF");
        }

    }

    @Override
    public double getVoltageLimit() throws IOException, DeviceException {
        return queryInt("VOLT:PROP:STAT?") == 1 ? queryDouble("VOLT:PROP:LEVEL?") : 0;
    }

    @Override
    public void setCurrentLimit(double current) throws IOException, DeviceException {
        throw new DeviceException("Device not available");
    }

    public void setVoltage(double voltage) throws IOException {
        write(C_SET_VOLTAGE, voltage);
    }

    public void setCurrent(double current) throws IOException {
        write(C_SET_CURRENT, current);
    }

    public void turnOn() throws IOException {
        write(C_SET_OUTPUT, OUTPUT_ON);
    }

    public void turnOff() throws IOException {
        write(C_SET_OUTPUT, OUTPUT_OFF);
    }

    public boolean isOn() throws IOException {
        return query(C_QUERY_OUTPUT).equals(OUTPUT_ON);
    }

}
