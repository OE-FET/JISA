package JISA.Devices;

import JISA.Addresses.Address;

import java.io.IOException;
import java.security.Guard;

public class K2450 extends KeithleySCPI {

    protected static final String C_SET_LIMIT_2450        = ":SOUR:%s:%sLIM %e";
    protected static final String C_QUERY_LIMIT_2450      = ":SOUR:%s:%sLIM?";

    public K2450(Address address) throws IOException, DeviceException {

        super(address);

        String idn = getIDN();

        if (!idn.contains("MODEL 2450")) {
            throw new DeviceException("Instrument at address \"%s\" is not a Keithley 2450.", address.toString());
        }

    }

    @Override
    public void setOutputLimit(double value) throws IOException {
        write(C_SET_LIMIT_2450, getSourceMode().getTag(), getMeasureMode().getSymbol(), value);
    }

    @Override
    public double getOutputLimit() throws IOException {
        return queryDouble(C_QUERY_LIMIT_2450, getSourceMode().getTag(), getMeasureMode().getSymbol());
    }

    @Override
    public void setVoltageLimit(double voltage) throws IOException {
        write(C_SET_LIMIT_2450, Source.CURRENT.getTag(), Source.VOLTAGE.getSymbol(), voltage);
    }

    @Override
    public double getVoltageLimit() throws IOException {
        return queryDouble(C_QUERY_LIMIT_2450, Source.CURRENT.getTag(), Source.VOLTAGE.getSymbol());
    }

    @Override
    public void setCurrentLimit(double current) throws IOException {
        write(C_SET_LIMIT_2450, Source.VOLTAGE.getTag(), Source.CURRENT.getSymbol(), current);
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
    public void setOffMode(OffMode mode) throws DeviceException, IOException {

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

}
