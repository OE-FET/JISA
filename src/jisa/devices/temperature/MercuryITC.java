package jisa.devices.temperature;

import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.PID;
import jisa.devices.interfaces.TC;
import jisa.visa.VISADevice;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Class MercuryITC
 * <p>
 * VISADevice class for controlling MercuryITC temperature controllers via TCP IP.
 * Written by Thomas Marsh 2021.
 */
public class MercuryITC extends VISADevice implements TC {

    private static double getScale(char character) {

        switch (character) {

            case 'n':
                return 1e-9;
            case 'u':
                return 1e-6;
            case 'm':
                return 1e-3;
            case 'k':
                return 1e+3;
            case 'M':
                return 1e+6;

        }

        return 1.0;

    }

    private final List<TMeter> inputs  = new LinkedList<>();
    private final List<Heater> outputs = new LinkedList<>();
    private final List<Loop>   loops   = new LinkedList<>();


    public MercuryITC(Address address) throws IOException {

        super(address);

        setWriteTerminator("\n");
        setReadTerminator("\n");
        addAutoRemove("\n", "\r");

        String[] responses = query("READ:SYS:CAT").split(":");

        for (int i = 3; i < responses.length - 3; i += 3) {

            String uid  = responses[i + 1].trim();
            String type = responses[i + 2].trim().toUpperCase();

            switch (type) {

                case "TEMP":
                    TMeter tm = new TMeter(uid);
                    Loop   lp = new Loop(tm);
                    inputs.add(tm);
                    loops.add(lp);
                    break;

                case "HTR":
                    outputs.add(new Heater(uid));
                    break;

            }
        }
    }

    public String[] readITC(String... parts) throws IOException {

        String[] response = query(String.join(":", parts)).split(":");
        String[] answer   = new String[response.length - parts.length];

        System.arraycopy(response, parts.length, answer, 0, answer.length);

        return answer;

    }

    public String[] writeITC(String... parts) throws IOException, DeviceException {

        String[] response = query(String.join(":", parts)).split(":");
        String[] answer   = new String[response.length - parts.length - 1];

        System.arraycopy(response, parts.length + 1, answer, 0, answer.length);

        if (answer[answer.length - 1].equals("INVALID")) {
            throw new DeviceException("Tried to set invalid parameter");
        }

        return answer;

    }

    @Override
    public List<TMeter> getInputs() {
        return inputs;
    }

    @Override
    public List<Heater> getOutputs() {
        return outputs;
    }

    @Override
    public List<Loop> getLoops() {
        return loops;
    }

    private class Component {

        private final String dev;
        private final String uid;

        private Component(String dev, String uid) {
            this.dev = dev;
            this.uid = uid;
        }

        public String get(String... parts) throws IOException, DeviceException {

            String[] full = new String[4 + parts.length];

            full[0] = "READ";
            full[1] = "DEV";
            full[2] = uid;
            full[3] = dev;

            System.arraycopy(parts, 0, full, 4, parts.length);

            return readITC(full)[0];

        }

        public String getDev(String dev, String... parts) throws IOException, DeviceException {

            String[] full = new String[4 + parts.length];

            full[0] = "READ";
            full[1] = "DEV";
            full[2] = uid;
            full[3] = dev;

            System.arraycopy(parts, 0, full, 4, parts.length);

            return readITC(full)[0];

        }

        public double getDouble(String... parts) throws IOException, DeviceException {

            String[] full = new String[4 + parts.length];

            full[0] = "READ";
            full[1] = "DEV";
            full[2] = uid;
            full[3] = dev;

            System.arraycopy(parts, 0, full, 4, parts.length);

            String[] response = readITC(full);

            double value = Double.parseDouble(response[0].substring(0, response[0].length() - 1));
            double scale = (parts[0].equals("SIG") && response.length > 1 && response[1].length() == 2) ? getScale(response[1].charAt(0)) : 1.0;

            return value * scale;

        }

