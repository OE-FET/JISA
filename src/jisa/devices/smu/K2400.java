package jisa.devices.smu;

import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.SMU;
import jisa.enums.AMode;
import jisa.enums.Source;
import jisa.enums.TType;
import jisa.enums.Terminals;
import jisa.visa.Connection;
import jisa.visa.VISADevice;

import java.io.IOException;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class K2400 extends VISADevice implements SMU {

    private static final String OUTPUT_ON  = "1";
    private static final String OUTPUT_OFF = "0";

    private static final String C_SET_SOURCE_FUNCTION         = ":SOUR:FUNC %s";
    private static final String C_SET_OUTPUT_STATE            = ":OUTP:STATE %s";
    private static final String C_QUERY_SOURCE_FUNCTION       = ":SOUR:FUNC?";
    private static final String C_QUERY_OUTPUT_STATE          = ":OUTP:STATE?";
    private static final String C_SET_LIMIT                   = ":SENS:%s:PROT %e";
    private static final String C_QUERY_LIMIT                 = ":SENS:%s:PROT?";
    private static final String C_SET_SOURCE_VALUE            = ":SOUR:%s:LEV %e";
    private static final String C_SET_READ_OPTION             = ":FORM:ELEM %s";
    private static final String C_QUERY_READING               = "READ?";
    private static final String C_SET_MEASUREMENT_FUNCTION    = ":SENS:FUNC \"%s\"";
    private static final String C_QUERY_MEASUREMENT_FUNCTIONS = ":SENS:FUNC?";
    private static final String C_QUERY_LFR                   = ":SYST:LFR?";

    private static final String VOLTAGE_OPTION = "VOLT";
    private static final String CURRENT_OPTION = "CURR";



    private final double LINE_FREQUENCY;

    // Filter related. Some filters can be implemented, but it is not implemented here.
    // The values set here are the default values after a reset.
    // private final AMode filterMode = AMode.NONE;
    // private final int filterCount = 0;


    public static String getDescription() {
        return "Keithley 2400";
    }

    public K2400(Address address) throws IOException, DeviceException {

        super(address);
        enableLogger("K2400", null);

        String  idn     = getIDN();
        Matcher matcher = Pattern.compile("MODEL (2400|2410|2420|2425|2430|2440)").matcher(idn.toUpperCase());

        if (!matcher.find()) {
            throw new DeviceException("Instrument at address \"%s\" is not a Keithley 2400, 2410, 2420, 2425, 2430 or 2440.", address.toString());
        }

        switch(address.getType()) {

            case SERIAL:
                setSerialParameters(9600, 8, Connection.Parity.NONE, Connection.StopBits.ONE, Connection.Flow.NONE);
                setWriteTerminator("\r");
                setReadTerminator("\r");
                break;

            case GPIB:
                setEOI(true);
                break;

        }

        addAutoRemove("\r");
        addAutoRemove("\n");

        reset();

        write(":SYSTEM:CLEAR");

        LINE_FREQUENCY = queryDouble(C_QUERY_LFR);
    }

    // On off control
    @Override
    public void turnOn() throws DeviceException, IOException {
        write(C_SET_OUTPUT_STATE, OUTPUT_ON);
    }

    @Override
    public void turnOff() throws DeviceException, IOException {
        write(C_SET_OUTPUT_STATE, OUTPUT_OFF);
    }

    @Override
    public boolean isOn() throws DeviceException, IOException {
        return query(C_QUERY_OUTPUT_STATE).trim().equals(OUTPUT_ON);
    }

    @Override
    public void setFourProbeEnabled(boolean fourProbe) throws IOException {
        // an override is needed because the commands are slightly different!!
        if (fourProbe)
            write(":SYST:RSEN ON");
        else
            write(":SYST:RSEN OFF");
    }

    @Override
    public boolean isFourProbeEnabled() throws IOException {
        // an override is needed because the commands are slightly different!!
        return query(":SYST:RSEN?").trim().equals(OUTPUT_ON);
    }

    @Override
    public boolean isLineFilterEnabled() throws DeviceException, IOException {
        return false;
    }

    @Override
    public void setLineFilterEnabled(boolean enabled) throws DeviceException, IOException {
        addLog(Level.WARNING, "No line filter available!");
    }

    @Override
    public String getChannelName() {
        return "Keithley 2400 SMU";
    }

    @Override
    public double getVoltage() throws DeviceException, IOException {
        if (!isOn()) {
            addLog(Level.INFO, "Attempts to measure voltage when the output is off.");
            return 0;
        }
        if (!query(C_QUERY_MEASUREMENT_FUNCTIONS).contains(VOLTAGE_OPTION))
            throw new DeviceException("Attempts to measure voltage when current sensing is not enabled!");

        write(C_SET_READ_OPTION, VOLTAGE_OPTION);
        return queryDouble(C_QUERY_READING);
    }

    @Override
    public void setVoltage(double voltage) throws DeviceException, IOException {
        if (!(getSource() == Source.VOLTAGE)){
            throw new DeviceException("Setting voltage when not sourcign voltage is not allowed!");
        }
        if (Math.abs(voltage) > 20)
            write(":SOUR:VOLT:RANG MAX");
        write(C_SET_SOURCE_VALUE, VOLTAGE_OPTION, voltage);

    }

    @Override
    public double getCurrent() throws DeviceException, IOException {
        if (!isOn()) {
            addLog(Level.INFO, "Attempts to measure current when the output is off.");
            return 0;
        }
        if (!query(C_QUERY_MEASUREMENT_FUNCTIONS).contains(CURRENT_OPTION))
            throw new DeviceException("Attempts to measure current when current sensing is not enabled!");

        write(C_SET_READ_OPTION, CURRENT_OPTION);
        return queryDouble(C_QUERY_READING);
    }

    @Override
    public void setCurrent(double current) throws DeviceException, IOException {
        write(C_SET_SOURCE_VALUE, CURRENT_OPTION, current);
    }

    @Override
    public void setSource(Source mode) throws IOException, DeviceException {
        if (mode == Source.VOLTAGE) {
            write(C_SET_SOURCE_FUNCTION, VOLTAGE_OPTION);
            write(C_SET_MEASUREMENT_FUNCTION, VOLTAGE_OPTION);
            write(C_SET_MEASUREMENT_FUNCTION, CURRENT_OPTION);
            return;
        }
        if (mode == Source.CURRENT){
            write(C_SET_SOURCE_FUNCTION, CURRENT_OPTION);
            write(C_SET_MEASUREMENT_FUNCTION, VOLTAGE_OPTION);
            write(C_SET_MEASUREMENT_FUNCTION, CURRENT_OPTION);
            return;
        }
        throw new DeviceException("Source mode not supported!");
    }

    @Override
    public Source getSource() throws IOException {
        if (query(C_QUERY_SOURCE_FUNCTION).contains(VOLTAGE_OPTION))
            return Source.VOLTAGE;
        if (query(C_QUERY_SOURCE_FUNCTION).contains(VOLTAGE_OPTION))
            return Source.CURRENT;
        addLog(Level.INFO, "Source mode not recognized.");
        return null;
    }

    // functions that are not implemented

    @Override
    public AMode getAverageMode() throws DeviceException, IOException {
        throw new DeviceException("Not implemented!");
    }

    @Override
    public void setAverageMode(AMode mode) throws DeviceException, IOException {
        throw new DeviceException("No Implementation!");
    }

    @Override
    public int getAverageCount() throws DeviceException, IOException {
        throw new DeviceException("Not implemented!");
    }

    @Override
    public void setAverageCount(int count) throws DeviceException, IOException {
        throw new DeviceException("No Implementation!");
    }

    @Override
    public double getOutputLimit() throws DeviceException, IOException {
        throw new DeviceException("Not implemented!");
    }

    @Override
    public void setOutputLimit(double value) throws DeviceException, IOException {
        throw new DeviceException("Not implemented!");
    }

    @Override
    public double getSetCurrent() throws DeviceException, IOException
    {
        throw new DeviceException("Not implemented.");
    }

    @Override
    public double getSetVoltage() throws DeviceException, IOException
    {
        throw new DeviceException("Not implemented.");
    }


    @Override
    public void setSourceValue(double level) throws DeviceException, IOException {
        throw new DeviceException("Not implemented!");
    }

    @Override
    public double getSourceValue() throws DeviceException, IOException {
        throw new DeviceException("Not implemented!");
    }

    @Override
    public double getMeasureValue() throws DeviceException, IOException {
        throw new DeviceException("Not implemented!");
    }

    @Override
    public TType getTerminalType(Terminals terminals) {
        return TType.BANANA;
    }

    @Override
    public Terminals getTerminals() throws DeviceException, IOException {
        throw new DeviceException("Not implemented!");
    }

    @Override
    public void setTerminals(Terminals terminals) throws DeviceException, IOException {
        throw new DeviceException("Not implemented!");
    }

    @Override
    public OffMode getOffMode() throws DeviceException, IOException {
        throw new DeviceException("Not implemented!");
    }

    @Override
    public void setOffMode(OffMode mode) throws IOException, DeviceException {
        throw new DeviceException("Not implemented!");

//        switch (mode) {
//
//            case NORMAL:
//                write(C_SET_OFF_STATE, OFF_NORMAL);
//                break;
//
//            case ZERO:
//                write(C_SET_OFF_STATE, OFF_ZERO);
//                break;
//
//            case HIGH_IMPEDANCE:
//                write(C_SET_OFF_STATE, OFF_HIGH_Z);
//                break;
//
//            case GUARD:
//                write(C_SET_OFF_STATE, OFF_GUARD);
//                break;
//
//        }

    }

    @Override
    public double getSourceRange() throws DeviceException, IOException {
        throw new DeviceException("Not implemented!");
    }

    @Override
    public void setSourceRange(double value) throws DeviceException, IOException {
        throw new DeviceException("Not implemented!");
    }

    @Override
    public void useAutoSourceRange() throws DeviceException, IOException {
        throw new DeviceException("Not implemented!");
    }

    @Override
    public boolean isAutoRangingSource() throws DeviceException, IOException {
        throw new DeviceException("Not implemented!");
    }

    @Override
    public double getMeasureRange() throws DeviceException, IOException {
        throw new DeviceException("Not implemented!");
    }

    @Override
    public void setMeasureRange(double value) throws DeviceException, IOException {
        throw new DeviceException("Not implemented!");
    }

    @Override
    public void useAutoMeasureRange() throws DeviceException, IOException {

    }

    @Override
    public boolean isAutoRangingMeasure() throws DeviceException, IOException {
        throw new DeviceException("Not implemented!");
    }

    @Override
    public double getVoltageRange() throws DeviceException, IOException {
        throw new DeviceException("Not implemented!");
    }

    @Override
    public void setVoltageRange(double value) throws DeviceException, IOException {
        throw new DeviceException("Not implemented!");
    }

    @Override
    public void useAutoVoltageRange() throws DeviceException, IOException {
        throw new DeviceException("Not implemented!");
    }

    @Override
    public boolean isAutoRangingVoltage() throws DeviceException, IOException {
        throw new DeviceException("Not implemented!");
    }

    @Override
    public double getCurrentRange() throws DeviceException, IOException {
        throw new DeviceException("Not implemented!");
    }

    @Override
    public void setCurrentRange(double value) throws DeviceException, IOException {
        throw new DeviceException("Not implemented!");
    }

    @Override
    public void useAutoCurrentRange() throws DeviceException, IOException {
        throw new DeviceException("Not implemented!");
    }

    @Override
    public boolean isAutoRangingCurrent() throws DeviceException, IOException {
        throw new DeviceException("Not implemented!");
    }

    @Override
    public double getVoltageLimit() throws DeviceException, IOException {
        return queryDouble(C_QUERY_LIMIT, VOLTAGE_OPTION);
    }

    @Override
    public void setVoltageLimit(double voltage) throws DeviceException, IOException {
        write(C_SET_LIMIT, VOLTAGE_OPTION, voltage);
    }

    @Override
    public double getCurrentLimit() throws DeviceException, IOException {
        return queryDouble(C_QUERY_LIMIT, CURRENT_OPTION);
    }

    @Override
    public void setCurrentLimit(double current) throws DeviceException, IOException {
        write(C_SET_LIMIT, CURRENT_OPTION, current);
    }

    @Override
    public double getIntegrationTime() throws DeviceException, IOException {
        throw new DeviceException("Not implemented!");
    }

    @Override
    public void setIntegrationTime(double time) throws DeviceException, IOException {
        throw new DeviceException("Not implemented!");
    }
}
