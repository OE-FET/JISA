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

    private double setPoint = 300.0;
    private double current  = 300.0;
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
    public double getPValue() throws IOException, DeviceException {
        return 0;
    }

    @Override
    public void setPValue(double value) throws IOException, DeviceException {

    }

    @Override
    public double getIValue() throws IOException, DeviceException {
        return 0;
    }

    @Override
    public void setIValue(double value) throws IOException, DeviceException {

    }

    @Override
    public double getDValue() throws IOException, DeviceException {
        return 0;
    }

    @Override
    public void setDValue(double value) throws IOException, DeviceException {

    }

    @Override
    public void useAutoPID(boolean flag) throws IOException, DeviceException {

    }

    @Override
    public boolean isUsingAutoPID() throws IOException, DeviceException {
        return false;
    }

    @Override
    public List<Zone> getAutoPIDZones() throws IOException, DeviceException {
        return null;
    }

    @Override
    public void setAutoPIDZones(Zone... zones) throws IOException, DeviceException {

    }

    @Override
    public String getSensorName() {
        return "Main Sensor";
    }

    @Override
    public double getTemperature() throws IOException, DeviceException {
        return current;
    }

    @Override
    public void setTemperatureRange(double range) throws IOException, DeviceException {

    }

    @Override
    public double getTemperatureRange() throws IOException, DeviceException {
        return 999.9;
    }

    @Override
    public double getTargetTemperature() throws IOException, DeviceException {
        return setPoint;
    }

    @Override
    public void setTargetTemperature(double temperature) throws IOException, DeviceException {
        setPoint = temperature;
    }

    @Override
    public double getTemperatureRampRate() throws IOException, DeviceException {
        return 0;
    }

    @Override
    public void setTemperatureRampRate(double kPerMin) throws IOException, DeviceException {

    }

    @Override
    public double getHeaterPower() throws IOException, DeviceException {
        return 0;
    }

    @Override
    public void setHeaterPower(double powerPCT) throws IOException, DeviceException {

    }

    @Override
    public double getFlow() throws IOException, DeviceException {
        return 0;
    }

    @Override
    public void setFlow(double outputPCT) throws IOException, DeviceException {

    }

    @Override
    public void useAutoHeater() throws IOException, DeviceException {

    }

    @Override
    public boolean isUsingAutoHeater() throws IOException, DeviceException {
        return false;
    }

    @Override
    public void useAutoFlow() throws IOException, DeviceException {

    }

    @Override
    public boolean isUsingAutoFlow() throws IOException, DeviceException {
        return false;
    }

    @Override
    public double getHeaterRange() throws IOException, DeviceException {
        return 0;
    }

    @Override
    public void setHeaterRange(double rangePCT) throws IOException, DeviceException {

    }
}
