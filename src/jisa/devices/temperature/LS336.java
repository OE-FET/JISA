package jisa.devices.temperature;

import jisa.Util;
import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.MSMOTC;
import jisa.visa.Connection.Flow;
import jisa.visa.Connection.Parity;
import jisa.visa.Connection.StopBits;
import jisa.visa.RawTCPIPDriver;
import jisa.visa.VISADevice;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * Class for controlling Lake Shore Model 336 temperature controllers.
 * <p>
 * They are generally annoying with a shoddy communications implementation. Hooray!
 */
public class LS336 extends VISADevice implements MSMOTC {

    private static final String[]        CHANNELS             = {"A", "B", "C", "D"};
    private static final String          C_QUERY_SENSOR       = "KRDG? %s";
    private static final String          C_SET_SET_POINT      = "SETP %d,%f";
    private static final String          C_QUERY_SET_POINT    = "SETP? %d";
    private static final String          C_SET_RAMP           = "RAMP %d,%d,%f";
    private static final String          C_QUERY_RAMP         = "RAMP? %d";
    private static final String          C_QUERY_PID          = "PID? %d";
    private static final String          C_SET_PID            = "PID %d,%f,%f,%f";
    private static final String          C_SET_OUT_MODE       = "OUTMODE %d,%d,%d,%d";
    private static final String          C_QUERY_OUT_MODE     = "OUTMODE? %d";
    private static final String          C_QUERY_HEATER       = "HTR? %d";
    private static final String          C_SET_HEATER         = "MOUT %d,%f";
    private static final String          C_QUERY_M_HEATER     = "MOUT? %d";
    private static final String          C_SET_HEATER_RANGE   = "RANGE %d,%d";
    private static final String          C_QUERY_HEATER_RANGE = "RANGE? %d";
    private static final String          C_SET_ZONE           = "ZONE %d,%d,%f,%f,%f,%f,%f,%d,0,0";
    private static final String          C_QUERY_ZONE         = "ZONE? %d,%d";
    private static final String          TERMINATOR           = "\r\n";
    private final        Semaphore       timingControl        = new Semaphore(1);
    private final        ExecutorService timingService        = Executors.newFixedThreadPool(1);
    private final        boolean[]       autoPID              = {false, false};
    private final        Zone[][]        zones                = new Zone[2][0];

    public LS336(Address address) throws IOException, DeviceException {

        super(address, RawTCPIPDriver.class);

        if (address.getType() == Address.Type.SERIAL) {
            setSerialParameters(57600, 7, Parity.ODD, StopBits.ONE, Flow.NONE);
        }

        setReadTerminator(LF_TERMINATOR);
        setWriteTerminator(TERMINATOR);
        addAutoRemove("\r");
        addAutoRemove("\n");

        manuallyClearReadBuffer();

        try {

            if (!getIDN().trim().split(",")[1].trim().equals("MODEL336")) {
                throw new DeviceException("Device at address %s is not a LakeShore 336!", address.toString());
            }

        } catch (IOException e) {
            throw new DeviceException("Device at address %s is not responding!", address.toString());
        }

        write("MODE 1");

    }

    public static String getDescription() {
        return "LakeShore 336";
    }

    public synchronized void write(String command, Object... args) throws IOException {

        // Can only write to the device if we have waited enough time since the last write (50 ms)
        try {
            timingControl.acquire();
        } catch (InterruptedException ignored) {
        }

        try {

            super.write(command, args);

        } finally {

            // Do not allow another write until 50 ms from now.
            timingService.submit(() -> {
                Util.sleep(50);
                timingControl.release();
            });

        }

    }

    @Override
    public double getTemperature(int sensor) throws IOException, DeviceException {
        checkSensor(sensor);
        return queryDouble(C_QUERY_SENSOR, CHANNELS[sensor]);
    }

    @Override
    public int getNumSensors() {
        return 4;
    }

