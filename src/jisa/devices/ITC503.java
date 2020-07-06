package jisa.devices;

import jisa.Util;
import jisa.addresses.Address;
import jisa.visa.VISADevice;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class ITC503
 * <p>
 * GPIBDevice class for controlling mercury ITC503 temperature controllers via GPIB.
 */
public class ITC503 extends VISADevice implements MSTC {

    private static final String TERMINATOR         = "\r";
    private static final String C_SET_COMM_MODE    = "Q2";
    private static final String C_READ             = "R%d";
    private static final String C_SET_MODE         = "C%d";
    private static final String C_SET_TEMP         = "T%f";
    private static final String C_SET_AUTO         = "A%d";
    private static final String C_SET_SENSOR       = "H%d";
    private static final String C_SET_AUTO_PID     = "L%d";
    private static final String C_SET_P            = "P%f";
    private static final String C_SET_I            = "I%f";
    private static final String C_SET_D            = "D%f";
    private static final String C_SET_HEATER       = "O%.01f";
    private static final String C_SET_FLOW         = "G%f";
    private static final String C_SET_HEATER_LIM   = "M%.01f";
    private static final String C_QUERY_STATUS     = "X";
    private static final int    SET_TEMP_CHANNEL   = 0;
    private static final int    TEMP_ERROR_CHANNEL = 4;
    private static final int    HEATER_OP_PERC     = 5;
    private static final int    HEATER_OP_VOLTS    = 6;
    private static final int    GAS_OP             = 7;
    private static final int    PROP_BAND          = 8;
    private static final int    INT_ACTION_TIME    = 9;
    private static final int    DER_ACTION_TIME    = 10;
    private static final int    FREQ_CHAN_OFFSET   = 10;
    private static final double MAX_HEATER_VOLTAGE = 40.0;


    private static final long   STANDARD_TEMP_STABLE_DURATION = 5 * 60 * 1000;    // 5 mins
    private static final int    STANDARD_CHECK_INTERVAL       = 100;              // 0.1 sec
    private static final double STANDARD_ERROR_PERC           = 10;

    private boolean   autoPID = false;
    private PIDZone[] zones   = new PIDZone[0];

    /**
     * Open the ITC503 device at the given bus and address
     *
     * @param address The GPIB address on the bus that the ITC503 has
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException If the specified device does not identify as an ITC503
     */
    public ITC503(Address address) throws IOException, DeviceException {

        super(address);
        setEOI(false);
        setWriteTerminator(TERMINATOR);
        write(C_SET_COMM_MODE);
        setReadTerminator(EOS_RETURN);

        clearReadBuffer();

        try {
            String idn = query("V");
            if (!idn.split(" ")[0].trim().equals("ITC503")) {
                throw new DeviceException("Device at address %s is not an ITC503!", address.toString());
            }
        } catch (IOException e) {
            throw new DeviceException("Device at address %s is not responding!", address.toString());
        }

        setMode(Mode.REMOTE_UNLOCKED);
        write(C_SET_AUTO_PID, 0);

        clearReadBuffer();

    }

    public void setTimeout(int value) throws IOException {
        super.setTimeout(value);
    }

