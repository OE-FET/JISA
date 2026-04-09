package jisa.devices.smu;

import jisa.Util;
import jisa.addresses.Address;
import jisa.control.*;
import jisa.devices.DeviceException;
import jisa.enums.AMode;
import jisa.enums.Source;
import jisa.enums.TType;
import jisa.enums.Terminals;
import jisa.visa.VISADevice;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static jisa.devices.smu.KeithleySCPI.*;

public class K2400 extends VISADevice implements SMU {

    // === CONSTANTS ========================================================

    // MEAS COMMANDS
    protected static final String C_MEASURE_VOLTAGE           = ":MEAS:VOLT?";
    protected static final String C_MEASURE_CURRENT           = ":MEAS:CURR?";
    protected static final String C_MEASURE_RESISTANCE        = ":MEAS:RES?";
    protected static final String C_SET_SOURCE_FUNCTION       = ":SOUR:FUNC %s";
    protected static final String C_SET_OUTPUT_STATE          = ":OUTP:STATE %s";
    protected static final String C_QUERY_SOURCE_FUNCTION     = ":SOUR:FUNC?";
    protected static final String C_QUERY_OUTPUT_STATE        = ":OUTP:STATE?";
    protected static final String OUTPUT_ON                   = "1";
    protected static final String OUTPUT_OFF                  = "0";
    protected static final String TERMS_FRONT                 = "FRON";
    protected static final String TERMS_REAR                  = "REAR";
    protected static final String C_SET_SRC_RANGE             = ":SOUR:%s:RANG %e";
    protected static final String C_QUERY_SRC_RANGE           = ":SOUR:%s:RANG?";
    protected static final String C_QUERY_SRC_RANGE_MIN       = ":SOUR:%s:RANG? MIN";
    protected static final String C_QUERY_SRC_RANGE_MAX       = ":SOUR:%s:RANG? MAX";
    protected static final String C_SET_SRC_AUTO_RANGE        = ":SOUR:%s:RANG:AUTO %s";
    protected static final String C_QUERY_SRC_AUTO_RANGE      = ":SOUR:%s:RANG:AUTO?";
    protected static final String C_SET_MEAS_RANGE            = ":SENS:%s:RANG %e";
    protected static final String C_QUERY_MEAS_RANGE          = ":SENS:%s:RANG?";
    protected static final String C_QUERY_MEAS_RANGE_MIN      = ":SENS:%s:RANG? MIN";
    protected static final String C_QUERY_MEAS_RANGE_MAX      = ":SENS:%s:RANG? MAX";
    protected static final String C_SET_MEAS_AUTO_RANGE       = ":SENS:%s:RANG:AUTO %s";
    protected static final String C_QUERY_MEAS_AUTO_RANGE     = ":SENS:%s:RANG:AUTO?";
    // ========== Everything needed for getVoltage()
    protected static final String C_SET_AVG_MODE              = "AVER:TCON %s";
    protected static final String C_SET_AVG_COUNT             = "AVER:COUNT %d";
    protected static final String C_SET_AVG_STATE             = "AVER %s";
    protected static final String C_QUERY_PROBE_MODE          = "CURR:RSEN?";
    // === INTEGRATION TIME ================================
    protected static final String C_QUERY_LFR                 = ":SYST:LFR?";
    protected static final String C_QUERY_NPLC                = ":SENS:%s:NPLC?";
    private static final   String C_SET_SOURCE_MODE_CURRENT   = ":SOUR:CURR:MODE FIX";
    private static final   String C_SET_SOURCE_MODE_VOLTAGE   = ":SOUR:VOLT:MODE FIX";
    // === SET RANGES
    private static final   String C_SET_CURRENT_RANGE         = ":SOUR:CURR:RANG %f";
    private static final   String C_SET_VOLTAGE_RANGE         = ":SOUR:VOLT:RANG %f";
    private static final   String C_SET_CURRENT_LEVEL         = ":SOUR:CURR:LEV %f";
    private static final   String C_SET_VOLTAGE_LEVEL         = ":SOUR:VOLT:LEV %f";
    // Sense Function Commands
    private static final   String C_SET_SENSE_FUNCTION        = ":SENS:FUNCtion %s";
    private static final   String C_SET_CURRENT_PROTECTION    = ":SENSe:CURRent:PROTection %f";
    private static final   String C_SET_VOLTAGE_PROTECTION    = ":SENSe:VOLTage:PROTection %f";
    private static final   String C_SET_N_COUNTS_VOLTAGE      = ":SENSe:VOLTage:NPLCycles %f";
    private static final   String C_SET_N_COUNTS_CURRENT      = ":SENSe:CURRent:NPLCycles %f";
    private static final   String C_SET_CURRENT_MEASURE_RANGE = ":SENSe:CURRent:RANGe %f";
    private static final   String C_SET_VOLTAGE_MEASURE_RANGE = ":SENSe:VOLTage:RANGe %f";
    // Output Commands
    private static final   String C_TRIGGER_AND_READ          = ":READ?";

