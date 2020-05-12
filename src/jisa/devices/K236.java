package jisa.devices;

import jisa.addresses.Address;
import jisa.control.*;
import jisa.enums.AMode;
import jisa.enums.TType;
import jisa.enums.Terminals;
import jisa.Util;
import jisa.visa.VISADevice;

import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class K236 extends VISADevice implements SMU {

    // == CONSTANTS ====================================================================================================
    private static final String C_SET_SRC_FUNC   = "F%d,%d";
    private static final String C_SET_BIAS       = "B%f,%d,%d";
    private static final String C_GET_VALUE      = "G%d,%d,%d";
    private static final String C_EXECUTE        = "X";
    private static final String C_SET_SENSE      = "O%d";
    private static final String C_OPERATE        = "N%d";
    private static final String C_NO_TERM        = "Y4";
    private static final String C_TRIGGER        = "H0";
    private static final String C_RESET          = "J0";
    private static final String C_DISABLE_FILTER = "P0";
    private static final String C_SET_COMPLIANCE = "L%f,%d";
    private static final String C_GET_STATUS     = "U3";
    private static final String C_GET_PARAMS     = "U4";
    private static final String C_GET_COMPLIANCE = "U5";
    private static final String C_SET_INT_TIME   = "S%d";
    private static final int    OPERATE_OFF      = 0;
    private static final int    OPERATE_ON       = 1;
    private static final int    OUTPUT_NOTHING   = 0;
    private static final int    OUTPUT_SOURCE    = 1;
    private static final int    OUTPUT_DELAY     = 2;
    private static final int    OUTPUT_MEASURE   = 4;
    private static final int    OUTPUT_TIME      = 8;
    private static final int    FORMAT_CLEAN     = 2;
    private static final int    ONE_DC_DATA      = 0;
    private static final int    SENSE_LOCAL      = 0;
    private static final int    SENSE_REMOTE     = 1;
    private static final double MIN_CURRENT      = -100e-3;
    private static final double MAX_CURRENT      = +100e-3;
    private static final double MIN_VOLTAGE      = -110;
    private static final double MAX_VOLTAGE      = +110;

    private double lineFrequency;

    // == FILTERS ======================================================================================================
    private final MedianRepeatFilter MEDIAN_REPEAT_S = new MedianRepeatFilter(
        () -> readValue(OUTPUT_SOURCE),
        (c) -> write(C_DISABLE_FILTER)
    );

    private final MedianRepeatFilter MEDIAN_REPEAT_M = new MedianRepeatFilter(
        () -> readValue(OUTPUT_MEASURE),
        (c) -> write(C_DISABLE_FILTER)
    );

    private final MedianMovingFilter MEDIAN_MOVING_S = new MedianMovingFilter(
        () -> readValue(OUTPUT_SOURCE),
        (c) -> write(C_DISABLE_FILTER)
    );

    private final MedianMovingFilter MEDIAN_MOVING_M = new MedianMovingFilter(
        () -> readValue(OUTPUT_MEASURE),
        (c) -> write(C_DISABLE_FILTER)
    );

    private final MeanRepeatFilter MEAN_REPEAT_S = new MeanRepeatFilter(
        () -> readValue(OUTPUT_SOURCE),
        (c) -> write(C_DISABLE_FILTER)
    );

    private final MeanRepeatFilter MEAN_REPEAT_M = new MeanRepeatFilter(
        () -> readValue(OUTPUT_MEASURE),
        (c) -> write(C_DISABLE_FILTER)
    );

    private final MeanMovingFilter MEAN_MOVING_S = new MeanMovingFilter(
        () -> readValue(OUTPUT_SOURCE),
        (c) -> write(C_DISABLE_FILTER)
    );

    private final MeanMovingFilter MEAN_MOVING_M = new MeanMovingFilter(
        () -> readValue(OUTPUT_MEASURE),
        (c) -> write(C_DISABLE_FILTER)
    );

    private final BypassFilter NONE_S = new BypassFilter(
        () -> readValue(OUTPUT_SOURCE),
        (c) -> write(C_DISABLE_FILTER)
    );

    private final BypassFilter NONE_M = new BypassFilter(
        () -> readValue(OUTPUT_MEASURE),
        (c) -> write(C_DISABLE_FILTER)
    );


    // == INTERNAL VARIABLES ===========================================================================================
    private Source     source      = null;
    private Function   function    = null;
    private double     biasLevel   = 0;
    private boolean    on          = false;
    private boolean    remote      = true;
    private AMode      filterMode  = AMode.NONE;
    private ReadFilter filterS     = NONE_S;
    private ReadFilter filterM     = NONE_M;
    private int        filterCount = 1;
    private SRange     sRange      = SRange.AUTO;
    private SRange     mRange      = SRange.AUTO;
    private double     iLimit      = 0.1;
    private double     vLimit      = 110;
    private double     mLimit      = 0.1;

    public K236(Address address) throws IOException, DeviceException {

        super(address);
        setWriteTerminator(C_TRIGGER + C_EXECUTE);
        write(C_RESET);
        write(C_NO_TERM);
        turnOff();
        setSourceFunction(Source.VOLTAGE, Function.DC);
        useAutoSourceRange();
        useAutoMeasureRange();
        setVoltageLimit(110);
        setCurrentLimit(0.1);
        setAverageMode(AMode.NONE);

        for (int i = 0; i < 10; i++) {
            read();
        }

        try {

            if (!getIDN().trim().substring(0, 3).equals("236")) {
                throw new DeviceException("Device at address %s is not a Keithley 236!", address.toString());
            }

        } catch (IOException e) {
            throw new DeviceException("Device at address %s is not responding!", address.toString());
        }

    }

    public void setBias(double level) throws IOException, DeviceException {

        switch (source) {

            case VOLTAGE:
                if (!Util.isBetween(level, MIN_VOLTAGE, MAX_VOLTAGE)) {
                    throw new DeviceException("Voltage value of %e V is out of range.", level);
                }
                break;

            case CURRENT:
                if (!Util.isBetween(level, MIN_CURRENT, MAX_CURRENT)) {
                    throw new DeviceException("Current value of %e A is out of range.", level);
                }
                break;

        }

        biasLevel = level;
        setBias();

    }

    private void setBias() throws IOException {
        write(C_SET_BIAS, biasLevel, sRange.toInt(), 0);
    }

    private void updateLimit() {

        switch (source) {

            case VOLTAGE:
                mLimit = iLimit;
                break;

            case CURRENT:
                mLimit = vLimit;
                break;

        }

    }

    private void setCompliance() throws IOException {
        write(C_SET_COMPLIANCE, mLimit, mRange.toInt());
    }

    public String getIDN() throws IOException {
        return query("U0").trim();
    }

    private double readValue(int channel) throws IOException {

        return queryDouble(C_GET_VALUE, channel, FORMAT_CLEAN, ONE_DC_DATA);

    }

    public double getSourceValue() throws IOException, DeviceException {
        return filterS.getValue();
    }

    public double getMeasureValue() throws IOException, DeviceException {
        return filterM.getValue();
    }

    @Override
    public void useFourProbe(boolean fourProbes) throws IOException {
        write(C_SET_SENSE, fourProbes ? SENSE_REMOTE : SENSE_LOCAL);
        remote = fourProbes;
    }

    @Override
    public boolean isUsingFourProbe() throws IOException {
        return getMeasureParams().fourProbe;
    }

    private void resetFilters() throws DeviceException, IOException {

        filterS.setCount(filterCount);
        filterM.setCount(filterCount);

        filterS.setUp();
        filterM.setUp();

        filterS.clear();
        filterM.clear();

    }

    @Override
    public void setAverageMode(AMode mode) throws DeviceException, IOException {

        switch (mode) {

            case NONE:
                filterS = NONE_S;
                filterM = NONE_M;
                break;

            case MEAN_REPEAT:
                filterS = MEAN_REPEAT_S;
                filterM = MEAN_REPEAT_M;
                break;

            case MEAN_MOVING:
                filterS = MEAN_MOVING_S;
                filterM = MEAN_MOVING_M;
                break;

            case MEDIAN_REPEAT:
                filterS = MEDIAN_REPEAT_S;
                filterM = MEDIAN_REPEAT_M;
                break;

            case MEDIAN_MOVING:
                filterS = MEDIAN_MOVING_S;
                filterM = MEDIAN_MOVING_M;
                break;

        }

        filterMode = mode;
        resetFilters();

    }

    @Override
    public void setAverageCount(int count) throws DeviceException, IOException {
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

        SRange range;

        switch (getSource()) {

            case VOLTAGE:
                range = SRange.fromVoltage(value, true);
                break;

            case CURRENT:
                range = SRange.fromCurrent(value, true);
                break;

            default:
                range = SRange.AUTO;

        }

        sRange = range;
        setBias();

    }

    @Override
    public double getSourceRange() {

        switch (source) {

            case VOLTAGE:
                return sRange.getVoltage();

            case CURRENT:
                return sRange.getCurrent();

            default:
                return sRange.getVoltage();

        }

    }

    @Override
    public void useAutoSourceRange() throws IOException {
        sRange = SRange.AUTO;
        setBias();
    }

    @Override
    public boolean isAutoRangingSource() {
        return sRange.equals(SRange.AUTO);
    }

    @Override
    public void setMeasureRange(double value) throws IOException {
        SRange range;

        switch (source) {

            case VOLTAGE:
                range = SRange.fromVoltage(value, true);
                break;

            case CURRENT:
                range = SRange.fromCurrent(value, true);
                break;

            default:
                range = SRange.AUTO;

        }

        sRange = range;
        setCompliance();

    }

    @Override
    public double getMeasureRange() {

        switch (source) {

            case VOLTAGE:
                return mRange.getCurrent();

            case CURRENT:
                return mRange.getVoltage();

            default:
                return mRange.getCurrent();

        }

    }

    @Override
    public void useAutoMeasureRange() throws IOException {
        mRange = SRange.AUTO;
        setCompliance();
    }

    @Override
    public boolean isAutoRangingMeasure() {
        return mRange.equals(SRange.AUTO);
    }

    @Override
    public void setVoltageRange(double value) throws DeviceException, IOException {

        switch (source) {

            case VOLTAGE:
                setSourceRange(value);
                break;

            case CURRENT:
                setMeasureRange(value);
                break;

        }

    }

    @Override
    public double getVoltageRange() {

        switch (source) {

            case VOLTAGE:
                return getSourceRange();

            case CURRENT:
                return getMeasureRange();

            default:
                return getSourceRange();

        }

    }

    @Override
    public void useAutoVoltageRange() throws DeviceException, IOException {

        switch (source) {

            case VOLTAGE:
                useAutoSourceRange();
                break;

            case CURRENT:
                useAutoMeasureRange();
                break;

        }

    }

    @Override
    public boolean isAutoRangingVoltage() {

        switch (source) {

            case VOLTAGE:
                return isAutoRangingSource();

            case CURRENT:
                return isAutoRangingMeasure();

            default:
                return isAutoRangingSource();

        }

    }

    @Override
    public void setCurrentRange(double value) throws DeviceException, IOException {

        switch (source) {

            case CURRENT:
                setSourceRange(value);
                break;

            case VOLTAGE:
                setMeasureRange(value);
                break;

        }

    }

    @Override
    public double getCurrentRange() {

        switch (source) {

            case CURRENT:
                return getSourceRange();

            case VOLTAGE:
                return getMeasureRange();

            default:
                return getMeasureRange();

        }

    }

    @Override
    public void useAutoCurrentRange() throws DeviceException, IOException {

        switch (source) {

            case VOLTAGE:
                useAutoMeasureRange();
                break;

            case CURRENT:
                useAutoSourceRange();
                break;

        }

    }

    @Override
    public boolean isAutoRangingCurrent() {

        switch (source) {

            case VOLTAGE:
                return isAutoRangingMeasure();

            case CURRENT:
                return isAutoRangingSource();

            default:
                return isAutoRangingMeasure();

        }

    }

    @Override
    public void setOutputLimit(double value) throws DeviceException, IOException {

        switch (source) {

            case CURRENT:
                if (!Util.isBetween(value, 0, +110)) {
                    throw new DeviceException("Output limit of %e V is out of range.", value);
                }
                vLimit = value;
                break;

            case VOLTAGE:
                if (!Util.isBetween(value, 0, 0.1)) {
                    throw new DeviceException("Output limit of %e A is out of range.", value);
                }
                iLimit = value;
                break;

        }

        mLimit = value;
        setCompliance();
    }

    @Override
    public double getOutputLimit() throws IOException {
        return Double.parseDouble(query(C_GET_COMPLIANCE).substring(3));
    }

    @Override
    public void setVoltageLimit(double voltage) throws IOException {
        vLimit = voltage;
        updateLimit();
        setCompliance();
    }

    @Override
    public double getVoltageLimit() throws IOException {

        if (source.equals(Source.CURRENT)) {
            vLimit = getOutputLimit();
        }

        return vLimit;
    }

    @Override
    public void setCurrentLimit(double current) throws IOException {
        iLimit = current;
        updateLimit();
        setCompliance();
    }

    @Override
    public double getCurrentLimit() throws IOException {

        if (source.equals(Source.VOLTAGE)) {
            iLimit = getOutputLimit();
        }

        return iLimit;

    }

    @Override
    public void setIntegrationTime(double time) throws IOException {
        write(C_SET_INT_TIME, IntTime.fromDouble(time).toInt());
    }

    @Override
    public double getIntegrationTime() throws IOException {
        return getMeasureParams().intTime.toDouble();
    }

    public TType getTerminalType(Terminals terminals) {

        switch (terminals) {

            case FRONT:
                return TType.NONE;

            case REAR:
                return TType.TRIAX;

            default:
                return TType.NONE;

        }

    }

    @Override
    public void setTerminals(Terminals terminals) {

    }

    @Override
    public Terminals getTerminals() {
        return Terminals.REAR;
    }

    @Override
    public void setOffMode(OffMode mode) {
        Util.errLog.println("WARNING: Keithley 236 SMUs do not have configurable off states.");
    }

    @Override
    public OffMode getOffMode() {
        return OffMode.NORMAL;
    }

    public double getVoltage() throws IOException, DeviceException {

        switch (source) {
            case VOLTAGE:
                return getSourceValue();
            case CURRENT:
                return getMeasureValue();
        }

        return 0;

    }

    public double getCurrent() throws IOException, DeviceException {

        switch (source) {
            case VOLTAGE:
                return getMeasureValue();
            case CURRENT:
                return getSourceValue();
        }

        return 0;
    }

    @Override
    public void setVoltage(double voltage) throws IOException, DeviceException {

        if (!source.equals(Source.VOLTAGE)) {
            setSourceFunction(Source.VOLTAGE, getFunction());
        }
        setBias(voltage);

    }

    @Override
    public void setCurrent(double current) throws IOException, DeviceException {

        if (!source.equals(Source.CURRENT)) {
            setSourceFunction(Source.CURRENT, getFunction());
        }
        setBias(current);

    }

    @Override
    public void turnOn() throws IOException {
        write(C_OPERATE, OPERATE_ON);
        on = true;
    }

    @Override
    public void turnOff() throws IOException {
        write(C_OPERATE, OPERATE_OFF);
        on = false;
    }

    @Override
    public boolean isOn() throws IOException {
        return getMachineStatus().on;
    }

    @Override
    public void setSource(jisa.enums.Source source) throws IOException {
        setSourceFunction(Source.fromSMU(source), getFunction());
    }

    public jisa.enums.Source getSource() throws IOException {
        return getMeasureParams().source.getOriginal();
    }

    public Function getFunction() throws IOException {
        return getMeasureParams().function;
    }

    public void setSourceFunction(Source s, Function f) throws IOException {
        write(C_SET_SRC_FUNC, s.toInt(), f.toInt());
        source   = s;
        function = f;
        updateLimit();
        setCompliance();
    }

    private MStatus getMachineStatus() throws IOException {
        return new MStatus(query(C_GET_STATUS));
    }

    private MParams getMeasureParams() throws IOException {
        return new MParams(query(C_GET_PARAMS));
    }

    public enum Source {

        CURRENT(1, jisa.enums.Source.CURRENT),
        VOLTAGE(0, jisa.enums.Source.VOLTAGE);

        private        int                                c;
        private        jisa.enums.Source                  src;
        private static HashMap<Integer, Source>           lookup  = new HashMap<>();
        private static HashMap<jisa.enums.Source, Source> convert = new HashMap<>();

        static Source fromInt(int i) {
            return lookup.getOrDefault(i, null);
        }

        static Source fromSMU(jisa.enums.Source s) {
            return convert.getOrDefault(s, null);
        }

        static {
            for (Source s : Source.values()) {
                lookup.put(s.toInt(), s);
                convert.put(s.getOriginal(), s);
            }
        }

        Source(int code, jisa.enums.Source s) {
            c   = code;
            src = s;
        }

        int toInt() {
            return c;
        }

        jisa.enums.Source getOriginal() {
            return src;
        }

    }

    public enum Function {

        DC(0),
        SWEEP(1);

        private        int                        c;
        private static HashMap<Integer, Function> lookup = new HashMap<>();

        static Function fromInt(int i) {
            return lookup.getOrDefault(i, null);
        }

        static {
            for (Function f : Function.values()) {
                lookup.put(f.toInt(), f);
            }
        }

        Function(int code) {
            c = code;
        }

        int toInt() {
            return c;
        }
    }

    private enum SRange {

        AUTO(0, 0, 0),
        R_1NA_1_1V(1, 1e-9, 1.1),
        R_10NA_11V(2, 10e-9, 11),
        R_100NA_110V(3, 100e-9, 110),
        R_1UA(4, 1e-6, -1),
        R_10UA(5, 10e-6, -1),
        R_100UA(6, 100e-6, -1),
        R_1MA(7, 1e-3, -1),
        R_10MA(8, 10e-3, -1),
        R_100MA(9, 100e-3, -1);

        private static final HashMap<Integer, SRange> lookup = new HashMap<>();

        static {
            for (SRange r : values()) {
                lookup.put(r.toInt(), r);
            }
        }

        static SRange fromInt(int value) {
            return lookup.getOrDefault(value, null);
        }

        static SRange fromVoltage(double value, boolean over) {

            SRange found = R_100NA_110V;

            for (SRange r : values()) {
                if ((Math.abs(r.getVoltage() - value) < Math.abs(found.getVoltage() - value)) && (r.getVoltage() >= value || !over) && (r.getVoltage() <= value || over)) {
                    found = r;
                }
            }

            return found;

        }

        static SRange fromCurrent(double value, boolean over) {

            SRange found = R_100MA;

            for (SRange r : values()) {
                if ((Math.abs(r.getCurrent() - value) < Math.abs(found.getCurrent() - value)) && (r.getCurrent() >= value || !over) && (r.getCurrent() <= value || over)) {
                    found = r;
                }
            }

            return found;

        }

        private int    mode;
        private double current;
        private double voltage;

        SRange(int mode, double current, double voltage) {
            this.mode    = mode;
            this.current = current;
            this.voltage = voltage;
        }

        int toInt() {
            return mode;
        }

        double getCurrent() {
            return current;
        }

        double getVoltage() {
            return voltage;
        }

    }

    private enum IntTime {

        S0(0, 416e-6),
        S1(1, 4e-3),
        S2(2, 16.67e-3),
        S3(3, 20e-3);

        private int    code;
        private double time;

        public static IntTime fromInt(int i) {

            for (IntTime it : values()) {
                if (it.toInt() == i) {
                    return it;
                }
            }

            return null;

        }

        public static IntTime fromDouble(double time) {

            IntTime found = S3;

            for (IntTime it : values()) {
                if (it.toDouble() >= time && it.toDouble() < found.toDouble()) {
                    found = it;
                }
            }

            return found;

        }

        IntTime(int c, double t) {
            code = c;
            time = t;
        }

        public int toInt() {
            return code;
        }

        public double toDouble() {
            return time;
        }

    }

    private static class MStatus {

        private static final Pattern PATTERN = Pattern.compile(
            "MSTG([0-9]{2}),([0-9]),([0-9])K([0-3])M([0-9]{3}),([0-9])N([0-1])R([0-1])T([0-4]),([0-8]),([0-8]),([0-1])V([0-1])Y([0-4])"
        );

        public int     items;
        public int     format;
        public int     lines;
        public int     EOI;
        public int     mask;
        public boolean on;
        public boolean triggering;


        public MStatus(String response) {

            Matcher matcher = PATTERN.matcher(response);

            if (matcher.find()) {

                items      = Integer.parseInt(matcher.group(1).trim());
                format     = Integer.parseInt(matcher.group(2).trim());
                lines      = Integer.parseInt(matcher.group(3).trim());
                EOI        = Integer.parseInt(matcher.group(4).trim());
                mask       = Integer.parseInt(matcher.group(5).trim());
                on         = matcher.group(7).trim().equals("1");
                triggering = matcher.group(8).trim().equals("1");

            }

        }

    }

    private static class MParams {

        private static final Pattern PATTERN = Pattern.compile(
            "[IV]MPL,([0-9]{2})F([0-1]),([0-1])O([0-1])P([0-5])S([0-3])W([0-1])Z([0-1])"
        );

        public SRange   mRange;
        public Source   source;
        public Function function;
        public boolean  fourProbe;
        public IntTime  intTime;

        public MParams(String response) {

            Matcher matcher = PATTERN.matcher(response);

            if (matcher.find()) {

                mRange    = SRange.fromInt(Integer.parseInt(matcher.group(1).trim()));
                source    = Source.fromInt(Integer.parseInt(matcher.group(2).trim()));
                function  = Function.fromInt(Integer.parseInt(matcher.group(3).trim()));
                fourProbe = matcher.group(4).trim().equals("1");
                intTime   = IntTime.fromInt(Integer.parseInt(matcher.group(6).trim()));

            }

        }

    }

}
