package jisa.devices.temperature;

import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.MSMOTC;
import jisa.visa.Connection;
import jisa.visa.VISADevice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CryoCon22C extends VISADevice implements MSMOTC {

    public static String getDescription() {
        return "Cryo-Con 22C";
    }

    @Override
    public void setSensorType(int sensor, SensorType type) throws IOException, DeviceException {

    }

    @Override
    public SensorType getSensorType(int sensor) throws IOException, DeviceException {
        return SensorType.UNKNOWN;
    }

    public static final List<String> SENSORS = List.of("A", "B");

    private final List<List<Zone>> pidZones  = List.of(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    private final boolean[]        autoPID   = {false, false, false, false};

    public CryoCon22C(Address address) throws IOException, DeviceException {

        super(address);

        if (address.getType() == Address.Type.SERIAL) {

            setSerialParameters(
                9600,
                8,
                Connection.Parity.NONE,
                Connection.StopBits.ONE,
                Connection.Flow.NONE
            );

        }

        if (address.getType() == Address.Type.GPIB) {
            setEOI(true);
        } else {
            setWriteTerminator("\n");
            setReadTerminator("\n");
        }

        addAutoRemove("\n");
        addAutoRemove("\r");

        String[] idn = getIDN().split(",");

        if (!(idn[0].equals("Cryo-con") && idn[1].equals("22C"))) {
            throw new DeviceException("The device connected on \"%s\" is not a Cryo-Con 22C.", address.toString());
        }

        query("INPUT A:UNITS K");
        query("INPUT B:UNITS K");

    }


    @Override
    public int getNumOutputs() {
        return 4;
    }

    @Override
    public String getOutputName(int outputNumber) {
        return String.format("Loop %d", outputNumber + 1);
    }

    @Override
    public void useSensor(int output, int sensor) throws IOException, DeviceException {

        checkOutput(output);
        checkSensor(sensor);

        query("LOOP %d:SOURCE %s", output + 1, SENSORS.get(sensor));

    }

    @Override
    public int getUsedSensor(int output) throws IOException, DeviceException {
        checkOutput(output);

        switch(query("LOOP %d:SOURCE?", output+1)) {

            case "CHA":
                return 0;

            case "CHB":
                return 1;

            default:
                throw new IOException("Unexpected response from Cryo-Con 22C.");

        }

    }

    @Override
    public void setPValue(int output, double value) throws IOException, DeviceException {
        checkOutput(output);
        query("LOOP %d:PGAIN %e", output + 1, value);
    }

    @Override
    public void setIValue(int output, double value) throws IOException, DeviceException {
        checkOutput(output);
        query("LOOP %d:IGAIN %e", output + 1, value);
    }

    @Override
    public void setDValue(int output, double value) throws IOException, DeviceException {
        checkOutput(output);
        query("LOOP %d:DGAIN %e", output + 1, value);
    }

    @Override
    public double getPValue(int output) throws IOException, DeviceException {
        checkOutput(output);
        return queryDouble("LOOP %d:PGAIN?");
    }

    @Override
    public double getIValue(int output) throws IOException, DeviceException {
        checkOutput(output);
        return queryDouble("LOOP %d:IGAIN?");
    }

    @Override
    public double getDValue(int output) throws IOException, DeviceException {
        checkOutput(output);
        return queryDouble("LOOP %d:DGAIN?");
    }

    @Override
    public void useAutoPID(int output, boolean flag) throws IOException, DeviceException {
        checkOutput(output);
        autoPID[output] = false;
    }

    @Override
    public boolean isUsingAutoPID(int output) throws IOException, DeviceException {
        checkOutput(output);
        return autoPID[output];
    }

    @Override
    public List<Zone> getAutoPIDZones(int output) throws IOException, DeviceException {
        checkOutput(output);
        return pidZones.get(output);
    }

    @Override
    public void setAutoPIDZones(int output, Zone... zones) throws IOException, DeviceException {
        checkOutput(output);
        pidZones.get(output).clear();
        pidZones.get(output).addAll(Arrays.asList(zones));
    }

    @Override
    public void setHeaterRange(int output, double range) throws IOException, DeviceException {

        checkOutput(output);

        String value;

        switch (output) {

            case 0:

                if (range > 10.0) { value = "HIGH"; } else if (range > 1.0) { value = "MID"; } else { value = "LOW"; }

                break;

            case 1:

                if (range > 10.0) { value = "HIGH"; } else { value = "LOW"; }

                break;

            case 3:
            case 4:

                if (range > 50.0) { value = "10V"; } else { value = "5V"; }

                break;

            default:
                throw new DeviceException("That range is not supported.");

        }

        query("LOOP %d:RANGE %s", output + 1, value);

    }

    @Override
    public double getHeaterRange(int output) throws IOException, DeviceException {

        checkOutput(output);
        String response = query("LOOP %d:RANGE?", output + 1);
        Double range    = null;

        switch (output) {

            case 1:

                switch (response) {

                    case "HIGH":
                        range = 100.0;
                        break;

                    case "MID":
                        range = 10.0;
                        break;

                    case "LOW":
                        range = 1.0;
                        break;

                }

                break;

            case 2:

                switch (response) {

                    case "HIGH":
                        range = 100.0;
                        break;

                    case "LOW":
                        range = 10.0;
                        break;

                }

                break;

            case 3:
            case 4:

                switch (response) {

                    case "10V":
                        range = 100.0;
                        break;

                    case "5V":
                        range = 50.0;
                        break;

                }

                break;

        }

        if (range == null) {
            throw new IOException("Invalid response from Cryo-Con 22C.");
        }

        return range;

    }

    @Override
    public void setTargetTemperature(int output, double temperature) throws IOException, DeviceException {
        checkOutput(output);
        query("LOOP %d:SETPT %e", output + 1, temperature);
        updateAutoPID(output);
    }

    @Override
    public double getTargetTemperature(int output) throws IOException, DeviceException {
        checkOutput(output);
        return Double.parseDouble(query("LOOP %d:SETPT?", output + 1).replace("K", ""));
    }

    @Override
    public void setTemperatureRampRate(int output, double kPerMin) throws IOException, DeviceException {

        checkOutput(output);

        query("LOOP %d:RATE %f", output + 1, kPerMin);

        if (kPerMin == 0 && isUsingAutoHeater(output)) {
            query("LOOP %d:TYPE PID", output + 1);
        } else if (isUsingAutoHeater(output)) {
            query("LOOP %d:TYPE RAMPP", output + 1);
        }

    }

    @Override
    public double getTemperatureRampRate(int output) throws IOException, DeviceException {
        checkOutput(output);
        return queryDouble("LOOP %d:RATE?", output);
    }

    @Override
    public double getHeaterPower(int output) throws IOException, DeviceException {
        checkOutput(output);
        return queryDouble("LOOP %d:OUTPWR?", output + 1);
    }

    @Override
    public double getFlow(int output) throws DeviceException {
        checkOutput(output);
        return 0;
    }

    @Override
    public void useAutoHeater(int output) throws IOException, DeviceException {

        checkOutput(output);

        if (getTemperatureRampRate(output) == 0) {
            query("LOOP %d:TYPE PID", output + 1);
        } else {
            query("LOOP %d:TYPE RAMPP", output + 1);
        }

    }

    @Override
    public boolean isUsingAutoHeater(int output) throws IOException, DeviceException {
        checkOutput(output);
        String response = query("LOOP %d:TYPE?").trim().toUpperCase();
        return response.equals("PID") || response.equals("RAMPP");
    }

    @Override
    public void useAutoFlow(int output) throws IOException, DeviceException {
        checkOutput(output);
    }

    @Override
    public boolean isUsingAutoFlow(int output) throws IOException, DeviceException {
        checkOutput(output);
        return false;
    }

    @Override
    public void setHeaterPower(int output, double powerPCT) throws IOException, DeviceException {
        checkOutput(output);
        query("LOOP %d:TYPE MAN");
        query("LOOP %d:PMANUAL %e", powerPCT);
    }

    @Override
    public void setFlow(int output, double outputPCT) throws IOException, DeviceException {
        checkOutput(output);
    }

    @Override
    public double getTemperature(int sensor) throws IOException, DeviceException {
        checkSensor(sensor);
        return queryDouble("INPUT? %s", SENSORS.get(sensor));
    }

    @Override
    public int getNumSensors() {
        return 2;
    }

    @Override
    public String getSensorName(int sensorNumber) {

        try {
            checkSensor(sensorNumber);
            return String.format("%s (%s)", SENSORS.get(sensorNumber), query("INPUT %s:NAME?", SENSORS.get(sensorNumber)));
        } catch (Exception e) {
            return "Unknown Sensor";
        }

    }

    @Override
    public void setTemperatureRange(int sensor, double range) throws IOException, DeviceException {
        checkSensor(sensor);
    }

    @Override
    public double getTemperatureRange(int sensor) throws IOException, DeviceException {
        checkSensor(sensor);
        return 999.999;
    }

    @Override
    public String getOutputName() {
        return getOutputName(0);
    }

    @Override
    public String getSensorName() {

        try {
            return getSensorName(getUsedSensor());
        } catch (IOException | DeviceException e) {
            return "Unknown Sensor";
        }

    }
}
