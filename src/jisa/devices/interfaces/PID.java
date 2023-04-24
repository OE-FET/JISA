package jisa.devices.interfaces;

import jisa.control.Synch;
import jisa.devices.DeviceException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public interface PID extends Instrument, MultiInstrument {

    /**
     * Represents an input channel for a PID controller
     */
    interface Input extends Instrument {

        /**
         * Returns the value currently being sensed by this input.
         *
         * @return Sensed value
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon compatibility error
         */
        double getValue() throws IOException, DeviceException;

        /**
         * Returns the measurement range currently being used by this input.
         *
         * @return Measurement range
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon compatibility error
         */
        double getRange() throws IOException, DeviceException;

        /**
         * Sets the measurement range to be used by this input. Specify the largest (absolute) value you wish to measure,
         * and it will select the smallest range that contains it.
         *
         * @param range Maximum (absolute) value to be measured
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon compatibility error
         */
        void setRange(double range) throws IOException, DeviceException;

        /**
         * Returns the name of this input.
         *
         * @return Input name
         */
        String getName();

        default String getSensorName() {
            return getName();
        }

        /**
         * Returns the name of the quantity this input is measuring.
         *
         * @return Input quantity name
         */
        String getValueName();

        /**
         * Returns the units (if any) of the quantity this input is measuring.
         *
         * @return Input quantity units
         */
        String getUnits();

        default void close() throws IOException, DeviceException {}

    }

    /**
     * Represents an output channel for a PID controller
     */
    interface Output extends Instrument {

        /**
         * Returns the value this output is currently outputting.
         *
         * @return Value being output
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon compatibility error
         */
        double getValue() throws IOException, DeviceException;

        /**
         * Returns the limit/range currently being applied to the output value.
         *
         * @return Output limit
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon compatibility error
         */
        double getLimit() throws IOException, DeviceException;

        /**
         * Sets the limit/range to apply to the output value.
         *
         * @param range Output limit/range
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon compatibility error
         */
        void setLimit(double range) throws IOException, DeviceException;

        /**
         * Returns the name of the output.
         *
         * @return Output name
         */
        String getName();

        /**
         * Returns the name of the output quantity.
         *
         * @return Output quantity name
         */
        String getValueName();

        /**
         * Returns the units (if any) of the output quantity.
         *
         * @return Output quantity units
         */
        String getUnits();

        default void close() throws IOException, DeviceException {}

    }

    /**
     * Represents a single (independent) PID loop within a PID controller
     */
    interface Loop extends Instrument {

        /**
         * Returns the name of this loop.
         *
         * @return Name of loop
         */
        String getName();

        /**
         * Sets the value of the set-point for this loop.
         *
         * @param value Set-point value to use
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon compatibility error
         */
        void setSetPoint(double value) throws IOException, DeviceException;

        /**
         * Returns the current configured set-point value for this loop.
         *
         * @return Currently configured set-point value
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon compatibility error
         */
        double getSetPoint() throws IOException, DeviceException;

        /**
         * Sets whether this loop should ramp to its set-point using the configured ramp rate.
         *
         * @param flag Should ramping be enabled?
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon compatibility error
         */
        void setRampEnabled(boolean flag) throws IOException, DeviceException;

        /**
         * Returns whether the loop is currently using the configured ramp rate to ramp to its set-point or not.
         *
         * @return Is ramping enabled?
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon compatibility error
         */
        boolean isRampEnabled() throws IOException, DeviceException;

        /**
         * Sets the maximum rate at which this loop should ramp to its set-point should ramping be enabled by use of
         * setRampEnabled(...).
         *
         * @param limit Maximum ramping rate
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon compatibility error
         */
        void setRampRate(double limit) throws IOException, DeviceException;

        /**
         * Returns the currently configured ramping rate for this loop.
         *
         * @return Currently configured ramping rate
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon compatibility error
         */
        double getRampRate() throws IOException, DeviceException;

        /**
         * Returns the currently configured P single-value for the PID control of this loop. This is overridden by any
         * PID zoning, if enabled.
         *
         * @return Currently configured P value
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon compatibility error
         */
        double getPValue() throws IOException, DeviceException;

        /**
         * Returns the currently configured I single-value for the PID control of this loop. This is overridden by any
         * PID zoning, if enabled.
         *
         * @return Currently configured I value
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon compatibility error
         */
        double getIValue() throws IOException, DeviceException;

        /**
         * Returns the currently configured D single-value for the PID control of this loop. This is overridden by any
         * PID zoning, if enabled.
         *
         * @return Currently configured D value
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon compatibility error
         */
        double getDValue() throws IOException, DeviceException;

        /**
         * Sets the (single) P value to use for the PID control of this loop. This is only used if PID
         * zoning is not enabled, otherwise it is overridden.
         *
         * @param value P (single) value to use.
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon compatibility error
         */
        void setPValue(double value) throws IOException, DeviceException;

        /**
         * Sets the (single) I value to use for the PID control of this loop. This is only used if PID
         * zoning is not enabled, otherwise it is overridden.
         *
         * @param value I (single) value to use.
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon compatibility error
         */
        void setIValue(double value) throws IOException, DeviceException;

        /**
         * Sets the (single) D value to use for the PID control of this loop. This is only used if PID
         * zoning is not enabled, otherwise it is overridden.
         *
         * @param value D (single) value to use.
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon compatibility error
         */
        void setDValue(double value) throws IOException, DeviceException;

        /**
         * Sets the (single) P, I, and D values for the PID control of this loop. These are only used if
         * PID zoning is not enabled, otherwise they are overridden.
         *
         * @param p P value to use
         * @param i I value to use
         * @param d D value to use
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon compatibility error
         */
        default void setPIDValues(double p, double i, double d) throws IOException, DeviceException {
            setPValue(p);
            setIValue(i);
            setDValue(d);
        }

        /**
         * Sets the zones to use if PID zoning were to be enabled.
         *
         * @param zones List of PID.Zone objects representing the PID zone table to use
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon compatibility error
         */
        void setPIDZones(List<Zone> zones) throws IOException, DeviceException;

        /**
         * Sets the zones to use if PID zoning were to be enabled.
         *
         * @param zones PID.Zone objects representing the PID zone table to use
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon compatibility error
         */
        default void setPIDZones(Zone... zones) throws IOException, DeviceException {
            setPIDZones(List.of(zones));
        }

        /**
         * Returns a list of the currently configured PID zones to use for when PID zoning is enabled.
         *
         * @return List of configured PID zones
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon compatibility error
         */
        List<Zone> getPIDZones() throws IOException, DeviceException;

        /**
         * Sets whether PID zoning should be enabled for this loop or not.
         *
         * @param flag Should it be enabled?
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon compatibility error
         */
        void setPIDZoningEnabled(boolean flag) throws IOException, DeviceException;

        /**
         * Returns whether PID zoning is currently enabled for this loop or not.
         *
         * @return Is it enabled?
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon compatibility error
         */
        boolean isPIDZoningEnabled() throws IOException, DeviceException;

        /**
         * Returns the Input object representing which input value this loop is configured to monitor.
         *
         * @return Input object
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon compatibility error
         */
        Input getInput() throws IOException, DeviceException;

        /**
         * Returns the Output object representing which output channel this loop is configured to control.
         *
         * @return Output object
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon compatibility error
         */
        Output getOutput() throws IOException, DeviceException;

        /**
         * Sets which input value this loop should be monitoring. Make sure to only pass this method an Input object
         * that this loop is allowed to use. Check by using getAvailableInputs().
         *
         * @param input Input object
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon compatibility error
         */
        void setInput(Input input) throws IOException, DeviceException;

        /**
         * Sets which output channel thsi loop should be controlling. Make sure to only pass this method an Output
         * object that this loop is allowed to use. Check by using getAvailableOutputs().
         *
         * @param output Output object
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon compatibility error
         */
        void setOutput(Output output) throws IOException, DeviceException;

        /**
         * Returns a list of all outputs that this loop is allowed to control.
         *
         * @return List of usable outputs
         */
        List<? extends Output> getAvailableOutputs();

        /**
         * Returns a list of all inputs that this loop is allowed to monitor.
         *
         * @return List of usable inputs
         */
        List<? extends Input> getAvailableInputs();

        /**
         * Sets the value this loop should output through its output channel should PID control be disabled.
         *
         * @param value Manual value to use
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon compatibility error
         */
        void setManualValue(double value) throws IOException, DeviceException;

        /**
         * Returns the value this loop is configured to use should PID control be disabled.
         *
         * @return Manual value configured to use
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon compatibility error
         */
        double getManualValue() throws IOException, DeviceException;

        /**
         * Sets whether PID control is enabled or disabled for this loop.
         *
         * @param flag Should PID control be enabled?
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon compatibility error
         */
        void setPIDEnabled(boolean flag) throws IOException, DeviceException;

        /**
         * Returns whether PID control is currently enabled or disabled for this loop.
         *
         * @return Is PID control currently enabled?
         *
         * @throws IOException     Upon communications error
         * @throws DeviceException Upon compatibility error
         */
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

    /**
     * Partial implementation of PID.Loop with pre-defined, software-based PID zoning
     */
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

    /**
     * Returns a list of all input channels connected to this PID controller.
     *
     * @return List of input channels
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    List<? extends Input> getInputs();

    /**
     * Returns the input channel with a given index connected to this PID controller.
     *
     * @param index Input index
     *
     * @return Input channel with specified index
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    default Input getInput(int index) {
        return getInputs().get(index);
    }

    /**
     * Returns a list of all output channels connected to this PID controller.
     *
     * @return List of output channels
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    List<? extends Output> getOutputs();

    /**
     * Returns the output channel with a given index connected to this PID controller.
     *
     * @param index Output index
     *
     * @return Output channel with specified index
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    default Output getOutput(int index) {
        return getOutputs().get(index);
    }

    /**
     * Returns a list of all control loops provided by this PID controller.
     *
     * @return List of control loops
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    List<? extends Loop> getLoops();

    /**
     * Returns the control loop with a given index, provided by this PID controller.
     *
     * @param index Control loop index
     *
     * @return PID control loop with specified index
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    default Loop getLoop(int index) {
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

        /**
         * Creates a PID Zone where PID control is enabled with specified PID values.
         *
         * @param min   Min set-point value
         * @param max   Max set-point value
         * @param P     P value
         * @param I     I Value
         * @param D     D value
         * @param range Output range
         */
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

        /**
         * Creates a PID Zone where PID control is disabled, with a manual output value used instead.
         *
         * @param min    Min set-point value
         * @param max    Max set-point value
         * @param output Manual output value
         * @param range  Output range
         */
        public Zone(double min, double max, double output, double range) {
            this.min    = min;
            this.max    = max;
            P           = 0;
            I           = 0;
            D           = 0;
            this.limit  = range;
            auto        = false;
            this.output = output;
        }

        /**
         * Returns the minimum set-point value that this zone should be used for.
         *
         * @return Min set-point value
         */
        public double getMin() {
            return min;
        }

        /**
         * Returns the maximum set-point value that this zone should be used for.
         *
         * @return Max set-point value
         */
        public double getMax() {
            return max;
        }

        /**
         * Returns the P value this zone is configured to use.
         *
         * @return P value
         */
        public double getP() {
            return P;
        }

        /**
         * Returns the I value this zone is configured to use.
         *
         * @return I value
         */
        public double getI() {
            return I;
        }

        /**
         * Returns the D value this zone is configured to use.
         *
         * @return D value
         */
        public double getD() {
            return D;
        }

        /**
         * Returns the limit on the output to use for this zone.
         *
         * @return Output limit/range
         */
        public double getLimit() {
            return limit;
        }

        /**
         * Returns whether PID control is to be use for this zone or not.
         *
         * @return Should PID control be enabled?
         */
        public boolean isAuto() {
            return auto;
        }

        /**
         * Returns the manual output value to be used for this zone, should PID control be disabled.
         *
         * @return Manual output value
         */
        public double getOutput() {
            return output;
        }

        /**
         * Returns whether this zone should be used for a given set-point value.
         *
         * @param value Set-point value
         *
         * @return Should it be used?
         */
        public boolean matches(double value) {
            return (value >= min && value <= max);
        }

        /**
         * Converts this object into JSON representation.
         *
         * @return JSON representation of this object
         */
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
