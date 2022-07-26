package jisa.devices.temperature;

import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.TC;
import jisa.visa.Connection;
import jisa.visa.RawTCPIPDriver;
import jisa.visa.VISADevice;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

public class LS336 extends VISADevice implements TC {

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

    private final List<Thermometer> thermometers = List.of(
        new Thermometer("A", 1),
        new Thermometer("B", 2),
        new Thermometer("C", 3),
        new Thermometer("D", 4)
    );

    private final List<Heater> heaters = List.of(
        new Heater(1),
        new Heater(2)
    );

    private final List<Loop> loops = heaters.stream().map(Loop::new).collect(Collectors.toUnmodifiableList());

    public LS336(Address address) throws IOException, DeviceException {

        super(address, RawTCPIPDriver.class);

        if (address.getType() == Address.Type.SERIAL) {
            setSerialParameters(57600, 7, Connection.Parity.ODD, Connection.StopBits.ONE, Connection.Flow.NONE);
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

    public class Thermometer implements TC.Thermometer {

        private final String name;
        private final int    number;

        private Thermometer(String name, int number) {
            this.name   = name;
            this.number = number;
        }

        public String getLetter() {
            return name;
        }

        public int getNumber() {
            return number;
        }

        @Override
        public String getIDN() throws IOException, DeviceException {
            return LS336.this.getIDN();
        }

        @Override
        public Address getAddress() {
            return LS336.this.getAddress();
        }

        @Override
        public String getName() {
            try {
                return String.format("%s (%s)", name, query("INNAME? %s", name).trim());
            } catch (IOException e) {
                return String.format("%s (%s)", name, "Name Unknown");
            }
        }

        @Override
        public String getSensorName() {
            return getName();
        }

        @Override
        public double getTemperature() throws IOException, DeviceException {
            return queryDouble(C_QUERY_SENSOR, name);
        }

        @Override
        public void setTemperatureRange(double range) throws IOException, DeviceException {
            // No range options
        }

        @Override
        public double getTemperatureRange() throws IOException, DeviceException {
            return 999.999;
        }

    }

    public class Heater implements TC.Heater {

        private final int number;

        private Heater(int number) {
            this.number = number;
        }

        public int getNumber() {
            return number;
        }

        @Override
        public String getIDN() throws IOException, DeviceException {
            return LS336.this.getIDN();
        }

        @Override
        public Address getAddress() {
            return LS336.this.getAddress();
        }

        @Override
        public double getValue() throws IOException, DeviceException {
            return queryDouble(C_QUERY_HEATER, number);
        }

        @Override
        public double getLimit() throws IOException {
            return HRange.values()[queryInt(C_QUERY_HEATER_RANGE, number)].getPCT();
        }

        @Override
        public void setLimit(double range) throws IOException {
            write(C_SET_HEATER_RANGE, number, HRange.fromDouble(range).ordinal());
        }

        @Override
        public String getName() {
            return String.format("Heater %d", number);
        }

    }

    public class Loop extends ZonedLoop {

        private final Heater output;
        private       double rampRate = 0.0;
        private       double manual   = 0.0;


        private Loop(Heater output) {
            this.output = output;
        }

        @Override
        public String getIDN() throws IOException, DeviceException {
            return LS336.this.getIDN();
        }

        @Override
        public Address getAddress() {
            return LS336.this.getAddress();
        }

        @Override
        public String getName() throws IOException, DeviceException {
            return getOutput().getName();
        }

        @Override
        public void setTemperature(double value) throws IOException, DeviceException {
            write(C_SET_SET_POINT, getOutput().getNumber(), value);
            updatePID(value);
        }

        @Override
        public double getTemperature() throws IOException, DeviceException {
            return queryDouble(C_QUERY_SET_POINT, getOutput().getNumber());
        }

        @Override
        public void setRampEnabled(boolean flag) throws IOException, DeviceException {
            write(C_SET_RAMP, getOutput().getNumber(), flag ? 1 : 0, flag ? rampRate : 0.0);
        }

        @Override
        public boolean isRampEnabled() throws IOException, DeviceException {
            String[] response = query(C_QUERY_RAMP, getOutput().getNumber()).split(",");
            return response[0].trim().equals("1");
        }

        @Override
        public void setRampRate(double limit) throws IOException, DeviceException {
            rampRate = Math.abs(limit);
            boolean enabled = isRampEnabled();
            write(C_SET_RAMP, getOutput().getNumber(), enabled, enabled ? rampRate : 0.0);
        }

        @Override
        public double getRampRate() {
            return rampRate;
        }

        @Override
        public double getPValue() throws IOException, DeviceException {
            return getPID().pValue;
        }

        @Override
        public double getIValue() throws IOException, DeviceException {
            return getPID().iValue;
        }

        @Override
        public double getDValue() throws IOException, DeviceException {
            return getPID().dValue;
        }

        public PIDValue getPID() throws IOException, DeviceException {
            return new PIDValue(query(C_QUERY_PID, getOutput().getNumber()));
        }

        @Override
        public void setPValue(double value) throws IOException, DeviceException {
            PIDValue pid = getPID();
            setPIDValues(value, pid.iValue, pid.dValue);
        }

        @Override
        public void setIValue(double value) throws IOException, DeviceException {
            PIDValue pid = getPID();
            setPIDValues(pid.pValue, value, pid.dValue);
        }

        @Override
        public void setDValue(double value) throws IOException, DeviceException {
            PIDValue pid = getPID();
            setPIDValues(pid.pValue, pid.iValue, value);
        }

        public void setPIDValues(double p, double i, double d) throws IOException, DeviceException {
            write(C_SET_PID, getOutput().getNumber(), p, i, d);
        }

        private OutMode getOutMode() throws IOException, DeviceException {
            return new OutMode(query(C_QUERY_OUT_MODE, getOutput().getNumber()));
        }

        @Override
        public Thermometer getInput() throws IOException, DeviceException {
            int number = getOutMode().input;
            return thermometers.stream().filter(i -> i.getNumber() == number).findFirst().orElse(null);
        }

        @Override
        public Heater getOutput() throws IOException, DeviceException {
            return output;
        }

        @Override
        public void setInput(Input input) throws IOException, DeviceException {

            if (!getAvailableInputs().contains(input)) {
                throw new DeviceException("That input cannot be used for this PID loop.");
            }

            Thermometer tm = (Thermometer) input;

            OutMode mode = getOutMode();
            mode.input = tm.number;
            write(C_SET_OUT_MODE, getOutput().getNumber(), mode.mode, mode.input, mode.powerUp ? 1 : 0);

        }

        @Override
        public void setOutput(Output output) {
            // Nothing to set
        }

        @Override
        public List<? extends Output> getAvailableOutputs() {
            return List.of(output);
        }

        @Override
        public List<? extends Input> getAvailableInputs() {
            return thermometers;
        }

        @Override
        public void setManualValue(double value) throws IOException {

            manual = value;

            if (!isPIDEnabled()) {
                write(C_SET_HEATER, output.getNumber(), manual);
            }

        }

        @Override
        public double getManualValue() {
            return manual;
        }

        @Override
        public void setPIDEnabled(boolean flag) throws IOException {

            if (flag) {
                write(C_SET_HEATER, output.getNumber(), 0.0);
            } else {
                write(C_SET_HEATER, output.getNumber(), manual);
            }

        }

        @Override
        public boolean isPIDEnabled() throws IOException {
            return queryDouble(C_QUERY_M_HEATER, output.getNumber()) == 0.0;
        }

    }

    @Override
    public List<Thermometer> getInputs() {
        return thermometers;
    }

    public List<Thermometer> getThermometers() {
        return getInputs();
    }

    @Override
    public List<Heater> getOutputs() {
        return heaters;
    }

    public List<Heater> getHeaters() {
        return getOutputs();
    }

    @Override
    public List<Loop> getLoops() {
        return loops;
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

    private static class PIDValue {

        public final double pValue;
        public final double iValue;
        public final double dValue;

        public PIDValue(String response) {

            String[] vals = response.trim().split(",");
            pValue = Double.parseDouble(vals[0].trim());
            iValue = Double.parseDouble(vals[1].trim());
            dValue = Double.parseDouble(vals[2].trim());

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

}
