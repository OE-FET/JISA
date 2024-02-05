package jisa.devices.amp;

import jisa.Util;
import jisa.addresses.Address;
import jisa.control.Nameable;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.DPIPALockIn;
import jisa.devices.interfaces.LineFilter;
import jisa.devices.interfaces.LineFilter2X;
import jisa.devices.interfaces.LockIn;
import jisa.enums.Coupling;
import jisa.visa.VISADevice;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.stream.Collectors;

public class SR810 extends VISADevice implements DPIPALockIn, LineFilter, LineFilter2X {

    public static String getDescription() {
        return "Stanford Research Systems SR810";
    }

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
    private static final int    OUTPUT_X             = 1;
    private static final int    OUTPUT_Y             = 2;
    private static final int    OUTPUT_R             = 3;
    private static final int    OUTPUT_T             = 4;
    private static final int    SOURCE_VOLT_SINGLE   = 0;
    private static final int    SOURCE_VOLT_DIFF     = 1;
    private static final int    SOURCE_CURR_LOW_IMP  = 2;
    private static final int    SOURCE_CURR_HIGH_IMP = 3;
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

    public SR810(Address address) throws IOException, DeviceException {

        super(address);

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
            if (!idn[1].trim().equals("SR810")) {
                throw new DeviceException("Device at address %s is not an SR810!", address.toString());
            }

        } catch (IOException e) {
            throw new DeviceException("Device at address %s is not responding!", address.toString());
        }


    }

    @Override
    public double getFrequency() throws IOException {
        return queryDouble(C_QUERY_FREQ);
    }

    @Override
    public double getFrequencyRange() throws IOException, DeviceException {
        return 999.9;
    }

    @Override
    public void setFrequencyRange(double range) throws IOException, DeviceException {
        // No ranging options to set
    }

    @Override
    public double getRefPhase() throws IOException {
        return queryDouble(C_QUERY_PHASE);
    }

    @Override
    public double getRefAmplitude() throws IOException {
        return queryDouble(C_QUERY_INT_AMP);
    }

    @Override
    public void setOscFrequency(double freq) throws IOException {
        write(C_SET_FREQ, freq);
    }

    @Override
    public void setOscPhase(double phase) throws IOException {
        write(C_SET_PHASE, phase);
    }

    @Override
    public void setOscAmplitude(double amp) throws IOException {
        write(C_SET_INT_AMP, amp);
    }

    public LockIn.RefMode getRefMode() throws IOException {
        return RefMode.fromInt(queryInt(C_QUERY_REF)).getRefMode();
    }

    @Override
    public void setRefMode(LockIn.RefMode mode) throws IOException {
        write(C_SET_REF, RefMode.fromRefMode(mode).toInt());
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

    public DataPacket getAll() throws IOException {
        return new DataPacket(query(C_QUERY_ALL));
    }

    @Override
    public double getTimeConstant() throws IOException {
        return TimeConst.fromInt(queryInt(C_QUERY_TIME_CONST)).getValue();
    }

    @Override
    public void setTimeConstant(double seconds) throws IOException {
        write(C_SET_TIME_CONST, TimeConst.fromSeconds(seconds).toInt());
    }

    public void setTimeConstant(TimeConst mode) throws IOException {
        write(C_SET_TIME_CONST, mode.toInt());
    }

    @Override
    public double getRange() throws IOException {
        return Sensitivity.fromInt(queryInt(C_QUERY_SENSITIVITY)).toDouble();
    }

    @Override
    public void setRange(double voltRange) throws IOException, DeviceException {
        write(C_SET_SENSITIVITY, Sensitivity.fromDouble(voltRange).toInt());
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
    public double getFilterRollOff() throws IOException {
        return FilterRO.fromInt(queryInt(C_QUERY_FILTER)).toDB();
    }

    @Override
    public void setFilterRollOff(double dBperOct) throws IOException {
        write(C_SET_FILTER, FilterRO.fromDouble(dBperOct).toInt());
    }

    @Override
    public Coupling getCoupling() throws IOException {

        switch (queryInt(C_QUERY_COUPLING)) {

            case COUPLING_AC:
                return Coupling.AC;

            case COUPLING_DC:
                return Coupling.DC;

            default:
                return null;

        }

    }

    @Override
    public void setCoupling(Coupling mode) throws IOException, DeviceException {

        switch (mode) {

            case AC:
                write(C_SET_COUPLING, COUPLING_AC);
                break;

            case DC:
                write(C_SET_COUPLING, COUPLING_DC);
                break;

        }

    }

    @Override
    public boolean isShieldGrounded() throws IOException, DeviceException {
        return queryInt(C_QUERY_GROUND) == GND_GROUND;
    }

    @Override
    public void setShieldGrounded(boolean mode) throws IOException, DeviceException {
        write(C_SET_GROUND, mode ? GND_GROUND : GND_FLOAT);
    }

    @Override
    public boolean isDifferentialInput() throws IOException, DeviceException {

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
    public void setDifferentialInput(boolean differential) throws IOException, DeviceException {

        if (isCurrentInputEnabled()) {
            throw new DeviceException("Differential input is disabled for current input.");
        }

        write(C_SET_SOURCE, differential ? SOURCE_VOLT_DIFF : SOURCE_VOLT_SINGLE);

    }

    public void setOffsetExpansion(double offset, double expansion) throws IOException {

        int key = 0;

        if (expansion > 1) {
            key = 1;
        }

        if (expansion > 10) {
            key = 2;
        }

        write("OEXP 3,%f,%d", offset, key);
    }

    @Override
    public double getOffset() throws IOException {
        return Double.parseDouble(query("OEXP? 3").split(",")[0]);
    }

    @Override
    public void setOffset(double offset) throws IOException {
        setOffsetExpansion(offset, getExpansion());
    }

    @Override
    public double getExpansion() throws IOException {
        double[] values = {1, 10, 100};
        return values[Integer.parseInt(query("OEXP? 3").split(",")[1])];
    }

    @Override
    public void setExpansion(double expand) throws IOException {
        setOffsetExpansion(getOffset(), expand);
    }

    @Override
    public void autoOffsetX() throws IOException {

        write("AOFF 1");
        Util.sleep(100);

    }

    @Override
    public void autoOffsetY() throws IOException {

        write("AOFF 2");
        Util.sleep(100);

    }

    @Override
    public void autoOffsetAmplitude() throws IOException {

        write("AOFF 3");
        Util.sleep(100);

    }

    @Override
    public void autoRange(double factor, double intTime, long waitTime) throws IOException, DeviceException, InterruptedException {

        double timeConst = getTimeConstant();
        setTimeConstant(intTime);

        Sensitivity found = Sensitivity.S_1V_uA;

        for (Sensitivity sensitivity : Arrays.stream(Sensitivity.values()).sorted(Comparator.comparingDouble(Sensitivity::toDouble)).collect(Collectors.toList())) {

            setRange(sensitivity.toDouble());
            Thread.sleep(waitTime);
            autoOffset();
            Thread.sleep(waitTime);

            if (Math.abs(getLockedX()) < (sensitivity.toDouble() * factor) && Math.abs(getLockedY()) < (sensitivity.toDouble() * factor)) {
                found = sensitivity;
                break;
            }

        }

        setRange(found.toDouble());
        Thread.sleep(500);
        autoOffset();
        Thread.sleep(500);
        setTimeConstant(timeConst);

    }

    private boolean isWorking() throws IOException {
        return queryInt("*SRE? 1") == 0;
    }

    @Override
    public TrigMode getExternalTriggerMode() throws IOException {
        return TrigMode.values()[queryInt("RSLP?")];
    }

    @Override
    public void setExternalTriggerMode(TrigMode mode) throws IOException {

        switch (mode) {

            case SINE:
                write("RSLP 0");
                break;

            case POS_TTL:
                write("RSLP 1");
                break;

            case NEG_TTL:
                write("RSLP 2");
                break;

        }

    }


    public TimeConst getTimeConst() throws IOException {
        return TimeConst.fromInt(queryInt(C_QUERY_TIME_CONST));
    }

    @Override
    public void setCurrentInputEnabled(boolean flag) throws IOException, DeviceException {
        write(C_SET_SOURCE, flag ? currentMode : voltageMode);
    }

    @Override
    public boolean isCurrentInputEnabled() throws IOException, DeviceException {

        switch (queryInt(C_QUERY_SOURCE)) {

            case SOURCE_CURR_LOW_IMP:
            case SOURCE_CURR_HIGH_IMP:
                return true;

            default:
                return false;

        }

    }

    @Override
    public void setCurrentInputGain(double voltsPerAmp) throws IOException, DeviceException {
        currentMode = voltsPerAmp >= 1e8 ? SOURCE_CURR_HIGH_IMP : SOURCE_CURR_LOW_IMP;
        setCurrentInputEnabled(isCurrentInputEnabled());
    }

    @Override
    public double getCurrentInputGain() throws IOException, DeviceException {

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
    public boolean is2xLineFilterEnabled() throws IOException, DeviceException {

        switch (queryInt(C_QUERY_LINE)) {

            case LINE_X2:
            case LINE_X1_X2:
                return true;

            default:
                return false;

        }

    }

    public enum RefMode implements Nameable {

        EXTERNAL(0, LockIn.RefMode.EXTERNAL, "External"),
        INTERNAL(1, LockIn.RefMode.INTERNAL, "Internal");

        private static final HashMap<Integer, RefMode>        lookup  = new HashMap<>();
        private static final HashMap<LockIn.RefMode, RefMode> convert = new HashMap<>();

        static {
            for (RefMode mode : RefMode.values()) {
                lookup.put(mode.toInt(), mode);
                convert.put(mode.getRefMode(), mode);
            }
        }

        private final int            c;
        private final LockIn.RefMode refMode;
        private final String         name;

        RefMode(int code, LockIn.RefMode mode, String name) {
            c         = code;
            refMode   = mode;
            this.name = name;
        }

        static RefMode fromInt(int i) {
            return lookup.getOrDefault(i, null);
        }

        static RefMode fromRefMode(LockIn.RefMode refMode) {
            return convert.getOrDefault(refMode, null);
        }

        int toInt() {
            return c;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public String getName() {
            return name;
        }

        public LockIn.RefMode getRefMode() {
            return refMode;
        }
    }

    private enum Sensitivity implements Nameable {

        S_2nV_fA(0, 2e-9, 2e-15),
        S_5nV_fA(1, 5e-9, 5e-15),
        S_10nV_fA(2, 10e-9, 10e-15),
        S_20nV_fA(3, 20e-9, 20e-15),
        S_50nV_fA(4, 50e-9, 50e-15),
        S_100nV_fA(5, 100e-9, 100e-15),
        S_200nV_fA(6, 200e-9, 200e-15),
        S_500nV_fA(7, 500e-9, 500e-15),
        S_1uV_pA(8, 1e-6, 1e-12),
        S_2uV_pA(9, 2e-6, 2e-12),
        S_5uV_pA(10, 5e-6, 5e-12),
        S_10uV_pA(11, 10e-6, 10e-12),
        S_20uV_pA(12, 20e-6, 20e-12),
        S_50uV_pA(13, 50e-16, 50e-12),
        S_100uV_pA(14, 100e-6, 100e-12),
        S_200uV_pA(15, 200e-6, 200e-12),
        S_500uV_pA(16, 500e-6, 500e-12),
        S_1mV_nA(17, 1e-3, 1e-9),
        S_2mV_nA(18, 2e-3, 2e-9),
        S_5mV_nA(19, 5e-3, 5e-9),
        S_10mV_nA(20, 10e-3, 10e-9),
        S_20mV_nA(21, 20e-3, 20e-9),
        S_50mV_nA(22, 50e-3, 50e-9),
        S_100mV_nA(23, 100e-3, 100e-9),
        S_200mV_nA(24, 200e-3, 200e-9),
        S_500mV_nA(25, 500e-3, 500e-9),
        S_1V_uA(26, 1.0, 1e-6);

        private static final HashMap<Integer, Sensitivity> lookup = new HashMap<>();

        static {
            for (Sensitivity mode : Sensitivity.values()) {
                lookup.put(mode.toInt(), mode);
            }
        }

        private final int    c;
        private final double volt;
        private       double current;

        Sensitivity(int code, double V, double I) {
            c    = code;
            volt = V;
        }

        static Sensitivity fromInt(int i) {
            return lookup.getOrDefault(i, null);
        }

        public static Sensitivity fromDouble(double voltage) throws DeviceException {

            Sensitivity selected = S_1V_uA;

            for (Sensitivity s : values()) {

                if (s.toDouble() >= voltage && s.toDouble() < selected.toDouble()) {
                    selected = s;
                }

            }

            if (selected.toDouble() < voltage) {
                throw new DeviceException("Range of %f is out of range for SR810.", voltage);
            }

            return selected;

        }

        int toInt() {
            return c;
        }

        @Override
        public String getName() {
            return String.format("V: %f, I: %f", volt, current);
        }

        public double toDouble() {
            return volt;
        }
    }

    public enum TimeConst implements Nameable {

        T_10us(0, 10e-6),
        T_30us(1, 30e-6),
        T_100us(2, 1003 - 6),
        T_300us(3, 300e-6),
        T_1ms(4, 1e-3),
        T_3ms(5, 3e-3),
        T_10ms(6, 10e-3),
        T_30ms(7, 30e-3),
        T_100ms(8, 100e-3),
        T_300ms(9, 300e-3),
        T_1s(10, 1.0),
        T_3s(11, 3.0),
        T_10s(12, 10.0),
        T_30s(13, 30.0),
        T_100s(14, 100.0),
        T_300s(15, 300.0),
        T_1ks(16, 1e3),
        T_3ks(17, 3e3),
        T_10ks(18, 10e3),
        T_30ks(19, 30e3);

        private static final HashMap<Integer, TimeConst> lookup = new HashMap<>();

        static {
            for (TimeConst mode : TimeConst.values()) {
                lookup.put(mode.toInt(), mode);
            }
        }

        private final int    c;
        private final double value;

        TimeConst(int code, double value) {
            c          = code;
            this.value = value;
        }

        static TimeConst fromInt(int i) {
            return lookup.getOrDefault(i, null);
        }

        static TimeConst fromSeconds(double seconds) {

            TimeConst found = T_30ks;

            for (TimeConst t : values()) {

                if (t.getValue() >= seconds && t.getValue() < found.getValue()) {
                    found = t;
                }

            }

            return found;

        }

        int toInt() {
            return c;
        }

        double getValue() {
            return value;
        }

        @Override
        public String getName() {
            return String.format("%e s", value);
        }
    }

    private enum FilterRO {

        F6(0, 6),
        F12(1, 12),
        F18(2, 18),
        F24(3, 24);

        private final int    tag;
        private final double db;

        FilterRO(int mode, double value) {
            tag = mode;
            db  = value;
        }

        public static FilterRO fromDouble(double value) {

            FilterRO found = F6;

            for (FilterRO f : values()) {

                if (Math.abs(f.toDB() - value) < Math.abs(found.toDB() - value)) {
                    found = f;
                }

            }

            return found;

        }

        public static FilterRO fromInt(int value) {


            for (FilterRO f : values()) {

                if (f.toInt() == value) {
                    return f;
                }

            }

            return null;

        }

        public int toInt() {
            return tag;
        }

        public double toDB() {
            return db;
        }

    }

    public class DataPacket {

        public double x;
        public double y;
        public double r;
        public double t;
        public double f;

        public DataPacket(double x, double y, double r, double t, double f) {
            this.x = x;
            this.y = y;
            this.r = r;
            this.t = t;
            this.f = f;
        }

        public DataPacket(String data) {

            String[] raw = data.split(",");

            x = Double.parseDouble(raw[0]);
            y = Double.parseDouble(raw[1]);
            r = Double.parseDouble(raw[2]);
            t = Double.parseDouble(raw[3]);
            f = Double.parseDouble(raw[4]);

        }

    }


}
