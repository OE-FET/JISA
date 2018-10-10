package JISA.Devices;

import JISA.Addresses.InstrumentAddress;

import java.io.IOException;
import java.util.HashMap;

public class K2450 extends SMU {

    private static final String C_MEASURE_VOLTAGE       = ":MEAS:VOLT?";
    private static final String C_MEASURE_CURRENT       = ":MEAS:CURR?";
    private static final String C_MEASURE_RESISTANCE    = ":MEAS:RES?";
    private static final String C_SET_SOURCE_FUNCTION   = ":SOUR:FUNC %s";
    private static final String C_SET_OUTPUT_STATE      = ":OUTP:STATE %s";
    private static final String C_QUERY_SOURCE_FUNCTION = ":SOUR:FUNC?";
    private static final String C_QUERY_OUTPUT_STATE    = ":OUTP:STATE?";
    private static final String C_SET_SOURCE_VALUE      = ":SOUR:%s %f";
    private static final String C_SET_TERMINALS         = ":ROUT:TERM %s";
    private static final String C_QUERY_TERMINALS       = ":ROUT:TERM?";
    private static final String C_SET_PROBE_MODE        = ":SENS:RSEN %s";
    private static final String C_QUERY_PROBE_MODE      = ":SENS:RSEN?";
    private static final String OUTPUT_ON               = "1";
    private static final String OUTPUT_OFF              = "0";

    /**
     * Constant values for referring to the FRONT and REAR terminals of the Keithley 2450 SMU
     */
    public static class Terminals {
        public static final int FRONT = 0;
        public static final int REAR  = 1;
    }

    public K2450(InstrumentAddress address) throws IOException, DeviceException {

        super(address);

        try {

            String[] iden = query("*IDN?").split(",");

            if (!iden[1].trim().equals("MODEL 2450")) {
                throw new DeviceException("Device at address %s is not a Keithley 2450!", address.getVISAAddress());
            }

        } catch (IOException e) {
            throw new DeviceException("Device at address %s is not responding!", address.getVISAAddress());
        }

    }

    public void useFourProbe(boolean fourProbe) throws IOException {

        if (fourProbe) {
            write(C_SET_PROBE_MODE, OUTPUT_ON);
        } else {
            write(C_SET_PROBE_MODE, OUTPUT_OFF);
        }

    }

    public boolean isUsingFourProbe() throws IOException {
        return query(C_QUERY_PROBE_MODE).trim().equals(OUTPUT_ON);
    }

    public double getVoltage() throws IOException {
        return queryDouble(C_MEASURE_VOLTAGE);
    }

    public double getCurrent() throws IOException {
        return queryDouble(C_MEASURE_CURRENT);
    }

    public double getResistance() throws IOException {
        return queryDouble(C_MEASURE_RESISTANCE);
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

        switch (getSource()) {

            case VOLTAGE:
                setVoltage(value);
                break;

            case CURRENT:
                setCurrent(value);
                break;

        }

    }

    @Override
    public double getSourceValue() throws IOException {

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
    public double getMeasureValue() throws IOException {
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
    public int getNumTerminals() {
        return 2;
    }

    public void setSourceValue(Source type, double value) throws IOException {
        write(C_SET_SOURCE_VALUE, type.getTag(), value);
        setSource(type);
    }

    public void setTerminals(int terminalIndex) throws IOException, DeviceException {

        if (terminalIndex >= getNumTerminals()) {
            throw new DeviceException("Those terminals do not exist!");
        }

        write(C_SET_TERMINALS, Terms.fromInt(terminalIndex).getTag());
    }

    public int getTerminals() throws IOException {
        return Terms.fromTag(query(C_QUERY_TERMINALS)).toInt();
    }

    public enum Source {

        VOLTAGE("VOLT", SMU.Source.VOLTAGE),
        CURRENT("CURR", SMU.Source.CURRENT);

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
        private SMU.Source orig;

        Source(String tag, SMU.Source orig) {
            this.tag = tag;
            this.orig = orig;
        }

        String getTag() {
            return tag;
        }

        SMU.Source getSMU() {
            return orig;
        }

    }

    private enum Terms {

        FRONT(0, "FRONT"),
        REAR(1, "REAR");

        private static HashMap<String, Terms>  lookup  = new HashMap<>();
        private static HashMap<Integer, Terms> indices = new HashMap<>();

        static {
            for (Terms t : Terms.values()) {
                lookup.put(t.getTag(), t);
                indices.put(t.toInt(), t);
            }
        }

        public static Terms fromTag(String tag) {
            return lookup.getOrDefault(tag, null);
        }

        public static Terms fromInt(int index) {
            return indices.getOrDefault(index, null);
        }

        private String tag;
        private int    index;

        Terms(int index, String tag) {
            this.index = index;
            this.tag = tag;
        }

        String getTag() {
            return tag;
        }

        int toInt() {
            return index;
        }

    }


}
