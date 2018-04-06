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
    private static final String C_QUERY_OUTPUT      = "OUTP ? %d";
    private static final int    OUTPUT_X            = 1;
    private static final int    OUTPUT_Y            = 2;
    private static final int    OUTPUT_R            = 3;
    private static final int    OUTPUT_T            = 4;
    private static final String C_QUERY_ALL         = "SNAP ? 1,2,3,4,9";

    /**
     * Open an SR830 device on the given bus and address
     *
     * @param bus     The bus the device is on
     * @param address The address used by the device on the bus
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException If the device does not identify itself as an SR830
     */
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

    /**
     * Returns the current value of the reference signal frequency
     *
     * @return Reference frequency
     *
     * @throws IOException Upon communication error
     */
    public double getRefFrequency() throws IOException {
        return queryDouble(C_QUERY_FREQ);
    }

    /**
     * Returns the current phase offset of the reference signal (internal)
     *
     * @return Reference phase in degrees
     *
     * @throws IOException Upon communication error
     */
    public double getRefPhase() throws IOException {
        return queryDouble(C_QUERY_PHASE);
    }

    /**
     * Returns the voltage amplitude of the reference signal
     *
     * @return Reference amplitude
     *
     * @throws IOException Upon communication error
     */
    public double getRefAmplitude() throws IOException {
        return queryDouble(C_QUERY_INT_AMP);
    }

    /**
     * Sets the frequency of the internal sine function generator for internal referencing
     *
     * @param freq Frequency
     *
     * @throws IOException Upon communication error
     */
    public void setRefFrequency(double freq) throws IOException {
        write(C_SET_FREQ, freq);
    }

    /**
     * Sets the phase offset of the internal sine function generator for internal referencing
     *
     * @param phase Phase in degrees
     *
     * @throws IOException Upon communication error
     */
    public void setRefPhase(double phase) throws IOException {
        write(C_SET_PHASE, phase);
    }

    /**
     * Sets the amplitude of the internal sine function generator for internal referencing
     *
     * @param amp Amplitude
     *
     * @throws IOException Upon communication error
     */
    public void setRefAmplitude(double amp) throws IOException {
        write(C_SET_INT_AMP, amp);
    }

    /**
     * Returns the current reference mode of the SR830 (internal or external)
     *
     * @return Reference mode
     *
     * @throws IOException Upon communication error
     */
    public RefMode getRefMode() throws IOException {
        return RefMode.fromInt(queryInt(C_QUERY_REF));
    }

    /**
     * Sets the reference mode of the SR830 (internal or external)
     *
     * @param mode Reference mode
     *
     * @throws IOException Upon communication error
     */
    public void setRefMode(RefMode mode) throws IOException {
        write(C_SET_REF, mode.toInt());
    }

    /**
     * Returns the current sensitivity mode of the SR830
     *
     * @return Sensitivity mode
     *
     * @throws IOException Upon communication error
     */
    public Sensitivity getSensitivity() throws IOException {
        return Sensitivity.fromInt(queryInt(C_QUERY_SENSITIVITY));
    }

    /**
     * Sets the sensitivity mode of the SR830
     *
     * @param mode Sensitivity mode
     *
     * @throws IOException Upon communication error
     */
    public void setSensitivity(Sensitivity mode) throws IOException {
        write(C_SET_SENSITIVITY, mode.toInt());
    }

    /**
     * Returns the voltage reported by the X channel (in phase with reference)
     *
     * @return X channel voltage
     *
     * @throws IOException Upon communication error
     */
    public double getX() throws IOException {
        return queryDouble(C_QUERY_OUTPUT, OUTPUT_X);
    }

    /**
     * Returns the voltage reported by the Y channel (pi out of phase with reference)
     *
     * @return Y channel voltage
     *
     * @throws IOException Upon communication error
     */
    public double getY() throws IOException {
        return queryDouble(C_QUERY_OUTPUT, OUTPUT_Y);
    }

    /**
     * Returns the absolute value (R where R^2 = X^2 + Y^2) of the locked-in signal
     *
     * @return R channel voltage
     *
     * @throws IOException Upon communication error
     */
    public double getR() throws IOException {
        return queryDouble(C_QUERY_OUTPUT, OUTPUT_R);
    }

    /**
     * Returns phase of the locked-in signal
     *
     * @return Theta channel value in degrees
     *
     * @throws IOException Upon communication error
     */
    public double getT() throws IOException {
        return queryDouble(C_QUERY_OUTPUT, OUTPUT_T);
    }

    /**
     * Returns the current values of X, Y, T, R and frequency (F) all at once
     *
     * @return {X,Y,T,R,F} DataPacket
     * @throws IOException Upon communication error
     */
    public DataPacket getAll() throws IOException {
        return new DataPacket(query(C_QUERY_ALL));
    }

    public enum RefMode {

        EXTERNAL(0),
        INTERNAL(1);

        private        int                       c;
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

        private        int                           c;
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
