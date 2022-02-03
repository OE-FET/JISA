package jisa.devices.smu;

import jisa.Util;
import jisa.addresses.Address;
import jisa.control.*;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.SMU;
import jisa.enums.AMode;
import jisa.enums.Terminals;
import jisa.visa.Connection;
import jisa.visa.Driver;
import jisa.visa.VISADevice;

import java.io.IOException;
import java.util.HashMap;

public abstract class KeithleySCPI extends VISADevice implements SMU {

    protected static final String C_MEASURE_VOLTAGE       = ":MEAS:VOLT?";
    protected static final String C_MEASURE_CURRENT       = ":MEAS:CURR?";
    protected static final String C_MEASURE_RESISTANCE    = ":MEAS:RES?";
    protected static final String C_SET_SOURCE_FUNCTION   = ":SOUR:FUNC %s";
    protected static final String C_SET_OUTPUT_STATE      = ":OUTP:STATE %s";
    protected static final String C_QUERY_SOURCE_FUNCTION = ":SOUR:FUNC?";
    protected static final String C_QUERY_OUTPUT_STATE    = ":OUTP:STATE?";
    protected static final String C_SET_SOURCE_VALUE      = ":SOUR:%s %e";
    protected static final String C_SET_TERMINALS         = ":ROUT:TERM %s";
    protected static final String C_QUERY_TERMINALS       = ":ROUT:TERM?";
    protected static final String C_SET_PROBE_MODE        = ":SENS:RSEN %s";
    protected static final String C_QUERY_PROBE_MODE      = "CURR:RSEN?";
    protected static final String C_SET_AVG_COUNT         = "AVER:COUNT %d";
    protected static final String C_QUERY_AVG_COUNT       = "VOLT:AVER:COUNT?";
    protected static final String C_SET_AVG_MODE          = "AVER:TCON %s";
    protected static final String C_QUERY_AVG_MODE        = "VOLT:AVER:TCON?";
    protected static final String C_SET_AVG_STATE         = "AVER %s";
    protected static final String C_QUERY_AVG_STATE       = "VOLT:AVER?";
    protected static final String C_SET_SRC_RANGE         = ":SOUR:%s:RANG %e";
    protected static final String C_QUERY_SRC_RANGE       = ":SOUR:%s:RANG?";
    protected static final String C_SET_SRC_AUTO_RANGE    = ":SOUR:%s:RANG:AUTO %s";
    protected static final String C_QUERY_SRC_AUTO_RANGE  = ":SOUR:%s:RANG:AUTO?";
    protected static final String C_SET_MEAS_RANGE        = ":SENS:%s:RANG %e";
    protected static final String C_QUERY_MEAS_RANGE      = ":SENS:%s:RANG?";
    protected static final String C_SET_MEAS_AUTO_RANGE   = ":SENS:%s:RANG:AUTO %s";
    protected static final String C_QUERY_MEAS_AUTO_RANGE = ":SENS:%s:RANG:AUTO?";
    protected static final String C_SET_NPLC              = ":SENS:NPLC %f";
    protected static final String C_QUERY_NPLC            = ":SENS:%s:NPLC?";
    protected static final String C_SET_OFF_STATE         = ":OUTP:SMOD %s";
    protected static final String C_QUERY_OFF_STATE       = ":OUTP:SMOD?";
    protected static final String C_SET_LIMIT             = ":SENS:%s:PROTECTION %e";
    protected static final String C_QUERY_LIMIT           = ":SENS:%s:PROTECTION?";
    protected static final String C_QUERY_LFR             = ":SYST:LFR?";

    protected static final String OFF_NORMAL = "NORM";
    protected static final String OFF_ZERO   = "ZERO";
    protected static final String OFF_HIGH_Z = "HIMP";
    protected static final String OFF_GUARD  = "GUAR";

    protected static final String OUTPUT_ON  = "1";
    protected static final String OUTPUT_OFF = "0";

    protected static final String TERMS_FRONT = "FRON";
    protected static final String TERMS_REAR  = "REAR";

