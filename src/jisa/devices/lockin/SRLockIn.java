package jisa.devices.lockin;

import jisa.Util;
import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.SubInstrument;
import jisa.devices.features.*;
import jisa.devices.source.FSource;
import jisa.visa.VISADevice;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public abstract class SRLockIn<L extends SRLockIn> extends VISADevice implements DPLockIn, CurrentPreAmpInput, SyncFilter, LineFilter, LineFilter2X, InternalFrequencyReference<SRLockIn<L>.Oscillator> {

    private static final String C_QUERY_FREQ         = "FREQ?";
    private static final String C_SET_FREQ           = "FREQ %f";
    private static final String C_QUERY_PHASE        = "PHAS?";
    private static final String C_SET_PHASE          = "PHAS %f";
    private static final String C_QUERY_INT_AMP      = "SLVL?";
    private static final String C_SET_INT_AMP        = "SLVL %f";
    private static final String C_QUERY_REF          = "FMOD?";
    private static final String C_SET_REF            = "FMOD %d";
    private static final String C_QUERY_SENSITIVITY  = "SENS?";
    private static final String C_SET_SENSITIVITY    = "SENS %d";
    private static final String C_QUERY_OUTPUT       = "OUTP? %d";
    private static final String C_QUERY_TIME_CONST   = "OFLT?";
    private static final String C_SET_TIME_CONST     = "OFLT %d";
    private static final String C_QUERY_ALL          = "SNAP? 1,2,3,4,9";
    private static final String C_QUERY_FILTER       = "OFSL?";
    private static final String C_SET_FILTER         = "OFSL %d";
    private static final String C_QUERY_SYNC         = "SYNC?";
    private static final String C_SET_SYNC           = "SYNC %d";
    private static final String C_QUERY_COUPLING     = "ICPL?";
    private static final String C_SET_COUPLING       = "ICPL %d";
    private static final String C_QUERY_GROUND       = "IGND?";
    private static final String C_SET_GROUND         = "IGND %d";
    private static final String C_QUERY_LINE         = "ILIN?";
    private static final String C_SET_LINE           = "ILIN %d";
    private static final String C_SET_SOURCE         = "ISRC %d";
    private static final String C_QUERY_SOURCE       = "ISRC?";
    private static final String C_QUERY_OFF_EXP      = "OEXP? %d";
    private static final String C_SET_OFF_EXP        = "OEXP? %d,%.02f,%d";
    private static final String C_AUTO_OFFSET        = "AOFF %d";
    private static final String C_QUERY_TRIGGER      = "RSLP?";
    private static final String C_SET_TRIGGER        = "RSLP %d";
    private static final int    OUTPUT_X             = 1;
    private static final int    OUTPUT_Y             = 2;
    private static final int    OUTPUT_R             = 3;
    private static final int    OUTPUT_T             = 4;
    private static final int    SOURCE_VOLT_SINGLE   = 0;
    private static final int    SOURCE_VOLT_DIFF     = 1;
    private static final int    SOURCE_CURR_LOW_IMP  = 2;
    private static final int    SOURCE_CURR_HIGH_IMP = 3;
    private static final int    REFERENCE_EXTERNAL   = 0;
    private static final int    REFERENCE_INTERNAL   = 1;
    private static final int    TRIGGER_SINE         = 0;
    private static final int    TRIGGER_POS_TTL      = 1;
    private static final int    TRIGGER_NEG_TTL      = 2;
    private static final double STANDARD_ERROR       = 1.0;
    private static final int    STANDARD_INTERVAL    = 100;
    private static final long   STANDARD_DURATION    = 10000;

    private static final int COUPLING_AC = 0;
    private static final int COUPLING_DC = 1;
    private static final int GND_FLOAT   = 0;
    private static final int GND_GROUND  = 1;
    private static final int LINE_NONE   = 0;
    private static final int LINE_X1     = 1;
    private static final int LINE_X2     = 2;
    private static final int LINE_X1_X2  = 3;

    private int currentMode = SOURCE_CURR_LOW_IMP;
    private int voltageMode = SOURCE_VOLT_SINGLE;

    private Sensitivity currentSensitivity;
    private Sensitivity voltageSensitivity;

    private final String              MODEL;
    private final double              MIN_FREQUENCY;
    private final double              MAX_FREQUENCY;
    private final List<Sensitivity>   SENSITIVITIES;
    private final List<TimeConstant>  TIME_CONSTANTS;
    private final List<FilterRollOff> FILTER_ROLLOFFS;

    public final Oscillator INTERNAL_OSCILLATOR = new Oscillator();

    public SRLockIn(Address address, String model, double minFrequency, double maxFrequency, List<Sensitivity> sensitivities, List<TimeConstant> timeConstants, List<FilterRollOff> filterRolloffs) throws IOException, DeviceException {

        super(address);

        MODEL           = model;
        MIN_FREQUENCY   = minFrequency;
        MAX_FREQUENCY   = maxFrequency;
        SENSITIVITIES   = sensitivities;
        TIME_CONSTANTS  = timeConstants;
        FILTER_ROLLOFFS = filterRolloffs;

        configSerial(serial -> {
            serial.setSerialParameters(9600, 8);
            setReadTerminator("\r");
            setWriteTerminator("\r");
        });

        configGPIB(gpib -> {
            setReadTerminator("\n");
            setWriteTerminator("\n");
        });

        addAutoRemove("\n", "\r");

        manuallyClearReadBuffer();

        try {

            String[] idn = query("*IDN?").split(",");
            if (!idn[1].trim().equals(MODEL)) {
                throw new DeviceException("Device at address \"%s\" is not an SRS %s!", address.toString(), MODEL);
            }

        } catch (IOException e) {
            throw new DeviceException("Device at address \"%s\" is not responding!", address.toString());
        }

        if (isInternalReferenceEnabled()) {
            INTERNAL_OSCILLATOR.frequency = getFrequency();
        }

        voltageSensitivity = sensitivityFromInt(queryInt(C_QUERY_SENSITIVITY));
        currentSensitivity = voltageSensitivity;

    }

    protected Sensitivity sensitivityFromInt(int code) throws IOException {

        return SENSITIVITIES.stream()
                            .filter(s -> s.getCode() == code)
                            .findAny()
                            .orElseThrow(() -> new IOException(String.format(
                                "Unknown sensitivity code (%d) in response from %s.",
                                code,
                                MODEL
                            )));

    }

    protected Sensitivity sensitivityFromVoltage(double voltage) throws DeviceException {

        final double v = Math.abs(voltage);

        return SENSITIVITIES.stream()
                            .filter(s -> s.getVoltage() >= v)
                            .min(Comparator.comparingDouble(Sensitivity::getVoltage))
                            .orElseThrow(() -> new DeviceException(
                                "Voltage range of %.02e is out of range for %s.",
                                v, MODEL
                            ));

    }

    protected Sensitivity sensitivityFromCurrent(double current) throws DeviceException {

        final double i = Math.abs(current);

        return SENSITIVITIES.stream()
                            .filter(s -> s.getCurrent() >= i)
                            .min(Comparator.comparingDouble(Sensitivity::getCurrent))
                            .orElseThrow(() -> new DeviceException(
                                "Current range of %.02e is out of range for %s.",
                                i, MODEL
                            ));

    }

    protected TimeConstant timeConstantFromInt(int code) throws IOException {

        return TIME_CONSTANTS.stream()
                             .filter(tc -> tc.getCode() == code)
                             .findAny()
                             .orElseThrow(() -> new IOException(String.format(
                                 "Unknown time-constant code (%d) in response from %s.",
                                 code,
                                 MODEL
                             )));

    }

    protected TimeConstant timeConstantFromTime(double time) throws DeviceException {

        final double t = Math.abs(time);

        return TIME_CONSTANTS.stream()
                             .filter(s -> s.getTime() >= t)
                             .min(Comparator.comparingDouble(TimeConstant::getTime))
                             .orElseThrow(() -> new DeviceException(
                                 "Integration time of %.02e s is out of range for %s.",
                                 t, MODEL
                             ));

    }

    protected FilterRollOff filterRollOffFromInt(int code) throws IOException {

        return FILTER_ROLLOFFS.stream()
                              .filter(s -> s.getCode() == code)
                              .findAny()
                              .orElseThrow(() -> new IOException(String.format(
                                  "Unknown filter roll-off code (%d) in response from %s.",
                                  code,
                                  MODEL
                              )));

    }

    protected FilterRollOff filterRollOffFromDB(double db) throws DeviceException {

        final double ro = Math.abs(db);

        return FILTER_ROLLOFFS.stream()
                              .filter(s -> s.getDbPerOct() >= ro)
                              .min(Comparator.comparingDouble(FilterRollOff::getDbPerOct))
                              .orElseThrow(() -> new DeviceException(
                                  "Filter roll-off of %.02e dB/oct is out of range for %s.",
                                  ro, MODEL
                              ));

    }

    public static String getDescription() {
        return "Stanford Research Systems SR830";
    }

    @Override
    public void turnOn() throws IOException, DeviceException {
        /* Nothing to do */
    }

    @Override
    public void turnOff() throws IOException, DeviceException {
        /* Nothing to do */
    }

    @Override
    public boolean isOn() throws IOException, DeviceException {
        return true;
    }

    @Override
    public double getFrequency() throws IOException {
        return queryDouble(C_QUERY_FREQ);
    }

    @Override
    public double getFrequencyRange() throws IOException, DeviceException {
        return 102.4e3;
    }

    @Override
    public void setFrequencyRange(double range) throws IOException, DeviceException {

        if (Math.abs(range) > 102.4e3) {
            throw new DeviceException("Specified frequency range is out of range for SR830.");
        }

    }

    @Override
    public void setInternalReferenceEnabled(boolean enabled) throws IOException {

        write(C_SET_REF, enabled ? REFERENCE_INTERNAL : REFERENCE_EXTERNAL);

        if (enabled) {
            INTERNAL_OSCILLATOR.writeFrequency();
        }

    }

    @Override
    public boolean isInternalReferenceEnabled() throws IOException {
        return queryInt(C_QUERY_REF) == REFERENCE_INTERNAL;
    }

    @Override
    public Oscillator getInternalReferenceOscillator() {
        return INTERNAL_OSCILLATOR;
    }

    @Override
    public double getLockedX() throws IOException {
        return queryDouble(C_QUERY_OUTPUT, OUTPUT_X);
    }

    @Override
    public double getLockedY() throws IOException {
        return queryDouble(C_QUERY_OUTPUT, OUTPUT_Y);
    }

    @Override
    public double getLockedAmplitude() throws IOException {
        return queryDouble(C_QUERY_OUTPUT, OUTPUT_R);
    }

    @Override
    public double getLockedPhase() throws IOException {
        return queryDouble(C_QUERY_OUTPUT, OUTPUT_T);
    }

    @Override
    public double getPhaseShift() throws IOException {
        return queryDouble(C_QUERY_PHASE);
    }

    @Override
    public void setPhaseShift(double offset) throws IOException {
        write(C_SET_PHASE, offset);
    }

    @Override
    public void autoShiftPhase() throws IOException {
        write("APHS");
    }

    @Override
    public double getXOffset() throws IOException {
        return querySplitSingleDouble(C_QUERY_OFF_EXP, ",", 0, OUTPUT_X);
    }

    @Override
    public void setXOffset(double offset) throws IOException {
        int expand = querySplitSingleInt(C_QUERY_OFF_EXP, ",", 1, OUTPUT_X);
        write(C_SET_OFF_EXP, OUTPUT_X, offset, expand);
    }

    @Override
    public double getIntegrationTime() throws IOException {
        return timeConstantFromInt(queryInt(C_QUERY_TIME_CONST)).getTime();
    }

    @Override
    public void setIntegrationTime(double seconds) throws IOException, DeviceException {
        write(C_SET_TIME_CONST, timeConstantFromTime(seconds).getCode());
    }

    @Override
    public double getVoltageRange() throws IOException {
        return voltageSensitivity.getVoltage();
    }

    @Override
    public void setVoltageRange(double range) throws IOException, DeviceException {

        voltageSensitivity = sensitivityFromVoltage(range);

        if (!isCurrentInputEnabled()) {
            write(C_SET_SENSITIVITY, voltageSensitivity.getCode());
        }

    }

    @Override
    public void setSyncFilterEnabled(boolean flag) throws IOException {
        write(C_SET_SYNC, flag ? 1 : 0);
    }

    @Override
    public boolean isSyncFilterEnabled() throws IOException {
        return queryInt(C_QUERY_SYNC) == 1;
    }

    @Override
    public double getLowPassRollOff() throws IOException {
        return filterRollOffFromInt(queryInt(C_QUERY_FILTER)).getDbPerOct();
    }

    @Override
    public void setLowPassRollOff(double dBperOct) throws IOException, DeviceException {
        write(C_SET_FILTER, filterRollOffFromDB(dBperOct).getCode());
    }

    @Override
    public boolean isCouplingAC() throws IOException {

        switch (queryInt(C_QUERY_COUPLING)) {

            case COUPLING_AC:
                return true;

            case COUPLING_DC:
                return false;

            default:
                throw new IOException(String.format("Unexpected response from %s.", MODEL));

        }
    }

    @Override
    public void setCouplingAC(boolean ac) throws IOException {
        write(C_SET_COUPLING, ac ? COUPLING_AC : COUPLING_DC);
    }

    @Override
    public boolean isShieldGrounded() throws IOException {
        return queryInt(C_QUERY_GROUND) == GND_GROUND;
    }

    @Override
    public void setShieldGrounded(boolean mode) throws IOException {
        write(C_SET_GROUND, mode ? GND_GROUND : GND_FLOAT);
    }

    @Override
    public boolean isDifferentialInputEnabled() throws IOException {

        int mode = queryInt(C_QUERY_SOURCE);

        switch (mode) {

            case SOURCE_CURR_LOW_IMP:
            case SOURCE_CURR_HIGH_IMP:
            case SOURCE_VOLT_SINGLE:
                return false;

            case SOURCE_VOLT_DIFF:
                return true;

        }

        return false;

    }

    @Override
    public void setDifferentialInputEnabled(boolean differential) throws IOException {

        voltageMode = differential ? SOURCE_VOLT_DIFF : SOURCE_VOLT_SINGLE;

        if (!isCurrentInputEnabled()) {
            write(C_SET_SOURCE, voltageMode);
        }

    }

    @Override
    public double getAmplitudeOffset() throws IOException {
        return querySplitSingleDouble(C_QUERY_OFF_EXP, ",", 0, OUTPUT_R);
    }

    @Override
    public void setAmplitudeOffset(double offset) throws IOException {
        int expand = querySplitSingleInt(C_QUERY_OFF_EXP, ",", 1, OUTPUT_R);
        write(C_SET_OFF_EXP, OUTPUT_R, offset, expand);
    }

    @Override
    public void autoOffsetX() throws IOException {
        write(C_AUTO_OFFSET, OUTPUT_X);
        Util.sleep(100);
    }

    @Override
    public double getYOffset() throws IOException {
        return querySplitSingleDouble(C_QUERY_OFF_EXP, ",", 0, OUTPUT_Y);
    }

    @Override
    public void setYOffset(double offset) throws IOException {
        int expand = querySplitSingleInt(C_QUERY_OFF_EXP, ",", 1, OUTPUT_Y);
        write(C_SET_OFF_EXP, OUTPUT_Y, offset, expand);
    }

    @Override
    public void autoOffsetY() throws IOException {
        write(C_AUTO_OFFSET, OUTPUT_Y);
        Util.sleep(100);
    }

    @Override
    public void autoOffsetAmplitude() throws IOException {
        write(C_AUTO_OFFSET, OUTPUT_R);
        Util.sleep(100);
    }

    protected void autoRange(double factor, boolean offset) throws IOException, DeviceException, InterruptedException {

        int         waitTime = 3 * ((int) (1e3 * getIntegrationTime()));
        Sensitivity found    = null;

        List<Sensitivity> sorted = SENSITIVITIES.stream()
                                                .sorted(Comparator.comparingDouble(Sensitivity::getVoltage))
                                                .collect(Collectors.toList());

        boolean current = isCurrentInputEnabled();

        for (Sensitivity sensitivity : sorted) {

            if (Thread.interrupted()) {
                throw new InterruptedException();
            }

            write(C_SET_SENSITIVITY, sensitivity.getCode());

            Thread.sleep(waitTime);

            if (offset) {
                autoOffsetAmplitude();
                autoOffsetX();
                autoOffsetY();
            }

            double lockedX = Math.abs(getLockedX());
            double lockedY = Math.abs(getLockedY());
            double lockedR = Math.abs(getLockedAmplitude());
            double max     = Math.abs((current ? sensitivity.getCurrent() : sensitivity.getVoltage()) * factor);

            if ((lockedX <= max) && (lockedY <= max) && (lockedR <= max)) {
                found = sensitivity;
                break;
            }

        }

        if (found == null) {
            throw new DeviceException("Auto ranging %s failed: input is saturated.", MODEL);
        }

        setVoltageRange(found.getVoltage());

    }

    @Override
    public void autoRange(double factor) throws IOException, DeviceException, InterruptedException {
        autoRange(factor, false);
    }

    @Override
    public void autoRangeOffset(double factor) throws IOException, DeviceException, InterruptedException {
        autoRange(factor, true);
    }

    @Override
    public Trigger getReferenceTriggerMode() throws IOException {

        switch (queryInt(C_QUERY_TRIGGER)) {

            case TRIGGER_SINE:
                return Trigger.SINE;

            case TRIGGER_POS_TTL:
                return Trigger.POS_TTL;

            case TRIGGER_NEG_TTL:
                return Trigger.NEG_TTL;

            default:
                throw new IOException(String.format("Unexpected response to %s from %s.", C_QUERY_TRIGGER, MODEL));

        }

    }

    @Override
    public void setReferenceTriggerMode(Trigger mode) throws IOException, DeviceException {

        switch (mode) {

            case SINE:
                write(C_SET_TRIGGER, TRIGGER_SINE);
                break;

            case POS_TTL:
                write(C_SET_TRIGGER, TRIGGER_POS_TTL);
                break;

            case NEG_TTL:
                write(C_SET_TRIGGER, TRIGGER_NEG_TTL);
                break;

            default:
                throw new DeviceException("%s does not support %s trigger mode.", MODEL, mode.toString());

        }

    }

    @Override
    public void setCurrentInputEnabled(boolean flag) throws IOException {

        write(C_SET_SOURCE, flag ? currentMode : voltageMode);
        write(C_SET_SENSITIVITY, (flag ? currentSensitivity : voltageSensitivity).getCode());

    }

    @Override
    public boolean isCurrentInputEnabled() throws IOException {

        switch (queryInt(C_QUERY_SOURCE)) {

            case SOURCE_CURR_LOW_IMP:
            case SOURCE_CURR_HIGH_IMP:
                return true;

            default:
                return false;

        }

    }

    @Override
    public void setCurrentGain(double voltsPerAmp) throws IOException {
        currentMode = voltsPerAmp >= 1e8 ? SOURCE_CURR_HIGH_IMP : SOURCE_CURR_LOW_IMP;
        setCurrentInputEnabled(isCurrentInputEnabled());
    }

    @Override
    public double getCurrentGain() throws DeviceException {

        switch (currentMode) {

            case SOURCE_CURR_LOW_IMP:
                return 1e6;

            case SOURCE_CURR_HIGH_IMP:
                return 1e8;

            default:
                throw new DeviceException("Unknown current mode");

        }

    }

    @Override
    public void setCurrentRange(double currentRange) throws IOException, DeviceException {

        currentSensitivity = sensitivityFromCurrent(currentRange);

        if (isCurrentInputEnabled()) {
            write(C_SET_SENSITIVITY, currentSensitivity.getCode());
        }

    }

    @Override
    public double getCurrentRange() throws IOException, DeviceException {
        return currentSensitivity.getCurrent();
    }

    @Override
    public void setLineFilterEnabled(boolean enabled) throws IOException, DeviceException {

        if (is2xLineFilterEnabled()) {
            write(C_SET_LINE, enabled ? LINE_X1_X2 : LINE_X2);
        } else {
            write(C_SET_LINE, enabled ? LINE_X1 : LINE_NONE);
        }

    }

    @Override
    public boolean isLineFilterEnabled() throws IOException, DeviceException {

        switch (queryInt(C_QUERY_LINE)) {

            case LINE_X1:
            case LINE_X1_X2:
                return true;

            default:
                return false;

        }

    }

    @Override
    public void set2xLineFilterEnabled(boolean enabled) throws IOException, DeviceException {

        if (isLineFilterEnabled()) {
            write(C_SET_LINE, enabled ? LINE_X1_X2 : LINE_X1);
        } else {
            write(C_SET_LINE, enabled ? LINE_X2 : LINE_NONE);
        }

    }

    @Override
    public boolean is2xLineFilterEnabled() throws IOException {

        switch (queryInt(C_QUERY_LINE)) {

            case LINE_X2:
            case LINE_X1_X2:
                return true;

            default:
                return false;

        }

    }

    public static class Sensitivity {

        private final int    code;
        private final double voltage;
        private final double current;

        public Sensitivity(int code, double voltage, double current) {
            this.code    = code;
            this.voltage = voltage;
            this.current = current;
        }

        public int getCode() {
            return code;
        }

        public double getVoltage() {
            return voltage;
        }

        public double getCurrent() {
            return current;
        }

    }

    public static class TimeConstant {

        private final int    code;
        private final double time;

        public TimeConstant(int code, double time) {
            this.code = code;
            this.time = time;
        }

        public int getCode() {
            return code;
        }

        public double getTime() {
            return time;
        }

    }

    public static class FilterRollOff {

        private final int    code;
        private final double dbPerOct;

        public FilterRollOff(int code, double dbPerOct) {
            this.code     = code;
            this.dbPerOct = dbPerOct;
        }

        public int getCode() {
            return code;
        }

        public double getDbPerOct() {
            return dbPerOct;
        }

    }

    public class Oscillator implements FSource, SubInstrument<L> {

        private double frequency = 100.0;

        @Override
        public L getParentInstrument() {
            return (L) SRLockIn.this;
        }

        @Override
        public void setFrequency(double frequency) throws IOException {

            this.frequency = frequency;

            if (isInternalReferenceEnabled()) {
                writeFrequency();
            }

        }

        @Override
        public void setAmplitude(double amplitude) throws IOException {
            write(C_SET_INT_AMP, amplitude);
        }

        /**
         * Sets the phase shift of this lock-in's internal reference oscillator --- this is the same as calling
         * setPhaseShift(...) on the lock-in itself.
         *
         * @param phase Phase offset, in degrees.
         *
         * @throws IOException Upon communications error
         */
        @Override
        public void setPhase(double phase) throws IOException {
            setPhaseShift(phase);
        }

        @Override
        public double getFrequency() {
            return frequency;
        }

        @Override
        public double getAmplitude() throws IOException {
            return queryDouble(C_QUERY_INT_AMP);
        }

        /**
         * Returns the phase shift of this lock-in's internal reference oscillator -- this is the same as calling
         * getPhaseShift() on the lock-in itself.
         *
         * @return Phase offset, in degrees.
         *
         * @throws IOException Upon communications error
         */
        @Override
        public double getPhase() throws IOException {
            return getPhaseShift();
        }

        @Override
        public String getName() {
            return String.format("SRS %s Internal Oscillator", MODEL);
        }

        protected void writeFrequency() throws IOException {
            write(C_SET_FREQ, frequency);
        }

    }

}
