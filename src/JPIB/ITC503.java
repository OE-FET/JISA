package JPIB;

import java.io.IOException;
import java.util.HashMap;

public class ITC503 extends GPIBDevice {

    private static final String TERMINATOR         = "\r";
    private static final String C_SET_COMM_MODE    = "Q2";
    private static final String C_READ             = "R%d";
    private static final String C_SET_MODE         = "C%d";
    private static final String C_SET_TEMP         = "T%f";
    private static final int    SET_TEMP_CHANNEL   = 0;
    private static final int    TEMP_ERROR_CHANNEL = 4;
    private static final int    HEATER_OP_PERC     = 5;
    private static final int    HEATER_OP_VOLTS    = 6;
    private static final int    GAS_OP             = 7;
    private static final int    PROP_BAND          = 8;
    private static final int    INT_ACTION_TIME    = 9;
    private static final int    DER_ACTION_TIME    = 10;
    private static final int    FREQ_CHAN_OFFSET   = 10;

    private static final long STANDARD_TEMP_STABLE_DURATION = 5 * 60 * 60 * 1000; // 5 mins
    private static final int  STANDARD_CHECK_INTERVAL       = 100; // 0.1 sec

    public enum Mode {

        LOCAL_LOCKED(0),
        REMOTE_LOCKED(1),
        LOCAL_UNLOCKED(2),
        REMOTE_UNLOCKED(3);

        private int c;
        private static HashMap<Integer, Mode> lookup = new HashMap<>();

        static Mode fromInt(int i) {
            return lookup.getOrDefault(i, null);
        }

        static {
            for (Mode mode : Mode.values()) {
                lookup.put(mode.toInt(), mode);
            }
        }

        Mode(int code) {
            c = code;
        }

        int toInt() {
            return c;
        }

    }

    public ITC503(int bus, int address) throws IOException, DeviceException {

        super(bus, address, DEFAULT_TIMEOUT, 0, EOS_RETURN);

        setTerminator(TERMINATOR);

        write(C_SET_COMM_MODE);

        try {
            String[] idn = query("V").split(" ");

            if (!idn[0].trim().equals("ITC503")) {
                throw new DeviceException("Device at address %d on bus %d is not an ITC503!", address, bus);
            }
        } catch (IOException e) {
            throw new DeviceException("Device at address %d on bus %d is not responding!", address, bus);
        }

    }

    private double readChannel(int channel) throws IOException {
        String reply = query(C_READ, channel);
        return Double.parseDouble(reply.substring(1));
    }

    public double getTargetTemperature() throws IOException {
        return readChannel(SET_TEMP_CHANNEL);
    }

    public double getTemperature(int sensor) throws IOException, DeviceException {

        if (sensor < 1 || sensor > 3) {
            throw new DeviceException("Sensor index, %d, out of range!", sensor);
        }

        return readChannel(sensor);

    }

    public double getTemperatureError() throws IOException {
        return readChannel(TEMP_ERROR_CHANNEL);
    }

    public double getHeaterOutputPercentage() throws IOException {
        return readChannel(HEATER_OP_PERC);
    }

    public double getHeaterOutputVolts() throws IOException {
        return readChannel(HEATER_OP_VOLTS);
    }

    public double getGasFlowOutput() throws IOException {
        return readChannel(GAS_OP);
    }

    public double getProportionalBand() throws IOException {
        return readChannel(PROP_BAND);
    }

    public double getIntegrationActionTime() throws IOException {
        return readChannel(INT_ACTION_TIME);
    }

    public double getDerivativeActionTime() throws IOException {
        return readChannel(DER_ACTION_TIME);
    }

    public double getChannelFrequency(int channel) throws IOException, DeviceException {

        if (channel < 1 || channel > 3) {
            throw new DeviceException("Sensor index, %d, out of range!", channel);
        }

        return readChannel(FREQ_CHAN_OFFSET + channel);

    }

    public void setTemperature(double temperature) throws IOException {
        write(C_SET_TEMP, temperature);
    }

    public void setMode(Mode mode) throws IOException {
        write(C_SET_MODE, mode.toInt());
    }

    public void onStableTemperature(final int sensor, double temperature, double percError, SRunnable onStable) {
        onStableTemperature(sensor, temperature, STANDARD_TEMP_STABLE_DURATION, STANDARD_CHECK_INTERVAL, percError, onStable);
    }

    public void onStableTemperature(final int sensor, double temperature, long minDuration, int checkInterval, double percError, SRunnable onStable) {

        Asynch.onParamWithinError(
                () -> getTemperature(sensor),
                temperature,
                percError,
                minDuration,
                checkInterval,
                onStable,
                (e) -> {
                    e.printStackTrace();
                    System.exit(1);
                }
        );

    }


}
