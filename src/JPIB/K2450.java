package JPIB;

import java.io.IOException;
import java.util.HashMap;

public class K2450 extends GPIBDevice {

    private static final String C_MEASURE_VOLTAGE       = ":MEAS:VOLT?";
    private static final String C_MEASURE_CURRENT       = ":MEAS:CURR?";
    private static final String C_MEASURE_RESISTANCE    = ":MEAS:RES?";
    private static final String C_SET_SOURCE_FUNCTION   = ":SOUR:FUNC %s";
    private static final String C_SET_OUTPUT_STATE      = ":OUTP:STATE %s";
    private static final String C_QUERY_SOURCE_FUNCTION = ":SOUR:FUNC?";
    private static final String C_QUERY_OUTPUT_STATE    = ":OUTP:STATE?";
    private static final String C_SET_SOURCE_VALUE      = ":SOUR:%s %f";
    private static final String OUTPUT_ON               = "ON";
    private static final String OUTPUT_OFF              = "OFF";

    public K2450(int bus, int address) throws IOException, DeviceException {

        super(bus, address);

        try {

            String[] iden = query("*IDN?").split(",");

            if (!iden[1].trim().equals("MODEL 2450")) {
                throw new DeviceException("Device at address %d on bus %d is not a Keithley 2450!", address, bus);
            }

        } catch (IOException e) {
            throw new DeviceException("Device at address %d on bus %d is not responding!", address, bus);
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

    public Source getSource() throws IOException {
        return Source.fromTag(query(C_QUERY_SOURCE_FUNCTION));
    }

    public boolean isOn() throws IOException {
        return query(C_SET_OUTPUT_STATE).equals(OUTPUT_ON);
    }

    public void setVoltage(double voltage) throws IOException {
        setSourceValue(Source.VOLTAGE, voltage);
    }

    public void setCurrent(double current) throws IOException {
        setSourceValue(Source.CURRENT, current);
    }

    public void setSourceValue(Source type, double value) throws IOException {
        write(C_SET_SOURCE_VALUE, type.getTag(), value);
    }

    public enum Source {

        VOLTAGE("VOLT"),
        CURRENT("CURR");

        private static HashMap<String, Source> lookup = new HashMap<>();

        static {
            for (Source mode : Source.values()) {
                lookup.put(mode.getTag(), mode);
            }
        }

        public static Source fromTag(String tag) {
            return lookup.getOrDefault(tag, null);
        }

        private String tag;

        Source(String tag) {
            this.tag = tag;
        }

        String getTag() {
            return tag;
        }

    }


}