    protected final double LINE_FREQUENCY;

    // === FILTERS ============================================================================
    private final MedianRepeatFilter MEDIAN_REPEAT_V = new MedianRepeatFilter(
            this::measureVoltage,
            (c) -> disableAveraging()
    );

    private final MedianRepeatFilter MEDIAN_REPEAT_I = new MedianRepeatFilter(
            this::measureCurrent,
            (c) -> disableAveraging()
    );

    private final MedianMovingFilter MEDIAN_MOVING_V = new MedianMovingFilter(
            this::measureVoltage,
            (c) -> disableAveraging()
    );

    private final MedianMovingFilter MEDIAN_MOVING_I = new MedianMovingFilter(
            this::measureCurrent,
            (c) -> disableAveraging()
    );

    private final MeanRepeatFilter MEAN_REPEAT_V = new MeanRepeatFilter(
            this::measureVoltage,
            (c) -> disableAveraging()
    );

    private final MeanRepeatFilter MEAN_REPEAT_I = new MeanRepeatFilter(
            this::measureCurrent,
            (c) -> disableAveraging()
    );

    private final MeanMovingFilter MEAN_MOVING_V = new MeanMovingFilter(
            this::measureVoltage,
            (c) -> disableAveraging()
    );

    private final MeanMovingFilter MEAN_MOVING_I = new MeanMovingFilter(
            this::measureCurrent,
            (c) -> disableAveraging()
    );

    private final BypassFilter NONE_V = new BypassFilter(
            this::measureVoltage,
            (c) -> disableAveraging()
    );

    private final BypassFilter NONE_I = new BypassFilter(
            this::measureCurrent,
            (c) -> disableAveraging()
    );

    protected double     vLimit;
    protected double     iLimit;
    private   ReadFilter filterV = MEAN_REPEAT_V;

    // New methods for range and digits commands
    private ReadFilter filterI     = MEAN_REPEAT_I;
    private AMode      filterMode  = AMode.MEAN_REPEAT;
    private int        filterCount = 1; // This is for the averaging feature, not integration time/NPLC

    public K2400(Address address) throws IOException, DeviceException {

        super(address);

        // Set these *before* trying to ask the SMU for anything, otherwise it will just timeout
        setIOLimit(50, true, true);
        setWriteTerminator("\n");
        setReadTerminator("\n");
        addAutoRemove("\r");
        addAutoRemove("\n");

        String  idn     = getIDN();
        Matcher matcher = Pattern.compile("MODEL (2400|2410|2420|2425|2430|2440)").matcher(idn.toUpperCase());

        if (!matcher.find()) {
            throw new DeviceException("Instrument at address \"%s\" is not a Keithley 2400, 2410, 2420, 2425, 2430 or 2440.", address.toString());
        }

        LINE_FREQUENCY = queryDouble(C_QUERY_LFR);

    }

    public static String getDescription() {
        return "Keithley 2400";
    }

