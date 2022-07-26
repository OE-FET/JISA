package jisa.devices.temperature;

import com.intelligt.modbus.jlibmodbus.serial.SerialPort;
import jisa.Util;
import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.TC;
import jisa.visa.ModbusRTUDevice;

import java.io.IOException;
import java.util.List;

public class ET2408 extends ModbusRTUDevice implements TC {

    public static String getDescription() {
        return "EuroTherm 2408";
    }

    // Constants
    private static final int        UNITS_CELSIUS    = 0;
    private static final int        UNITS_FAHRENHEIT = 1;
    private static final int        UNITS_KELVIN     = 2;
    private static final int        UNITS_NONE       = 3;
    private static final int        MODE_CONFIG      = 2;
    private static final int        MODE_NORMAL      = 0;
    private static final int        DEC_PLACES_0     = 0;
    private static final int        DEC_PLACES_1     = 1;
    private static final int        DEC_PLACES_2     = 2;
    // Registers and Coils
    private final        RORegister sensor           = new RORegister(1);
    private final        RWRegister setPoint         = new RWRegister(2);
    private final        RWRegister output           = new RWRegister(3);
    private final        RWRegister P                = new RWRegister(6);
    private final        RWRegister I                = new RWRegister(8);
    private final        RWRegister D                = new RWRegister(9);
    private final        RWRegister mode             = new RWRegister(199);
    private final        RWRegister units            = new RWRegister(516);
    private final        RWRegister manual           = new RWRegister(273);
    private final        RWRegister decPlaces        = new RWRegister(525);

    public ET2408(Address address, SerialPort.BaudRate baud, int dataBits, int stopBits, SerialPort.Parity parity) throws IOException, DeviceException {

        super(address, baud, dataBits, stopBits, parity);

        if (units.get() != UNITS_KELVIN) {

            mode.set(MODE_CONFIG);
            units.set(UNITS_KELVIN);
            mode.set(MODE_NORMAL);

            // Need to wait for the device to re-initialise
            Util.sleep(5000);

        }

    }

    /**
     * Opens a connection to a Eurotherm 2408 process controller at the given modbus address, using default serial parameters.
     *
     * @param address Modbus address
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon incompatibility error
     */
    public ET2408(Address address) throws IOException, DeviceException {
        this(address, SerialPort.BaudRate.BAUD_RATE_9600, 8, 1, SerialPort.Parity.NONE);
    }

    private double getScale() throws IOException {

        switch (decPlaces.get()) {

            case 2:
                return 100.0;

            case 1:
                return 10.0;

            default:
            case 0:
                return 1.0;

        }

    }

    @Override
    public List<Thermometer> getInputs() {
        return List.of(THERMOMETER);
    }

    @Override
    public List<Heater> getOutputs() throws IOException, DeviceException {
        return List.of(HEATER);
    }

    @Override
    public List<? extends Loop> getLoops() {
        return List.of(LOOP);
    }


    public final Thermometer THERMOMETER = new Thermometer() {

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
            return (double) sensor.get() / getScale();
        }

        @Override
        public void setTemperatureRange(double range) throws IOException {

            if (range < 100) {
                decPlaces.set(DEC_PLACES_2);
            } else if (range < 1000) {
                decPlaces.set(DEC_PLACES_1);
            } else {
                decPlaces.set(DEC_PLACES_0);
            }

        }

        @Override
        public double getTemperatureRange() throws IOException {

            switch (decPlaces.get()) {

                case DEC_PLACES_0:
                    return 9999;

                case DEC_PLACES_1:
                    return 999.9;

                case DEC_PLACES_2:
                    return 99.9;

                default:
                    return 0;

            }

        }

        @Override
        public String getIDN() throws IOException {
            return ET2408.this.getIDN();
        }

        @Override
        public Address getAddress() {
            return ET2408.this.getAddress();
        }

    };

    public final Heater HEATER = new Heater() {

        @Override
        public double getValue() throws IOException {
            return (double) output.get() / 10.0;
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
            return "Heater";
        }

        @Override
        public String getIDN() throws IOException {
            return ET2408.this.getIDN();
        }

        @Override
        public Address getAddress() {
            return ET2408.this.getAddress();
        }

    };

    public final Loop LOOP = new ZonedLoop() {

        @Override
        public void setSetPoint(double temperature) throws IOException, DeviceException {
            setPoint.set((int) (temperature * getScale()));
            updatePID(temperature);
        }

        @Override
        public double getSetPoint() throws IOException {
            return (double) setPoint.get() / getScale();
        }

        @Override
        public String getName() {
            return "PID Loop";
        }

        @Override
        public void setRampEnabled(boolean flag) {
            // No ramping feature available
        }

        @Override
        public boolean isRampEnabled() {
            return false;
        }

        @Override
        public void setRampRate(double limit) {
            // No ramping feature available
        }

        @Override
        public double getRampRate() {
            return 0.0;
        }

        @Override
        public double getPValue() throws IOException {
            return (double) P.get() / 10.0;
        }

        @Override
        public double getIValue() throws IOException {
            return (double) I.get() / 10.0;
        }

        @Override
        public double getDValue() throws IOException {
            return (double) D.get() / 10.0;
        }

        @Override
        public void setPValue(double value) throws IOException {
            P.set((int) (value * 10.0));
        }

        @Override
        public void setIValue(double value) throws IOException {
            I.set((int) (value * 10.0));
        }

        @Override
        public void setDValue(double value) throws IOException {
            D.set((int) (value * 10.0));
        }

        @Override
        public Input getInput() {
            return THERMOMETER;
        }

        @Override
        public Output getOutput() {
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
        public List<Thermometer> getAvailableInputs() {
            return List.of(THERMOMETER);
        }

        @Override
        public void setManualValue(double value) throws IOException {
            output.set((int) (value * 10.0));
        }

        @Override
        public double getManualValue() throws IOException {
            return (double) output.get() / 10.0;
        }

        @Override
        public void setPIDEnabled(boolean flag) throws IOException {
            manual.set(flag ? 0 : 1);
        }

        @Override
        public boolean isPIDEnabled() throws IOException {
            return manual.get() == 0;
        }

        @Override
        public String getIDN() throws IOException {
            return ET2408.this.getIDN();
        }

        @Override
        public Address getAddress() {
            return ET2408.this.getAddress();
        }

    };

}