    protected final double LINE_FREQUENCY;

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
    private final MeanRepeatFilter   MEAN_REPEAT_V   = new MeanRepeatFilter(
            this::measureVoltage,
            (c) -> disableAveraging()
    );
    private final MeanRepeatFilter   MEAN_REPEAT_I   = new MeanRepeatFilter(
            this::measureCurrent,
            (c) -> disableAveraging()
    );
    private final MeanMovingFilter   MEAN_MOVING_V   = new MeanMovingFilter(
            this::measureVoltage,
            (c) -> disableAveraging()
    );
    private final MeanMovingFilter   MEAN_MOVING_I   = new MeanMovingFilter(
            this::measureCurrent,
            (c) -> disableAveraging()
    );
    private final BypassFilter       NONE_V          = new BypassFilter(
            this::measureVoltage,
            (c) -> disableAveraging()
    );
    private final BypassFilter       NONE_I          = new BypassFilter(
            this::measureCurrent,
            (c) -> disableAveraging()
    );
    protected     double             vLimit;
    protected     double             iLimit;
    private       ReadFilter         filterV         = NONE_V;
    private       ReadFilter         filterI         = NONE_I;
    private       AMode              filterMode      = AMode.NONE;
    private       int                filterCount     = 1;

    public KeithleySCPI(Address address, Class<? extends Driver> prefDriver) throws IOException, DeviceException {

        super(address, prefDriver);

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

        write(":SYSTEM:CLEAR");
        write(":TRAC:CLE"); // clears all readings and statistics from default buffer
        write(":STAT:CLE"); // clears event registers and the event log
        //manuallyClearReadBuffer();
        setAverageMode(AMode.NONE);

        LINE_FREQUENCY = queryDouble(C_QUERY_LFR);
        vLimit         = getVoltageLimit();
        iLimit         = getCurrentLimit();

    }

    public KeithleySCPI(Address address) throws IOException, DeviceException {
        this(address, null);
    }

    public void setFourProbeEnabled(boolean fourProbe) throws IOException {
        write("VOLT:RSEN %s", fourProbe ? OUTPUT_ON : OUTPUT_OFF);
        write("CURR:RSEN %s", fourProbe ? OUTPUT_ON : OUTPUT_OFF);
    }