    // METHODS TO IMPLEMENT

    // These functions were modified as follows because query(:MEAS:<MODE>?) returns a string of 5 elements.
    // See "FETch?" command in the K2400 manual.
    public double measureVoltage() throws IOException, DeviceException {
        String   s               = query(C_MEASURE_VOLTAGE);
        String[] values          = s.split(",");
        String   measuredVoltage = "9.91E37"; // NaN value
        if (values.length >= 2) {
            measuredVoltage = values[0]; // Index 0 represents the first position (i.e., measured voltage)
            //System.out.println(measuredVoltage);
        }
        return Double.parseDouble(measuredVoltage);

    }

    public double measureCurrent() throws IOException, DeviceException {
        String   s               = query(C_MEASURE_CURRENT);
        String[] values          = s.split(",");
        String   measuredCurrent = "0.0";
        if (values.length >= 2) {
            measuredCurrent = values[1]; // Index 1 represents the first position (i.e., measured current)
            //System.out.println(measuredCurrent);
        }
        return Double.parseDouble(measuredCurrent);
    }

    private void setCurrRangeManual(double range) throws IOException {
        write(C_SET_CURRENT_MEASURE_RANGE, range);
    }

    private void setCurrRangeAuto(boolean state) throws IOException {
        write(":SENSe:CURRent:RANGe:AUTO " + (state ? "ON" : "OFF"));
    }

    private void setVoltRangeManual(double range) throws IOException {
        write(":SENSe:VOLTage:RANGe " + range);
    }

    private void setVoltRangeAuto(boolean state) throws IOException {
        write(":SENSe:VOLTage:RANGe:AUTO " + (state ? "ON" : "OFF"));
    }

    private void setResistanceRangeManual(double range) throws IOException {
        write(":SENSe:RESistance:RANGe " + range);
    }

    private void setResistanceRangeAuto(boolean state) throws IOException {
        write(":SENSe:RESistance:RANGe:AUTO " + (state ? "ON" : "OFF"));
    }

    private void setDisplayDigits(int digits) throws IOException {
        write(":DISPlay:DIGits " + digits);
    }

    @Override
    public String getName() {
        return "Keithley 2400";
    }

    @Override
    public double getSetCurrent() throws DeviceException, IOException {
        throw new DeviceException("Not implemented.");
    }

    @Override
    public double getSetVoltage() throws DeviceException, IOException {
        throw new DeviceException("Not implemented.");
    }

    public void disableAveraging() throws IOException {

        write(C_SET_AVG_MODE, "REPEAT");
        write(C_SET_AVG_COUNT, 1);
        write(C_SET_AVG_STATE, OUTPUT_OFF);

    }

    @Override
    public double getVoltage() throws DeviceException, IOException {
        return filterV.getValue();
    }

    @Override
    public void setVoltage(double voltage) throws DeviceException, IOException {
        write(C_SET_VOLTAGE_LEVEL, voltage);
    }

    @Override
    public double getCurrent() throws DeviceException, IOException {
        return filterI.getValue();
    }

    @Override
    public void setCurrent(double current) throws DeviceException, IOException {
        write(C_SET_CURRENT_LEVEL, current);
    }

    @Override
    public void turnOn() throws DeviceException, IOException {
        write(C_SET_OUTPUT_STATE, "ON");

    }

    @Override
    public void turnOff() throws DeviceException, IOException {
        write(C_SET_OUTPUT_STATE, "OFF");

    }

    @Override
    public boolean isOn() throws DeviceException, IOException {
        return query(C_QUERY_OUTPUT_STATE).trim().equals(OUTPUT_ON);
    }

    @Override
    public Source getSource() throws DeviceException, IOException {
        return KeithleySCPI.Source.fromTag(query(C_QUERY_SOURCE_FUNCTION)).getSMU();
    }

    @Override
    public void setSource(Source source) throws DeviceException, IOException {
        write(C_SET_SOURCE_FUNCTION, source);
    }

