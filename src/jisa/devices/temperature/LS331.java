package jisa.devices.temperature;

import jisa.Util;
import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.PID;
import jisa.devices.interfaces.TC;
import jisa.visa.VISADevice;
import jisa.visa.connections.Connection;
import jisa.visa.connections.GPIBConnection;
import jisa.visa.connections.SerialConnection;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class LS331 extends VISADevice implements TC {

    public static String getDescription() {
        return "LakeShore 331";
    }

    public  final  TMeter       INPUT_A  = new TMeter("A");
    public  final  TMeter       INPUT_B  = new TMeter("B");
    public  final  Heater       OUTPUT_1 = new Heater();
    public  final  Analogue     OUTPUT_2 = new Analogue();
    public  final  Loop         LOOP_1   = new Loop(OUTPUT_1);
    public  final  Loop         LOOP_2   = new Loop(OUTPUT_2);
    private final  List<TMeter> inputs   = List.of(INPUT_A, INPUT_B);
    private final  List<Output> outputs  = List.of(OUTPUT_1, OUTPUT_2);
    private final  List<Loop>   loops    = List.of(LOOP_1, LOOP_2);

    public LS331(Address address) throws IOException, DeviceException {

        super(address);

        Connection connection = getConnection();

        connection.setEncoding(StandardCharsets.US_ASCII);

        if (connection instanceof GPIBConnection) {
            ((GPIBConnection) connection).setEOIEnabled(false);
        }

        if (connection instanceof SerialConnection) {
            ((SerialConnection) connection).setSerialParameters(9600, 7, SerialConnection.Parity.ODD, SerialConnection.Stop.BITS_10);
        }

        setReadTerminator("\r\n");
        setWriteTerminator("\r\n");

        addAutoRemove("\r", "\n");

        String idn = getIDN();

        if (!idn.split(",")[1].contains("MODEL331")) {
            throw new DeviceException("The instrument at address \"%s\" is not a LakeShore 331.", address.getJISAString());
        }


    }

    @Override
    public List<? extends Input> getInputs() {
        return inputs;
    }

    @Override
    public List<? extends Output> getOutputs() {
        return outputs;
    }

    @Override
    public List<? extends Loop> getLoops() {
        return loops;
    }

    public class TMeter implements TC.TMeter {

        private final String label;

        public TMeter(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        @Override
        public String getIDN() throws IOException, DeviceException {
            return LS331.this.getIDN();
        }

        @Override
        public Address getAddress() {
            return LS331.this.getAddress();
        }

        @Override
        public String getName() {
            return String.format("Input %s", label);
        }

        @Override
        public double getTemperature() throws IOException, DeviceException {
            return queryDouble("KRDG? %s", label);
        }

        @Override
        public void setTemperatureRange(double range) throws IOException, DeviceException {
            // No range options
        }

        @Override
        public double getTemperatureRange() throws IOException, DeviceException {
            return 999.9;
        }
    }

    public class Heater implements TC.Heater {

        @Override
        public String getIDN() throws IOException, DeviceException {
            return LS331.this.getIDN();
        }

        @Override
        public Address getAddress() {
            return LS331.this.getAddress();
        }

        @Override
        public double getValue() throws IOException, DeviceException {
            return queryDouble("HTR?");
        }

        @Override
        public double getLimit() throws IOException, DeviceException {

            switch (queryInt("RANGE?")) {

                case 0:
                    return 0.0;

                case 1:
                    return 1.0;

                case 2:
                    return 10.0;

                case 3:
                    return 100.0;

                default:
                    throw new IOException("Invalid response from LS331");

            }

        }

        @Override
        public void setLimit(double range) throws IOException, DeviceException {

            if (!Util.isBetween(range, 0, 100)) {
                throw new DeviceException("Heater range limits must be specified as a percentage between 0 - 100.");
            }

            int setting;

            if (range > 10) {
                setting = 3;
            } else if (range > 1) {
                setting = 2;
            } else if (range > 0) {
                setting = 1;
            } else {
                setting = 0;
            }

            write("RANGE %d", setting);

        }

        @Override
        public String getName() {
            return "Heater Output";
        }
    }

    public class Analogue implements PID.Output {

        @Override
        public String getIDN() throws IOException, DeviceException {
            return LS331.this.getIDN();
        }

        @Override
        public Address getAddress() {
            return LS331.this.getAddress();
        }

        @Override
        public double getValue() throws IOException, DeviceException {
            return queryDouble("AOUT?");
        }

        @Override
        public double getLimit() throws IOException, DeviceException {
            return 100.0;
        }

        @Override
        public void setLimit(double range) throws IOException, DeviceException {

        }

        @Override
        public String getName() {
            return "Analogue Output";
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

        private final Output output;
        private final int    number;
        private       double manual = 0.0;

        private Loop(Output output) {

            this.output = output;

            if (output instanceof Heater) {
                number = 1;
            } else {
                number = 2;
            }

        }

        @Override
        public String getIDN() throws IOException, DeviceException {
            return LS331.this.getIDN();
        }

        @Override
        public Address getAddress() {
            return LS331.this.getAddress();
        }

        @Override
        public String getName() {

            if (output instanceof Heater) {
                return "Heater Loop";
            } else {
                return "Analogue Loop";
            }

        }

        @Override
        public void setRampEnabled(boolean flag) throws IOException, DeviceException {
            write("RAMP %d, %s, %f", number, flag ? "on" : "off", getRampRate());
        }

        @Override
        public boolean isRampEnabled() throws IOException, DeviceException {
            return query("RAMP? %d", number).split(",")[0].trim().equals("1");
        }

        @Override
        public void setRampRate(double limit) throws IOException, DeviceException {
            write("RAMP %d, %s, %f", number, isRampEnabled(), limit);
        }

        @Override
        public double getRampRate() throws IOException, DeviceException {
            return Double.parseDouble(query("RAMP? %d", number).split(",")[1].trim());
        }

        @Override
        public double getPValue() throws IOException, DeviceException {
            return Double.parseDouble(query("PID? %d", number).split(",")[0].trim());
        }

        @Override
        public double getIValue() throws IOException, DeviceException {
            return Double.parseDouble(query("PID? %d", number).split(",")[1].trim());
        }

        @Override
        public double getDValue() throws IOException, DeviceException {
            return Double.parseDouble(query("PID? %d", number).split(",")[2].trim());
        }

        @Override
        public void setPValue(double value) throws IOException, DeviceException {

            String[] values = query("PID? %d", number).split(",");
            values[0] = String.format("%f", value);

            write("PID %s", String.join(",", values));

        }

        @Override
        public void setIValue(double value) throws IOException, DeviceException {

            String[] values = query("PID? %d", number).split(",");
            values[1] = String.format("%f", value);

            write("PID %s", String.join(",", values));

        }

        @Override
        public void setDValue(double value) throws IOException, DeviceException {

            String[] values = query("PID? %d", number).split(",");
            values[2] = String.format("%f", value);

            write("PID %s", String.join(",", values));

        }


        @Override
        public TMeter getInput() throws IOException, DeviceException {

            switch (query("CSET? %d", number).split(",")[0].trim()) {

                case "A":
                    return INPUT_A;

                case "B":
                    return INPUT_B;

                default:
                    throw new IOException("Invalid response from LS331");

            }

        }

        @Override
        public Output getOutput() throws IOException, DeviceException {
            return output;
        }

        @Override
        public void setInput(Input input) throws IOException, DeviceException {

            if (input instanceof TMeter && (input == INPUT_A || input == INPUT_B)) {
                write("CSET %d,%s,1,0,2", number, ((TMeter) input).getLabel());
            } else {
                throw new DeviceException("That input cannot be used with this loop.");
            }

        }

        @Override
        public void setOutput(Output output) throws IOException, DeviceException {

            if (output != this.output) {
                throw new DeviceException("That output cannot be used with this loop.");
            }

        }

        @Override
        public List<? extends Output> getAvailableOutputs() {
            return List.of(output);
        }

        @Override
        public List<? extends Input> getAvailableInputs() {
            return inputs;
        }

        @Override
        public void setManualValue(double value) throws IOException, DeviceException {

            if (!Util.isBetween(value, 0, 100)) {
                throw new DeviceException("Output values must be specified in percent between 0 - 100.");
            }

            manual = value;

            if (!isPIDEnabled()) {
                write("MOUT %d,%f", number, manual);
            }

        }

        @Override
        public double getManualValue() throws IOException, DeviceException {
            return manual;
        }

        @Override
        public void setPIDEnabled(boolean flag) throws IOException, DeviceException {
            write("MOUT %d,%f", number, flag ? manual : 0.0);
        }

        @Override
        public boolean isPIDEnabled() throws IOException, DeviceException {
            return queryDouble("MOUT? %d", number) > 0;
        }

        @Override
        public void setSetPoint(double temperature) throws IOException, DeviceException {
            write("SETP %d,%f", number, temperature);
        }

        @Override
        public double getSetPoint() throws IOException, DeviceException {
            return queryDouble("SETP? %d", number);
        }
    }

}
