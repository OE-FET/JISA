package jisa.devices.temperature;

import jisa.Util;
import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.TC;
import jisa.visa.VISADevice;

import java.io.IOException;
import java.util.List;

public class CryoCon22C extends VISADevice implements TC {

    public final TMeter SENSOR_A = new TMeter("A");
    public final TMeter SENSOR_B = new TMeter("B");
    public final Heater HEATER_1 = new Heater(1);
    public final Heater HEATER_2 = new Heater(2);
    public final Heater HEATER_3 = new Heater(3);
    public final Heater HEATER_4 = new Heater(4);
    public final Loop   LOOP_1   = new Loop(HEATER_1);
    public final Loop   LOOP_2   = new Loop(HEATER_2);
    public final Loop   LOOP_3   = new Loop(HEATER_3);
    public final Loop   LOOP_4   = new Loop(HEATER_4);

    public static String getDescription() {
        return "Cryo-Con 22C";
    }

    public CryoCon22C(Address address) throws IOException, DeviceException {

        super(address);

        setWriteTerminator("\n");
        setReadTerminator("\n");

        configSerial(serial -> serial.setSerialParameters(9600, 8));

        configGPIB(gpib -> {

            gpib.setEOIEnabled(true);
            setReadTerminator("");
            setWriteTerminator("");

        });

        addAutoRemove("\n", "\r");

        String[] idn = getIDN().split(",");

        if (!(idn[0].equals("Cryo-con") && idn[1].equals("22C"))) {
            throw new DeviceException("The device connected on \"%s\" is not a Cryo-Con 22C.", address.toString());
        }

        query("INPUT A:UNITS K");
        query("INPUT B:UNITS K");

    }

    @Override
    public List<TMeter> getInputs() {
        return List.of(SENSOR_A, SENSOR_B);
    }

    @Override
    public List<Heater> getOutputs() {
        return List.of(HEATER_1, HEATER_2, HEATER_3, HEATER_4);
    }

    @Override
    public List<Loop> getLoops() {
        return List.of(LOOP_1, LOOP_2, LOOP_3, LOOP_4);
    }

    public class TMeter implements TC.TMeter {

        private final String sensor;

        private TMeter(String sensor) {
            this.sensor = sensor;
        }

        public String getSensor() {
            return sensor;
        }

        @Override
        public String getIDN() throws IOException, DeviceException {
            return CryoCon22C.this.getIDN();
        }

        @Override
        public Address getAddress() {
            return CryoCon22C.this.getAddress();
        }

        @Override
        public String getName() {

            try {
                return String.format("%s (%s)", sensor, query("INPUT %s:NAME?", sensor).trim());
            } catch (IOException e) {
                return String.format("%s (%s)", sensor, "Name Unknown");
            }

        }

        @Override
        public String getSensorName() {
            return getName();
        }

        @Override
        public double getTemperature() throws IOException, DeviceException {
            return queryDouble("INPUT? %s", sensor);
        }

        @Override
        public void setTemperatureRange(double range) throws IOException, DeviceException {
            // No ranging options available
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
            return CryoCon22C.this.getIDN();
        }

        @Override
        public Address getAddress() {
            return CryoCon22C.this.getAddress();
        }

        @Override
        public double getValue() throws IOException, DeviceException {
            return queryDouble("LOOP %d:OUTPWR?", number);
        }

