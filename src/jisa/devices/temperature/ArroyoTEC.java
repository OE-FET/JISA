package jisa.devices.temperature;

import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.PID;
import jisa.devices.interfaces.TC;
import jisa.devices.interfaces.TCouple;
import jisa.visa.Connection;
import jisa.visa.VISADevice;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


public class ArroyoTEC extends VISADevice implements TC {

    public static String getDescription() {
        return "Arroyo 585 TecPak";
    }

    private boolean    autoPID = false;
    private PID.Zone[] zones   = new PID.Zone[0];

    public ArroyoTEC(Address address) throws IOException, DeviceException {

        super(address);

        setSerialParameters(38400, 8, Connection.Parity.NONE, Connection.StopBits.ONE, Connection.Flow.NONE);

        setWriteTerminator("\n");
        setReadTerminator("\n");
        addAutoRemove("\n");
        addAutoRemove("\r");

        if (!getIDN().toLowerCase().contains("arroyo")) {
            throw new DeviceException("Instrument at \"%s\" is not an Arroyo.", address.toString());
        }

        write("TEC:MODE:T");
        write("TEC:HEATCOOL BOTH");

    }


    @Override
    public String getSensorName() {
        return "Main Sensor";
    }

    @Override
    public double getTemperature() throws IOException {
        return queryDouble("TEC:T?") + 273.15;
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
        return queryDouble("TEC:SET:T?") + 273.15;
    }

    @Override
    public void setTargetTemperature(double temperature) throws IOException, DeviceException {
        write("TEC:T %f", temperature - 273.15);
        updateAutoPID();
    }

    @Override
    public double getTemperatureRampRate() throws IOException, DeviceException {
        return queryDouble("TEC:TRATE?");
    }

    @Override
    public void setTemperatureRampRate(double kPerMin) throws IOException {
        write("TEC:TRATE %f", Math.min(100.0, Math.max(0, kPerMin)));
    }

    @Override
    public double getHeaterPower() throws IOException {
        return Math.pow(queryDouble("TEC:V?") / 2.5, 2) * 100.0;
    }

    @Override
    public void setHeaterPower(double powerPCT) throws IOException {
        write("TEC:OUT 0");
    }

    @Override
    public double getFlow() {
        return 0.0;
    }

    @Override
    public void useAutoHeater() throws IOException {
        write("TEC:OUT 1");
    }

    @Override
    public boolean isUsingAutoHeater() throws IOException {
        return queryInt("TEC:OUT?") == 1;
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
        setPIDValues(value, getIValue(), getDValue());
    }

    @Override
    public double getIValue() throws IOException {
        return Double.parseDouble(query("TEC:PID?").split(",")[1]);
    }

    @Override
    public void setIValue(double value) throws IOException {
        setPIDValues(getPValue(), value, getDValue());
    }

    public void setPIDValues(double p, double i, double d) throws IOException {

        write(
            "TEC:PID %f,%f,%f",
            Math.min(10.0, Math.max(0, p)),
            Math.min(10.0, Math.max(0, i)),
            Math.min(10.0, Math.max(0, d))
        );

    }

    @Override
    public void useAutoPID(boolean flag) throws IOException, DeviceException {
        autoPID = flag;
        updateAutoPID();
    }

    @Override
    public boolean isUsingAutoPID() {
        return autoPID;
    }

    @Override
    public List<Zone> getAutoPIDZones() {
        return Arrays.asList(zones);
    }

    @Override
    public void setAutoPIDZones(Zone... zones) throws IOException, DeviceException {
        this.zones = zones;
        updateAutoPID();
    }

    @Override
    public double getDValue() throws IOException {
        return Double.parseDouble(query("TEC:PID?").split(",")[2]);
    }

    @Override
    public void setDValue(double value) throws IOException {
        setPIDValues(getPValue(), getIValue(), value);
    }

    @Override
    public double getHeaterRange() {
        return 100.0;
    }

    @Override
    public void setHeaterRange(double rangePCT) {

    }

}
