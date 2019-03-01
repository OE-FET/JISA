package JISA.Devices;

import JISA.Addresses.InstrumentAddress;
import com.sun.javafx.UnmodifiableArrayList;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class K2400 extends SMU {

    private static final String C_MEASURE_VOLTAGE = ":MEAS:VOLT?";
    private static final String C_MEASURE_CURRENT = ":MEAS:CURR?";
    private static final String C_SET_AVG_COUNT   = "AVER:COUNT %d";
    private static final String C_QUERY_AVG_COUNT = "VOLT:AVER:COUNT?";
    private static final String C_SET_AVG_MODE    = "AVER:TCON %s";
    private static final String C_QUERY_AVG_MODE  = "VOLT:AVER:TCON?";
    private static final String C_SET_AVG_STATE   = "AVER %s";
    private static final String C_QUERY_AVG_STATE = "VOLT:AVER?";
    private static final String OUTPUT_ON         = "1";
    private static final String OUTPUT_OFF        = "0";
    private final        Model  MODEL;

    private enum Model {
        K2400,
        K2410,
        K2420,
        K2425,
        K2430,
        K2440
    }

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

    public K2400(InstrumentAddress address) throws IOException, DeviceException {

        super(address);

        String  idn     = getIDN();
        Matcher matcher = Pattern.compile("MODEL (2400|2410|2420|2425|2430|2440)").matcher(idn.toUpperCase());

        if (!matcher.find()) {
            throw new DeviceException(
                    "The device at address \"%s\" is not a Keithley 2400 series SMU." +
                            "\nThe K2400 driver only works with models 2400, 2410, 2420, 2425, 2430, 2440." +
                            "\nFor model 2450, please use the K2450 driver.",
                    address.getVISAAddress()
            );
        } else {
            MODEL = Model.valueOf("K" + matcher.group(1).trim());
        }

        setAverageMode(AMode.NONE);
    }

    @Override
    public double getVoltage() throws DeviceException, IOException {
        return filterV.getValue();
    }

    @Override
    public double getCurrent() throws DeviceException, IOException {
        return filterI.getValue();
    }

    @Override
    public void setVoltage(double voltage) throws DeviceException, IOException {
        write(":SOUR:VOLT %e", voltage);
        write(":SOUR:FUNC VOLT");
    }

    @Override
    public void setCurrent(double current) throws DeviceException, IOException {
        write(":SOUR:CURR %e", current);
        write(":SOUR:FUNC CURR");
    }

    @Override
    public void turnOn() throws IOException {
        write(":OUTP:STATE ON");
    }

    @Override
    public void turnOff() throws IOException {
        write(":OUTP:STATE OFF");
    }

    @Override
    public boolean isOn() throws IOException {
        return query(":OUTP:STATE?").trim().equals(OUTPUT_ON);
    }

    @Override
    public void setSource(Source source) throws IOException {

        switch (source) {

            case VOLTAGE:
                write(":SOUR:FUNC VOLT");
                break;

            case CURRENT:
                write(":SOUR:FUNC CURR");
                break;

        }

    }

    @Override
    public Source getSource() throws IOException {

        String response = query(":SOUR:FUNC?").trim();

        if (response.equals("VOLT")) {
            return Source.VOLTAGE;
        } else if (response.equals("CURR")) {
            return Source.CURRENT;
        } else {
            throw new IOException("Invalid response from Keithley 2400");
        }

    }

    @Override
    public void setBias(double level) throws DeviceException, IOException {

        switch (getSource()) {

            case VOLTAGE:
                setVoltage(level);
                break;

            case CURRENT:
                setCurrent(level);
                break;

        }

    }

    @Override
    public double getSourceValue() throws DeviceException, IOException {

        switch (getSource()) {

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

        switch (getSource()) {

            case VOLTAGE:
                return getCurrent();

            case CURRENT:
                return getVoltage();

            default:
                return getCurrent();

        }

    }

    @Override
    public void useFourProbe(boolean fourProbes) throws IOException {
        write(":SENS:RSEN %d", fourProbes ? 1 : 0);
    }

    @Override
    public boolean isUsingFourProbe() throws IOException {
        return query(":SENS:RSEN?").trim().equals("1");
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
    public void setSourceRange(double value) throws DeviceException, IOException {

    }

    @Override
    public double getSourceRange() throws DeviceException, IOException {
        return 0;
    }

    @Override
    public void useAutoSourceRange() throws DeviceException, IOException {

    }

    @Override
    public boolean isSourceRangeAuto() throws DeviceException, IOException {
        return false;
    }

    @Override
    public void setMeasureRange(double value) throws DeviceException, IOException {

    }

    @Override
    public double getMeasureRange() throws DeviceException, IOException {
        return 0;
    }

    @Override
    public void useAutoMeasureRange() throws DeviceException, IOException {

    }

    @Override
    public boolean isMeasureRangeAuto() throws DeviceException, IOException {
        return false;
    }

    @Override
    public void setVoltageRange(double value) throws DeviceException, IOException {

    }

    @Override
    public double getVoltageRange() throws DeviceException, IOException {
        return 0;
    }

    @Override
    public void useAutoVoltageRange() throws DeviceException, IOException {

    }

    @Override
    public boolean isVoltageRangeAuto() throws DeviceException, IOException {
        return false;
    }

    @Override
    public void setCurrentRange(double value) throws DeviceException, IOException {

    }

    @Override
    public double getCurrentRange() throws DeviceException, IOException {
        return 0;
    }

    @Override
    public void useAutoCurrentRange() throws DeviceException, IOException {

    }

    @Override
    public boolean isCurrentRangeAuto() throws DeviceException, IOException {
        return false;
    }

    @Override
    public void setOutputLimit(double value) throws DeviceException, IOException {

    }

    @Override
    public double getOutputLimit() throws DeviceException, IOException {
        return 0;
    }

    @Override
    public void setVoltageLimit(double voltage) throws DeviceException, IOException {

    }

    @Override
    public double getVoltageLimit() throws DeviceException, IOException {
        return 0;
    }

    @Override
    public void setCurrentLimit(double current) throws DeviceException, IOException {

    }

    @Override
    public double getCurrentLimit() throws DeviceException, IOException {
        return 0;
    }

    @Override
    public void setIntegrationTime(double time) throws DeviceException, IOException {

    }

    @Override
    public double getIntegrationTime() throws DeviceException, IOException {
        return 0;
    }

    public TType getTerminalType(Terminals terminals) {
        return TType.BANANA;
    }

    @Override
    public void setTerminals(Terminals terminals) throws IOException {

        switch (terminals) {

            case FRONT:
                write(":ROUT:TERM FRONT");
                break;

            case REAR:
                write(":ROUT:TERM REAR");
                break;

        }

    }

    @Override
    public Terminals getTerminals() throws IOException {

        String response = query(":ROUT:TERM?");

        if (response.contains("FRON")) {
            return Terminals.FRONT;
        } else if (response.contains("REAR")) {
            return Terminals.REAR;
        } else {
            throw new IOException("Invalid response from Keithley 2400");
        }

    }

    @Override
    public void setOffMode(OffMode mode) throws DeviceException, IOException {

    }

    @Override
    public OffMode getOffMode() throws DeviceException, IOException {
        return null;
    }

    @Override
    public void setOffVoltageLimit(double limit) throws DeviceException, IOException {

    }

    @Override
    public void setOffCurrentLimit(double limit) throws DeviceException, IOException {

    }

    @Override
    public double getOffVoltageLimit() throws DeviceException, IOException {
        return 0;
    }

    @Override
    public double getOffCurrentLimit() throws DeviceException, IOException {
        return 0;
    }
}
