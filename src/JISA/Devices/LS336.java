package JISA.Devices;

import JISA.Addresses.InstrumentAddress;
import JISA.Util;

import java.io.IOException;

public class LS336 extends MSMOTController {

    private static final String C_QUERY_SENSOR    = "KRDG? %s";
    private static final String C_SET_SET_POINT   = "SETP %d,%f";
    private static final String C_QUERY_SET_POINT = "SETP? %d";
    private static final String C_QUERY_PID       = "PID?";
    private static final String C_SET_PID         = "PID %f,%f,%f";
    private static final String C_SET_OUT_MODE    = "OUTMODE %d,%d,%d,%d";
    private static final String C_QUERY_OUT_MODE  = "OUTMODE? %d";
    private static final String C_QUERY_HEATER    = "HTR? %d";
    private static final String C_SET_HEATER      = "MOUT %d,%f";
    private static final String C_QUERY_M_HEATER  = "MOUT? %d";

    public LS336(InstrumentAddress address) throws IOException, DeviceException {

        super(address);

        try {

            if (!getIDN().trim().split(",")[1].trim().equals("MODEL336")) {
                throw new DeviceException("Device at address %s is not a LakeShore 336!", address.getVISAAddress());
            }

        } catch (IOException e) {
            throw new DeviceException("Device at address %s is not responding!", address.getVISAAddress());
        }

    }

    @Override
    public double getTemperature(int sensor) throws IOException, DeviceException {
        checkSensor(sensor);
        return queryDouble(C_QUERY_SENSOR, sensor + 1);
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
        write(C_SET_OUT_MODE, mode.output, mode.mode, mode.input, mode.powerUp ? 1 : 0);
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
        OutMode mode = getOutMode(output);
        mode.mode = auto ? 2 : 1;
        write(C_SET_OUT_MODE, mode.output, mode.mode, mode.input, mode.powerUp ? 1 : 0);
    }

    @Override
    public boolean isPIDAuto(int output) throws IOException, DeviceException {
        checkOutput(output);
        return getOutMode(output).mode == 2;
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

        public int     output;
        public int     mode;
        public int     input;
        public boolean powerUp;

        public OutMode(String response) {

            String[] vals = response.trim().split(",");

            output = Integer.valueOf(vals[0].trim());
            mode = Integer.valueOf(vals[1].trim());
            input = Integer.valueOf(vals[2].trim());
            powerUp = vals[3].trim().equals("1");

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

}
