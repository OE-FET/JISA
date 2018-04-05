package JPIB;

import java.io.IOException;
import java.util.HashMap;

public class K236 extends GPIBDevice {

    private static final String   C_SET_SRC_FUNC = "F%d,%d";
    private static final String   C_SET_BIAS     = "B%f,%d,%d";
    private static final double   MIN_CURRENT    = -100e-3;
    private static final double   MAX_CURRENT    = +100e-3;
    private static final double   MIN_VOLTAGE    = -110;
    private static final double   MAX_VOLTAGE    = +110;
    private              Source   source         = null;
    private              Function function       = null;

    public K236(int bus, int address) throws IOException {

        super(bus, address);

        // TODO: Add check for correct device (need to test with actual device to see query response)

    }

    public void setBias(double level) throws IOException, DeviceException {

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

    public void setSourceFunction(Source s, Function f) throws IOException {
        write(C_SET_SRC_FUNC, s.toInt(), f.toInt());
        source = s;
        function = f;
    }

    public enum Source {

        CURRENT(1),
        VOLTAGE(0);

        private        int                      c;
        private static HashMap<Integer, Source> lookup = new HashMap<>();

        static Source fromInt(int i) {
            return lookup.getOrDefault(i, null);
        }

        static {
            for (Source s : Source.values()) {
                lookup.put(s.toInt(), s);
            }
        }

        Source(int code) {
            c = code;
        }

        int toInt() {
            return c;
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
