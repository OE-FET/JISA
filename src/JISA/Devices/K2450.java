package JISA.Devices;

import JISA.Addresses.InstrumentAddress;
import JISA.Util;
import com.sun.javafx.UnmodifiableArrayList;

import java.io.IOException;
import java.util.*;

public class K2450 extends SMU {

    // == COMMANDS =====================================================================================================
    private static final String C_MEASURE_VOLTAGE       = ":MEAS:VOLT?";
    private static final String C_MEASURE_CURRENT       = ":MEAS:CURR?";
    private static final String C_MEASURE_RESISTANCE    = ":MEAS:RES?";
    private static final String C_SET_SOURCE_FUNCTION   = ":SOUR:FUNC %s";
    private static final String C_SET_OUTPUT_STATE      = ":OUTP:STATE %s";
    private static final String C_QUERY_SOURCE_FUNCTION = ":SOUR:FUNC?";
    private static final String C_QUERY_OUTPUT_STATE    = ":OUTP:STATE?";
    private static final String C_SET_SOURCE_VALUE      = ":SOUR:%s %e";
    private static final String C_SET_TERMINALS         = ":ROUT:TERM %s";
    private static final String C_QUERY_TERMINALS       = ":ROUT:TERM?";
    private static final String C_SET_PROBE_MODE        = ":SENS:RSEN %s";
    private static final String C_QUERY_PROBE_MODE      = ":SENS:RSEN?";
    private static final String C_SET_AVG_COUNT         = "AVER:COUNT %d";
    private static final String C_QUERY_AVG_COUNT       = "VOLT:AVER:COUNT?";
    private static final String C_SET_AVG_MODE          = "AVER:TCON %s";
    private static final String C_QUERY_AVG_MODE        = "VOLT:AVER:TCON?";
    private static final String C_SET_AVG_STATE         = "AVER %s";
    private static final String C_QUERY_AVG_STATE       = "VOLT:AVER?";
    private static final String C_SET_SRC_RANGE         = ":SOUR:%s:RANG %e";
    private static final String C_QUERY_SRC_RANGE       = ":SOUR:%s:RANG?";
    private static final String C_SET_SRC_AUTO_RANGE    = ":SOUR:%s:RANG:AUTO %s";
    private static final String C_QUERY_SRC_AUTO_RANGE  = ":SOUR:%s:RANG:AUTO?";
    private static final String C_SET_MEAS_RANGE        = ":SENS:%s:RANG %e";
    private static final String C_QUERY_MEAS_RANGE      = ":SENS:%s:RANG?";
    private static final String C_SET_MEAS_AUTO_RANGE   = ":SENS:%s:RANG:AUTO %s";
    private static final String C_QUERY_MEAS_AUTO_RANGE = ":SENS:%s:RANG:AUTO?";
    private static final String C_SET_LIMIT             = ":SOUR:%s:%sLIM %e";
    private static final String C_QUERY_LIMIT           = ":SOUR:%s:%sLIM?";
    private static final String C_SET_NPLC              = ":SENS:NPLC %f";
    private static final String C_QUERY_NPLC            = ":SENS:%s:NPLC?";
    private static final String C_SET_OFF_STATE         = ":OUTP:SMOD %s";
    private static final String C_QUERY_OFF_STATE       = ":OUTP:SMOD?";
    private static final String OFF_NORMAL              = "NORM";
    private static final String OFF_ZERO                = "ZERO";
    private static final String OFF_HIGH_Z              = "HIMP";
    private static final String OUTPUT_ON               = "1";
    private static final String OUTPUT_OFF              = "0";
    private static final String TERMS_FRONT             = "FRON";
    private static final String TERMS_REAR              = "REAR";
    private static final String C_QUERY_LFR             = ":SYST:LFR?";
    private final        double LINE_FREQUENCY;

