package JISA.Devices;

import JISA.Addresses.InstrumentAddress;
import JISA.Util;

import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Pattern;

public class K236 extends SMU {

    // == CONSTANTS ====================================================================================================
    private static final String C_SET_SRC_FUNC   = "F%d,%d";
    private static final String C_SET_BIAS       = "B%f,%d,%d";
    private static final String C_GET_VALUE      = "G%d,%d,%d";
    private static final String C_EXECUTE        = "X";
    private static final String C_SET_SENSE      = "0%d";
    private static final String C_OPERATE        = "N%d";
    private static final String C_NO_TERM        = "Y4";
    private static final String C_TRIGGER        = "H0";
    private static final String C_RESET          = "J0";
    private static final String C_DISABLE_FILTER = "P0";
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

    public K236(InstrumentAddress address) throws IOException, DeviceException {

        super(address);
        setTerminator(C_TRIGGER + C_EXECUTE);
        write(C_RESET);
        turnOff();
        setSourceFunction(Source.VOLTAGE, Function.DC);
        write(C_NO_TERM);
        setAverageMode(AMode.NONE);

        read();

        try {

            if (!getIDN().trim().substring(0, 3).equals("236")) {
                throw new DeviceException("Device at address %s is not a Keithley 236!", address.getVISAAddress());
            }

        } catch (IOException e) {
            throw new DeviceException("Device at address %s is not responding!", address.getVISAAddress());
        }

    }

    public void setBias(double level) throws IOException, DeviceException {

        biasLevel = level;

        switch (source) {

            case VOLTAGE:
                if (!Util.isBetween(level, MIN_VOLTAGE, MAX_VOLTAGE)) {
                    throw new DeviceException("Voltage value of %f V is out of range.", level);
                }
                break;

            case CURRENT:
                if (!Util.isBetween(level, MIN_CURRENT, MAX_CURRENT)) {
                    throw new DeviceException("Current value of %f A is out of range.", level);
                }
                break;

        }

        write(C_SET_BIAS, level, 0, 0);

    }

    public String getIDN() throws IOException {
        return query("U0");
    }

    private double readValue(int channel) throws IOException {

        // TODO: Test that this works with the actual device in actual reality in the actual lab, actually.
        return queryDouble(C_GET_VALUE, channel, FORMAT_CLEAN, ONE_DC_DATA);

    }

    public double getSourceValue() throws IOException, DeviceException {
        return filterS.getValue();
    }

    public double getMeasureValue() throws IOException, DeviceException {
        return filterM.getValue();
    }

    @Override
    public void useFourProbe(boolean fourProbes) throws DeviceException, IOException {
        write(C_SET_SENSE, fourProbes ? SENSE_REMOTE : SENSE_LOCAL);
        remote = fourProbes;
    }

    @Override
    public boolean isUsingFourProbe() throws DeviceException, IOException {
        return remote;
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
    public AMode getAverageMode() throws DeviceException, IOException {
        return filterMode;
    }

    @Override
    public int getAverageCount() throws DeviceException, IOException {
        return filterCount;
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

        setSourceFunction(Source.VOLTAGE, getFunction());
        setBias(voltage);

    }

    @Override
    public void setCurrent(double current) throws IOException, DeviceException {

        setSourceFunction(Source.CURRENT, getFunction());
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
    public boolean isOn() throws DeviceException, IOException {
        return on;
    }

    @Override
    public void setSource(SMU.Source source) throws IOException {
        setSourceFunction(Source.fromSMU(source), getFunction());
    }

    public SMU.Source getSource() {
        return source.getOriginal();
    }

    public Function getFunction() {
        return function;
    }

    public void setSourceFunction(Source s, Function f) throws IOException {
        write(C_SET_SRC_FUNC, s.toInt(), f.toInt());
        source = s;
        function = f;
    }

    public enum Source {

        CURRENT(1, SMU.Source.CURRENT),
        VOLTAGE(0, SMU.Source.VOLTAGE);

        private        int                         c;
        private        SMU.Source                  src;
        private static HashMap<Integer, Source>    lookup  = new HashMap<>();
        private static HashMap<SMU.Source, Source> convert = new HashMap<>();

        static Source fromInt(int i) {
            return lookup.getOrDefault(i, null);
        }

        static Source fromSMU(SMU.Source s) {
            return convert.getOrDefault(s, null);
        }

        static {
            for (Source s : Source.values()) {
                lookup.put(s.toInt(), s);
                convert.put(s.getOriginal(), s);
            }
        }

        Source(int code, SMU.Source s) {
            c = code;
            src = s;
        }

        int toInt() {
            return c;
        }

        SMU.Source getOriginal() {
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
}
