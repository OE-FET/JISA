package jisa.devices.pid;

import jisa.Util;
import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.ParameterList;
import jisa.visa.VISADevice;
import jisa.visa.connections.SerialConnection.Parity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class ITC503
 * <p>
 * VISADevice class for controlling mercury ITC503 temperature controllers.
 */
public class ITC503 extends VISADevice implements TC {

    private static final String TERMINATOR_1                  = "\r";
    private static final String TERMINATOR_2                  = "\n";
    private static final String TERMINATOR_3                  = "\r\n";
    private static final String C_SET_COMM_MODE               = "Q2";
    private static final String C_READ                        = "R%d";
    private static final String C_SET_MODE                    = "C%d";
    private static final String C_SET_TEMP                    = "T%f";
    private static final String C_SET_AUTO                    = "A%d";
    private static final String C_SET_SENSOR                  = "H%d";
    private static final String C_SET_AUTO_PID                = "L%d";
    private static final String C_SET_P                       = "P%f";
    private static final String C_SET_I                       = "I%f";
    private static final String C_SET_D                       = "D%f";
    private static final String C_SET_HEATER                  = "O%.01f";
    private static final String C_SET_FLOW                    = "G%f";
    private static final String C_SET_HEATER_LIM              = "M%.01f";
    private static final String C_QUERY_STATUS                = "X";
    private static final int    SET_TEMP_CHANNEL              = 0;
    private static final int    TEMP_ERROR_CHANNEL            = 4;
    private static final int    HEATER_OP_PERC                = 5;
    private static final int    HEATER_OP_VOLTS               = 6;
    private static final int    GAS_OP                        = 7;
    private static final int    PROP_BAND                     = 8;
    private static final int    INT_ACTION_TIME               = 9;
    private static final int    DER_ACTION_TIME               = 10;
    private static final int    FREQ_CHAN_OFFSET              = 10;
    private static final double MAX_HEATER_VOLTAGE            = 40.0;
    private static final long   STANDARD_TEMP_STABLE_DURATION = 5 * 60 * 1000;    // 5 mins
    private static final int    STANDARD_CHECK_INTERVAL       = 100;              // 0.1 sec
    private static final double STANDARD_ERROR_PERC = 10;
    private static final int    MIN_IO_INTERVAL     = 5;

    private final List<TMeter> thermometers;
    private final List<Heater> heaters;
    private final List<Loop>   loops;

    /**
     * Open the ITC503 device at the given bus and address
     *
     * @param address The GPIB address on the bus that the ITC503 has
     *
     * @throws IOException     Upon communication error
     * @throws DeviceException If the specified device does not identify as an ITC503
     */
    public ITC503(Address address) throws IOException, DeviceException {

        // There's nothing super about an ITC503, but this needs to be called
        super(address);

        getConnection().setEncoding(StandardCharsets.US_ASCII);

        configGPIB(gpib -> gpib.setEOIEnabled(false));

        configSerial(serial -> serial.setSerialParameters(9600, 8, Parity.NONE, 2));

        // Tell the object to limit the rate at which read/writes can be performed
        setIOLimit(MIN_IO_INTERVAL, true, true);

        // The ITC503 has an unfortunate tendency to change which line terminator(s) it uses, so we need to
        // programmatically determine which one it has randomly selected this time
        setWriteTerminator(TERMINATOR_1);
        setReadTerminator(TERMINATOR_1);

        addAutoRemove(TERMINATOR_1, TERMINATOR_2, TERMINATOR_3);

        setTimeout(500);
        String terminator = determineTerminator();
        setTimeout(1000);

        if (terminator == null) {
            throw new IOException("ITC503 is refusing to terminate replies correctly.");
        }

        setReadTerminator(terminator);

        String idn;
        int    count = 0;

        // Try to extract the correct IDN response up to 3 times before giving up
        do {

            clearBuffers();
            manuallyClearReadBuffer();

            try {

                idn = query("V");

            } catch (Exception e) {

                if (count < 2) {
                    idn = "";
                } else {
                    throw e;
                }

            }

            count++;
            System.out.printf("ITC503 IDN Response %d: \"%s\"%n", count, idn);

        } while (!idn.split(" ")[0].trim().equals("ITC503") && count < 3);

        if (!idn.split(" ")[0].trim().equals("ITC503")) {
            throw new DeviceException("Device at address \"%s\" is not claiming to be an ITC503!", address.toString());
        }

        // If we've made it this far, it seems that all is well
        setMode(Mode.REMOTE_UNLOCKED);
        write(C_SET_AUTO_PID, 0);

        thermometers = List.of(new TMeter(1), new TMeter(2), new TMeter(3));
        heaters      = List.of(heater);
        loops        = List.of(loop);

    }

    @Override
    public List<TMeter> getInputs() {
        return thermometers;
    }