    public boolean isFourProbeEnabled() throws IOException {
        return query(C_QUERY_PROBE_MODE).trim().equals(OUTPUT_ON);
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
    public AMode getAverageMode() {
        return filterMode;

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

    @Override
    public int getAverageCount() {

        return filterCount;

    }

    @Override
    public void setAverageCount(int count) throws IOException, DeviceException {

        filterCount = count;
        resetFilters();

    }

    @Override
    public double getSourceRange() throws IOException {

        return queryDouble(C_QUERY_SRC_RANGE, getSourceMode().getTag());

    }

    @Override
    public void setSourceRange(double value) throws IOException {

        Source mode = getSourceMode();
        write(C_SET_SRC_AUTO_RANGE, mode.getTag(), OUTPUT_OFF);
        write(C_SET_SRC_RANGE, mode.getTag(), value);

    }

    @Override
    public void useAutoSourceRange() throws IOException {

        write(C_SET_SRC_AUTO_RANGE, getSourceMode().getTag(), OUTPUT_ON);

    }

    @Override
    public boolean isAutoRangingSource() throws IOException {

        return query(C_QUERY_SRC_AUTO_RANGE, getSourceMode().getTag()).equals(OUTPUT_ON);

    }

    @Override
    public double getMeasureRange() throws IOException {

        return queryDouble(C_QUERY_SRC_RANGE, getMeasureMode().getTag());

    }

    @Override
    public void setMeasureRange(double value) throws IOException {

        Source mode = getMeasureMode();
        write(C_SET_SRC_AUTO_RANGE, mode.getTag(), OUTPUT_OFF);
        write(C_SET_SRC_RANGE, mode.getTag(), value);

    }

    @Override
    public void useAutoMeasureRange() throws IOException {

        write(C_SET_SRC_AUTO_RANGE, getMeasureMode().getTag(), OUTPUT_ON);

    }

    @Override
    public boolean isAutoRangingMeasure() throws IOException {

        return query(C_QUERY_SRC_AUTO_RANGE, getMeasureMode().getTag()).equals(OUTPUT_ON);

    }

    private boolean isSourcing(Source func) throws IOException {

        return getSourceMode() == func;

    }

    private boolean isMeasuring(Source func) throws IOException {

        return getMeasureMode() == func;

    }

    @Override
    public double getVoltageRange() throws IOException {

        return queryDouble(isSourcing(Source.VOLTAGE) ? C_QUERY_SRC_RANGE : C_QUERY_MEAS_RANGE, Source.VOLTAGE.getTag());

    }

    @Override
    public void setVoltageRange(double value) throws IOException {

        boolean src = isSourcing(Source.VOLTAGE);

        write(src ? C_SET_SRC_AUTO_RANGE : C_SET_MEAS_AUTO_RANGE, Source.VOLTAGE.getTag(), OUTPUT_OFF);
        write(src ? C_SET_SRC_RANGE : C_SET_MEAS_RANGE, Source.VOLTAGE.getTag(), value);

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
    public boolean isAutoRangingVoltage() throws IOException {

        return query(
                isSourcing(Source.VOLTAGE) ? C_QUERY_SRC_AUTO_RANGE : C_QUERY_MEAS_AUTO_RANGE,
                Source.VOLTAGE.getTag()
        ).equals(OUTPUT_ON);

    }

    @Override
    public double getCurrentRange() throws IOException {

        return queryDouble(isSourcing(Source.CURRENT) ? C_QUERY_SRC_RANGE : C_QUERY_MEAS_RANGE, Source.CURRENT.getTag());

    }

    @Override
    public void setCurrentRange(double value) throws IOException {

        boolean src = isSourcing(Source.CURRENT);

        write(src ? C_SET_SRC_AUTO_RANGE : C_SET_MEAS_AUTO_RANGE, Source.CURRENT.getTag(), OUTPUT_OFF);
        write(src ? C_SET_SRC_RANGE : C_SET_MEAS_RANGE, Source.CURRENT.getTag(), value);

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
    public boolean isAutoRangingCurrent() throws IOException {

        return query(
                isSourcing(Source.CURRENT) ? C_QUERY_SRC_AUTO_RANGE : C_QUERY_MEAS_AUTO_RANGE,
                Source.CURRENT.getTag()
        ).equals(OUTPUT_ON);

    }

    public double getVoltageLimit() throws IOException {

        return queryDouble(C_QUERY_LIMIT, Source.VOLTAGE.getTag());

    }

    public void setVoltageLimit(double limit) throws IOException {

        write(C_SET_LIMIT, Source.VOLTAGE.getTag(), limit);
        vLimit = limit;

    }

    public double getCurrentLimit() throws IOException {

        return queryDouble(C_QUERY_LIMIT, Source.CURRENT.getTag());

    }

    public void setCurrentLimit(double limit) throws IOException {

        write(C_SET_LIMIT, Source.CURRENT.getTag(), limit);
        iLimit = limit;

    }

    public double getOutputLimit() throws IOException {

        switch (getMeasureMode()) {

            case VOLTAGE:
                return getVoltageLimit();

            case CURRENT:
                return getCurrentLimit();

            default:
                return getCurrentLimit();

        }

    }

    public void setOutputLimit(double limit) throws IOException {

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
    public double getIntegrationTime() throws IOException {

        return queryDouble(C_QUERY_NPLC, getMeasureMode().getTag()) / LINE_FREQUENCY;

    }

    @Override
    public void setIntegrationTime(double time) throws IOException {

        write(C_SET_NPLC, LINE_FREQUENCY * time);

    }

    protected double measureVoltage() throws IOException, DeviceException {

        return isOn() ? queryDouble(C_MEASURE_VOLTAGE) : 0.0;

    }

    protected double measureCurrent() throws IOException, DeviceException {

        return isOn() ? queryDouble(C_MEASURE_CURRENT) : 0.0;

    }

    protected void disableAveraging() throws IOException {

        write(C_SET_AVG_MODE, "REPEAT");
        write(C_SET_AVG_COUNT, 1);
        write(C_SET_AVG_STATE, OUTPUT_OFF);

    }

    public double getVoltage() throws DeviceException, IOException {

        return filterV.getValue();

    }

    public void setVoltage(double voltage) throws IOException, DeviceException {

        setSourceValue(Source.VOLTAGE, voltage);

    }

    public double pulseVoltage(double pulseVoltage, double offTime, double measureDelay) throws IOException, DeviceException {

        turnOff();
        setVoltage(pulseVoltage);
        write(":TRIG:LOAD \"EMPTY\"");
        write(":TRIG:BLOCK:DELAY:CONSTANT 1 %e", offTime);
        write(":TRIG:BLOCK:SOURCE:STATE 2 ON");
        write(":TRIG:BLOCK:DELAY:CONSTANT 3 %e", measureDelay);
        write(":TRIG:BLOCK:MEASURE 4");
        write(":TRIG:BLOCK:SOURCE:STATE 5 OFF");
        write(":INITIATE");
        return queryDouble(":FETCH?");

    }

    public double getCurrent() throws IOException, DeviceException {
        return filterI.getValue();
    }

    public void setCurrent(double current) throws IOException, DeviceException {
        setSourceValue(Source.CURRENT, current);
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

    public jisa.enums.Source getSource() throws IOException {
        return Source.fromTag(query(C_QUERY_SOURCE_FUNCTION)).getSMU();
    }

    public void setSource(Source mode) throws IOException {

        if (getSourceMode() != mode) {
            write(C_SET_SOURCE_FUNCTION, mode.getTag());
        }

        setVoltageLimit(vLimit);
        setCurrentLimit(iLimit);

    }

    public void setSource(jisa.enums.Source source) throws IOException {
        setSource(Source.fromSMU(source));
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

    public boolean isOn() throws IOException {
        return query(C_QUERY_OUTPUT_STATE).trim().equals(OUTPUT_ON);
    }

    public void setSourceValue(double value) throws IOException, DeviceException {

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

    public void setSourceValue(Source type, double value) throws IOException, DeviceException {
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

    public Terminals getTerminals() throws IOException {

        String response = query(C_QUERY_TERMINALS);

        if (response.contains(TERMS_FRONT)) {
            return Terminals.FRONT;
        } else if (response.contains(TERMS_REAR)) {
            return Terminals.REAR;
        } else {
            throw new IOException("Invalid response from Keithley");
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

        }

    }

    @Override
    public OffMode getOffMode() throws IOException, DeviceException {

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

    public enum Source {

        VOLTAGE("VOLT", "V", jisa.enums.Source.VOLTAGE),
        CURRENT("CURR", "I", jisa.enums.Source.CURRENT);

        private static HashMap<String, Source>            lookup  = new HashMap<>();
        private static HashMap<jisa.enums.Source, Source> convert = new HashMap<>();

        static {
            for (Source mode : Source.values()) {
                lookup.put(mode.getTag(), mode);
                convert.put(mode.getSMU(), mode);
            }
        }

        private String            tag;
        private String            symbol;
        private jisa.enums.Source orig;

        Source(String tag, String symbol, jisa.enums.Source orig) {
            this.tag    = tag;
            this.symbol = symbol;
            this.orig   = orig;
        }

        public static Source fromTag(String tag) {
            return lookup.getOrDefault(tag.trim(), null);
        }

        public static Source fromSMU(jisa.enums.Source orig) {
            return convert.getOrDefault(orig, null);
        }

        String getTag() {
            return tag;
        }

        String getSymbol() {
            return symbol;
        }

        jisa.enums.Source getSMU() {
            return orig;
        }

    }

}
