package JISA.Devices;

import JISA.*;
import JISA.Control.*;

import java.io.IOException;
import java.util.HashMap;

public class SR830 extends VISADevice implements Commandable {

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
    private static final String C_QUERY_TIME_CONST  = "OFLT?";
    private static final String C_SET_TIME_CONST    = "OFLT %d";
    private static final int    OUTPUT_X            = 1;
    private static final int    OUTPUT_Y            = 2;
    private static final int    OUTPUT_R            = 3;
    private static final int    OUTPUT_T            = 4;
    private static final String C_QUERY_ALL         = "SNAP ? 1,2,3,4,9";

    private static final double STANDARD_ERROR    = 1.0;
    private static final int    STANDARD_INTERVAL = 100;
    private static final long   STANDARD_DURATION = 10000;

    private final DeviceCommand[] commands = {

            new DeviceCommand("Get Frequency", "Returns the reference frequency") {
                @Override
                public double execute() throws IOException {
                    return getRefFrequency();
                }
            },
            new DeviceCommand("Get Amplitude", "Returns the amplitude of the locked-in signal") {
                @Override
                public double execute() throws IOException {
                    return getRefAmplitude();
                }
            },
            new DeviceCommand("Set Time Constant", "Sets the time constant to use for locking-on to a signal",
                              new DeviceCommand.DeviceArgument<TimeConst>("Time Constant", TimeConst.class)) {

                @Override
                public double execute() throws IOException {
                    setTimeConst((TimeConst) getArgumentValue(0));
                    return 0;
                }
            },
            new DeviceCommand("Set Ref Mode", "Sets whether the lock-in uses an internal or external reference signal",
                              new DeviceCommand.DeviceArgument<RefMode>("Ref Mode", RefMode.class)) {

                @Override
                public double execute() throws IOException {
                    setRefMode((RefMode) getArgumentValue(0));
                    return 0;
                }
            }

    };

    /**
     * Open an SR830 device on the given bus and address
     *
     * @param address The address used by the device
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException If the device does not identify itself as an SR830
     */
    public SR830(InstrumentAddress address) throws IOException, DeviceException {

        super(address);

        try {

            String[] idn = query("*IDN?").split(",");
            if (!idn[1].trim().equals("SR830")) {
                throw new DeviceException("Device at address %s is not an SR830!", address.getVISAAddress());
            }

        } catch (IOException e) {
            throw new DeviceException("Device at address %s is not responding!", address.getVISAAddress());
        }


    }


    @Override
    public DeviceCommand[] getCommands() {
        return commands.clone();
    }

    @Override
    public DeviceCommand getCommand(int index) {
        return (index >= 0 && index < commands.length) ? commands[index].clone() : null;
    }

    @Override
    public String getName() {
        return "SR830 Lock-In Amplifier";
    }