    @Override
    public List<Heater> getOutputs() {
        return heaters;
    }

    @Override
    public List<Loop> getLoops() {
        return loops;
    }

    public class TMeter implements TC.TMeter {

        private final int number;

        private TMeter(int number) {
            this.number = number;
        }

        public int getNumber() {
            return number;
        }

        @Override
        public String getIDN() throws IOException, DeviceException {
            return ITC503.this.getIDN();
        }

        @Override
        public Address getAddress() {
            return ITC503.this.getAddress();
        }

        @Override
        public String getName() {
            return String.format("Sensor %d", number);
        }

        @Override
        public String getSensorName() {
            return getName();
        }

        @Override
        public double getTemperature() throws IOException, DeviceException {
            return readChannel(number);
        }

        @Override
        public void setTemperatureRange(double range) throws IOException, DeviceException {
            // No ranging options
        }

        @Override
        public double getTemperatureRange() throws IOException, DeviceException {
            return 999.999;
        }

    }

    public final TC.Heater heater = new Heater() {

        @Override
        public String getIDN() throws IOException {
            return ITC503.this.getIDN();
        }

        @Override
        public Address getAddress() {
            return ITC503.this.getAddress();
        }

        @Override
        public double getValue() throws IOException {
            return Math.pow(readChannel(HEATER_OP_PERC) / 100.0, 2) * 100.0;
        }

        @Override
        public double getLimit() throws IOException {
            return Math.pow(((readChannel(HEATER_OP_VOLTS) / (readChannel(HEATER_OP_PERC) / 100.0)) / MAX_HEATER_VOLTAGE), 2) * 100.0;
        }

        @Override
        public void setLimit(double range) throws IOException {
            double voltage = MAX_HEATER_VOLTAGE * Math.sqrt(range / 100.0);
            query(C_SET_HEATER_LIM, voltage);
        }

        @Override
        public String getName() {
            return "Main Heater";
        }

    };

    public final ZonedLoop loop = new ZonedLoop() {

        private boolean ramping = false;
        private double rampRate = 0.0;
        private double setPoint = 0.0;

        @Override
        public String getName() {
            return "Main Loop";
        }

        @Override
        public void setSetPoint(double temperature) throws IOException, DeviceException {

            query("S0");

            if (!ramping || rampRate <= 0) {

                query(C_SET_TEMP, temperature);
                updatePID(temperature);

            } else {

                for (int i = 1; i <= 16; i++) {

                    query("x%d", i);
                    query("y1");
                    query("s%f", temperature);
                    query("y2");
                    query("s0.0");
                    query("y3");
                    query("s0.0");

                }

                double currentTemperature = getInput().getValue();

                query("x1");
                query("y2");
                query("s%.1f", Math.abs(temperature - currentTemperature) / Math.abs(rampRate));

                query(C_SET_TEMP, currentTemperature);
                updatePID(temperature);
                query("S1");

            }

            setPoint = temperature;

        }

        @Override
        public double getSetPoint() throws IOException {
            return readChannel(SET_TEMP_CHANNEL);
        }

        @Override
        public void setRampEnabled(boolean flag) throws IOException, DeviceException {
            ramping = flag;
            setSetPoint(getSetPoint());
        }

        @Override
        public boolean isRampEnabled() {
            return ramping;
        }

        @Override
        public void setRampRate(double limit) {
            rampRate = limit;
        }

        @Override
        public double getRampRate() {
            return rampRate;
        }

        @Override
        public double getPValue() throws IOException {
            return readChannel(PROP_BAND);
        }

        @Override
        public double getIValue() throws IOException {
            return readChannel(INT_ACTION_TIME);
        }

        @Override
        public double getDValue() throws IOException {
            return readChannel(DER_ACTION_TIME);
        }

        @Override
        public void setPValue(double value) throws IOException {
            query(C_SET_P, value);
        }

        @Override
        public void setIValue(double value) throws IOException {
            query(C_SET_I, value);
        }

        @Override
        public void setDValue(double value) throws IOException {
            query(C_SET_D, value);
        }

        @Override
        public Input getInput() throws IOException {
            int used = getStatus().H;
            return thermometers.stream().filter(i -> i.getNumber() == used).findFirst().orElse(thermometers.get(0));
        }

        @Override
        public Output getOutput() {
            return heater;
        }

        @Override
        public void setInput(Input input) throws IOException, DeviceException {

            if (input instanceof TMeter && thermometers.contains(input)) {
                query(C_SET_SENSOR, ((TMeter) input).getNumber());
            } else {
                throw new DeviceException("That input cannot be used for this PID loop");
            }

        }

        @Override
        public void setOutput(Output output) throws DeviceException {

            if (output != heater) {
                throw new DeviceException("That output cannot be used with this PID loop");
            }

        }

        @Override
        public List<? extends Output> getAvailableOutputs() {
            return List.of(heater);
        }

        @Override
        public List<? extends Input> getAvailableInputs() {
            return thermometers;
        }

        @Override
        public String getIDN() throws IOException {
            return ITC503.this.getIDN();
        }

        @Override
        public Address getAddress() {
            return ITC503.this.getAddress();
        }

        @Override
        public void setManualValue(double value) throws IOException, DeviceException {

            if (!Util.isBetween(value, 0, 100)) {
                throw new DeviceException("Heater power must be a value between 0 and 100, %s given", value);
            }

            query(C_SET_HEATER, Math.sqrt(value / 100.0) * 100.0);

        }

        @Override
        public double getManualValue() throws IOException, DeviceException {
            return getOutput().getValue();
        }

        @Override
        public void setPIDEnabled(boolean flag) throws IOException {
            query(C_SET_AUTO, AutoMode.fromMode(flag, false).toInt());
        }

        @Override
        public boolean isPIDEnabled() throws IOException {
            return AutoMode.fromInt(getStatus().A).heaterAuto();
        }

        @Override
        public List<Parameter<?>> getInstrumentParameters(Class<?> target) {

            List<Parameter<?>> list = super.getBaseParameters(target);
            list.add(new Parameter<>("Use Internal PID Table", true, v -> query(C_SET_AUTO_PID, v ? 1 : 0)));
            return list;

        }

    };