    // == FILTERS ======================================================================================================
    private final MedianRepeatFilter MEDIAN_REPEAT_V = new MedianRepeatFilter(
            () -> queryDouble(C_MEASURE_VOLTAGE),
            (c) -> {
                write(C_SET_AVG_MODE, "REPEAT");
                write(C_SET_AVG_COUNT, 1);
                write(C_SET_AVG_STATE, OUTPUT_OFF);
            }
    );

    private final MedianRepeatFilter MEDIAN_REPEAT_I = new MedianRepeatFilter(
            () -> queryDouble(C_MEASURE_CURRENT),
            (c) -> {
                write(C_SET_AVG_MODE, "REPEAT");
                write(C_SET_AVG_COUNT, 1);
                write(C_SET_AVG_STATE, OUTPUT_OFF);
            });

    private final MedianMovingFilter MEDIAN_MOVING_V = new MedianMovingFilter(
            () -> queryDouble(C_MEASURE_VOLTAGE),
            (c) -> {
                write(C_SET_AVG_MODE, "REPEAT");
                write(C_SET_AVG_COUNT, 1);
                write(C_SET_AVG_STATE, OUTPUT_OFF);
            }
    );

    private final MedianMovingFilter MEDIAN_MOVING_I = new MedianMovingFilter(
            () -> queryDouble(C_MEASURE_CURRENT),
            (c) -> {
                write(C_SET_AVG_MODE, "REPEAT");
                write(C_SET_AVG_COUNT, 1);
                write(C_SET_AVG_STATE, OUTPUT_OFF);
            });

    private final BypassFilter MEAN_REPEAT_V = new BypassFilter(
            () -> queryDouble(C_MEASURE_VOLTAGE),
            (c) -> {
                write(C_SET_AVG_MODE, "REPEAT");
                write(C_SET_AVG_COUNT, c);
                write(C_SET_AVG_STATE, OUTPUT_ON);
            }
    );

    private final BypassFilter MEAN_REPEAT_I = new BypassFilter(
            () -> queryDouble(C_MEASURE_CURRENT),
            (c) -> {
                write(C_SET_AVG_MODE, "REPEAT");
                write(C_SET_AVG_COUNT, c);
                write(C_SET_AVG_STATE, OUTPUT_ON);
            }
    );

    private final BypassFilter MEAN_MOVING_V = new BypassFilter(
            () -> queryDouble(C_MEASURE_VOLTAGE),
            (c) -> {
                write(C_SET_AVG_MODE, "MOVING");
                write(C_SET_AVG_COUNT, c);
                write(C_SET_AVG_STATE, OUTPUT_ON);
            }
    );

    private final BypassFilter MEAN_MOVING_I = new BypassFilter(
            () -> queryDouble(C_MEASURE_CURRENT),
            (c) -> {
                write(C_SET_AVG_MODE, "MOVING");
                write(C_SET_AVG_COUNT, c);
                write(C_SET_AVG_STATE, OUTPUT_ON);
            }
    );

    private final BypassFilter NONE_V = new BypassFilter(
            () -> queryDouble(C_MEASURE_VOLTAGE),
            (c) -> {
                write(C_SET_AVG_MODE, "REPEAT");
                write(C_SET_AVG_COUNT, 1);
                write(C_SET_AVG_STATE, OUTPUT_OFF);
            }
    );

    private final BypassFilter NONE_I = new BypassFilter(
            () -> queryDouble(C_MEASURE_CURRENT),
            (c) -> {
                write(C_SET_AVG_MODE, "REPEAT");
                write(C_SET_AVG_COUNT, 1);
                write(C_SET_AVG_STATE, OUTPUT_OFF);
            }
    );

    // == INTERNAL VARIABLES ===========================================================================================
    private ReadFilter filterV     = NONE_V;
    private ReadFilter filterI     = NONE_I;
    private AMode      filterMode  = AMode.NONE;
    private int        filterCount = 1;

