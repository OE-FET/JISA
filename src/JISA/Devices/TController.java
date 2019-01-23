package JISA.Devices;

import JISA.Addresses.InstrumentAddress;
import JISA.Control.Returnable;
import JISA.Control.SetGettable;
import JISA.Control.Synch;
import JISA.Util;
import JISA.VISA.VISADevice;

import java.io.IOException;

/**
 * Abstract class to define the standard functionality of temperature controllers
 */
public abstract class TController extends VISADevice {

    private Zoner zoner = null;

    /**
     * Connects to the temperature controller at the given address, returning an instrument object to control it.
     *
     * @param address Address of instrument
     *
     * @throws IOException Upon communications error
     */
    public TController(InstrumentAddress address) throws IOException {
        super(address);
    }

    /**
     * Sets the target temperature of the temperature controller.
     *
     * @param temperature Target temperature, in Kelvin
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract void setTargetTemperature(double temperature) throws IOException, DeviceException;

    /**
     * Returns the temperature measured by the controller.
     *
     * @return Temperature, in Kelvin
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract double getTemperature() throws IOException, DeviceException;

    /**
     * Returns the target temperature set on the controller.
     *
     * @return Target temperature, in Kelvin
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract double getTargetTemperature() throws IOException, DeviceException;

    /**
     * Returns the heater output power percentage.
     *
     * @return Heater power, percentage of max
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract double getHeaterPower() throws IOException, DeviceException;

    /**
     * Returns the gas flow in whatever units the controller uses
     *
     * @return Gas flow (arbitrary units)
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract double getGasFlow() throws IOException, DeviceException;

    /**
     * Sets the heater to be operated automatically
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract void useAutoHeater() throws IOException, DeviceException;

    /**
     * Sets the heater to be operated manually with the specified power output percentage
     *
     * @param powerPCT Output power (percentage of max)
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract void setManualHeater(double powerPCT) throws IOException, DeviceException;

    /**
     * Returns whether the heater is currently operating automatically or manually
     *
     * @return Automatic?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract boolean isHeaterAuto() throws IOException, DeviceException;

    /**
     * Sets the gas flow to be controlled automatically
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract void useAutoFlow() throws IOException, DeviceException;

    /**
     * Sets the gas flow to be controlled manually with the specified output
     *
     * @param outputPCT Output
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract void setManualFlow(double outputPCT) throws IOException, DeviceException;

    /**
     * Returns whether the gas flow is currently controlled automatically or manually
     *
     * @return Automatic?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract boolean isFlowAuto() throws IOException, DeviceException;

    /**
     * Sets the proportional term co-efficient for PID control
     *
     * @param value Proportional co-efficient
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract void setPValue(double value) throws IOException, DeviceException;

    /**
     * Sets the integral term co-efficient for PID control
     *
     * @param value Integral co-efficient
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract void setIValue(double value) throws IOException, DeviceException;

    /**
     * Sets the derivative term co-efficient for PID control
     *
     * @param value Derivative co-efficient
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract void setDValue(double value) throws IOException, DeviceException;

    /**
     * Returns the proportional term co-efficient used for PID control
     *
     * @return Proportional co-efficient
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract double getPValue() throws IOException, DeviceException;

    /**
     * Returns the integral term co-efficient used for PID control
     *
     * @return Integral co-efficient
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract double getIValue() throws IOException, DeviceException;

    /**
     * Returns the derivative term co-efficient used for PID control
     *
     * @return Derivative co-efficient
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract double getDValue() throws IOException, DeviceException;

    /**
     * Sets the target temperature and waits for it to be stably reached (within 1% for at least 1 minute).
     *
     * @param temperature Target temperature, in Kelvin
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public void setTargetAndWait(double temperature) throws IOException, DeviceException {
        setTargetTemperature(temperature);
        waitForStableTemperature(temperature);
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
    public void waitForStableTemperature(double temperature, double pctMargin, long time) throws IOException, DeviceException {

        Synch.waitForStableTarget(
                this::getTemperature,
                temperature,
                pctMargin,
                100,
                time
        );

    }

    /**
     * Halts the current thread until the temperature has stabilised to the specified value within 1% for at least 1
     * minute.
     *
     * @param temperature Target temperature, in Kelvin
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public void waitForStableTemperature(double temperature) throws IOException, DeviceException {
        waitForStableTemperature(temperature, 1.0, 60000);
    }

    /**
     * Halts the current thread until the temperature has stabilised to the target value within 1% for at least 1
     * minute.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public void waitForStableTemperature() throws IOException, DeviceException {
        waitForStableTemperature(getTargetTemperature());
    }

    /**
     * Sets the zones to use (ie the look-up table) for auto PID control.
     *
     * @param zones Zones to use
     */
    public void setAutoPIDZones(PIDZone... zones) throws IOException, DeviceException {

        if (zoner != null && zoner.isRunning()) {
            zoner.stop();
            zoner = new Zoner(zones);
            zoner.start();
        } else {
            zoner = new Zoner(zones);
        }
    }

    /**
     * Returns an array of PIDZone object representing the zones used for auto PID control.
     *
     * @return Zones used
     */
    public PIDZone[] getAutoPIDZones() throws IOException, DeviceException {

        if (zoner == null) {
            return new PIDZone[0];
        } else {
            return zoner.getZones();
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
    public void useAutoPID(boolean auto) throws IOException, DeviceException {

        if (auto && zoner == null) {
            throw new DeviceException("You must set PID zones before using this feature.");
        }

        if (auto && !zoner.isRunning()) {
            zoner.start();
        } else if (zoner != null && zoner.isRunning()) {
            zoner.stop();
        }

    }

    /**
     * Returns whether auto PID control is currently active or not.
     *
     * @return Is PID control auto?
     */
    public boolean isPIDAuto() throws IOException, DeviceException {
        return zoner != null && zoner.isRunning();
    }

    protected class Zoner implements Runnable {

        private final PIDZone[] zones;
        private       PIDZone   currentZone;
        private       boolean   running = false;
        private       PIDZone   minZone;
        private       PIDZone   maxZone;
        private       Thread    thread;

        public Zoner(PIDZone[] zones) {

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

            while (running) {

                try {

                    double T = getTemperature();

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
                            } else {
                                currentZone = maxZone;
                            }

                        }

                        setPValue(currentZone.getP());
                        setIValue(currentZone.getI());
                        setDValue(currentZone.getD());

                    }

                } catch (Exception e) {
                    System.err.printf("Error in auto-PID control: \"%s\"\n", e.getMessage());
                }

                if (!running) {
                    break;
                }

                Util.sleep(1000);
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

    public static class PIDZone {

        private final double minT;
        private final double maxT;
        private final double P;
        private final double I;
        private final double D;

        public PIDZone(double minT, double maxT, double P, double I, double D) {
            this.minT = minT;
            this.maxT = maxT;
            this.P = P;
            this.I = I;
            this.D = D;
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

        public boolean matches(double temperature) {
            return (temperature >= minT && temperature <= maxT);
        }

    }

}