    @Override
    public String getSensorName(int sensorNumber) {

        try {
            checkSensor(sensorNumber);
            return String.format("%s (%s)", CHANNELS[sensorNumber], query("INNAME? %s", CHANNELS[sensorNumber]).trim());
        } catch (Exception e) {
            return "Unknown Sensor";
        }

    }

    @Override
    public void setTemperatureRange(int sensor, double range) throws DeviceException {
        checkSensor(sensor);
        // No range options
    }

    @Override
    public double getTemperatureRange(int sensor) throws DeviceException {
        checkSensor(sensor);
        return 999.999;
    }

    @Override
    public int getNumOutputs() {
        return 2;
    }

    @Override
    public String getOutputName(int outputNumber) {
        return String.format("Output %d", outputNumber + 1);
    }

    private OutMode getOutMode(int output) throws IOException {
        return new OutMode(query(C_QUERY_OUT_MODE, output + 1));
    }

    private PID getPID(int output) throws IOException {
        return new PID(query(C_QUERY_PID, output + 1));
    }

    @Override
    public void useSensor(int output, int sensor) throws IOException, DeviceException {
        checkOutput(output);
        checkSensor(sensor);
        OutMode mode = getOutMode(output);
        mode.input = sensor + 1;
        write(C_SET_OUT_MODE, output + 1, mode.mode, mode.input, mode.powerUp ? 1 : 0);
    }

    @Override
    public int getUsedSensor(int output) throws IOException, DeviceException {
        checkOutput(output);
        return getOutMode(output).input - 1;
    }

    @Override
    public void setPValue(int output, double value) throws IOException, DeviceException {
        checkOutput(output);
        PID pid = getPID(output);
        setPID(output, value, pid.iValue, pid.dValue);
    }

    @Override
    public void setIValue(int output, double value) throws IOException, DeviceException {
        checkOutput(output);
        PID pid = getPID(output);
        setPID(output, pid.pValue, value, pid.dValue);
    }

    @Override
    public void setDValue(int output, double value) throws IOException, DeviceException {
        checkOutput(output);
        PID pid = getPID(output);
        setPID(output, pid.pValue, pid.iValue, value);
    }

    private void setPID(int output, double P, double I, double D) throws IOException {
        write(C_SET_PID, output + 1, P, I, D);
    }

    @Override
    public double getPValue(int output) throws IOException, DeviceException {
        checkOutput(output);
        return getPID(output).pValue;
    }

    @Override
    public double getIValue(int output) throws IOException, DeviceException {
        checkOutput(output);
        return getPID(output).iValue;
    }

    @Override
    public double getDValue(int output) throws IOException, DeviceException {
        checkOutput(output);
        return getPID(output).dValue;
    }

    @Override
    public void useAutoPID(int output, boolean flag) throws DeviceException, IOException {
        checkOutput(output);
        autoPID[output] = flag;
        updateAutoPID(output);
    }

    @Override
    public boolean isUsingAutoPID(int output) throws DeviceException {
        checkOutput(output);
        return autoPID[output];
    }

    @Override
    public List<Zone> getAutoPIDZones(int output) throws DeviceException {
        checkOutput(output);
        return Arrays.asList(zones[output]);
    }

    @Override
    public void setAutoPIDZones(int output, Zone... zones) throws IOException, DeviceException {
        checkOutput(output);
        this.zones[output] = zones;
        updateAutoPID(output);
    }

    @Override
    public void setHeaterRange(int output, double range) throws IOException, DeviceException {
        checkOutput(output);
        write(C_SET_HEATER_RANGE, output + 1, HRange.fromDouble(range).ordinal());
    }

    @Override
    public double getHeaterRange(int output) throws IOException, DeviceException {
        checkOutput(output);
        return HRange.values()[queryInt(C_QUERY_HEATER_RANGE, output + 1)].getPCT();
    }

