package JISA.Devices;

import JISA.Control.Synch;
import JISA.Util;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Abstract class to define the standard functionality of temperature controllers
 */
public interface TC extends TMeter {

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
    double getGasFlow() throws IOException, DeviceException;

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
    void setManualHeater(double powerPCT) throws IOException, DeviceException;

    /**
     * Returns whether the heater is currently operating automatically or manually
     *
     * @return Automatic?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    boolean isHeaterAuto() throws IOException, DeviceException;

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
    void setManualFlow(double outputPCT) throws IOException, DeviceException;

    /**
     * Returns whether the gas flow is currently controlled automatically or manually
     *
     * @return Automatic?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    boolean isFlowAuto() throws IOException, DeviceException;

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
     * Sets the integral term co-efficient for PID control
     *
     * @param value Integral co-efficient
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void setIValue(double value) throws IOException, DeviceException;

    /**
     * Sets the derivative term co-efficient for PID control
     *
     * @param value Derivative co-efficient
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void setDValue(double value) throws IOException, DeviceException;

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
     * Returns the integral term co-efficient used for PID control
     *
     * @return Integral co-efficient
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    double getIValue() throws IOException, DeviceException;

    /**
     * Returns the derivative term co-efficient used for PID control
     *
     * @return Derivative co-efficient
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    double getDValue() throws IOException, DeviceException;

    void setHeaterRange(double rangePCT) throws IOException, DeviceException;

    double getHeaterRange() throws IOException, DeviceException;

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

    Zoner getZoner();

    void setZoner(Zoner zoner);

    /**
     * Sets the zones to use (ie the look-up table) for auto PID control.
     *
     * @param zones Zones to use
     */
    default void setAutoPIDZones(PIDZone... zones) throws IOException, DeviceException {

        Zoner zoner = getZoner();

        if (zoner != null && zoner.isRunning()) {
            zoner.stop();
            zoner = new Zoner(this, zones);
            zoner.start();
            setZoner(zoner);
        } else {
            setZoner(new Zoner(this, zones));
        }
    }

    /**
     * Returns an array of PIDZone object representing the zones used for auto PID control.
     *
     * @return Zones used
     */
    default PIDZone[] getAutoPIDZones() throws IOException, DeviceException {

        if (getZoner() == null) {
            return new PIDZone[0];
        } else {
            return getZoner().getZones();
        }

    }

    /**
     * Sets whether the PID values are being automatically controlled using the look-up table defined with setAutoPIDZones().
     *
     * @param auto Automatic?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    default void useAutoPID(boolean auto) throws IOException, DeviceException {

        Zoner zoner = getZoner();

        if (auto && zoner == null) {
            throw new DeviceException("You must set PID zones before using this feature.");
        }

        if (auto && !zoner.isRunning()) {
            zoner.start();
        } else if (zoner != null && zoner.isRunning() && !auto) {
            zoner.stop();
        }

    }

    /**
     * Returns whether auto PID control is currently active or not.
     *
     * @return Is PID control auto?
     */
    default boolean isUsingAutoPID() throws IOException, DeviceException {
        Zoner zoner = getZoner();
        return zoner != null && zoner.isRunning();
    }

    class Zoner implements Runnable {

        private final PIDZone[] zones;
        private       PIDZone   currentZone;
        private       boolean   running = false;
        private       PIDZone   minZone;
        private       PIDZone   maxZone;
        private       Thread    thread;
        private       TC        tc;

        public Zoner(TC tc, PIDZone[] zones) {

            this.tc = tc;
            this.zones = zones;
            currentZone = zones[0];
            minZone = zones[0];
            maxZone = zones[0];

            for (PIDZone zone : zones) {

                if (zone.getMinT() < minZone.getMinT()) {
                    minZone = zone;
                }

                if (zone.getMaxT() > maxZone.getMaxT()) {
                    maxZone = zone;
                }

            }

        }

        public PIDZone[] getZones() {
            return zones.clone();
        }

        @Override
        public void run() {

            try {
                applyZone(currentZone);
            } catch (Exception e) {
                Util.errLog.printf("Error in starting auto-PID control: \"%s\"\n", e.getMessage());
            }

            while (running) {

                try {

                    double T = tc.getTemperature();

                    if (!currentZone.matches(T)) {

                        boolean found = false;
                        for (PIDZone zone : zones) {
                            if (zone.matches(T)) {
                                currentZone = zone;
                                found = true;
                                break;
                            }
                        }

                        if (!found) {

                            if (T <= minZone.getMinT()) {
                                currentZone = minZone;
                            } else if (T >= maxZone.getMaxT()) {
                                currentZone = maxZone;
                            }

                        }

                        applyZone(currentZone);

                    }

                } catch (Exception e) {
                    Util.errLog.printf("Error in auto-PID control: \"%s\"\n", e.getMessage());
                }

                if (!running) {
                    break;
                }

                Util.sleep(1000);
            }

        }

        private void applyZone(PIDZone zone) throws IOException, DeviceException {

            if (zone.isAuto()) {
                tc.useAutoHeater();
                tc.setHeaterRange(zone.getRange());
                tc.setPValue(currentZone.getP());
                tc.setIValue(currentZone.getI());
                tc.setDValue(currentZone.getD());
            } else {
                tc.setHeaterRange(zone.getRange());
                tc.setManualHeater(currentZone.getPower());
            }

        }

        public void start() {
            running = true;
            thread = new Thread(this);
            thread.start();
        }

        public void stop() {
            running = false;
            thread.interrupt();
        }

        public boolean isRunning() {
            return running;
        }

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

            minT = data.getDouble("minT");
            maxT = data.getDouble("maxT");
            P = data.getDouble("P");
            I = data.getDouble("I");
            D = data.getDouble("D");
            range = data.getDouble("range");
            auto = data.getBoolean("auto");
            power = data.getDouble("power");

        }

        public PIDZone(double minT, double maxT, double P, double I, double D, double range) {
            this.minT = minT;
            this.maxT = maxT;
            this.P = P;
            this.I = I;
            this.D = D;
            this.range = range;
            auto = true;
            power = 0;
        }

        public PIDZone(double minT, double maxT, double heaterPower, double range) {
            this.minT = minT;
            this.maxT = maxT;
            P = 0;
            I = 0;
            D = 0;
            this.range = range;
            auto = false;
            power = heaterPower;
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
