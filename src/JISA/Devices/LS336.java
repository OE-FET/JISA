package JISA.Devices;

import JISA.Addresses.InstrumentAddress;
import JISA.Util;
import JISA.VISA.Connection;
import JISA.VISA.VISA;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * Class for controlling Lake Shore Model 336 temperature controllers.
 * <p>
 * They are generally annoying with a shoddy communications implementation. Hooray!
 */
public class LS336 extends MSMOTController {

    private static final String[]        CHANNELS             = {"A", "B", "C", "D"};
    private static final String          C_QUERY_SENSOR       = "KRDG? %s";
    private static final String          C_SET_SET_POINT      = "SETP %d,%f";
    private static final String          C_QUERY_SET_POINT    = "SETP? %d";
    private static final String          C_QUERY_PID          = "PID?";
    private static final String          C_SET_PID            = "PID %f,%f,%f";
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
    private              Semaphore       timingControl        = new Semaphore(1);
    private              ExecutorService timingService        = Executors.newFixedThreadPool(1);
    private              boolean[]       nativeAPID           = {false, false};

    public LS336(InstrumentAddress address) throws IOException, DeviceException {

        super(address);
        setSerialParameters(57600, 7, Connection.Parity.ODD, Connection.StopBits.ONE, Connection.Flow.NONE);
        setReadTerminationCharacter(CRLF_TERMINATOR);
        setTerminator(TERMINATOR);

        clearRead();

        try {

            if (!getIDN().trim().split(",")[1].trim().equals("MODEL336")) {
                throw new DeviceException("Device at address %s is not a LakeShore 336!", address.getVISAAddress());
            }

        } catch (IOException e) {
            throw new DeviceException("Device at address %s is not responding!", address.getVISAAddress());
        }

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
    public int getNumOutputs() {
        return 2;
    }

    private OutMode getOutMode(int output) throws IOException {
        return new OutMode(query(C_QUERY_OUT_MODE, output));
    }

    private PID getPID(int output) throws IOException {
        return new PID(query(C_QUERY_PID, output));
    }

    @Override
    public void useSensor(int output, int sensor) throws IOException, DeviceException {
        checkOutput(output);
        checkSensor(sensor);
        OutMode mode = getOutMode(output);
        mode.input = sensor + 1;
        write(C_SET_OUT_MODE, output, mode.mode, mode.input, mode.powerUp ? 1 : 0);
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
        write(C_SET_PID, value, pid.I, pid.D);
    }

    @Override
    public void setIValue(int output, double value) throws IOException, DeviceException {
        checkOutput(output);
        PID pid = getPID(output);
        write(C_SET_PID, pid.P, value, pid.D);
    }

    @Override
    public void setDValue(int output, double value) throws IOException, DeviceException {
        checkOutput(output);
        PID pid = getPID(output);
        write(C_SET_PID, pid.P, pid.I, value);
    }

    @Override
    public double getPValue(int output) throws IOException, DeviceException {
        checkOutput(output);
        return getPID(output).P;
    }

    @Override
    public double getIValue(int output) throws IOException, DeviceException {
        checkOutput(output);
        return getPID(output).I;
    }

    @Override
    public double getDValue(int output) throws IOException, DeviceException {
        checkOutput(output);
        return getPID(output).D;
    }

    @Override
    public void setHeaterRange(int output, double range) throws IOException, DeviceException {
        checkOutput(output);
        write(C_SET_HEATER_RANGE, output, HRange.fromDouble(range).ordinal());
    }

    @Override
    public double getHeaterRange(int output) throws IOException, DeviceException {
        checkOutput(output);
        return HRange.values()[queryInt(C_QUERY_HEATER_RANGE, output)].getPCT();
    }

    @Override
    public void setTargetTemperature(int output, double temperature) throws IOException, DeviceException {
        checkOutput(output);
        write(C_SET_SET_POINT, output + 1, temperature);
    }

    @Override
    public double getTargetTemperature(int output) throws IOException, DeviceException {
        checkOutput(output);
        return queryDouble(C_QUERY_SET_POINT, output);
    }

    @Override
    public double getHeaterPower(int output) throws IOException, DeviceException {
        checkOutput(output);
        return queryDouble(C_QUERY_HEATER, output);
    }

    @Override
    public double getGasFlow(int output) throws IOException, DeviceException {
        System.err.println("LakeShore 336 does not control gas flow.");
        return 0;
    }

    @Override
    public void useAutoHeater(int output) throws IOException, DeviceException {
        checkOutput(output);
        write(C_SET_HEATER, output, 0.0);
    }

    @Override
    public boolean isHeaterAuto(int output) throws IOException, DeviceException {
        checkOutput(output);
        return queryDouble(C_QUERY_M_HEATER, output) == 0;
    }

    @Override
    public void useAutoFlow(int output) {
        System.err.println("WARNING: LakeShore 336 does not control gas flow.");
    }

    @Override
    public boolean isFlowAuto(int output) {
        System.err.println("WARNING: LakeShore 336 does not control gas flow.");
        return false;
    }

    @Override
    public void useAutoPID(int output, boolean auto) throws IOException, DeviceException {

        checkOutput(output);

        if (nativeAPID[output]) {
            OutMode mode = getOutMode(output);
            mode.mode = auto ? 2 : 1;
            write(C_SET_OUT_MODE, output, mode.mode, mode.input, mode.powerUp ? 1 : 0);
        } else {
            super.useAutoPID(output, auto);
        }

    }

    @Override
    public boolean isPIDAuto(int output) throws IOException, DeviceException {
        checkOutput(output);
        if (nativeAPID[output]) {
            return getOutMode(output).mode == 2;
        } else {
            return super.isPIDAuto(output);
        }
    }

    public void setAutoPIDZones(int output, PIDZone[] zones) throws IOException, DeviceException {

        checkOutput(output);
        nativeAPID[output] = false;

        if (nativeAPID[output]) {

            for (int i = 0; i < zones.length; i++) {
                PIDZone z = zones[i];
                write(C_SET_ZONE, output, i + 1, z.getMaxT(), z.getP(), z.getI(), z.getD(), z.getPower(), HRange.fromDouble(z.getRange()).ordinal());
            }

        } else {
            super.setAutoPIDZones(output, zones);
        }

    }

    public PIDZone[] getAutoPIDZones(int output) throws IOException, DeviceException {

        checkOutput(output);

        if (nativeAPID[output]) {

            return new PIDZone[10];

        } else {
            return super.getAutoPIDZones(output);
        }

    }

    @Override
    public void setManualHeater(int output, double powerPCT) throws IOException, DeviceException {
        checkOutput(output);
        write(C_SET_HEATER, output, powerPCT);
    }

    @Override
    public void setManualFlow(int output, double outputPCT) throws DeviceException {
        throw new DeviceException("LS336 does not control gas flow.");
    }

    private static class OutMode {

        public int     mode;
        public int     input;
        public boolean powerUp;

        public OutMode(String response) {

            String[] vals = response.trim().split(",");

            mode = Integer.valueOf(vals[0].trim());
            input = Integer.valueOf(vals[1].trim());
            powerUp = vals[2].trim().equals("1");

        }

    }

    private static class PID {
        public double P;
        public double I;
        public double D;

        public PID(String response) {

            String[] vals = response.trim().split(",");
            P = Double.valueOf(vals[0].trim());
            I = Double.valueOf(vals[1].trim());
            D = Double.valueOf(vals[2].trim());

        }
    }

    private enum HRange {
        OFF(0.0),
        LOW(1.0),
        MED(10.0),
        HIGH(100.0);

        private double pct;

        public static HRange fromDouble(double pct) {

            HRange found = HIGH;

            for (HRange range : values()) {

                if (range.getPCT() <= pct && Math.abs(range.getPCT() - pct) < Math.abs(found.getPCT() - pct)) {
                    found = range;
                }

            }

            return found;

        }

        HRange(double factorPCT) {
            pct = factorPCT;
        }

        public double getPCT() {
            return pct;
        }

    }

}
