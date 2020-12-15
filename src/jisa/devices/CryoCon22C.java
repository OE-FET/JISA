package jisa.devices;

import jisa.addresses.Address;
import jisa.visa.Connection;
import jisa.visa.VISADevice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CryoCon22C extends VISADevice implements MSMOTC {

    public static final List<String> SENSORS = List.of("A", "B");

    private final List<List<Zone>> pidZones = List.of(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    private final boolean[]        autoPID  = {false, false, false, false};

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

        write("INPUT A:UNITS K");
        write("INPUT B:UNITS K");

    }


    @Override
    public int getNumOutputs() {
        return 4;
    }

    @Override
    public void useSensor(int output, int sensor) throws IOException, DeviceException {

        checkOutput(output);
        checkSensor(sensor);

        write("LOOP %d:SOURCE %s", output + 1, SENSORS.get(sensor));

    }

    @Override
    public int getUsedSensor(int output) throws IOException, DeviceException {
        checkOutput(output);
        return SENSORS.indexOf(query("LOOP %d:SOURCE?", output + 1));
    }

    @Override
    public void setPValue(int output, double value) throws IOException, DeviceException {
        checkOutput(output);
        write("LOOP %d:PGAIN %e", output + 1, value);
    }

    @Override
    public void setIValue(int output, double value) throws IOException, DeviceException {
        checkOutput(output);
        write("LOOP %d:IGAIN %e", output + 1, value);
    }

    @Override
    public void setDValue(int output, double value) throws IOException, DeviceException {
        checkOutput(output);
        write("LOOP %d:DGAIN %e", output + 1, value);
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

        write("LOOP %d:RANGE %s", output + 1, value);

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
        write("LOOP %d:SETPT %e", output + 1, temperature);
        updateAutoPID(output);
    }

    @Override
    public double getTargetTemperature(int output) throws IOException, DeviceException {
        checkOutput(output);
        return queryDouble("LOOP %d:SETPT?", output + 1);
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
        write("LOOP %d:TYPE PID", output + 1);
    }

    @Override
    public boolean isUsingAutoHeater(int output) throws IOException, DeviceException {
        checkOutput(output);
        return query("LOOP %d:TYPE?").equals("PID");
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
        write("LOOP %d:TYPE MAN");
        write("LOOP %d:PMANUAL %e", powerPCT);
    }

    @Override
    public void setFlow(int output, double outputPCT) throws IOException, DeviceException {
        checkOutput(output);
    }

    @Override
    public double getTemperature(int sensor) throws IOException, DeviceException {
        checkSensor(sensor);
        return queryDouble("INPUT %s?", SENSORS.get(sensor));
    }

    @Override
    public int getNumSensors() {
        return 2;
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
}
