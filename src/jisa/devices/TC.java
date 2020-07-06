package jisa.devices;

import jisa.control.Synch;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Abstract class to define the standard functionality of temperature controllers
 */
public interface TC extends TMeter {

    /**
     * Returns the temperature measured by the controller.
     *
     * @return Temperature, in Kelvin
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    double getTemperature() throws IOException, DeviceException;

    /**
     * Returns the target temperature set on the controller.
     *
     * @return Target temperature, in Kelvin
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    double getTargetTemperature() throws IOException, DeviceException;

    /**
     * Sets the target temperature of the temperature controller.
     *
     * @param temperature Target temperature, in Kelvin
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void setTargetTemperature(double temperature) throws IOException, DeviceException;

    /**
     * Returns the heater output power percentage.
     *
     * @return Heater power, percentage of max
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    double getHeaterPower() throws IOException, DeviceException;

    /**
     * Returns the gas flow in whatever units the controller uses
     *
     * @return Gas flow (arbitrary units)
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    double getFlow() throws IOException, DeviceException;

    /**
     * Sets the heater to be operated automatically
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void useAutoHeater() throws IOException, DeviceException;

    /**
     * Sets the heater to be operated manually with the specified power output percentage
     *
     * @param powerPCT Output power (percentage of max)
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void setHeaterPower(double powerPCT) throws IOException, DeviceException;

    /**
     * Returns whether the heater is currently operating automatically or manually
     *
     * @return Automatic?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    boolean isUsingAutoHeater() throws IOException, DeviceException;

    /**
     * Sets the gas flow to be controlled automatically
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void useAutoFlow() throws IOException, DeviceException;

    /**
     * Sets the gas flow to be controlled manually with the specified output
     *
     * @param outputPCT Output
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void setFlow(double outputPCT) throws IOException, DeviceException;

    /**
     * Returns whether the gas flow is currently controlled automatically or manually
     *
     * @return Automatic?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    boolean isUsingAutoFlow() throws IOException, DeviceException;

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
     * Sets whether auto PID-zoning should be used.
     *
     * @param flag Should it be used?
     */
    void useAutoPID(boolean flag) throws IOException, DeviceException;

    /**
     * Returns whether auto PID-zoning is being used currently.
     *
     * @return Is it being used?
     */
    boolean isUsingAutoPID() throws IOException, DeviceException;

    /**
     * Returns a list of all PID zones currently set.
     *
     * @return List of zones
     */
    List<PIDZone> getAutoPIDZones() throws IOException, DeviceException;

    /**
     * Sets the PID zones to use for auto-zoning.
     *
     * @param zones Zones to use
     */
    void setAutoPIDZones(PIDZone... zones) throws IOException, DeviceException;

    /**
     * Sets the PID zones to use for auto-zoning.
     *
     * @param zones Zones to use
     */
    default void setAutoPIDZones(Collection<PIDZone> zones) throws IOException, DeviceException {
        setAutoPIDZones(zones.toArray(new PIDZone[0]));
    }

    default void updateAutoPID() throws IOException, DeviceException {

        if (isUsingAutoPID()) {

            double temp = getTargetTemperature();

            for (PIDZone zone : getAutoPIDZones()) {

                if (zone.matches(temp)) {
                    usePIDZone(zone);
                    break;
                }

            }

        }

    }

    default void usePIDZone(PIDZone zone) throws IOException, DeviceException {

        setPValue(zone.getP());
        setIValue(zone.getI());
        setDValue(zone.getD());
        setHeaterRange(zone.getRange());

        if (zone.isAuto()) {
            useAutoHeater();
        } else {
            setHeaterPower(zone.getPower());
        }

    }

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

    double getHeaterRange() throws IOException, DeviceException;

    void setHeaterRange(double rangePCT) throws IOException, DeviceException;

    default TMeter asThermometer() {
        return this;
    }

    /**
     * Halts the current thread until the temperature has stabilised to the specified value within the given percentage
     * margin for at least the given amount of time.
     *
     * @param temperature Target temperature, in Kelvin
     * @param pctMargin   Percentage margin
     * @param time        Amount of time to be considered stable, in milliseconds
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    default void waitForStableTemperature(double temperature, double pctMargin, long time) throws IOException, DeviceException, InterruptedException {

        Synch.waitForStableTarget(
                this::getTemperature,
                temperature,
                pctMargin,
                100,
                time
        );

    }

    class PIDZone {

        private final double  minT;
        private final double  maxT;
        private final double  P;
        private final double  I;
        private final double  D;
        private final double  range;
        private final boolean auto;
        private final double  power;

        public PIDZone(JSONObject data) {

            minT  = data.getDouble("minT");
            maxT  = data.getDouble("maxT");
            P     = data.getDouble("P");
            I     = data.getDouble("I");
            D     = data.getDouble("D");
            range = data.getDouble("range");
            auto  = data.getBoolean("auto");
            power = data.getDouble("power");

        }

        public PIDZone(double minT, double maxT, double P, double I, double D, double range) {
            this.minT  = minT;
            this.maxT  = maxT;
            this.P     = P;
            this.I     = I;
            this.D     = D;
            this.range = range;
            auto       = true;
            power      = 0;
        }

        public PIDZone(double minT, double maxT, double heaterPower, double range) {
            this.minT  = minT;
            this.maxT  = maxT;
            P          = 0;
            I          = 0;
            D          = 0;
            this.range = range;
            auto       = false;
            power      = heaterPower;
        }

        public double getMinT() {
            return minT;
        }

        public double getMaxT() {
            return maxT;
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
            return (temperature >= minT && temperature <= maxT);
        }

        public JSONObject toJSON() {

            JSONObject json = new JSONObject();

            json.put("minT", minT);
            json.put("maxT", maxT);
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
