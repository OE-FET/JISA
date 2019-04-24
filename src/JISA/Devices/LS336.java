package JISA.Devices;

import JISA.Addresses.Address;
import JISA.Util;
import JISA.VISA.Connection;
import JISA.VISA.RawTCPIPDriver;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * Class for controlling Lake Shore Model 336 temperature controllers.
 * <p>
 * They are generally annoying with a shoddy communications implementation. Hooray!
 */
public class LS336 extends MSMOTC {

    private static final String[]        CHANNELS             = {"A", "B", "C", "D"};
    private static final String          C_QUERY_SENSOR       = "KRDG? %s";
    private static final String          C_SET_SET_POINT      = "SETP %d,%f";
    private static final String          C_QUERY_SET_POINT    = "SETP? %d";
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
    private              Semaphore       timingControl        = new Semaphore(1);
    private              ExecutorService timingService        = Executors.newFixedThreadPool(1);
    private              boolean[]       nativeAPID           = {false, false};

    public LS336(Address address) throws IOException, DeviceException {

        super(address, RawTCPIPDriver.class);

        if (address.getType() == Address.Type.SERIAL) {
            setSerialParameters(57600, 7, Connection.Parity.ODD, Connection.StopBits.ONE, Connection.Flow.NONE);
        }

        setReadTerminationCharacter(LF_TERMINATOR);
        setTerminator(TERMINATOR);
        setRemoveTerminator(TERMINATOR);

        clearRead();

        try {

            if (!getIDN().trim().split(",")[1].trim().equals("MODEL336")) {
                throw new DeviceException("Device at address %s is not a LakeShore 336!", address.toString());
            }

        } catch (IOException e) {
            throw new DeviceException("Device at address %s is not responding!", address.toString());
        }

        write("MODE 1");

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
        return new OutMode(query(C_QUERY_OUT_MODE, output+1));
    }

    private PID getPID(int output) throws IOException {
        return new PID(query(C_QUERY_PID, output+1));
    }

    @Override
    public void useSensor(int output, int sensor) throws IOException, DeviceException {
        checkOutput(output);
        checkSensor(sensor);
        OutMode mode = getOutMode(output);
        mode.input = sensor + 1;
        write(C_SET_OUT_MODE, output+1, mode.mode, mode.input, mode.powerUp ? 1 : 0);
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
        setPID(output, value, pid.I, pid.D);
    }

    @Override
    public void setIValue(int output, double value) throws IOException, DeviceException {
        checkOutput(output);
        PID pid = getPID(output);
        setPID(output, pid.P, value, pid.D);
    }

    @Override
    public void setDValue(int output, double value) throws IOException, DeviceException {
        checkOutput(output);
        PID pid = getPID(output);
        setPID(output, pid.P, pid.I, value);
    }

    private void setPID(int output, double P, double I, double D) throws IOException {
        write(C_SET_PID,output+1, P, I, D);
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
        write(C_SET_HEATER_RANGE, output+1, HRange.fromDouble(range).ordinal());
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
    }

    @Override
    public double getTargetTemperature(int output) throws IOException, DeviceException {
        checkOutput(output);
        return queryDouble(C_QUERY_SET_POINT, output + 1);
    }

    @Override
    public double getHeaterPower(int output) throws IOException, DeviceException {
        checkOutput(output);
        return queryDouble(C_QUERY_HEATER, output + 1);
    }

    @Override
    public double getGasFlow(int output) throws IOException, DeviceException {
        Util.errLog.println("LakeShore 336 does not control gas flow.");
        return 0;
    }

    @Override
    public void useAutoHeater(int output) throws IOException, DeviceException {
        checkOutput(output);
        write(C_SET_HEATER, output + 1, 0.0);
    }

    @Override
    public boolean isHeaterAuto(int output) throws IOException, DeviceException {
        checkOutput(output);
        return queryDouble(C_QUERY_M_HEATER, output + 1) == 0;
    }

    @Override
    public void useAutoFlow(int output) {
        Util.errLog.println("WARNING: LakeShore 336 does not control gas flow.");
    }

    @Override
    public boolean isFlowAuto(int output) {
        Util.errLog.println("WARNING: LakeShore 336 does not control gas flow.");
        return false;
    }

    @Override
    public void useAutoPID(int output, boolean auto) throws IOException, DeviceException {

        checkOutput(output);

        if (nativeAPID[output]) {
            OutMode mode = getOutMode(output);
            mode.mode = auto ? 2 : 1;
            write(C_SET_OUT_MODE, output + 1, mode.mode, mode.input, mode.powerUp ? 1 : 0);
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
                write(C_SET_ZONE, output + 1, i + 1, z.getMaxT(), z.getP(), z.getI(), z.getD(), z.getPower(), HRange.fromDouble(z.getRange()).ordinal());
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
        write(C_SET_HEATER, output + 1, powerPCT);
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
