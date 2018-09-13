package JISA.Devices;

import JISA.Addresses.InstrumentAddress;
import JISA.VISA.VISADevice;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.util.ArrayList;
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
    private static final String C_GET_TERMINALS         = ":ROUT:TERM?";
    private static final String OUTPUT_ON               = "1";
    private static final String OUTPUT_OFF              = "0";

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

    public void setSourceValue(Source type, double value) throws IOException {
        write(C_SET_SOURCE_VALUE, type.getTag(), value);
    }

    public void setTerminals(Terminals terminals) throws IOException {
        write(C_SET_TERMINALS, terminals.getTag());
    }

    public Terminals getTerminals() throws IOException {
        return Terminals.fromTag(query(C_GET_TERMINALS));
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
            return lookup.getOrDefault(tag, null);
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

    public enum Terminals {

        FRONT("FRONT"),
        REAR("REAR");

        private static HashMap<String, Terminals> lookup = new HashMap<>();

        static {
            for (Terminals t : Terminals.values()) {
                lookup.put(t.getTag(), t);
            }
        }

        public static Terminals fromTag(String tag) {
            return lookup.getOrDefault(tag, null);
        }

        private String tag;

        Terminals(String tag) {
            this.tag = tag;
        }

        String getTag() {
            return tag;
        }

    }


}
