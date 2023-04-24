package jisa.devices.temperature;

import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.TC;
import jisa.visa.VISADevice;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


public class ArroyoTEC extends VISADevice implements TC {

    public static String getDescription() {
        return "Arroyo 585 TecPak";
    }

    public ArroyoTEC(Address address) throws IOException, DeviceException {

        super(address);

        configSerial(serial -> serial.setSerialParameters(38400, 8));

        setWriteTerminator("\n");
        setReadTerminator("\n");
        addAutoRemove("\n");
        addAutoRemove("\r");

        if (!getIDN().toLowerCase().contains("arroyo")) {
            throw new DeviceException("Instrument at \"%s\" is not an Arroyo.", address.toString());
        }

        write("TEC:MODE:T");
        write("TEC:HEATCOOL BOTH");
        write("TEC:GAIN PID");

    }

    @Override
    public List<Loop> getLoops() {
        return List.of(LOOP);
    }

    @Override
    public List<TMeter> getInputs() {
        return List.of(THERMOMETER);
    }

    @Override
    public List<Heater> getOutputs() {
        return List.of(HEATER);
    }

    private final TMeter THERMOMETER = new TMeter() {

        @Override
        public String getName() {
            return "Thermometer";
        }

        @Override
        public String getSensorName() {
            return getName();
        }

        @Override
        public double getTemperature() throws IOException {
            return queryDouble("TEC:T?") + 273.15;
        }

        @Override
        public void setTemperatureRange(double range) {
            // No ranging options
        }

        @Override
        public double getTemperatureRange() {
            return 999.999;
        }

        @Override
        public String getIDN() throws IOException {
            return ArroyoTEC.this.getIDN();
        }

        @Override
        public Address getAddress() {
            return ArroyoTEC.this.getAddress();
        }

    };

    private final Heater HEATER = new Heater() {

        @Override
        public double getValue() throws IOException {
            return Math.pow(queryDouble("TEC:V?") / 2.5, 2) * 100.0;
        }

        @Override
        public double getLimit() {
            return 100.0;
        }

        @Override
        public void setLimit(double range) {
            // No limiting options
        }

        @Override
        public String getName() {
            return "Peltier Element";
        }

        @Override
        public String getIDN() throws IOException {
            return ArroyoTEC.this.getIDN();
        }

        @Override
        public Address getAddress() {
            return ArroyoTEC.this.getAddress();
        }

    };

    private final Loop LOOP = new ZonedLoop() {

        private double rampRate = 0.0;
        private boolean ramping = false;

        @Override
        public void setSetPoint(double temperature) throws IOException, DeviceException {
            write("TEC:T %f", temperature - 273.15);
            updatePID(temperature);
        }

        @Override
        public double getSetPoint() throws IOException {
            return queryDouble("TEC:SET:T?") + 273.15;
        }

        @Override
        public String getName() {
            return "PID Loop";
        }

        @Override
        public void setRampEnabled(boolean flag) throws IOException, DeviceException {
            ramping = flag;
            setRampRate(rampRate);
        }

        @Override
        public boolean isRampEnabled() {
            return ramping;
        }

        @Override
        public void setRampRate(double limit) throws IOException {

            rampRate = limit;

            if (ramping) {
                write("TEC:TRATE %f", Math.min(100.0, Math.max(0, rampRate)));
            }

        }

        @Override
        public double getRampRate() {
            return rampRate;
        }

        @Override
        public double getPValue() throws IOException {
            return Double.parseDouble(query("TEC:PID?").split(",")[0]);
        }

        @Override
        public double getIValue() throws IOException {
            return Double.parseDouble(query("TEC:PID?").split(",")[1]);
        }

        @Override
        public double getDValue() throws IOException {
            return Double.parseDouble(query("TEC:PID?").split(",")[2]);
        }

        public void setPIDValues(double p, double i, double d) throws IOException {

            write(
                "TEC:PID %f,%f,%f",
                Math.min(10.0, Math.max(0, p)),
                Math.min(10.0, Math.max(0, i)),
                Math.min(10.0, Math.max(0, d))
            );

            write("TEC:GAIN PID");

        }

        @Override
        public void setPValue(double value) throws IOException {
            double[] values = Arrays.stream(query("TEC:PID?").split(",")).mapToDouble(Double::parseDouble).toArray();
            values[0] = value;
            setPIDValues(values[0], values[1], values[2]);
        }

        @Override
        public void setIValue(double value) throws IOException {
            double[] values = Arrays.stream(query("TEC:PID?").split(",")).mapToDouble(Double::parseDouble).toArray();
            values[1] = value;
            setPIDValues(values[0], values[1], values[2]);
        }

        @Override
        public void setDValue(double value) throws IOException {
            double[] values = Arrays.stream(query("TEC:PID?").split(",")).mapToDouble(Double::parseDouble).toArray();
            values[2] = value;
            setPIDValues(values[0], values[1], values[2]);
        }

        @Override
        public TMeter getInput() {
            return THERMOMETER;
        }

        @Override
        public Heater getOutput() {
            return HEATER;
        }

        @Override
        public void setInput(Input input) throws DeviceException {

            if (input != THERMOMETER) {
                throw new DeviceException("You cannot use that input for this loop");
            }

        }

        @Override
        public void setOutput(Output output) throws DeviceException {

            if (output != HEATER) {
                throw new DeviceException("You cannot use that output for this loop");
            }

        }

        @Override
        public List<Heater> getAvailableOutputs() {
            return List.of(HEATER);
        }

        @Override
        public List<TMeter> getAvailableInputs() {
            return List.of(THERMOMETER);
        }

        @Override
        public void setManualValue(double value) {
            // Cannot set manual value other than "off"
        }

        @Override
        public double getManualValue() {
            return 0.0;
        }

        @Override
        public void setPIDEnabled(boolean flag) throws IOException {
            write("TEC:OUT %d", flag ? 1 : 0);
        }

        @Override
        public boolean isPIDEnabled() throws IOException {
            return queryInt("TEC:OUT?") == 1;
        }

        @Override
        public String getIDN() throws IOException {
            return ArroyoTEC.this.getIDN();
        }

        @Override
        public Address getAddress() {
            return ArroyoTEC.this.getAddress();
        }

        public void setSensorType(SensorType type) throws IOException {
            write("TEC:SEN %d", type.ordinal());
        }

        public SensorType getSensorType() throws IOException {
            int ordinal = queryInt("TEC:SEN?");
            return SensorType.values()[ordinal];
        }

        public List<Parameter<?>> getConfigurationParameters(Class<?> target) {

            List<Parameter<?>> defaultList = super.getConfigurationParameters(target);
            defaultList.add(new Parameter<>("Sensor Type", SensorType.DISABLED, this::setSensorType, SensorType.values()));
            return defaultList;

        }

    };

    public enum SensorType {
        DISABLED,
        THERMISTOR_100uA,
        THERMISTOR_10uA,
        LM335,
        AD590,
        RTD,
        RTD_4_WIRE,
        THERMISTOR_1mA
    }

}
