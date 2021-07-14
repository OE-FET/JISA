package jisa.devices.temperature;

import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.PID;
import jisa.devices.interfaces.TC;
import jisa.visa.Connection;
import jisa.visa.VISADevice;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;


public class Arroyo585 extends VISADevice implements TC {

    public static String getDescription() {
        return "Arroyo 585 TecPak";
    }

    private PID.Zone[] zones   = new PID.Zone[0];

    public Arroyo585(Address address) throws IOException, DeviceException {
        super(address);
        setSerialParameters(38400, 8, Connection.Parity.NONE, Connection.StopBits.ONE, Connection.Flow.NONE);
        write("TEC:MODE:T");
    }



    @Override
    public String getSensorName() {
        return "Arroyo";
    }

    @Override
    public void setSensorType(SensorType type) throws IOException, DeviceException {

    }

    @Override
    public SensorType getSensorType() throws IOException, DeviceException {
        return SensorType.UNKNOWN;
    }

    @Override
    public double getTemperature() throws IOException {
        return Integer.parseInt(query("TEC:T?")) + 273.15;
    }

    @Override
    public double getTemperatureRange() throws IOException {
        return 100.0;
    }

    @Override
    public void setTemperatureRange(double range) throws IOException {

    }

    @Override
    public double getTargetTemperature() throws IOException {
        return Integer.parseInt(query("TEC:SET:T?")) + 273.15;
    }

    @Override
    public void setTargetTemperature(double temperature) throws IOException, DeviceException {
        query("TEC:T %f",temperature - 273.15);

    }

    @Override
    public double getTemperatureRampRate() throws IOException, DeviceException {
        return Integer.parseInt(query("TEC:TRATE?"));
    }

    @Override
    public void setTemperatureRampRate(double kPerMin) throws IOException, DeviceException {
        write("TEC:TRATE %f",kPerMin);

    }

    @Override
    public double getHeaterPower() throws IOException {
        return 0.0;
    }

    @Override
    public void setHeaterPower(double powerPCT) throws IOException {

    }

    @Override
    public double getFlow() {
        return 0.0;
    }

    @Override
    public void useAutoHeater() throws IOException {
    }

    @Override
    public boolean isUsingAutoHeater() throws IOException {
        return false;
    }

    @Override
    public void useAutoFlow() {
    }

    @Override
    public void setFlow(double outputPCT) {
    }

    @Override
    public boolean isUsingAutoFlow() {
        return false;
    }

    @Override
    public double getPValue() throws IOException {
        return Double.parseDouble(query("TEC:PID?").split(",")[0]);
    }

    @Override
    public void setPValue(double value) throws IOException {
        write("TEC:PID %f",value);
    }

    @Override
    public double getIValue() throws IOException {
        return Double.parseDouble(query("TEC:PID?").split(",")[1]);
    }

    @Override
    public void setIValue(double value) throws IOException {
        double P = getPValue();
        double D = getDValue();
        write("TEC:PID %f ,%f ,%f",P,value,D);
    }

    @Override
    public void useAutoPID(boolean flag) throws IOException, DeviceException {


    }

    @Override
    public boolean isUsingAutoPID() {
        return false;
    }

    @Override
    public List<Zone> getAutoPIDZones() {
        return Arrays.asList(zones);

    }

    @Override
    public void setAutoPIDZones(Zone... zones) throws IOException, DeviceException {
    }

    @Override
    public double getDValue() throws IOException {
        return Double.parseDouble(query("TEC:PID?").split(",")[2]);
    }

    @Override
    public void setDValue(double value) throws IOException {
        double P = getPValue();
        double I = getIValue();
        write("TEC:PID %f ,%f ,%f",P,I,value);

    }

    @Override
    public double getHeaterRange() {
        return 100.0;
    }

    @Override
    public void setHeaterRange(double rangePCT) {

    }

}
