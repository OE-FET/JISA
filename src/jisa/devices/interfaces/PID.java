package jisa.devices.interfaces;

import jisa.control.Synch;
import jisa.devices.DeviceException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public interface PID extends Instrument {

    interface Input extends Instrument {

        double getValue() throws IOException, DeviceException;

        double getRange() throws IOException, DeviceException;

        void setRange(double range) throws IOException, DeviceException;

        String getName();

        default String getSensorName() {
            return getName();
        }

        String getValueName();

        String getUnits();

        default void close() throws IOException, DeviceException {}

    }

    interface Output extends Instrument {

        double getValue() throws IOException, DeviceException;

        double getLimit() throws IOException, DeviceException;

        void setLimit(double range) throws IOException, DeviceException;

        String getName();

        String getValueName();

        String getUnits();

        default void close() throws IOException, DeviceException {}

    }

    interface Loop extends Instrument {

        String getName() throws IOException, DeviceException;

        void setSetPoint(double value) throws IOException, DeviceException;

        double getSetPoint() throws IOException, DeviceException;

        void setRampEnabled(boolean flag) throws IOException, DeviceException;

        boolean isRampEnabled() throws IOException, DeviceException;

        void setRampRate(double limit) throws IOException, DeviceException;

        double getRampRate() throws IOException, DeviceException;

        double getPValue() throws IOException, DeviceException;

        double getIValue() throws IOException, DeviceException;

        double getDValue() throws IOException, DeviceException;

        void setPValue(double value) throws IOException, DeviceException;

        void setIValue(double value) throws IOException, DeviceException;

        void setDValue(double value) throws IOException, DeviceException;

        default void setPIDValues(double p, double i, double d) throws IOException, DeviceException {
            setPValue(p);
            setIValue(i);
            setDValue(d);
        }

        void setPIDZones(List<Zone> zones) throws IOException, DeviceException;

        default void setPIDZones(Zone... zones) throws IOException, DeviceException {
            setPIDZones(List.of(zones));
        }

        List<Zone> getPIDZones() throws IOException, DeviceException;

        void setPIDZoningEnabled(boolean flag) throws IOException, DeviceException;

        boolean isPIDZoningEnabled() throws IOException, DeviceException;

        Input getInput() throws IOException, DeviceException;

        Output getOutput() throws IOException, DeviceException;

        void setInput(Input input) throws IOException, DeviceException;

        void setOutput(Output output) throws IOException, DeviceException;

        List<? extends Output> getAvailableOutputs();

        List<? extends Input> getAvailableInputs();

        void setManualValue(double value) throws IOException, DeviceException;

        double getManualValue() throws IOException, DeviceException;

        void setPIDEnabled(boolean flag) throws IOException, DeviceException;

        boolean isPIDEnabled() throws IOException, DeviceException;

        default void close() throws IOException, DeviceException {}

        @Override
        default List<Parameter<?>> getConfigurationParameters(Class<?> target) {

            List<Parameter<?>> parameters = Instrument.super.getConfigurationParameters(target);

            if (Loop.class.isAssignableFrom(target)) {

                Input  input;
                Output output;

                try {input = getInput();} catch (Exception e) {input = getAvailableInputs().get(0);}
                try {output = getOutput();} catch (Exception e) {output = getAvailableOutputs().get(0);}

                parameters.add(new Parameter<>("Input", input.getName(), v -> getAvailableInputs().stream().filter(i -> i.getName().equals(v)).findFirst().orElse(null), getAvailableInputs().stream().map(Input::getName).toArray(String[]::new)));
                parameters.add(new Parameter<>("Output", output.getName(), v -> getAvailableOutputs().stream().filter(i -> i.getName().equals(v)).findFirst().orElse(null), getAvailableOutputs().stream().map(Output::getName).toArray(String[]::new)));

                parameters.add(new Parameter<>(

                    "PID Settings",

                    new TableQuantity(new String[]{"Min", "Max", "P", "I", "D", "Output Limit"}, List.of(
                        List.of(0.0, 1000.0, 70.0, 30.0, 0.0, 100.0)
                    )),

                    q -> {

                        List<List<Double>> values = q.getValue();

                        if (values.size() == 0) {
                            values.add(List.of(0.0, 1000.0, 70.0, 30.0, 0.0, 100.0));
                        }

                        if (values.size() < 2) {

                            setPIDZoningEnabled(false);
                            setPIDValues(values.get(0).get(2), values.get(0).get(3), values.get(0).get(4));
                            getOutput().setLimit(values.get(0).get(5));

                        } else {

                            PID.Zone[] zones = values
                                .stream().map(r -> new PID.Zone(r.get(0), r.get(1), r.get(2), r.get(3), r.get(4), r.get(5)))
                                .toArray(PID.Zone[]::new);

                            setPIDZones(zones);
                            setPIDZoningEnabled(true);

                        }

                    }

                ));

                parameters.add(new Parameter<>("Ramp Rate [per min]", new OptionalQuantity<>(false, 1.0), r -> {
                    setRampRate(r.getValue());
                    setRampEnabled(r.isUsed());
                }));

            }

            return parameters;

        }

        default void waitForStableValue(double target, double pct, long msec) {

            try {
                Synch.waitForStableTarget(
                    getInput()::getValue,
                    target,
                    pct,
                    500,
                    msec
                );
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    abstract class ZonedLoop implements Loop {

        private final List<Zone> zones  = new LinkedList<>();
        private       boolean    zoning = false;

        public void setPIDZones(List<Zone> zones) throws IOException, DeviceException {
            this.zones.clear();
            this.zones.addAll(zones);
            updatePID(getSetPoint());
        }

        public List<Zone> getPIDZones() {
            return List.copyOf(zones);
        }

        public void setPIDZoningEnabled(boolean flag) throws DeviceException, IOException {

            if (flag && zones.isEmpty()) {
                throw new DeviceException("No PID zones set");
            }

            zoning = flag;

            updatePID(getSetPoint());

        }

        public boolean isPIDZoningEnabled() {
            return zoning;
        }

        protected void updatePID(double setPoint) throws IOException, DeviceException {

            if (isPIDZoningEnabled() && isPIDEnabled()) {

                Zone zone = zones.stream().filter(z -> z.matches(setPoint)).findFirst().orElse(null);

                if (zone != null) {

                    Output output = getOutput();

                    setPIDValues(zone.getP(), zone.getI(), zone.getD());
                    output.setLimit(zone.getLimit());

                    if (zone.isAuto()) {
                        setPIDEnabled(true);
                    } else {
                        setManualValue(zone.getOutput());
                        setPIDEnabled(false);
                    }

                }

            }

        }

    }

    List<? extends Input> getInputs() throws IOException, DeviceException;

    default Input getInput(int index) throws IOException, DeviceException {
        return getInputs().get(index);
    }

    List<? extends Output> getOutputs() throws IOException, DeviceException;

    default Output getOutput(int index) throws IOException, DeviceException {
        return getOutputs().get(index);
    }

    List<? extends Loop> getLoops() throws IOException, DeviceException;

    default Loop getLoop(int index) throws IOException, DeviceException {
        return getLoops().get(index);
    }

    class Zone {

        private final double  min;
        private final double  max;
        private final double  P;
        private final double  I;
        private final double  D;
        private final double  limit;
        private final boolean auto;
        private final double  output;

        public Zone(JSONObject data) {

            min    = data.getDouble("minT");
            max    = data.getDouble("maxT");
            P      = data.getDouble("P");
            I      = data.getDouble("I");
            D      = data.getDouble("D");
            limit  = data.getDouble("limit");
            auto   = data.getBoolean("auto");
            output = data.getDouble("output");

        }

        public Zone(double min, double max, double P, double I, double D, double range) {
            this.min   = min;
            this.max   = max;
            this.P     = P;
            this.I     = I;
            this.D     = D;
            this.limit = range;
            auto       = true;
            output     = 0;
        }

        public Zone(double min, double max, double heaterPower, double range) {
            this.min   = min;
            this.max   = max;
            P          = 0;
            I          = 0;
            D          = 0;
            this.limit = range;
            auto       = false;
            output     = heaterPower;
        }

        public double getMin() {
            return min;
        }

        public double getMax() {
            return max;
        }

        public double getP() {
            return P;
        }

        public double getI() {
            return I;
        }

        public double getD() {
            return D;
        }

        public double getLimit() {
            return limit;
        }

        public boolean isAuto() {
            return auto;
        }

        public double getOutput() {
            return output;
        }

        public boolean matches(double value) {
            return (value >= min && value <= max);
        }

        public JSONObject toJSON() {

            JSONObject json = new JSONObject();

            json.put("minT", min);
            json.put("maxT", max);
            json.put("P", P);
            json.put("I", I);
            json.put("D", D);
            json.put("range", limit);
            json.put("auto", auto);
            json.put("power", output);

            return json;

        }

    }
}