    @Override
    public HashMap<String, Class> getNameableParams() {

        HashMap<String, Class> map = new HashMap<>();
        map.put("Reference Mode", RefMode.class);
        map.put("Sensitivity", Sensitivity.class);
        map.put("Time Constant", TimeConst.class);

        return map;

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
     *
     * @throws IOException Upon communication error
     */
    public DataPacket getAll() throws IOException {
        return new DataPacket(query(C_QUERY_ALL));
    }

    public void onStableLock(double error, long duration, int interval, SRunnable onStable) {
        onStableLock(error, duration, interval, onStable, Util::exceptionHandler);
    }

    public void onStableLock(double error, long duration, int interval, SRunnable onStable, ERunnable onException) {

        Asynch.onParamStable(
                this::getR,
                error,
                duration,
                interval,
                onStable,
                onException
        );

    }

    /**
     * Wait for the R output of the lock-in to remain within the given percentage error for the given duration.
     *
     * @param errorPct The percentage error required
     * @param duration The duration required
     *
     * @throws Exception
     */
    public void waitForStableLock(double errorPct, long duration) throws Exception {

        Synch.waitForParamStable(
                this::getR,
                errorPct,
                STANDARD_INTERVAL,
                duration
        );

    }

    /**
     * Wait for the R output of the lock-in to become stable with a 1% error range for at least 10 seconds.
     *
     * @throws Exception
     */
    public void waitForStableLock() throws Exception {
        waitForStableLock(STANDARD_ERROR, STANDARD_DURATION);
    }

    public void setTimeConst(TimeConst mode) throws IOException {
        write(C_SET_TIME_CONST, mode.toInt());
    }

    public TimeConst getTimeConst() throws IOException {
        return TimeConst.fromInt(queryInt(C_QUERY_TIME_CONST));
    }

    public enum RefMode implements Nameable {

        EXTERNAL(0, "External"),
        INTERNAL(1, "Internal");

        private        int                       c;
        private        String                    name;
        private static HashMap<Integer, RefMode> lookup = new HashMap<>();

        static RefMode fromInt(int i) {
            return lookup.getOrDefault(i, null);
        }

        static {
            for (RefMode mode : RefMode.values()) {
                lookup.put(mode.toInt(), mode);
            }
        }

        RefMode(int code, String name) {
            c = code;
            this.name = name;
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
    }

    public enum Sensitivity implements Nameable {

        S_2nV_PER_fA(0, 2e-9, 1e-15),
        S_5nV_PER_fA(1, 5e-9, 1e-15),
        S_10nV_PER_fA(2, 10e-9, 1e-15),
        S_20nV_PER_fA(3, 20e-9, 1e-15),
        S_50nV_PER_fA(4, 50e-9, 1e-15),
        S_100nV_PER_fA(5, 100e-9, 1e-15),
        S_200nV_PER_fA(6, 200e-9, 1e-15),
        S_500nV_PER_fA(7, 500e-9, 1e-15),
        S_1uV_PER_pA(8, 1e-6, 1e-12),
        S_2uV_PER_pA(9, 2e-6, 1e-12),
        S_5uV_PER_pA(10, 5e-6, 1e-12),
        S_10uV_PER_pA(11, 10e-6, 1e-12),
        S_20uV_PER_pA(12, 20e-6, 1e-12),
        S_50uV_PER_pA(13, 50e-16, 1e-12),
        S_100uV_PER_pA(14, 100e-6, 1e-12),
        S_200uV_PER_pA(15, 200e-6, 1e-12),
        S_500uV_PER_pA(16, 500e-6, 1e-12),
        S_1mV_PER_nA(17, 1e-3, 1e-9),
        S_2mV_PER_nA(18, 2e-3, 1e-9),
        S_5mV_PER_nA(19, 5e-3, 1e-9),
        S_10mV_PER_nA(20, 10e-3, 1e-9),
        S_20mV_PER_nA(21, 20e-3, 1e-9),
        S_50mV_PER_nA(22, 50e-3, 1e-9),
        S_100mV_PER_nA(23, 100e-3, 1e-9),
        S_200mV_PER_nA(24, 200e-3, 1e-9),
        S_500mV_PER_nA(25, 500e-3, 1e-9),
        S_1V_PER_uA(26, 1.0, 1e-6);

        private        int                           c;
        private        double                        volt;
        private        double                        current;
        private static HashMap<Integer, Sensitivity> lookup = new HashMap<>();

        static Sensitivity fromInt(int i) {
            return lookup.getOrDefault(i, null);
        }

        static {
            for (Sensitivity mode : Sensitivity.values()) {
                lookup.put(mode.toInt(), mode);
            }
        }

        Sensitivity(int code, double V, double I) {
            c = code;
            volt = V;
            current = I;
        }

        int toInt() {
            return c;
        }

        @Override
        public String getName() {
            return String.format("V: %f, I: %f", volt, current);
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
        S_300ms(9, 300e-3),
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

        private        int                         c;
        private        double                      value;
        private static HashMap<Integer, TimeConst> lookup = new HashMap<>();

        static TimeConst fromInt(int i) {
            return lookup.getOrDefault(i, null);
        }

        static {
            for (TimeConst mode : TimeConst.values()) {
                lookup.put(mode.toInt(), mode);
            }
        }

        TimeConst(int code, double value) {
            c = code;
            this.value = value;
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
