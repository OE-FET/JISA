package jisa.devices;

import jisa.addresses.Address;
import jisa.enums.TType;
import jisa.enums.Terminals;
import jisa.Util;

import java.io.IOException;

public class K6430 extends KeithleySCPI {

    public K6430(Address address) throws IOException, DeviceException {

        super(address);

        if (!getIDN().contains("MODEL 6430")) {
            throw new DeviceException("Instrument at \"%s\" is not a Keithley 6430", address.toString());
        }

        setRemoveTerminator("\r");

        write(":SENS:FUNC:CONC OFF");

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

    protected double measureVoltage() throws IOException {
        write(":SENS:FUNC \"VOLT\"");
        write(":FORMAT:ELEMENTS VOLT");
        return isOn() ? queryDouble(":READ?") : queryDouble(":FETCH?");
    }

    protected double measureCurrent() throws IOException {
        write(":SENS:FUNC \"CURR\"");
        write(":FORMAT:ELEMENTS CURR");
        return isOn() ? queryDouble(":READ?") : queryDouble(":FETCH?");
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
    }

    public void useFourProbe(boolean flag) throws IOException {
        write(":SYST:RSENSE %s", flag ? OUTPUT_ON : OUTPUT_OFF);
    }

    public boolean isUsingFourProbe() throws IOException {
        return query(":SYST:RSENSE?").equals(OUTPUT_ON);
    }

}