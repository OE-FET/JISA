package jisa.devices;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

public interface PID extends Instrument {

    /**
     * Returns the set-point of the PID controller (i.e. the "target" value).
     *
     * @return Set-point value
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    double getSetPoint() throws IOException, DeviceException;

    /**
     * Sets the set-point value of the PID controller (i.e. the "target" value).
     *
     * @param value Set-point value
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void setSetPoint(double value) throws IOException, DeviceException;

    /**
     * Returns the input/process value of the PID loop.
     *
     * @return Input/Process Value
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    double getInputValue() throws IOException, DeviceException;

    /**
     * Returns the name of the input/process variable.
     *
     * @return Name of input variable
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    String getInputName() throws IOException, DeviceException;

    /**
     * Returns the units of the input/process variable.
     *
     * @return Units of input variable
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    String getInputUnits() throws IOException, DeviceException;

    /**
     * Returns the output value as a percentage of its max value.
     *
     * @return Percentage output value
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    double getOutputValue() throws IOException, DeviceException;

    /**
     * Manually sets the output value as a percentage of its max value. Effectively disables PID control until
     * re-enabled by use of {@link #useAutoOutput()}.
     *
     * @param value Percentage output value
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void setOutputValue(double value) throws IOException, DeviceException;

    /**
     * Returns the range being used for the output value, expressed as a percentage of the absolute maximum value.
     *
     * @return Percentage output range.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    double getOutputRange() throws IOException, DeviceException;

    /**
     * Sets the range to use for the output value, expressed as a percentage of the absolute maximum value.
     *
     * @param range Percentage output range.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void setOutputRange(double range) throws IOException, DeviceException;

    /**
     * Enables PID control of the output value.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void useAutoOutput() throws IOException, DeviceException;

    /**
     * Returns whether the output value is currently being controlled by the PID controller (true) or if it's manunally
     * set to output a constant value (false).
     *
     * @return PID Enabled?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    boolean isUsingAutoOutput() throws IOException, DeviceException;

    /**
     * Returns the proportional term co-efficient used for PID control
     *
     * @return Proportional co-efficient
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    double getPValue() throws IOException, DeviceException;

    /**
     * Sets the proportional term co-efficient for PID control
     *
     * @param value Proportional co-efficient
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void setPValue(double value) throws IOException, DeviceException;

    /**
     * Returns the integral term co-efficient used for PID control
     *
     * @return Integral co-efficient
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    double getIValue() throws IOException, DeviceException;

    /**
     * Sets the integral term co-efficient for PID control
     *
     * @param value Integral co-efficient
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void setIValue(double value) throws IOException, DeviceException;

    /**
     * Returns the derivative term co-efficient used for PID control
     *
     * @return Derivative co-efficient
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    double getDValue() throws IOException, DeviceException;

    /**
     * Sets the derivative term co-efficient for PID control
     *
     * @param value Derivative co-efficient
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void setDValue(double value) throws IOException, DeviceException;

    default void setPIDValues(double pValue, double iValue, double dValue) throws IOException, DeviceException {
        setPValue(pValue);
        setIValue(iValue);
        setDValue(dValue);
    }

    void useAutoPID(boolean flag) throws IOException, DeviceException;

    boolean isUsingAutoPID() throws IOException, DeviceException;

    List<Zone> getAutoPIDZones() throws IOException, DeviceException;

    void setAutoPIDZones(Zone... zones) throws IOException, DeviceException;

    default void updateAutoPID() throws IOException, DeviceException {

        if (isUsingAutoPID()) {

            double set = getSetPoint();

            for (PID.Zone zone : getAutoPIDZones()) {

                if (zone.matches(set)) {
                    usePIDZone(zone);
                    break;
                }

            }

        }

    }

    default void usePIDZone(PID.Zone zone) throws IOException, DeviceException {

        setPValue(zone.getP());
        setIValue(zone.getI());
        setDValue(zone.getD());
        setOutputRange(zone.getRange());

        if (zone.isAuto()) {
            useAutoOutput();
        } else {
            setOutputValue(zone.getPower());
        }

    }

    class Zone {

        private final double  min;
        private final double  max;
        private final double  P;
        private final double  I;
        private final double  D;
        private final double  range;
        private final boolean auto;
        private final double  power;

        public Zone(JSONObject data) {

            min   = data.getDouble("minT");
            max   = data.getDouble("maxT");
            P     = data.getDouble("P");
            I     = data.getDouble("I");
            D     = data.getDouble("D");
            range = data.getDouble("range");
            auto  = data.getBoolean("auto");
            power = data.getDouble("power");

        }

        public Zone(double min, double max, double P, double I, double D, double range) {
            this.min   = min;
            this.max   = max;
            this.P     = P;
            this.I     = I;
            this.D     = D;
            this.range = range;
            auto       = true;
            power      = 0;
        }

        public Zone(double min, double max, double heaterPower, double range) {
            this.min   = min;
            this.max   = max;
            P          = 0;
            I          = 0;
            D          = 0;
            this.range = range;
            auto       = false;
            power      = heaterPower;
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

        public double getRange() {
            return range;
        }

        public boolean isAuto() {
            return auto;
        }

        public double getPower() {
            return power;
        }

        public boolean matches(double temperature) {
            return (temperature >= min && temperature <= max);
        }

        public JSONObject toJSON() {

            JSONObject json = new JSONObject();

            json.put("minT", min);
            json.put("maxT", max);
            json.put("P", P);
            json.put("I", I);
            json.put("D", D);
            json.put("range", range);
            json.put("auto", auto);
            json.put("power", power);

            return json;

        }

    }
}
