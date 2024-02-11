package jisa.devices.smu;

import jisa.Util;
import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.enums.TType;
import jisa.enums.Terminals;

import java.io.IOException;

public class K6430 extends KeithleySCPI {

    public static String getDescription() {
        return "Keithley 6430 SMU";
    }

    public K6430(Address address) throws IOException, DeviceException {

        super(address);

        if (!getIDN().contains("MODEL 6430")) {
            throw new DeviceException("Instrument at \"%s\" is not a Keithley 6430", address.toString());
        }

        write(":SENS:FUNC:CONC OFF");

    }

    @Override
    public double getSetCurrent() throws DeviceException, IOException {
        return queryDouble(":SOUR:CURR?");
    }

    @Override
    public double getSetVoltage() throws DeviceException, IOException {
        return queryDouble(":SOUR:VOLT?");
    }

    @Override
    public TType getTerminalType(Terminals terminals) {

        switch (terminals) {

            case FRONT:
                return TType.NONE;

            default:
            case REAR:
                return TType.BANANA;

        }

    }

    public void setTerminals(Terminals terminals) {}

    public Terminals getTerminals() {
        return Terminals.REAR;
    }


    @Override
    public void setOffMode(OffMode mode) throws IOException {

        switch (mode) {

            case NORMAL:
                write(C_SET_OFF_STATE, OFF_NORMAL);
                break;

            case ZERO:
                write(C_SET_OFF_STATE, OFF_ZERO);
                break;

            case GUARD:
                write(C_SET_OFF_STATE, OFF_GUARD);
                break;

            case HIGH_IMPEDANCE:
                Util.errLog.println("Keithley 6430 SMUs do not have a HIGH_IMPEDANCE off-mode. Switching to NORMAL instead.");
                write(C_SET_OFF_STATE, OFF_NORMAL);
                break;

        }

    }

    @Override
    public boolean isLineFilterEnabled() throws DeviceException, IOException {
        return false;
    }

    @Override
    public void setLineFilterEnabled(boolean enabled) throws DeviceException, IOException {

    }

    protected double measureVoltage() throws IOException {
        write(":SENS:FUNC \"VOLT\"");
        write(":FORMAT:ELEMENTS VOLT");
        return isOn() ? queryDouble(":READ?") : 0.0;
    }

    protected double measureCurrent() throws IOException {
        write(":SENS:FUNC \"CURR\"");
        write(":FORMAT:ELEMENTS CURR");
        return isOn() ? queryDouble(":READ?") : 0.0;
    }

    protected void disableAveraging() throws IOException {

        write(":AVER:AUTO OFF");
        write(":AVER:REPEAT OFF");
        write(":AVER OFF");
        write(":AVER:ADV OFF");
        write(":MED OFF");

    }

    public void setIntegrationTime(double seconds) throws IOException {

        write(":NPLC %e", seconds * LINE_FREQUENCY);

        // K6430 seems to need a bit of extra time to adjust after changing its integration time while output is on
        if (isOn()) {
            Util.sleep((long) (seconds * 1e3));
            queryDouble(":READ?");
        }

    }

    @Override
    public String getName() {
        return "Keithley 6430 SMU";
    }

    public void setFourProbeEnabled(boolean flag) throws IOException {
        write(":SYST:RSENSE %s", flag ? OUTPUT_ON : OUTPUT_OFF);
    }

    public boolean isFourProbeEnabled() throws IOException {
        return query(":SYST:RSENSE?").equals(OUTPUT_ON);
    }

    public Object getLockObject() {
        return this;
    }

}