    // == CONSTRUCTORS =================================================================================================
    public K2450(InstrumentAddress address) throws IOException, DeviceException {

        super(address);

        write(":SYSTEM:CLEAR");

        try {

            clearRead();

            String[] iden = query("*IDN?").split(",");

            if (!iden[1].trim().equals("MODEL 2450")) {
                throw new DeviceException("Device at address %s is not a Keithley 2450!", address.getVISAAddress());
            }

            setAverageMode(AMode.NONE);

        } catch (IOException e) {
            throw new DeviceException("Device at address %s is not responding!", address.getVISAAddress());
        }

        LINE_FREQUENCY = queryDouble(C_QUERY_LFR);

    }

    // == METHODS ======================================================================================================
    public void useFourProbe(boolean fourProbe) throws IOException {
        write(C_SET_PROBE_MODE, fourProbe ? OUTPUT_ON : OUTPUT_OFF);
    }

    public boolean isUsingFourProbe() throws IOException {
        return query(C_QUERY_PROBE_MODE).trim().equals(OUTPUT_ON);
    }

    @Override
    public void setAverageMode(AMode mode) throws IOException, DeviceException {

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
    public void setAverageCount(int count) throws IOException, DeviceException {
        filterCount = count;
        resetFilters();
    }

    @Override
    public AMode getAverageMode() {
        return filterMode;

    }

    @Override
    public int getAverageCount() {
        return filterCount;
    }

    @Override
    public void setSourceRange(double value) throws IOException {
        Source mode = getSourceMode();
        write(C_SET_SRC_AUTO_RANGE, mode.getTag(), OUTPUT_OFF);
        write(C_SET_SRC_RANGE, mode.getTag(), value);
    }

    @Override
    public double getSourceRange() throws IOException {
        return queryDouble(C_QUERY_SRC_RANGE, getSourceMode().getTag());
    }

    @Override
    public void useAutoSourceRange() throws IOException {
        write(C_SET_SRC_AUTO_RANGE, getSourceMode().getTag(), OUTPUT_ON);
    }

    @Override
    public boolean isSourceRangeAuto() throws IOException {
        return query(C_QUERY_SRC_AUTO_RANGE, getSourceMode().getTag()).equals(OUTPUT_ON);
    }

    @Override
    public void setMeasureRange(double value) throws IOException {
        Source mode = getMeasureMode();
        write(C_SET_SRC_AUTO_RANGE, mode.getTag(), OUTPUT_OFF);
        write(C_SET_SRC_RANGE, mode.getTag(), value);
    }

    @Override
    public double getMeasureRange() throws IOException {
        return queryDouble(C_QUERY_SRC_RANGE, getMeasureMode().getTag());
    }

    @Override
    public void useAutoMeasureRange() throws IOException {
        write(C_SET_SRC_AUTO_RANGE, getMeasureMode().getTag(), OUTPUT_ON);
    }

    @Override
    public boolean isMeasureRangeAuto() throws IOException {
        return query(C_QUERY_SRC_AUTO_RANGE, getMeasureMode().getTag()).equals(OUTPUT_ON);
    }

    private boolean isSourcing(Source func) throws IOException {
        return getSourceMode() == func;
    }

    private boolean isMeasuring(Source func) throws IOException {
        return getMeasureMode() == func;
    }

    @Override
    public void setVoltageRange(double value) throws IOException {

        boolean src = isSourcing(Source.VOLTAGE);

        write(src ? C_SET_SRC_AUTO_RANGE : C_SET_MEAS_AUTO_RANGE, Source.VOLTAGE.getTag(), OUTPUT_OFF);
        write(src ? C_SET_SRC_RANGE : C_SET_MEAS_RANGE, Source.VOLTAGE.getTag(), value);

    }

    @Override
    public double getVoltageRange() throws IOException {
        return queryDouble(isSourcing(Source.VOLTAGE) ? C_QUERY_SRC_RANGE : C_QUERY_MEAS_RANGE, Source.VOLTAGE.getTag());
    }

    @Override
    public void useAutoVoltageRange() throws IOException {
        write(
                isSourcing(Source.VOLTAGE) ? C_SET_SRC_AUTO_RANGE : C_SET_MEAS_AUTO_RANGE,
                Source.VOLTAGE.getTag(),
                OUTPUT_ON
        );
    }

    @Override
    public boolean isVoltageRangeAuto() throws IOException {
        return query(
                isSourcing(Source.VOLTAGE) ? C_QUERY_SRC_AUTO_RANGE : C_QUERY_MEAS_AUTO_RANGE,
                Source.VOLTAGE.getTag()
        ).equals(OUTPUT_ON);
    }

    @Override
    public void setCurrentRange(double value) throws IOException {

        boolean src = isSourcing(Source.CURRENT);

        write(src ? C_SET_SRC_AUTO_RANGE : C_SET_MEAS_AUTO_RANGE, Source.CURRENT.getTag(), OUTPUT_OFF);
        write(src ? C_SET_SRC_RANGE : C_SET_MEAS_RANGE, Source.CURRENT.getTag(), value);

    }

    @Override
    public double getCurrentRange() throws IOException {
        return queryDouble(isSourcing(Source.CURRENT) ? C_QUERY_SRC_RANGE : C_QUERY_MEAS_RANGE, Source.CURRENT.getTag());
    }

    @Override
    public void useAutoCurrentRange() throws IOException {
        write(
                isSourcing(Source.CURRENT) ? C_SET_SRC_AUTO_RANGE : C_SET_MEAS_AUTO_RANGE,
                Source.CURRENT.getTag(),
                OUTPUT_ON
        );
    }

    @Override
    public boolean isCurrentRangeAuto() throws IOException {
        return query(
                isSourcing(Source.CURRENT) ? C_QUERY_SRC_AUTO_RANGE : C_QUERY_MEAS_AUTO_RANGE,
                Source.CURRENT.getTag()
        ).equals(OUTPUT_ON);
    }

    @Override
    public void setOutputLimit(double value) throws IOException {
        write(C_SET_LIMIT, getSourceMode().getTag(), getMeasureMode().getSymbol(), value);
    }

    @Override
    public double getOutputLimit() throws IOException {
        return queryDouble(C_QUERY_LIMIT, getSourceMode().getTag(), getMeasureMode().getSymbol());
    }

    @Override
    public void setVoltageLimit(double voltage) throws IOException {
        write(C_SET_LIMIT, Source.CURRENT.getTag(), Source.VOLTAGE.getSymbol(), voltage);
    }

    @Override
    public double getVoltageLimit() throws IOException {
        return queryDouble(C_QUERY_LIMIT, Source.CURRENT.getTag(), Source.VOLTAGE.getSymbol());
    }

    @Override
    public void setCurrentLimit(double current) throws IOException {
        write(C_SET_LIMIT, Source.VOLTAGE.getTag(), Source.CURRENT.getSymbol(), current);
    }

    @Override
    public double getCurrentLimit() throws IOException {
        return queryDouble(C_QUERY_LIMIT, Source.VOLTAGE.getTag(), Source.CURRENT.getSymbol());
    }

    @Override
    public void setIntegrationTime(double time) throws IOException {
        write(C_SET_NPLC, LINE_FREQUENCY * time);
    }

    @Override
    public double getIntegrationTime() throws IOException {
        return queryDouble(C_QUERY_NPLC, getMeasureMode().getTag()) / LINE_FREQUENCY;
    }

    @Override
    public TType getTerminalType(Terminals terminals) {

        switch (terminals) {

            case FRONT:
                return TType.BANANA;

            case REAR:
                return TType.TRIAX;

            default:
                return TType.NONE;

        }

    }

    public double getVoltage() throws DeviceException, IOException {
        return filterV.getValue();
    }

    public double getCurrent() throws IOException, DeviceException {
        return filterI.getValue();
    }

    public void turnOn() throws IOException {
        setOutputState(true);
    }

    public void turnOff() throws IOException {
        setOutputState(false);
    }

    public void setOutputState(boolean on) throws IOException {
        write(C_SET_OUTPUT_STATE, on ? OUTPUT_ON : OUTPUT_OFF);
    }

    public void setSource(Source mode) throws IOException {
        write(C_SET_SOURCE_FUNCTION, mode.getTag());
    }

    public SMU.Source getSource() throws IOException {
        return Source.fromTag(query(C_QUERY_SOURCE_FUNCTION)).getSMU();
    }

    public Source getSourceMode() throws IOException {
        return Source.fromTag(query(C_QUERY_SOURCE_FUNCTION));
    }

    public Source getMeasureMode() throws IOException {

        switch (Source.fromTag(query(C_QUERY_SOURCE_FUNCTION))) {
            case VOLTAGE:
                return Source.CURRENT;

            case CURRENT:
                return Source.VOLTAGE;
        }

        return Source.CURRENT;

    }

    public void setSource(SMU.Source source) throws IOException {
        write(C_SET_SOURCE_FUNCTION, Source.fromSMU(source).getTag());
    }

    public boolean isOn() throws IOException {
        return query(C_QUERY_OUTPUT_STATE).equals(OUTPUT_ON);
    }

    public void setVoltage(double voltage) throws IOException {
        setSourceValue(Source.VOLTAGE, voltage);
    }

    public void setCurrent(double current) throws IOException {
        setSourceValue(Source.CURRENT, current);
    }

    public void setBias(double value) throws IOException {

        switch (getSourceMode()) {

            case VOLTAGE:
                setVoltage(value);
                break;

            case CURRENT:
                setCurrent(value);
                break;

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
                return getVoltage();

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
                return getCurrent();

        }

    }

    public void setSourceValue(Source type, double value) throws IOException {
        write(C_SET_SOURCE_VALUE, type.getTag(), value);
        setSource(type);
    }

    public void youDidIt() throws IOException {
        write("SYST:BEEP 500,0.25");
        Util.sleep(350);
        write("SYST:BEEP 500,0.15");
        write("SYST:BEEP 668,1");
    }
    
    public void mario() throws IOException {
        write("SYST:BEEP %s,%s", 660, 100.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 660, 100.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 660, 100.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 510, 100.0/1000.0);
        Util.sleep(100);
        write("SYST:BEEP %s,%s", 660, 100.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 770, 100.0/1000.0);
        Util.sleep(550);
        write("SYST:BEEP %s,%s", 380, 100.0/1000.0);
        Util.sleep(575);
        write("SYST:BEEP %s,%s", 510, 100.0/1000.0);
        Util.sleep(450);
        write("SYST:BEEP %s,%s", 380, 100.0/1000.0);
        Util.sleep(400);
        write("SYST:BEEP %s,%s", 320, 100.0/1000.0);
        Util.sleep(500);
        write("SYST:BEEP %s,%s", 440, 100.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 480, 80.0/1000.0);
        Util.sleep(330);
        write("SYST:BEEP %s,%s", 450, 100.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 430, 100.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 380, 100.0/1000.0);
        Util.sleep(200);
        write("SYST:BEEP %s,%s", 660, 80.0/1000.0);
        Util.sleep(200);
        write("SYST:BEEP %s,%s", 760, 50.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 860, 100.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 700, 80.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 760, 50.0/1000.0);
        Util.sleep(350);
        write("SYST:BEEP %s,%s", 660, 80.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 520, 80.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 580, 80.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 480, 80.0/1000.0);
        Util.sleep(500);
        write("SYST:BEEP %s,%s", 510, 100.0/1000.0);
        Util.sleep(450);
        write("SYST:BEEP %s,%s", 380, 100.0/1000.0);
        Util.sleep(400);
        write("SYST:BEEP %s,%s", 320, 100.0/1000.0);
        Util.sleep(500);
        write("SYST:BEEP %s,%s", 440, 100.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 480, 80.0/1000.0);
        Util.sleep(330);
        write("SYST:BEEP %s,%s", 450, 100.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 430, 100.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 380, 100.0/1000.0);
        Util.sleep(200);
        write("SYST:BEEP %s,%s", 660, 80.0/1000.0);
        Util.sleep(200);
        write("SYST:BEEP %s,%s", 760, 50.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 860, 100.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 700, 80.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 760, 50.0/1000.0);
        Util.sleep(350);
        write("SYST:BEEP %s,%s", 660, 80.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 520, 80.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 580, 80.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 480, 80.0/1000.0);
        Util.sleep(500);
        write("SYST:BEEP %s,%s", 500, 100.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 760, 100.0/1000.0);
        Util.sleep(100);
        write("SYST:BEEP %s,%s", 720, 100.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 680, 100.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 620, 150.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 650, 150.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 380, 100.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 430, 100.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 500, 100.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 430, 100.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 500, 100.0/1000.0);
        Util.sleep(100);
        write("SYST:BEEP %s,%s", 570, 100.0/1000.0);
        Util.sleep(220);
        write("SYST:BEEP %s,%s", 500, 100.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 760, 100.0/1000.0);
        Util.sleep(100);
        write("SYST:BEEP %s,%s", 720, 100.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 680, 100.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 620, 150.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 650, 200.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 380, 100.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 500, 100.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 760, 100.0/1000.0);
        Util.sleep(100);
        write("SYST:BEEP %s,%s", 720, 100.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 680, 100.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 620, 150.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 650, 150.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 380, 100.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 430, 100.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 500, 100.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 430, 100.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 500, 100.0/1000.0);
        Util.sleep(100);
        write("SYST:BEEP %s,%s", 570, 100.0/1000.0);
        Util.sleep(420);
        write("SYST:BEEP %s,%s", 585, 100.0/1000.0);
        Util.sleep(450);
        write("SYST:BEEP %s,%s", 550, 100.0/1000.0);
        Util.sleep(420);
        write("SYST:BEEP %s,%s", 500, 100.0/1000.0);
        Util.sleep(360);
        write("SYST:BEEP %s,%s", 380, 100.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 500, 100.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 500, 100.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 500, 100.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 500, 100.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 760, 100.0/1000.0);
        Util.sleep(100);
        write("SYST:BEEP %s,%s", 720, 100.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 680, 100.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 620, 150.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 650, 150.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 380, 100.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 430, 100.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 500, 100.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 430, 100.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 500, 100.0/1000.0);
        Util.sleep(100);
        write("SYST:BEEP %s,%s", 570, 100.0/1000.0);
        Util.sleep(220);
        write("SYST:BEEP %s,%s", 500, 100.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 760, 100.0/1000.0);
        Util.sleep(100);
        write("SYST:BEEP %s,%s", 720, 100.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 680, 100.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 620, 150.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 650, 200.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 380, 100.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 500, 100.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 760, 100.0/1000.0);
        Util.sleep(100);
        write("SYST:BEEP %s,%s", 720, 100.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 680, 100.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 620, 150.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 650, 150.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 380, 100.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 430, 100.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 500, 100.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 430, 100.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 500, 100.0/1000.0);
        Util.sleep(100);
        write("SYST:BEEP %s,%s", 570, 100.0/1000.0);
        Util.sleep(420);
        write("SYST:BEEP %s,%s", 585, 100.0/1000.0);
        Util.sleep(450);
        write("SYST:BEEP %s,%s", 550, 100.0/1000.0);
        Util.sleep(420);
        write("SYST:BEEP %s,%s", 500, 100.0/1000.0);
        Util.sleep(360);
        write("SYST:BEEP %s,%s", 380, 100.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 500, 100.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 500, 100.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 500, 100.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 500, 60.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 500, 80.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 500, 60.0/1000.0);
        Util.sleep(350);
        write("SYST:BEEP %s,%s", 500, 80.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 580, 80.0/1000.0);
        Util.sleep(350);
        write("SYST:BEEP %s,%s", 660, 80.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 500, 80.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 430, 80.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 380, 80.0/1000.0);
        Util.sleep(600);
        write("SYST:BEEP %s,%s", 500, 60.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 500, 80.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 500, 60.0/1000.0);
        Util.sleep(350);
        write("SYST:BEEP %s,%s", 500, 80.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 580, 80.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 660, 80.0/1000.0);
        Util.sleep(550);
        write("SYST:BEEP %s,%s", 870, 80.0/1000.0);
        Util.sleep(325);
        write("SYST:BEEP %s,%s", 760, 80.0/1000.0);
        Util.sleep(600);
        write("SYST:BEEP %s,%s", 500, 60.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 500, 80.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 500, 60.0/1000.0);
        Util.sleep(350);
        write("SYST:BEEP %s,%s", 500, 80.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 580, 80.0/1000.0);
        Util.sleep(350);
        write("SYST:BEEP %s,%s", 660, 80.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 500, 80.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 430, 80.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 380, 80.0/1000.0);
        Util.sleep(600);
        write("SYST:BEEP %s,%s", 660, 100.0/1000.0);
        Util.sleep(150);
        write("SYST:BEEP %s,%s", 660, 100.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 660, 100.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 510, 100.0/1000.0);
        Util.sleep(100);
        write("SYST:BEEP %s,%s", 660, 100.0/1000.0);
        Util.sleep(300);
        write("SYST:BEEP %s,%s", 770, 100.0/1000.0);
        Util.sleep(550);
        write("SYST:BEEP %s,%s", 380, 100.0/1000.0);
        Util.sleep(575);
    }

    public Terminals getTerminals() throws IOException {

        String response = query(C_QUERY_TERMINALS);

        if (response.contains(TERMS_FRONT)) {
            return Terminals.FRONT;
        } else if (response.contains(TERMS_REAR)) {
            return Terminals.REAR;
        } else {
            throw new IOException("Invalid response from Keithley 2450");
        }

    }

    public void setTerminals(Terminals terminals) throws IOException, DeviceException {

        switch (terminals) {

            case FRONT:
                write(C_SET_TERMINALS, TERMS_FRONT);
                break;

            case REAR:
                write(C_SET_TERMINALS, TERMS_REAR);
                break;

            default:
                throw new DeviceException("Keithley 2450 does not have terminals of type: %s", terminals.name());

        }

    }

    @Override
    public OffMode getOffMode() throws IOException {

        String code = query(C_QUERY_OFF_STATE);

        if (code.contains(OFF_NORMAL)) {
            return OffMode.NORMAL;
        } else if (code.contains(OFF_ZERO)) {
            return OffMode.ZERO;
        } else if (code.contains(OFF_HIGH_Z)) {
            return OffMode.HIGH_IMPEDANCE;
        } else {
            return OffMode.NORMAL;
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

        }

    }

    public enum Source {

        VOLTAGE("VOLT", "V", SMU.Source.VOLTAGE),
        CURRENT("CURR", "I", SMU.Source.CURRENT);

        private static HashMap<String, Source>     lookup  = new HashMap<>();
        private static HashMap<SMU.Source, Source> convert = new HashMap<>();

        static {
            for (Source mode : Source.values()) {
                lookup.put(mode.getTag(), mode);
                convert.put(mode.getSMU(), mode);
            }
        }

        public static Source fromTag(String tag) {
            return lookup.getOrDefault(tag.trim(), null);
        }

        public static Source fromSMU(SMU.Source orig) {
            return convert.getOrDefault(orig, null);
        }

        private String     tag;
        private String     symbol;
        private SMU.Source orig;

        Source(String tag, String symbol, SMU.Source orig) {
            this.tag = tag;
            this.symbol = symbol;
            this.orig = orig;
        }

        String getTag() {
            return tag;
        }

        String getSymbol() {
            return symbol;
        }

        SMU.Source getSMU() {
            return orig;
        }

    }


}
