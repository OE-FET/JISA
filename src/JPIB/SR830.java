package JPIB;

import java.io.IOException;
import java.util.HashMap;

public class SR830 extends GPIBDevice {

    private static final String C_QUERY_FREQ        = "FREQ?";
    private static final String C_SET_FREQ          = "FREQ %f";
    private static final String C_QUERY_PHASE       = "PHAS?";
    private static final String C_SET_PHASE         = "PHAS %f";
    private static final String C_QUERY_INT_AMP     = "SLVL?";
    private static final String C_SET_INT_AMP       = "SLVL %f";
    private static final String C_QUERY_REF         = "FMOD?";
    private static final String C_SET_REF           = "FMOD %d";
    private static final String C_QUERY_SENSITIVITY = "SENS?";
    private static final String C_SET_SENSITIVITY   = "SENS %d";

    public enum RefMode {

        EXTERNAL(0),
        INTERNAL(1);

        private int c;
        private static HashMap<Integer, RefMode> lookup = new HashMap<>();

        static RefMode fromInt(int i) {
            return lookup.getOrDefault(i, null);
        }

        static {
            for (RefMode mode : RefMode.values()) {
                lookup.put(mode.toInt(), mode);
            }
        }

        RefMode(int code) {
            c = code;
        }

        int toInt() {
            return c;
        }
    }

    public enum Sensitivity {

        S_2nV_PER_fA(0),
        S_5nV_PER_fA(1),
        S_10nV_PER_fA(2),
        S_20nV_PER_fA(3),
        S_50nV_PER_fA(4),
        S_100nV_PER_fA(5),
        S_200nV_PER_fA(6),
        S_500nV_PER_fA(7),
        S_1uV_PER_pA(8),
        S_2uV_PER_pA(9),
        S_5uV_PER_pA(10),
        S_10uV_PER_pA(11),
        S_20uV_PER_pA(12),
        S_50uV_PER_pA(13),
        S_100uV_PER_pA(14),
        S_200uV_PER_pA(15),
        S_500uV_PER_pA(16),
        S_1mV_PER_nA(17),
        S_2mV_PER_nA(18),
        S_5mV_PER_nA(19),
        S_10mV_PER_nA(20),
        S_20mV_PER_nA(21),
        S_50mV_PER_nA(22),
        S_100mV_PER_nA(23),
        S_200mV_PER_nA(24),
        S_500mV_PER_nA(25),
        S_1V_PER_uA(26);

        private int c;
        private static HashMap<Integer, Sensitivity> lookup = new HashMap<>();

        static Sensitivity fromInt(int i) {
            return lookup.getOrDefault(i, null);
        }

        static {
            for (Sensitivity mode : Sensitivity.values()) {
                lookup.put(mode.toInt(), mode);
            }
        }

        Sensitivity(int code) {
            c = code;
        }

        int toInt() {
            return c;
        }
    }

    public SR830(int bus, int address) throws IOException, DeviceException {

        super(bus, address, DEFAULT_TIMEOUT, DEFAULT_EOI, DEFAULT_EOS);

        try {
            String[] idn = query("*IDN?").split(",");

            if (!idn[1].trim().equals("SR830")) {
                throw new DeviceException("Device at address %d on bus %d is not an SR830!", address, bus);
            }
        } catch (IOException e) {
            throw new DeviceException("Device at address %d on bus %d is not responding!", address, bus);
        }

    }

    public double getRefFrequency() throws IOException {
        return Double.parseDouble(query(C_QUERY_FREQ));
    }

    public double getRefPhase() throws IOException {
        return Double.parseDouble(query(C_QUERY_PHASE));
    }

    public double getRefAmplitude() throws IOException {
        return Double.parseDouble(query(C_QUERY_INT_AMP));
    }

    public void setRefFrequency(double freq) throws IOException {
        write(C_SET_FREQ, freq);
    }

    public void setRefPhase(double phase) throws IOException {
        write(C_SET_PHASE, phase);
    }

    public void setRefAmplitude(double amp) throws IOException {
        write(C_SET_INT_AMP, amp);
    }

    public RefMode getRefMode() throws IOException {

        int code = Integer.parseInt(query(C_QUERY_REF));
        return RefMode.fromInt(code);

    }

    public void setRefMode(RefMode mode) throws IOException {
        write(C_SET_REF, mode.toInt());
    }

    public Sensitivity getSensitivity() throws IOException {

        int code = Integer.parseInt(query(C_QUERY_REF));
        return Sensitivity.fromInt(code);

    }

    public void setSensitivity(Sensitivity mode) throws IOException {
        write(C_SET_REF, mode.toInt());
    }

}