    private synchronized double readChannel(int channel) throws IOException {
        try {
            String reply = query(C_READ, channel);
            return Double.parseDouble(reply.substring(1));
        } catch (Exception e) {
            clearReadBuffer();
            String reply = query(C_READ, channel);
            return Double.parseDouble(reply.substring(1));
        }
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

    public void setTargetTemperature(double temperature) throws IOException, DeviceException {
        query(C_SET_TEMP, temperature);
        updateAutoPID();
    }

    @Override
    public double getHeaterPower() throws IOException {
        return Math.pow(readChannel(HEATER_OP_PERC) / 100.0, 2) * 100.0;
    }

    @Override
    public void setHeaterPower(double powerPCT) throws IOException {
        query(C_SET_HEATER, Math.sqrt(powerPCT / 100.0) * 100.0);
        AutoMode mode = AutoMode.fromMode(false, isUsingAutoFlow());
        query(C_SET_AUTO, mode.toInt());
    }

    @Override
    public double getFlow() throws IOException {
        return readChannel(GAS_OP);
    }

    @Override
    public void useAutoHeater() throws IOException {
        AutoMode mode = AutoMode.fromMode(true, isUsingAutoFlow());
        query(C_SET_AUTO, mode.toInt());
    }

    @Override
    public boolean isUsingAutoHeater() throws IOException {
        return AutoMode.fromInt(getStatus().A).heaterAuto();
    }

    @Override
    public void useAutoFlow() throws IOException {
        AutoMode mode = AutoMode.fromMode(isUsingAutoHeater(), true);
        query(C_SET_AUTO, mode.toInt());
    }

    @Override
    public void setFlow(double outputPCT) throws IOException {
        query(C_SET_FLOW, outputPCT);
        AutoMode mode = AutoMode.fromMode(isUsingAutoHeater(), false);
        query(C_SET_AUTO, mode.toInt());
    }

    @Override
    public boolean isUsingAutoFlow() throws IOException {
        return AutoMode.fromInt(getStatus().A).gasAuto();
    }

    @Override
    public void useAutoPID(boolean flag) throws IOException, DeviceException {
        autoPID = flag;
        updateAutoPID();
    }

    @Override
    public boolean isUsingAutoPID() throws IOException, DeviceException {
        return autoPID;
    }

    @Override
    public List<PIDZone> getAutoPIDZones() throws IOException, DeviceException {
        return Arrays.asList(zones);
    }

    @Override
    public void setAutoPIDZones(PIDZone... zones) throws IOException, DeviceException {
        this.zones = zones;
        updateAutoPID();
    }

    @Override
    public double getPValue() throws IOException {
        return readChannel(PROP_BAND);
    }

    @Override
    public void setPValue(double value) throws IOException {
        query(C_SET_P, value);
    }

    @Override
    public double getIValue() throws IOException {
        return readChannel(INT_ACTION_TIME);
    }

    @Override
    public void setIValue(double value) throws IOException {
        query(C_SET_I, value);

    }

    @Override
    public double getDValue() throws IOException {
        return readChannel(DER_ACTION_TIME);
    }

    @Override
    public void setDValue(double value) throws IOException {
        query(C_SET_D, value);

    }

    @Override
    public double getHeaterRange() throws IOException {
        return Math.pow(((readChannel(HEATER_OP_VOLTS) / (readChannel(HEATER_OP_PERC) / 100.0)) / MAX_HEATER_VOLTAGE), 2) * 100.0;
    }

    @Override
    public void setHeaterRange(double range) throws IOException {

        double voltage = MAX_HEATER_VOLTAGE * Math.sqrt(range / 100.0);


        query(C_SET_HEATER_LIM, voltage);

    }

    private Status getStatus() throws IOException {
        return new Status(query(C_QUERY_STATUS));
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

        if (!Util.isBetween(sensor, 0, 2)) {
            throw new DeviceException("Sensor index, %d, out of range!", sensor);
        }

        return readChannel(sensor + 1);

    }

    @Override
    public void useSensor(int sensor) throws IOException, DeviceException {

        if (!Util.isBetween(sensor, 0, 2)) {
            throw new DeviceException("Sensor index, %d, out of range!", sensor);
        }

        query(C_SET_SENSOR, sensor + 1);

    }

    @Override
    public int getUsedSensor() throws IOException {
        return getStatus().H - 1;
    }

    @Override
    public int getNumSensors() {
        return 3;
    }

    public String getIDN() throws IOException {
        return query("V").replace("\n", "").replace("\r", "");
    }

    public double getChannelFrequency(int channel) throws IOException, DeviceException {

        if (channel < 1 || channel > 3) {
            throw new DeviceException("Sensor index, %d, out of range!", channel);
        }

        return readChannel(FREQ_CHAN_OFFSET + channel);

    }

    public void setMode(Mode mode) throws IOException {
        query(C_SET_MODE, mode.toInt());
    }

    @Override
    public void setTemperatureRange(int sensor, double range) throws DeviceException {
        checkSensor(sensor);
        // No range options for ITC503
    }

    @Override
    public double getTemperatureRange(int sensor) throws DeviceException {
        checkSensor(sensor);
        return 999.9;
    }


    public enum Mode {

        LOCAL_LOCKED(0),
        REMOTE_LOCKED(1),
        LOCAL_UNLOCKED(2),
        REMOTE_UNLOCKED(3);

        private static HashMap<Integer, Mode> lookup = new HashMap<>();

        static {
            for (Mode mode : Mode.values()) {
                lookup.put(mode.toInt(), mode);
            }
        }

        private        int                    c;

        Mode(int code) {
            c = code;
        }

        static Mode fromInt(int i) {
            return lookup.getOrDefault(i, null);
        }

        int toInt() {
            return c;
        }

    }

    private enum AutoMode {

        H_MAN_G_MAN(0),
        H_AUTO_G_MAN(1),
        H_MAN_G_AUTO(2),
        H_AUTO_G_AUTO(3);

        private static final int                        H      = 1;
        private static final int                        G      = 2;
        private static       HashMap<Integer, AutoMode> lookup = new HashMap<>();

        static {
            for (AutoMode mode : values()) {
                lookup.put(mode.toInt(), mode);
            }
        }

        private              int                        c;

        AutoMode(int code) {
            c = code;
        }

        static AutoMode fromInt(int i) {
            return lookup.getOrDefault(i, null);
        }

        static AutoMode fromMode(boolean heater, boolean gas) {

            for (AutoMode mode : values()) {
                boolean mheater = (mode.toInt() & H) != 0;
                boolean mGas    = (mode.toInt() & G) != 0;
                if (mheater == heater && mGas == gas) {
                    return mode;
                }
            }

            return H_AUTO_G_AUTO;

        }

        int toInt() {
            return c;
        }

        boolean heaterAuto() {
            return (c & H) != 0;
        }

        boolean gasAuto() {
            return (c & G) != 0;
        }

    }

    private static class Status {

        private static final Pattern PATTERN = Pattern.compile("X([0-9])A([0-9])C([0-9])S([0-9][0-9])H([0-9])L([0-9])");

        public int X;
        public int A;
        public int C;
        public int S;
        public int H;
        public int L;

        public Status(String response) throws IOException {

            Matcher match = PATTERN.matcher(response);

            if (!match.find()) {
                System.out.println(response);
                throw new IOException("Improperly formatted response from ITC503");
            }

            X = Integer.parseInt(match.group(1));
            A = Integer.parseInt(match.group(2));
            C = Integer.parseInt(match.group(3));
            S = Integer.parseInt(match.group(4));
            H = Integer.parseInt(match.group(5));
            L = Integer.parseInt(match.group(6));

        }

    }

}