    private String determineTerminator() throws IOException {

        List<String> terminators = List.of(TERMINATOR_1, TERMINATOR_2, TERMINATOR_3);
        List<String> safe        = List.of("\\r", "\\n", "\\r\\n");

        int i = 0;
        for (String attempt : terminators) {

            clearBuffers();
            manuallyClearReadBuffer();
            setReadTerminator(attempt);

            try {

                System.out.printf("ITC503 Attempting Terminator \"%s\": ", safe.get(i++));
                String idn = query("V");

                System.out.printf("\"%s\"... ", idn);

                if (idn.contains("ITC503")) {
                    System.out.println("Success");
                    return attempt;
                } else {
                    System.out.println("Failure");
                }

            } catch (IOException e) {
                System.out.printf("%s... Failure%n", e.getMessage());
            }

        }

        return null;

    }

    public static String getDescription() {
        return "Oxford Instruments ITC-503";
    }

    public void setTimeout(int value) throws IOException {
        super.setTimeout(value);
    }

    protected synchronized double readChannel(int channel) throws IOException {

        for (int count = 0; count < 3; count++) {

            String response = query(C_READ, channel).trim();

            if (response.startsWith("R")) {

                try {
                    return Double.parseDouble(response.substring(1));
                } catch (NumberFormatException ignored) {}

            } else {
                System.err.printf("ITC503: Improper Response %d: %s%n", count + 1, response);
            }

            clearBuffers();
            manuallyClearReadBuffer();

        }

        throw new IOException("ITC503 is not responding to read command correctly");

    }


    private Status getStatus() throws IOException {

        Status response = null;
        int    count    = 0;

        do {

            try {
                response = new Status(query(C_QUERY_STATUS));
            } catch (Exception ignored) {
            }

            count++;

        } while (response == null && count < 3);

        if (response != null) {
            return response;
        } else {
            throw new IOException("ITC-503 is not responding to status command correctly");
        }

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

    public List<Parameter<?>> getInstrumentParameters(Class<?> target) {

        ParameterList parameters = new ParameterList();
        parameters.addValue("Use Internal PID Table", false, v -> query(C_SET_AUTO_PID, v ? 1 : 0));
        return parameters;

    }

    public enum Mode {

        LOCAL_LOCKED(0),
        REMOTE_LOCKED(1),
        LOCAL_UNLOCKED(2),
        REMOTE_UNLOCKED(3);

        private static final HashMap<Integer, Mode> lookup = new HashMap<>();

        static {
            for (Mode mode : Mode.values()) {
                lookup.put(mode.toInt(), mode);
            }
        }

        private final int c;

        Mode(int code) {
            c = code;
        }

        static Mode fromInt(int i) {
            return lookup.getOrDefault(i, null);
        }

        public int toInt() {
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
        private static final HashMap<Integer, AutoMode> lookup = new HashMap<>();

        static {
            for (AutoMode mode : values()) {
                lookup.put(mode.toInt(), mode);
            }
        }

        private final int c;

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

        public final int X;
        public final int A;
        public final int C;
        public final int S;
        public final int H;
        public final int L;

        public Status(String response) throws IOException {

            Matcher match = PATTERN.matcher(response);

            if (!match.find()) {
                System.out.println(response);
                throw new IOException(String.format("Improperly formatted response from ITC503: \"%s\"", response));
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
