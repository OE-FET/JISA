package JISA.Devices;

import JISA.Addresses.InstrumentAddress;
import JISA.Control.Asynch;
import JISA.Control.ERunnable;
import JISA.Control.SRunnable;
import JISA.VISA.VISADevice;

import java.io.IOException;

public class K2200 extends VISADevice {

    private static final String C_QUERY_VOLTAGE = "MEASURE:VOLTAGE:DC?";
    private static final String C_QUERY_CURRENT = "MEASURE:CURRENT:DC?";
    private static final String C_SET_VOLTAGE   = "VOLTAGE %f";
    private static final String C_SET_CURRENT   = "CURRENT %f";
    private static final String C_SET_OUTPUT    = "OUTPUT %s";
    private static final String OUTPUT_ON       = "ON";
    private static final String OUTPUT_OFF      = "OFF";

    public K2200(InstrumentAddress address) throws IOException, DeviceException {
        super(address);

        try {
            String[] idn = query("*IDN?").split(",");

            if (!idn[1].trim().equals("2200-30-5")) {
                throw new DeviceException("Device at address %s is not a Keithley 2200-30-5!", address.getVISAAddress());
            }
        } catch (IOException e) {
            throw new DeviceException("Device at address %s is not responding!", address.getVISAAddress());
        }

    }

    public double getVoltage() throws IOException {
        return Double.parseDouble(query(C_QUERY_VOLTAGE));
    }

    public double getCurrent() throws IOException {
        return Double.parseDouble(query(C_QUERY_CURRENT));
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

    public void onStableVoltage(double voltage, SRunnable action, ERunnable onException) {

        Asynch.onParamWithinError(
                () -> getVoltage(),
                voltage,
                10,
                5000,
                100,
                action,
                onException
        );

    }

}
