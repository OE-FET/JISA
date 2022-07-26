package jisa.devices.temperature;

import jisa.addresses.Address;
import jisa.control.RTask;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.TC;

import java.io.IOException;
import java.util.List;

public class FakeTC implements TC {

    public static String getDescription() {
        return "Fake TC";
    }

    public FakeTC(Address address) {
        updater.start();
    }

    @Override
    public String getIDN() throws IOException, DeviceException {
        return "FTC-1000";
    }

    @Override
    public void close() throws IOException, DeviceException {
        updater.stop();
    }

    @Override
    public Address getAddress() {
        return null;
    }

    @Override
    public List<Thermometer> getInputs() {
        return List.of(THERMOMETER);
    }

    @Override
    public List<Heater> getOutputs() {
        return List.of(HEATER);
    }

    @Override
    public List<Loop> getLoops() {
        return List.of(LOOP);
    }

    private       double setPoint = 300.0;
    private       double current  = 300.0;
    private final RTask  updater  = new RTask(500, () -> {

        double diff = (setPoint - current) / 10;

        if (Math.abs(diff) < 0.1) {
            diff = Math.signum(diff) * 0.1;
        }

        if (Math.abs(diff) > Math.abs(setPoint - current)) {
            diff = setPoint - current;
        }

        current += diff;

    });

    private final Thermometer THERMOMETER = new Thermometer() {

        @Override
        public String getName() {
            return "Thermometer";
        }

        @Override
        public String getSensorName() {
            return getName();
        }

        @Override
        public double getTemperature() {
            return current;
        }

        @Override
        public void setTemperatureRange(double range) {

        }

        @Override
        public double getTemperatureRange() {
            return 999.999;
        }

        @Override
        public String getIDN() throws IOException, DeviceException {
            return FakeTC.this.getIDN();
        }

        @Override
        public Address getAddress() {
            return FakeTC.this.getAddress();
        }

    };

    private final Heater HEATER = new Heater() {

        @Override
        public double getValue() {
            return 0;
        }

        @Override
        public double getLimit() {
            return 100.0;
        }

        @Override
        public void setLimit(double range) {

        }

        @Override
        public String getName() {
            return "Heater";
        }

        @Override
        public String getIDN() throws IOException, DeviceException {
            return FakeTC.this.getIDN();
        }

        @Override
        public Address getAddress() {
            return FakeTC.this.getAddress();
        }

    };

    private final Loop LOOP = new ZonedLoop() {

        @Override
        public void setTemperature(double temperature) {
            setPoint = temperature;
        }

        @Override
        public double getTemperature() {
            return setPoint;
        }

        @Override
        public String getName() {
            return "PID Loop";
        }

        @Override
        public void setRampEnabled(boolean flag) {

        }

        @Override
        public boolean isRampEnabled() {
            return false;
        }

        @Override
        public void setRampRate(double limit) {

        }

        @Override
        public double getRampRate() {
            return 0;
        }

        @Override
        public double getPValue() {
            return 0;
        }

        @Override
        public double getIValue() {
            return 0;
        }

        @Override
        public double getDValue() {
            return 0;
        }

        @Override
        public void setPValue(double value) {

        }

        @Override
        public void setIValue(double value) {

        }

        @Override
        public void setDValue(double value) {

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
        public void setInput(Input input) {

        }

        @Override
        public void setOutput(Output output) {

        }

        @Override
        public List<? extends Output> getAvailableOutputs() {
            return List.of(HEATER);
        }

        @Override
        public List<? extends Input> getAvailableInputs() {
            return List.of(THERMOMETER);
        }

        @Override
        public void setManualValue(double value) {

        }

        @Override
        public double getManualValue() {
            return 0;
        }

        @Override
        public void setPIDEnabled(boolean flag) {

        }

        @Override
        public boolean isPIDEnabled() {
            return true;
        }

        @Override
        public String getIDN() throws IOException, DeviceException {
            return FakeTC.this.getIDN();
        }

        @Override
        public Address getAddress() {
            return FakeTC.this.getAddress();
        }

    };

}