        public double getDevDouble(String dev, String... parts) throws IOException, DeviceException {

            String[] full = new String[4 + parts.length];

            full[0] = "READ";
            full[1] = "DEV";
            full[2] = uid;
            full[3] = dev;

            System.arraycopy(parts, 0, full, 4, parts.length);

            String[] response = readITC(full);

            double value = Double.parseDouble(response[0].substring(0, response[0].length() - 1));
            double scale = (parts[0].equals("SIG") && response.length > 1 && response[1].length() == 2) ? getScale(response[1].charAt(0)) : 1.0;

            return value * scale;

        }

        public String[] set(String... parts) throws IOException, DeviceException {

            String[] full = new String[4 + parts.length];

            full[0] = "SET";
            full[1] = "DEV";
            full[2] = uid;
            full[3] = dev;

            System.arraycopy(parts, 0, full, 4, parts.length);

            return writeITC(full);

        }

        public String[] setDev(String dev, String... parts) throws IOException, DeviceException {

            String[] full = new String[4 + parts.length];

            full[0] = "SET";
            full[1] = "DEV";
            full[2] = uid;
            full[3] = dev;

            System.arraycopy(parts, 0, full, 4, parts.length);

            return writeITC(full);

        }

    }

    public class TMeter extends Component implements TC.TMeter {

        private final String uid;

        public TMeter(String uid) {
            super("TEMP", uid);
            this.uid = uid;
        }

        @Override
        public String getIDN() throws IOException, DeviceException {
            return MercuryITC.this.getIDN();
        }

        @Override
        public Address getAddress() {
            return MercuryITC.this.getAddress();
        }

        @Override
        public String getName() {

            try {
                return String.format("%s (%s)", uid, get("NICK"));
            } catch (Exception e) {
                return String.format("%s (%s)", uid, "Name Unknown");
            }

        }

        @Override
        public double getTemperature() throws IOException, DeviceException {
            return getDouble("SIG", "TEMP");
        }

        @Override
        public void setTemperatureRange(double range) throws IOException, DeviceException {
            // No ranging options
        }

        @Override
        public double getTemperatureRange() throws IOException, DeviceException {
            return 999.999;
        }

    }

    public class Heater extends Component implements TC.Heater {

        private final String uid;

        public Heater(String uid) {
            super("HTR", uid);
            this.uid = uid;
        }

        @Override
        public String getIDN() throws IOException, DeviceException {
            return MercuryITC.this.getIDN();
        }

        @Override
        public Address getAddress() {
            return MercuryITC.this.getAddress();
        }

        @Override
        public double getValue() throws IOException, DeviceException {

            double power = getDouble("SIG", "POWR");
            double max   = getDouble("PMAX");

            return 100.0 * (power / max);

        }

        @Override
        public double getLimit() throws IOException, DeviceException {
            return 100.0 * Math.pow(getDouble("VLIM") / 40.0, 2);

        }

        @Override
        public void setLimit(double range) throws IOException, DeviceException {

            String limit = String.format("%e", Math.sqrt(range / 100.0) * 40.0);
            set("VLIM", limit);

        }

        @Override
        public String getName() {

            try {
                return String.format("%s (%s)", uid, get("NICK"));
            } catch (Exception e) {
                return String.format("%s (%s)", uid, "Name Unknown");
            }

        }
    }

    public class AuxOutput extends Component implements PID.Output {

        private final String uid;

        public AuxOutput(String uid) {
            super("AUX", uid + ".A1");
            this.uid = uid;
        }

        @Override
        public String getIDN() throws IOException, DeviceException {
            return null;
        }

        @Override
        public Address getAddress() {
            return null;
        }

        @Override
        public double getValue() throws IOException, DeviceException {
            return 0;
        }

        @Override
        public double getLimit() throws IOException, DeviceException {
            return 0;
        }

        @Override
        public void setLimit(double range) throws IOException, DeviceException {

        }

        @Override
        public String getName() {
            return "Aux Output";
        }

