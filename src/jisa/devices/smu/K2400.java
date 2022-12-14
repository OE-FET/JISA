package jisa.devices.smu;

import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.enums.TType;
import jisa.enums.Terminals;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class K2400 extends KeithleySCPI {

    public static String getDescription() {
        return "Keithley 2400";
    }

    public K2400(Address address) throws IOException, DeviceException {

        super(address);

        String  idn     = getIDN();
        Matcher matcher = Pattern.compile("MODEL (2400|2410|2420|2425|2430|2440)").matcher(idn.toUpperCase());

        if (!matcher.find()) {
            throw new DeviceException("Instrument at address \"%s\" is not a Keithley 2400, 2410, 2420, 2425, 2430 or 2440.", address.toString());
        }

    }

    @Override
    public double getSetCurrent() throws DeviceException, IOException {
        throw new DeviceException("Not implemented.");
    }

    @Override
    public double getSetVoltage() throws DeviceException, IOException {
        throw new DeviceException("Not implemented.");
    }

    @Override
    public String getName() {
        return "Keithley 2400 SMU";
    }

    @Override
    public TType getTerminalType(Terminals terminals) {
        return TType.BANANA;
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

            case HIGH_IMPEDANCE:
                write(C_SET_OFF_STATE, OFF_HIGH_Z);
                break;

            case GUARD:
                write(C_SET_OFF_STATE, OFF_GUARD);
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

}