        @Override
        public double getLimit() throws IOException {

            String response = query("LOOP %d:RANGE?", number);
            Double range    = null;

            switch (number) {

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
        public void setLimit(double range) throws IOException, DeviceException {

            String value;

            switch (number) {

                case 1:
                    if (range > 10.0) {value = "HIGH";} else if (range > 1.0) {value = "MID";} else {value = "LOW";}
                    break;

                case 2:
                    if (range > 10.0) {value = "HIGH";} else {value = "LOW";}
                    break;

                case 3:
                case 4:
                    if (range > 50.0) {value = "10V";} else {value = "5V";}
                    break;

                default:
                    throw new DeviceException("That range is not supported.");

            }

            query("LOOP %d:RANGE %s", number, value);

        }

        @Override
        public String getName() {
            return String.format("Heater %d", number);
        }

    }

    public class Loop extends ZonedLoop {

        private final Heater  heater;
        private       boolean ramping = false;

        private Loop(Heater heater) {
            this.heater = heater;
        }

        @Override
        public String getIDN() throws IOException, DeviceException {
            return CryoCon22C.this.getIDN();
        }

        @Override
        public Address getAddress() {
            return CryoCon22C.this.getAddress();
        }

        @Override
        public String getName() {
            return String.format("Loop %d", heater.getNumber());
        }

        @Override
        public void setRampEnabled(boolean flag) throws IOException, DeviceException {

            ramping = flag;

            if (isPIDEnabled()) {
                write("LOOP %d:TYPE RAMPP");
            }

        }

        @Override
        public boolean isRampEnabled() {
            return ramping;
        }

        @Override
        public void setRampRate(double limit) throws IOException {
            query("LOOP %d:RATE %f", heater.getNumber(), limit);
        }

        @Override
        public double getRampRate() throws IOException {
            return queryDouble("LOOP %d:RATE?", heater.getNumber());
        }

        @Override
        public double getPValue() throws IOException, DeviceException {
            return queryDouble("LOOP %d:PGAIN?", heater.getNumber());
        }

        @Override
        public double getIValue() throws IOException, DeviceException {
            return queryDouble("LOOP %d:IGAIN?", heater.getNumber());
        }

        @Override
        public double getDValue() throws IOException, DeviceException {
            return queryDouble("LOOP %d:DGAIN?", heater.getNumber());
        }

        @Override
        public void setPValue(double value) throws IOException, DeviceException {
            query("LOOP %d:PGAIN %e", heater.getNumber(), value);
        }

        @Override
        public void setIValue(double value) throws IOException, DeviceException {
            query("LOOP %d:IGAIN %e", heater.getNumber(), value);
        }

        @Override
        public void setDValue(double value) throws IOException, DeviceException {
            query("LOOP %d:DGAIN %e", heater.getNumber(), value);
        }

        @Override
        public Input getInput() throws IOException, DeviceException {

            String used = query("LOOP %d:SOURCE?", heater.getNumber());

            switch (used) {

                case "CHA":
                    return SENSOR_A;

                case "CHB":
                    return SENSOR_B;

                default:
                    throw new IOException("Invalid response from Cryo-Con 22C");

            }

        }

        @Override
        public Heater getOutput() throws IOException, DeviceException {
            return heater;
        }

        @Override
        public void setInput(Input input) throws IOException, DeviceException {

            if (input instanceof TMeter && (input == SENSOR_A || input == SENSOR_B)) {
                query("LOOP %d:SOURCE %s", heater.getNumber(), ((TMeter) input).getSensor());
            } else {
                throw new DeviceException("That input cannot be used for this TC/PID loop");
            }

        }

        @Override
        public void setOutput(Output output) throws DeviceException {

            if (output != heater) {
                throw new DeviceException("That output cannot be used for this TC/PID loop");
            }

        }

        @Override
        public List<Heater> getAvailableOutputs() {
            return List.of(heater);
        }

        @Override
        public List<TMeter> getAvailableInputs() {
            return List.of(SENSOR_A, SENSOR_B);
        }

        @Override
        public void setManualValue(double value) throws IOException, DeviceException {

            if (!Util.isBetween(value, 0, 100)) {
                throw new DeviceException("Heater power must be between 0 and 100, %s given", value);
            }

            query("LOOP %d:PMANUAL %e", heater.getNumber(), value);

        }

        @Override
        public double getManualValue() throws IOException {
            return queryDouble("LOOP %d:PMANUAL?", heater.getNumber());
        }

        @Override
        public void setPIDEnabled(boolean flag) throws IOException, DeviceException {

            if (isRampEnabled()) {
                query("LOOP %d:TYPE RAMPP", heater.getNumber());
                updatePID(getSetPoint());
            } else {
                query("LOOP %d:TYPE PID", heater.getNumber());
                updatePID(getSetPoint());
            }

        }

        @Override
        public boolean isPIDEnabled() throws IOException {
            return !query("LOOP %d:TYPE?", heater.getNumber()).trim().equalsIgnoreCase("MAN");
        }

        @Override
        public void setSetPoint(double temperature) throws IOException, DeviceException {
            query("LOOP %d:SETP %e", heater.getNumber(), temperature);
            updatePID(temperature);
        }

        @Override
        public double getSetPoint() throws IOException, DeviceException {
            return queryDouble("LOOP %d:SETP?", heater.getNumber());
        }

    }


}
