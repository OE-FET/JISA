package JPIB;

import java.io.IOException;
import java.util.HashMap;

/**
 * Class ITC503
 * <p>
 * GPIBDevice class for controlling mercury ITC503 temperature controllers via GPIB.
 */
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

    private static final long   STANDARD_TEMP_STABLE_DURATION = 5 * 60 * 1000;    // 5 mins
    private static final int    STANDARD_CHECK_INTERVAL       = 100;              // 0.1 sec
    private static final double STANDARD_ERROR_PERC           = 10;

    /**
     * Open the ITC503 device at the given bus and address
     *
     * @param bus     Which GPIB bus the ITC503 is on
     * @param address The GPIB address on the bus that the ITC503 has
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException If the specified device does not identify as an ITC503
     */
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

    private synchronized double readChannel(int channel) throws IOException {
        String reply = query(C_READ, channel);
        return Double.parseDouble(reply.substring(1));
    }

    /**
     * Returns the temperature that the ITC is currently programmed to reach.
     *
     * @return Target temperature
     *
     * @throws IOException Upon communication error
     */
    public double getTargetTemperature() throws IOException {
        return readChannel(SET_TEMP_CHANNEL);
    }

    /**
     * Returns the temperature reported by the given sensor.
     *
     * @param sensor The sensor to read (1, 2 or 3)
     *
     * @return Temperature reported by the sensor
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException Upon invalid sensor number
     */
    public synchronized double getTemperature(int sensor) throws IOException, DeviceException {

        if (!Util.isBetween(sensor, 1, 3)) {
            throw new DeviceException("Sensor index, %d, out of range!", sensor);
        }

        return readChannel(sensor);

    }

    /**
     * Returns the current error value in the cryostat temperature (vs what it was set to be)
     *
     * @return Error
     *
     * @throws IOException Upon communication error
     */
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

    public void setTargetTemperature(double temperature) throws IOException {
        query(C_SET_TEMP, temperature);
    }

    public void setMode(Mode mode) throws IOException {
        query(C_SET_MODE, mode.toInt());
    }

    public void onStableTemperature(final int sensor, double temperature, SRunnable onStable, ERunnable onException) {
        onStableTemperature(sensor, temperature, STANDARD_TEMP_STABLE_DURATION, STANDARD_CHECK_INTERVAL, STANDARD_ERROR_PERC, onStable, onException);
    }

    public void onStableTemperature(final int sensor, double temperature, long minDuration, int checkInterval, double percError, SRunnable onStable, ERunnable onException) {

        Asynch.onParamWithinError(
                () -> getTemperature(sensor),
                temperature,
                percError,
                minDuration,
                checkInterval,
                onStable,
                onException
        );

    }


    public enum Mode {

        LOCAL_LOCKED(0),
        REMOTE_LOCKED(1),
        LOCAL_UNLOCKED(2),
        REMOTE_UNLOCKED(3);

        private        int                    c;
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


}