    @Override
    public void setTargetTemperature(int output, double temperature) throws IOException, DeviceException {
        checkOutput(output);
        write(C_SET_SET_POINT, output + 1, temperature);
        updateAutoPID(output);
    }

    @Override
    public double getTargetTemperature(int output) throws IOException, DeviceException {
        checkOutput(output);
        return queryDouble(C_QUERY_SET_POINT, output + 1);
    }

    @Override
    public void setTemperatureRampRate(int output, double kPerMin) throws IOException, DeviceException {

        checkOutput(output);

        if (kPerMin == 0) {
            write(C_SET_RAMP, output + 1, 0, 0.0);
        } else {
            write(C_SET_RAMP, output + 1, 1, Math.abs(kPerMin));
        }

    }

    @Override
    public double getTemperatureRampRate(int output) throws IOException, DeviceException {

        checkOutput(output);

        String[] response = query(C_QUERY_RAMP, output + 1).split(",");

        if (response[0].equals("0")) {
            return 0.0;
        } else {
            return Double.parseDouble(response[1]);
        }

    }

    @Override
    public double getHeaterPower(int output) throws IOException, DeviceException {
        checkOutput(output);
        return queryDouble(C_QUERY_HEATER, output + 1);
    }

    @Override
    public double getFlow(int output) {
        Util.errLog.println("LakeShore 336 does not control gas flow.");
        return 0;
    }

    @Override
    public void useAutoHeater(int output) throws IOException, DeviceException {
        checkOutput(output);
        write(C_SET_HEATER, output + 1, 0.0);
    }

    @Override
    public boolean isUsingAutoHeater(int output) throws IOException, DeviceException {
        checkOutput(output);
        return queryDouble(C_QUERY_M_HEATER, output + 1) == 0;
    }

    @Override
    public void useAutoFlow(int output) {
        Util.errLog.println("WARNING: LakeShore 336 does not control gas flow.");
    }

    @Override
    public boolean isUsingAutoFlow(int output) {
        Util.errLog.println("WARNING: LakeShore 336 does not control gas flow.");
        return false;
    }

    @Override
    public void setHeaterPower(int output, double powerPCT) throws IOException, DeviceException {
        checkOutput(output);
        write(C_SET_HEATER, output + 1, powerPCT);
    }

    @Override
    public void setFlow(int output, double outputPCT) throws DeviceException {
        throw new DeviceException("LS336 does not control gas flow.");
    }

    @Override
    public String getOutputName() {
        return null;
    }

    @Override
    public String getSensorName() {

        try {
            return getSensorName(getUsedSensor());
        } catch (Exception e) {
            return "Unknown Sensor";
        }
    }

    private enum HRange {
        OFF(0.0),
        LOW(1.0),
        MED(10.0),
        HIGH(100.0);

        private final double pct;

        HRange(double factorPCT) {
            pct = factorPCT;
        }

        public static HRange fromDouble(double pct) {

            HRange found = HIGH;

            for (HRange range : values()) {

                if (range.getPCT() <= pct && Math.abs(range.getPCT() - pct) < Math.abs(found.getPCT() - pct)) {
                    found = range;
                }

            }

            return found;

        }

        public double getPCT() {
            return pct;
        }

    }

    private static class OutMode {

        public int     mode;
        public int     input;
        public boolean powerUp;

        public OutMode(String response) {

            String[] vals = response.trim().split(",");

            mode    = Integer.parseInt(vals[0].trim());
            input   = Integer.parseInt(vals[1].trim());
            powerUp = vals[2].trim().equals("1");

        }

    }

    private static class PID {

        public final double pValue;
        public final double iValue;
        public final double dValue;

        public PID(String response) {

            String[] vals = response.trim().split(",");
            pValue = Double.parseDouble(vals[0].trim());
            iValue = Double.parseDouble(vals[1].trim());
            dValue = Double.parseDouble(vals[2].trim());

        }
    }

}