        @Override
        public String getValueName() {
            return "Voltage";
        }

        @Override
        public String getUnits() {
            return "V";
        }

    }

    public class Loop extends ZonedLoop {

        private final TMeter sensor;

        public Loop(TMeter sensor) {
            this.sensor = sensor;
        }

        @Override
        public String getIDN() throws IOException, DeviceException {
            return MercuryITC.this.getIDN();
        }

        @Override
        public Address getAddress() {
            return MercuryITC.this.getAddress();
        }

        @Override
        public String getName() {
            return sensor.getName();
        }

        @Override
        public void setRampEnabled(boolean flag) throws IOException, DeviceException {
            sensor.setDev("LOOP", "RENA", flag ? "ON" : "OFF");
        }

        @Override
        public boolean isRampEnabled() throws IOException, DeviceException {
            return sensor.get("LOOP", "RENA").equals("ON");
        }

        @Override
        public void setRampRate(double limit) throws IOException, DeviceException {
            sensor.set("LOOP", "RSET", String.format("%e", limit));
        }

        @Override
        public double getRampRate() throws IOException, DeviceException {
            return sensor.getDouble("LOOP", "RSET");
        }

        @Override
        public double getPValue() throws IOException, DeviceException {
            return sensor.getDouble("LOOP", "P");
        }

        @Override
        public double getIValue() throws IOException, DeviceException {
            return sensor.getDouble("LOOP", "I");
        }

        @Override
        public double getDValue() throws IOException, DeviceException {
            return sensor.getDouble("LOOP", "D");
        }

        @Override
        public void setPValue(double value) throws IOException, DeviceException {
            sensor.set("LOOP", "P", String.format("%e", value));
        }

        @Override
        public void setIValue(double value) throws IOException, DeviceException {
            sensor.set("LOOP", "I", String.format("%e", value));
        }

        @Override
        public void setDValue(double value) throws IOException, DeviceException {
            sensor.set("LOOP", "D", String.format("%e", value));
        }

        @Override
        public TMeter getInput() throws IOException, DeviceException {
            return sensor;
        }

        @Override
        public Heater getOutput() throws IOException, DeviceException {
            String heater = sensor.get("LOOP", "HTR");
            return getAvailableOutputs().stream().filter(o -> o.uid.equals(heater)).findFirst().orElse(getAvailableOutputs().get(0));
        }

        @Override
        public void setInput(Input input) throws IOException, DeviceException {

            if (input != sensor) {
                throw new DeviceException("You cannot use that input for this loop");
            }

        }

        @Override
        public void setOutput(Output output) throws IOException, DeviceException {

            if (output instanceof Heater && getAvailableOutputs().contains(output)) {
                sensor.set("LOOP", "HTR", ((Heater) output).uid);
            } else {
                throw new DeviceException("You cannot use that output for this loop");
            }

        }

        @Override
        public List<Heater> getAvailableOutputs() {
            return (List<Heater>) getHeaters();
        }

        @Override
        public List<TMeter> getAvailableInputs() {
            return List.of(sensor);
        }

        @Override
        public void setManualValue(double value) throws IOException, DeviceException {
            sensor.set("LOOP", "HSET", String.format("%s", value));
        }

        @Override
        public double getManualValue() throws IOException, DeviceException {
            return sensor.getDouble("LOOP", "HSET");
        }

        @Override
        public void setPIDEnabled(boolean flag) throws IOException, DeviceException {
            sensor.set("LOOP", "ENAB", flag ? "ON" : "OFF");
        }

        @Override
        public boolean isPIDEnabled() throws IOException, DeviceException {
            return sensor.get("LOOP", "ENAB").equals("ON");
        }

        @Override
        public void setSetPoint(double temperature) throws IOException, DeviceException {
            sensor.set("LOOP", "TSET", String.format("%s", temperature));
        }

        @Override
        public double getSetPoint() throws IOException, DeviceException {
            return sensor.getDouble("LOOP", "TSET");
        }

    }

}
