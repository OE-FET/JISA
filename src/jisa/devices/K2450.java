package jisa.devices;

import jisa.addresses.Address;
import jisa.enums.TType;
import jisa.enums.Terminals;

import java.io.IOException;

public class K2450 extends KeithleySCPI {

    public static String getDescription() {
        return "Kiethley 2450";
    }

    protected static final String C_SET_LIMIT_2450   = ":SOUR:%s:%sLIM %e";
    protected static final String C_QUERY_LIMIT_2450 = ":SOUR:%s:%sLIM?";

    public K2450(Address address) throws IOException, DeviceException {

        super(address);

        String idn = getIDN();

        if (!idn.contains("MODEL 2450")) {
            throw new DeviceException("Instrument at address \"%s\" is not a Keithley 2450.", address.toString());
        }

        addAutoRemove("\n");

    }

    protected double measureVoltage() throws IOException, DeviceException {

        double val = super.measureVoltage();

        if (isLimitTripped() && isAutoRangingVoltage()) {
            setVoltageLimit(getVoltageLimit()/2);
            setVoltageLimit(getVoltageLimit()*2);
            val = super.measureVoltage();
        }

        return val;

    }

    protected double measureCurrent() throws IOException, DeviceException {

        double val = super.measureCurrent();

        if (isLimitTripped() && isAutoRangingCurrent()) {
            setCurrentLimit(getCurrentLimit()/2);
            setCurrentLimit(getCurrentLimit()*2);
            val = super.measureCurrent();
        }

        return val;

    }

    @Override
    public String getChannelName() {
        return "Main Channel";
    }

    @Override
    public double getOutputLimit() throws IOException {
        return queryDouble(C_QUERY_LIMIT_2450, getSourceMode().getTag(), getMeasureMode().getSymbol());
    }

    @Override
    public void setVoltageLimit(double voltage) throws IOException {
        write(C_SET_LIMIT_2450, Source.CURRENT.getTag(), Source.VOLTAGE.getSymbol(), voltage);
        vLimit = voltage;
    }

    @Override
    public double getVoltageLimit() throws IOException {
        return queryDouble(C_QUERY_LIMIT_2450, Source.CURRENT.getTag(), Source.VOLTAGE.getSymbol());
    }

    @Override
    public void setCurrentLimit(double current) throws IOException {
        write(C_SET_LIMIT_2450, Source.VOLTAGE.getTag(), Source.CURRENT.getSymbol(), current);
        iLimit = current;
    }

    @Override
    public double getCurrentLimit() throws IOException {
        return queryDouble(C_QUERY_LIMIT_2450, Source.VOLTAGE.getTag(), Source.CURRENT.getSymbol());
    }


    @Override
    public TType getTerminalType(Terminals terminals) throws DeviceException {

        switch (terminals) {

            case FRONT:
                return TType.BANANA;

            case REAR:
                return TType.TRIAX;

            default:
                throw new DeviceException("The Keithley 2450 does not have terminals at \"%s\"", terminals.name());

        }

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
    public boolean isLineFilterEnabled() {
        return false;
    }

    @Override
    public void setLineFilterEnabled(boolean enabled) {
        System.err.println("Keithley 2450s do not have a line-filter feature.");
    }

    public boolean isLimitTripped() throws IOException {
        boolean iTrip = query(":SOUR:VOLT:iLIM:TRIP?").equals(OUTPUT_ON);
        boolean vTrip = query(":SOUR:CURR:vLIM:TRIP?").equals(OUTPUT_ON);
        return iTrip || vTrip;
    }

    public void setSourceValue(Source type, double value) throws IOException, DeviceException {
        super.setSourceValue(type, value);

    }

    public void fixLimits() throws IOException, DeviceException {

        if (isAutoRangingMeasure()) {

            getMeasureValue();

            if (isLimitTripped()) {
                setOutputLimit(getOutputLimit() / 2);
                setOutputLimit(getOutputLimit() * 2);
                getMeasureValue();
            }

        }

    }

}