    public Source getSourceMode() throws IOException {

        String mode = query(C_QUERY_SOURCE_FUNCTION).trim().toUpperCase();

        if (mode.contains("CURR")) {
            return Source.CURRENT;
        } else if (mode.contains("VOLT")) {
            return Source.VOLTAGE;
        } else {
            throw new IOException("Unknown source mode returned by K2400: " + mode);
        }

    }

    @Override
    public double getSourceValue() throws DeviceException, IOException {

        switch (getSourceMode()) {

            case VOLTAGE:
                return getVoltage();
            case CURRENT:
                return getCurrent();
            default:
                throw new IllegalArgumentException();

        }

    }

    @Override
    public void setSourceValue(double level) throws DeviceException, IOException {

        switch (getSourceMode()) {

            case VOLTAGE:
                setVoltage(level);
                break;

            case CURRENT:
                setCurrent(level);
                break;

        }

    }

    public Source getMeasureMode() throws IOException {

        switch (getSourceMode()) {

            case VOLTAGE:
                return Source.CURRENT;

            case CURRENT:
                return Source.VOLTAGE;

            default:
                throw new IOException("Unexpected source mode returned by K2400: " + getSourceMode());

        }

    }

    @Override
    public double getMeasureValue() throws DeviceException, IOException {
        switch (getMeasureMode()) {
            case VOLTAGE:
                return getVoltage();
            case CURRENT:
                return getCurrent();
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public boolean isFourProbeEnabled() throws DeviceException, IOException {
        return query(":SYSTem:RSENse?").trim().equals(OUTPUT_ON);
    }

    @Override
    public void setFourProbeEnabled(boolean fourProbes) throws DeviceException, IOException {
        if (fourProbes) {
            write(":SYSTem:RSENse ON");
        } else {
            write(":SYSTem:RSENse OFF");
        }
    }

    @Override
    public AMode getAverageMode() throws DeviceException, IOException {
        return filterMode;
    }

    @Override
    public void setAverageMode(AMode mode) throws DeviceException, IOException {

        switch (mode) {

            case NONE:
                filterV = NONE_V;
                filterI = NONE_I;
                break;

            case MEAN_REPEAT:
                filterV = MEAN_REPEAT_V;
                filterI = MEAN_REPEAT_I;
                break;

            case MEAN_MOVING:
                filterV = MEAN_MOVING_V;
                filterI = MEAN_MOVING_I;
                break;

            case MEDIAN_REPEAT:
                filterV = MEDIAN_REPEAT_V;
                filterI = MEDIAN_REPEAT_I;
                break;

            case MEDIAN_MOVING:
                filterV = MEDIAN_MOVING_V;
                filterI = MEDIAN_MOVING_I;
                break;

        }

        filterMode = mode;
        resetFilters();
    }

    private void resetFilters() throws IOException, DeviceException {

        filterV.setCount(filterCount);
        filterI.setCount(filterCount);

        filterV.setUp();
        filterI.setUp();

        filterV.clear();
        filterI.clear();
    }

    @Override
    public int getAverageCount() throws DeviceException, IOException {
        return filterCount;
    }

    @Override
    public void setAverageCount(int count) throws DeviceException, IOException {
        filterCount = count;
        resetFilters();
    }

    @Override
    public double getSourceRange() throws DeviceException, IOException {
        String mode = query(C_QUERY_SOURCE_FUNCTION).trim();
        return queryDouble(C_QUERY_SRC_RANGE, mode);
    }

    @Override
    public void setSourceRange(double value) throws DeviceException, IOException {

        String mode = query(C_QUERY_SOURCE_FUNCTION).trim();
        double low  = queryDouble(C_QUERY_SRC_RANGE_MIN, mode);
        double upp  = queryDouble(C_QUERY_SRC_RANGE_MAX, mode);

        checkLimit(mode, value, low, upp);

        write(C_SET_SRC_AUTO_RANGE, mode, OUTPUT_OFF);
        write(C_SET_SRC_RANGE, mode, value);

    }

    @Override
    public void useAutoSourceRange() throws DeviceException, IOException {

        switch (getSourceMode()) {

            case VOLTAGE:
                useAutoVoltageRange();

            case CURRENT:
                useAutoCurrentRange();

        }

    }

    @Override
    public boolean isAutoRangingSource() throws DeviceException, IOException {

        switch (getSourceMode()) {

            case VOLTAGE:
                return isAutoRangingVoltage();

            case CURRENT:
                return isAutoRangingCurrent();

            default:
                throw new IOException("Invalid source mode");

        }

    }

    @Override
    public double getMeasureRange() throws DeviceException, IOException {

        switch (getMeasureMode()) {

            case VOLTAGE:
                return getVoltageRange();

            case CURRENT:
                return getCurrentRange();

            default:
                throw new IOException("Invalid measure mode");

        }

    }

    @Override
    public void setMeasureRange(double value) throws DeviceException, IOException {

        switch (getMeasureMode()) {

            case VOLTAGE:
                setVoltageRange(value);

            case CURRENT:
                setCurrentRange(value);

        }

    }

    @Override
    public void useAutoMeasureRange() throws DeviceException, IOException {

        switch (getMeasureMode()) {

            case VOLTAGE:
                useAutoVoltageRange();

            case CURRENT:
                useAutoCurrentRange();

        }

    }

    @Override
    public boolean isAutoRangingMeasure() throws DeviceException, IOException {

        switch (getMeasureMode()) {

            case VOLTAGE:
                return isAutoRangingVoltage();

            case CURRENT:
                return isAutoRangingCurrent();

            default:
                throw new IOException("Invalid measure mode");

        }

    }

    @Override
    public double getVoltageRange() throws DeviceException, IOException {

        if (isSourcing(Source.VOLTAGE)) {
            return queryDouble(C_QUERY_SRC_RANGE, "VOLT");
        } else {
            return queryDouble(C_QUERY_MEAS_RANGE, "VOLT");
        }

    }

    @Override
    public void setVoltageRange(double value) throws DeviceException, IOException {

        write(C_SET_SRC_AUTO_RANGE, "VOLTAGE", OUTPUT_OFF);
        write(C_SET_MEAS_AUTO_RANGE, "VOLTAGE", OUTPUT_OFF);

        write(C_SET_SRC_RANGE, "VOLTAGE", value);
        write(C_SET_MEAS_RANGE, "VOLTAGE", value);
    }

    @Override
    public void useAutoVoltageRange() throws DeviceException, IOException {
        write(C_SET_SRC_AUTO_RANGE, "VOLT", OUTPUT_ON);
    }

    @Override
    public boolean isAutoRangingVoltage() throws DeviceException, IOException {

        if (isSourcing(Source.VOLTAGE)) {
            return query(C_QUERY_SRC_AUTO_RANGE, "VOLT").contains(OUTPUT_ON);
        } else {
            return query(C_QUERY_MEAS_AUTO_RANGE, "VOLT").contains(OUTPUT_ON);
        }

    }

    @Override
    public double getCurrentRange() throws DeviceException, IOException {

        if (isSourcing(Source.CURRENT)) {
            return queryDouble(C_QUERY_SRC_RANGE, "CURR");
        } else {
            return queryDouble(C_QUERY_MEAS_RANGE, "CURR");
        }

    }

    @Override
    public void setCurrentRange(double value) throws DeviceException, IOException {
        write(C_SET_SRC_AUTO_RANGE, "CURRENT", OUTPUT_OFF);
        write(C_SET_MEAS_AUTO_RANGE, "CURRENT", OUTPUT_OFF);
        write(C_SET_SRC_RANGE, "CURRENT", value);
        write(C_SET_MEAS_RANGE, "CURRENT", value);
    }

    @Override
    public void useAutoCurrentRange() throws DeviceException, IOException {
        write(C_SET_SRC_AUTO_RANGE, "CURR", OUTPUT_ON);
        write(C_SET_MEAS_AUTO_RANGE, "CURR", OUTPUT_ON);
    }

    private boolean isSourcing(Source func) throws IOException {
        return getSourceMode() == func;
    }

    @Override
    public boolean isAutoRangingCurrent() throws DeviceException, IOException {

        if (isSourcing(Source.VOLTAGE)) {
            return query(C_QUERY_SRC_AUTO_RANGE, "CURR").contains(OUTPUT_ON);
        } else {
            return query(C_QUERY_MEAS_AUTO_RANGE, "CURR").contains(OUTPUT_ON);
        }

    }

    @Override
    public double getOutputLimit() throws DeviceException, IOException {

        switch (getMeasureMode()) {
            case VOLTAGE:
                return getVoltageLimit();
            case CURRENT:
                return getCurrentLimit();
            default:
                throw new IllegalArgumentException();
        }

    }

    @Override
    public void setOutputLimit(double limit) throws DeviceException, IOException {
        switch (getMeasureMode()) {
            case VOLTAGE:
                setVoltageLimit(limit);
                break;
            case CURRENT:
                setCurrentLimit(limit);
                break;
        }
    }

    @Override
    public double getVoltageLimit() throws DeviceException, IOException {
        return queryDouble(C_QUERY_LIMIT, KeithleySCPI.Source.VOLTAGE.getTag());
    }

    @Override
    public void setVoltageLimit(double limit) throws DeviceException, IOException {
        write(C_SET_LIMIT, KeithleySCPI.Source.VOLTAGE.getTag(), limit);
        vLimit = limit;
    }

    @Override
    public double getCurrentLimit() throws DeviceException, IOException {
        return queryDouble(C_QUERY_LIMIT, KeithleySCPI.Source.CURRENT.getTag());
    }

    @Override
    public void setCurrentLimit(double limit) throws DeviceException, IOException {
        write(C_SET_LIMIT, KeithleySCPI.Source.CURRENT.getTag(), limit);
        iLimit = limit;
    }

    @Override
    public double getIntegrationTime() throws DeviceException, IOException {

        switch (getMeasureMode()) {

            case VOLTAGE:
                return queryDouble(C_QUERY_NPLC, "VOLT") / LINE_FREQUENCY;

            case CURRENT:
                return queryDouble(C_QUERY_NPLC, "CURR") / LINE_FREQUENCY;

            default:
                throw new IOException("Invalid measure mode");

        }

    }

    @Override
    public void setIntegrationTime(double time) throws DeviceException, IOException {

        double counts = LINE_FREQUENCY * time;
        write(C_SET_N_COUNTS_VOLTAGE, counts);
        write(C_SET_N_COUNTS_CURRENT, counts);

    }

    @Override
    public TType getTerminalType(Terminals terminals) throws DeviceException, IOException {
        return TType.BANANA;
    }

    @Override
    public Terminals getTerminals() throws DeviceException, IOException {

        String response = query(C_QUERY_TERMINALS);

        if (response.contains(TERMS_FRONT)) {
            return Terminals.FRONT;
        } else if (response.contains(TERMS_REAR)) {
            return Terminals.REAR;
        } else {
            throw new IOException("Invalid response from Keithley");
        }

    }

    @Override
    public void setTerminals(Terminals terminals) throws DeviceException, IOException {

        switch (terminals) {

            case FRONT:
                write(C_SET_TERMINALS, TERMS_FRONT);
                break;

            case REAR:
                write(C_SET_TERMINALS, TERMS_REAR);
                break;

        }

    }

    @Override
    public OffMode getOffMode() throws DeviceException, IOException {

        String code = query(C_QUERY_OFF_STATE);

        if (code.contains(OFF_NORMAL)) {
            return OffMode.NORMAL;
        } else if (code.contains(OFF_ZERO)) {
            return OffMode.ZERO;
        } else if (code.contains(OFF_HIGH_Z)) {
            return OffMode.HIGH_IMPEDANCE;
        } else if (code.contains(OFF_GUARD)) {
            return OffMode.GUARD;
        } else {
            return OffMode.NORMAL;
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

    @Override
    public boolean isLineFilterEnabled() throws DeviceException, IOException {
        return false;
    }

    @Override
    public void setLineFilterEnabled(boolean enabled) throws DeviceException, IOException {
    }

    public void mario() throws IOException {
        write("SYST:BEEP %s,%s", 660, 100.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 660, 100.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 660, 100.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 510, 100.0 / 1000.0);
        Util.sleep(100);
        write("SYST:BEEP %s,%s", 660, 100.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 770, 100.0 / 1000.0);
        Util.sleep(550);
        write("SYST:BEEP %s,%s", 380, 100.0 / 1000.0);
        Util.sleep(575);
        write("SYST:BEEP %s,%s", 510, 100.0 / 1000.0);
        Util.sleep(450);
        write("SYST:BEEP %s,%s", 380, 100.0 / 1000.0);
        Util.sleep(400);
        write("SYST:BEEP %s,%s", 320, 100.0 / 1000.0);
        Util.sleep(500);
        write("SYST:BEEP %s,%s", 440, 100.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 480, 80.0 / 1000.0);
        Util.sleep(330);
        write("SYST:BEEP %s,%s", 450, 100.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 430, 100.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 380, 100.0 / 1000.0);
        Util.sleep(200);
        write("SYST:BEEP %s,%s", 660, 80.0 / 1000.0);
        Util.sleep(200);
        write("SYST:BEEP %s,%s", 760, 50.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 860, 100.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 700, 80.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 760, 50.0 / 1000.0);
        Util.sleep(350);
        write("SYST:BEEP %s,%s", 660, 80.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 520, 80.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 580, 80.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 480, 80.0 / 1000.0);
        Util.sleep(500);
        write("SYST:BEEP %s,%s", 510, 100.0 / 1000.0);
        Util.sleep(450);
        write("SYST:BEEP %s,%s", 380, 100.0 / 1000.0);
        Util.sleep(400);
        write("SYST:BEEP %s,%s", 320, 100.0 / 1000.0);
        Util.sleep(500);
        write("SYST:BEEP %s,%s", 440, 100.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 480, 80.0 / 1000.0);
        Util.sleep(330);
        write("SYST:BEEP %s,%s", 450, 100.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 430, 100.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 380, 100.0 / 1000.0);
        Util.sleep(200);
        write("SYST:BEEP %s,%s", 660, 80.0 / 1000.0);
        Util.sleep(200);
        write("SYST:BEEP %s,%s", 760, 50.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 860, 100.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 700, 80.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 760, 50.0 / 1000.0);
        Util.sleep(350);
        write("SYST:BEEP %s,%s", 660, 80.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 520, 80.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 580, 80.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 480, 80.0 / 1000.0);
        Util.sleep(500);
        write("SYST:BEEP %s,%s", 500, 100.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 760, 100.0 / 1000.0);
        Util.sleep(100);
        write("SYST:BEEP %s,%s", 720, 100.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 680, 100.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 620, 150.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 650, 150.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 380, 100.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 430, 100.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 500, 100.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 430, 100.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 500, 100.0 / 1000.0);
        Util.sleep(100);
        write("SYST:BEEP %s,%s", 570, 100.0 / 1000.0);
        Util.sleep(220);
        write("SYST:BEEP %s,%s", 500, 100.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 760, 100.0 / 1000.0);
        Util.sleep(100);
        write("SYST:BEEP %s,%s", 720, 100.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 680, 100.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 620, 150.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 650, 200.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 380, 100.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 500, 100.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 760, 100.0 / 1000.0);
        Util.sleep(100);
        write("SYST:BEEP %s,%s", 720, 100.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 680, 100.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 620, 150.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 650, 150.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 380, 100.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 430, 100.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 500, 100.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 430, 100.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 500, 100.0 / 1000.0);
        Util.sleep(100);
        write("SYST:BEEP %s,%s", 570, 100.0 / 1000.0);
        Util.sleep(420);
        write("SYST:BEEP %s,%s", 585, 100.0 / 1000.0);
        Util.sleep(450);
        write("SYST:BEEP %s,%s", 550, 100.0 / 1000.0);
        Util.sleep(420);
        write("SYST:BEEP %s,%s", 500, 100.0 / 1000.0);
        Util.sleep(360);
        write("SYST:BEEP %s,%s", 380, 100.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 500, 100.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 500, 100.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 500, 100.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 500, 100.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 760, 100.0 / 1000.0);
        Util.sleep(100);
        write("SYST:BEEP %s,%s", 720, 100.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 680, 100.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 620, 150.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 650, 150.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 380, 100.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 430, 100.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 500, 100.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 430, 100.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 500, 100.0 / 1000.0);
        Util.sleep(100);
        write("SYST:BEEP %s,%s", 570, 100.0 / 1000.0);
        Util.sleep(220);
        write("SYST:BEEP %s,%s", 500, 100.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 760, 100.0 / 1000.0);
        Util.sleep(100);
        write("SYST:BEEP %s,%s", 720, 100.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 680, 100.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 620, 150.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 650, 200.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 380, 100.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 500, 100.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 760, 100.0 / 1000.0);
        Util.sleep(100);
        write("SYST:BEEP %s,%s", 720, 100.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 680, 100.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 620, 150.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 650, 150.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 380, 100.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 430, 100.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 500, 100.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 430, 100.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 500, 100.0 / 1000.0);
        Util.sleep(100);
        write("SYST:BEEP %s,%s", 570, 100.0 / 1000.0);
        Util.sleep(420);
        write("SYST:BEEP %s,%s", 585, 100.0 / 1000.0);
        Util.sleep(450);
        write("SYST:BEEP %s,%s", 550, 100.0 / 1000.0);
        Util.sleep(420);
        write("SYST:BEEP %s,%s", 500, 100.0 / 1000.0);
        Util.sleep(360);
        write("SYST:BEEP %s,%s", 380, 100.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 500, 100.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 500, 100.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 500, 100.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 500, 60.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 500, 80.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 500, 60.0 / 1000.0);
        Util.sleep(350);
        write("SYST:BEEP %s,%s", 500, 80.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 580, 80.0 / 1000.0);
        Util.sleep(350);
        write("SYST:BEEP %s,%s", 660, 80.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 500, 80.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 430, 80.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 380, 80.0 / 1000.0);
        Util.sleep(600);
        write("SYST:BEEP %s,%s", 500, 60.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 500, 80.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 500, 60.0 / 1000.0);
        Util.sleep(350);
        write("SYST:BEEP %s,%s", 500, 80.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 580, 80.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 660, 80.0 / 1000.0);
        Util.sleep(550);
        write("SYST:BEEP %s,%s", 870, 80.0 / 1000.0);
        Util.sleep(325);
        write("SYST:BEEP %s,%s", 760, 80.0 / 1000.0);
        Util.sleep(600);
        write("SYST:BEEP %s,%s", 500, 60.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 500, 80.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 500, 60.0 / 1000.0);
        Util.sleep(350);
        write("SYST:BEEP %s,%s", 500, 80.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 580, 80.0 / 1000.0);
        Util.sleep(350);
        write("SYST:BEEP %s,%s", 660, 80.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 500, 80.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 430, 80.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 380, 80.0 / 1000.0);
        Util.sleep(600);
        write("SYST:BEEP %s,%s", 660, 100.0 / 1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 660, 100.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 660, 100.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 510, 100.0 / 1000.0);
        Util.sleep(100);
        write("SYST:BEEP %s,%s", 660, 100.0 / 1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 770, 100.0 / 1000.0);
        Util.sleep(550);
        write("SYST:BEEP %s,%s", 380, 100.0 / 1000.0);
        Util.sleep(575);
    }

}