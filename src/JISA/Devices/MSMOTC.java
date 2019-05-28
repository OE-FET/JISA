package JISA.Devices;

import JISA.Addresses.Address;
import JISA.Util;

import java.io.IOException;

public interface MSMOTC extends MSTC {

    /**
     * Returns the number of outputs the controller has
     *
     * @return Number of outputs
     */
    int getNumOutputs();

    /**
     * Configures the controller to use the given sensor for the given output/control-loop.
     *
     * @param output Output number to configure
     * @param sensor Sensor number to set
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void useSensor(int output, int sensor) throws IOException, DeviceException;

    /**
     * Configures the controller to use the given sensor for all outputs/control-loops.
     *
     * @param sensor Sensor number to set
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    default void useSensor(int sensor) throws IOException, DeviceException {
        for (int onum = 0; onum < getNumOutputs(); onum++) {
            useSensor(onum, sensor);
        }
    }

    /**
     * Returns which sensor the specified output/control-loop is configured to use.
     *
     * @param output Output number
     *
     * @return Sensor number
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    int getUsedSensor(int output) throws IOException, DeviceException;

    /**
     * Returns which sensor the default output/control-loop is configured to use.
     *
     * @return Sensor number
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    default int getUsedSensor() throws IOException, DeviceException {
        return getUsedSensor(0);
    }

    /**
     * Sets the (manual) proportional co-efficient of the specified output/control-loop.
     *
     * @param output Output number
     * @param value  P value to use
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void setPValue(int output, double value) throws IOException, DeviceException;

    /**
     * Sets the (manual) proportional co-efficient of the default output/control-loop.
     *
     * @param value P value to use
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    default void setPValue(double value) throws IOException, DeviceException {
        for (int onum = 0; onum < getNumOutputs(); onum++) {
            setPValue(onum, value);
        }
    }

    /**
     * Sets the (manual) integral co-efficient of the specified output/control-loop.
     *
     * @param output Output number
     * @param value  I value to use
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void setIValue(int output, double value) throws IOException, DeviceException;

    /**
     * Sets the (manual) integral co-efficient of the default output/control-loop.
     *
     * @param value I value to use
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    default void setIValue(double value) throws IOException, DeviceException {
        for (int onum = 0; onum < getNumOutputs(); onum++) {
            setPValue(onum, value);
        }
    }

    /**
     * Sets the (manual) derivative co-efficient of the specified output/control-loop.
     *
     * @param output Output number
     * @param value  D value to use
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void setDValue(int output, double value) throws IOException, DeviceException;

    /**
     * Sets the (manual) derivative co-efficient of all outputs/control-loops.
     *
     * @param value D value to use
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    default void setDValue(double value) throws IOException, DeviceException {
        for (int onum = 0; onum < getNumOutputs(); onum++) {
            setPValue(onum, value);
        }
    }

    /**
     * Returns the proportional co-efficient being used by the specified output/control-loop.
     *
     * @param output Output number
     *
     * @return P value
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    double getPValue(int output) throws IOException, DeviceException;

    /**
     * Returns the proportional co-efficient being used by the default output/control-loop.
     *
     * @return P value
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    default double getPValue() throws IOException, DeviceException {
        return getPValue(0);
    }

    /**
     * Returns the integral co-efficient being used by the specified output/control-loop.
     *
     * @param output Output number
     *
     * @return I value
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */

    double getIValue(int output) throws IOException, DeviceException;

    /**
     * Returns the integral co-efficient being used by the default output/control-loop.
     *
     * @return I value
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    default double getIValue() throws IOException, DeviceException {
        return getPValue(0);
    }

    /**
     * Returns the derivative co-efficient being used by the specified output/control-loop.
     *
     * @param output Output number
     *
     * @return D value
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    double getDValue(int output) throws IOException, DeviceException;

    /**
     * Returns the derivative co-efficient being used by the default output/control-loop.
     *
     * @return D value
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    default double getDValue() throws IOException, DeviceException {
        return getPValue(0);
    }

    /**
     * Sets the maximum output power of the heater, as a percentage of its absolute maximum, for the specified output.
     *
     * @param output Output number
     * @param range  Percentage max
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void setHeaterRange(int output, double range) throws IOException, DeviceException;

    /**
     * Sets the maximum output power of the heater, as a percentage of its absolute maximum, for the all outputs.
     *
     * @param range Percentage max
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    default void setHeaterRange(double range) throws IOException, DeviceException {
        for (int onum = 0; onum < getNumOutputs(); onum++) {
            setHeaterRange(onum, range);
        }
    }

    double getHeaterRange(int output) throws IOException, DeviceException;

    default double getHeaterRange() throws IOException, DeviceException {
        return getHeaterRange(0);
    }

    /**
     * Sets the target temperature (set-point) for the specified output/control-loop.
     *
     * @param output      Output number
     * @param temperature Set-point temperature
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void setTargetTemperature(int output, double temperature) throws IOException, DeviceException;

    /**
     * Sets the target temperature (set-point) for the all outputs/control-loops.
     *
     * @param temperature Set-point temperature
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    default void setTargetTemperature(double temperature) throws IOException, DeviceException {
        for (int onum = 0; onum < getNumOutputs(); onum++) {
            setTargetTemperature(onum, temperature);
        }
    }

    /**
     * Returns the target temperature (set-point) for the specified output/control-loop.
     *
     * @param output Output number
     *
     * @return Set-point temperature
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    double getTargetTemperature(int output) throws IOException, DeviceException;

    /**
     * Returns the target temperature (set-point) for the default output/control-loop.
     *
     * @return Set-point temperature
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    default double getTargetTemperature() throws IOException, DeviceException {
        return getTargetTemperature(0);
    }

    /**
     * Returns the output heater power for the specified output/control-loop.
     *
     * @param output Output number
     *
     * @return Heater power, percentage of max
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    double getHeaterPower(int output) throws IOException, DeviceException;

    /**
     * Returns the output heater power for the default output/control-loop.
     *
     * @return Heater power, percentage of max
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    default double getHeaterPower() throws IOException, DeviceException {
        return getHeaterPower(0);
    }

    /**
     * Returns the gas flow rate for the specified output/control-loop.
     *
     * @param output Output number
     *
     * @return Flow rate, in arbitrary units
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    double getGasFlow(int output) throws IOException, DeviceException;

    /**
     * Returns the gas flow rate for the default output/control-loop.
     *
     * @return Flow rate, in arbitrary units
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    default double getGasFlow() throws IOException, DeviceException {
        return getGasFlow(0);
    }

    /**
     * Tells the controller to automatically control the heater output power on the given output/control-loop.
     *
     * @param output Output number
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void useAutoHeater(int output) throws IOException, DeviceException;

    /**
     * Tells the controller to automatically control the heater output power on all outputs/control-loops.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    default void useAutoHeater() throws IOException, DeviceException {
        for (int onum = 0; onum < getNumOutputs(); onum++) {
            useAutoHeater(onum);
        }
    }

    /**
     * Returns whether the controller is automatically controlling the heater output on the given output/control-loop.
     *
     * @param output Output number
     *
     * @return Automatic?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    boolean isUsingAutoHeater(int output) throws IOException, DeviceException;

    /**
     * Returns whether the controller is automatically controlling the heater output on the default output/control-loop.
     *
     * @return Automatic?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    default boolean isUsingAutoHeater() throws IOException, DeviceException {
        return isUsingAutoHeater(0);
    }

    /**
     * Tells the controller to automatically control the gas flow on the given output/control-loop.
     *
     * @param output Output number
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void useAutoFlow(int output) throws IOException, DeviceException;

    /**
     * Tells the controller to automatically control the gas flow on all outputs/control-loops.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    default void useAutoFlow() throws IOException, DeviceException {
        for (int onum = 0; onum < getNumOutputs(); onum++) {
            useAutoFlow(onum);
        }
    }

    /**
     * Returns whether the controller is automatically controlling the gas flow on the given output/control-loop.
     *
     * @param output Output number
     *
     * @return Automatic?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    boolean isFlowAuto(int output) throws IOException, DeviceException;

    /**
     * Returns whether the controller is automatically controlling the gas flow on the default output/control-loop.
     *
     * @return Automatic?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    default boolean isFlowAuto() throws IOException, DeviceException {
        return isFlowAuto(0);
    }

    /**
     * Checks whether the specified output number is valid, throws a DeviceException if not.
     *
     * @param output Output number
     *
     * @throws DeviceException Upon an invalid output number being specified
     */
    default void checkOutput(int output) throws DeviceException {
        if (!Util.isBetween(output, 0, getNumOutputs() - 1)) {
            throw new DeviceException("This temperature controller only has %d outputs.", getNumOutputs());
        }
    }

    /**
     * Manually sets the heater output power for the specified output/control-loop.
     *
     * @param output   Output number
     * @param powerPCT Heater power, percentage of max
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void setManualHeater(int output, double powerPCT) throws IOException, DeviceException;

    /**
     * Manually sets the heater output power for all outputs/control-loops.
     *
     * @param powerPCT Heater power, percentage of max
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    default void setManualHeater(double powerPCT) throws IOException, DeviceException {
        for (int onum = 0; onum < getNumOutputs(); onum++) {
            setManualHeater(onum, powerPCT);
        }
    }

    /**
     * Manually sets the flow rate for the specified output/control-loop.
     *
     * @param output    Output number
     * @param outputPCT Flow rate, in arbitrary units
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    void setManualFlow(int output, double outputPCT) throws IOException, DeviceException;

    /**
     * Manually sets the flow rate for all outputs/control-loops.
     *
     * @param outputPCT Flow rate, in arbitrary units
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    default void setManualFlow(double outputPCT) throws IOException, DeviceException {
        for (int onum = 0; onum < getNumOutputs(); onum++) {
            setManualFlow(onum, outputPCT);
        }
    }

    Zoner getZoner(int output);

    void setZoner(int output, Zoner zoner);

    default void setAutoPIDZones(int output, PIDZone... zones) throws IOException, DeviceException {

        checkOutput(output);

        Zoner zoner = getZoner(output);

        if (getZoner(output) != null && zoner.isRunning()) {
            zoner.stop();
            zoner = new Zoner(this, output, zones);
            zoner.start();
            setZoner(output, zoner);
        } else {
            zoner = new Zoner(this, output, zones);
            setZoner(output, zoner);
        }

    }

    default void setAutoPIDZones(PIDZone... zones) throws IOException, DeviceException {
        for (int onum = 0; onum < getNumOutputs(); onum++) {
            setAutoPIDZones(onum, zones);
        }
    }

    default PIDZone[] getAutoPIDZones(int output) throws IOException, DeviceException {

        checkOutput(output);

        Zoner zoner = getZoner(output);

        if (zoner == null) {
            return new PIDZone[0];
        } else {
            return zoner.getZones();
        }

    }

    default PIDZone[] getAutoPIDZones() throws IOException, DeviceException {
        return getAutoPIDZones(0);
    }

    /**
     * Sets whether the controller should use automatic PID control on the specified output/control-loop.
     *
     * @param output Output number
     * @param auto   Should it be automatic?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    default void useAutoPID(int output, boolean auto) throws IOException, DeviceException {

        checkOutput(output);

        Zoner zoner = getZoner(output);

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
     * Sets whether the controller should use automatic PID control on all outputs/control-loops.
     *
     * @param auto Should it be automatic?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    default void useAutoPID(boolean auto) throws IOException, DeviceException {
        for (int onum = 0; onum < getNumOutputs(); onum++) {
            useAutoPID(onum, auto);
        }
    }

    /**
     * Returns whether the controller is automatically selecting PID values on the specified output/control-loop.
     *
     * @param output Output number
     *
     * @return Is it automatic?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    default boolean isUsingAutoPID(int output) throws IOException, DeviceException {
        checkOutput(output);
        Zoner zoner = getZoner(output);
        return zoner != null && zoner.isRunning();
    }

    /**
     * Returns whether the controller is automatically selecting PID values on the default output/control-loop.
     *
     * @return Is it automatic?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    default boolean isUsingAutoPID() throws IOException, DeviceException {
        return isUsingAutoPID(0);
    }

    /**
     * Returns a virtual TC object to control the specified output/control-loop as if it were a separate
     * controller.
     *
     * @param output Output number
     *
     * @return Virtual controller
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    default MSTC getOutput(int output) throws DeviceException {

        checkOutput(output);

        return new MSTC() {

            @Override
            public void setTargetTemperature(double temperature) throws IOException, DeviceException {
                MSMOTC.this.setTargetTemperature(output, temperature);
            }

            @Override
            public double getTemperature(int sensor) throws IOException, DeviceException {
                return MSMOTC.this.getTemperature(sensor);
            }

            @Override
            public void useSensor(int sensor) throws IOException, DeviceException {
                MSMOTC.this.useSensor(output, sensor);
            }

            @Override
            public int getUsedSensor() throws IOException, DeviceException {
                return MSMOTC.this.getUsedSensor(output);
            }

            @Override
            public int getNumSensors() {
                return MSMOTC.this.getNumSensors();
            }

            @Override
            public void setTemperatureRange(int sensor, double range) throws IOException, DeviceException {
                MSMOTC.this.setTemperatureRange(sensor, range);
            }

            @Override
            public double getTemperatureRange(int sensor) throws IOException, DeviceException {
                return MSMOTC.this.getTemperatureRange(sensor);
            }

            @Override
            public double getTargetTemperature() throws IOException, DeviceException {
                return MSMOTC.this.getTargetTemperature(output);
            }

            @Override
            public double getHeaterPower() throws IOException, DeviceException {
                return MSMOTC.this.getHeaterPower(output);
            }

            @Override
            public double getGasFlow() throws IOException, DeviceException {
                return MSMOTC.this.getGasFlow(output);
            }

            @Override
            public void useAutoHeater() throws IOException, DeviceException {
                MSMOTC.this.useAutoHeater(output);
            }

            @Override
            public void setManualHeater(double powerPCT) throws IOException, DeviceException {
                MSMOTC.this.setManualHeater(output, powerPCT);
            }

            @Override
            public boolean isUsingAutoHeater() throws IOException, DeviceException {
                return MSMOTC.this.isUsingAutoHeater(output);
            }

            @Override
            public void useAutoFlow() throws IOException, DeviceException {
                MSMOTC.this.useAutoFlow(output);
            }

            @Override
            public void setManualFlow(double outputPCT) throws IOException, DeviceException {
                MSMOTC.this.setManualFlow(output, outputPCT);
            }

            @Override
            public boolean isFlowAuto() throws IOException, DeviceException {
                return MSMOTC.this.isFlowAuto(output);
            }

            @Override
            public void setPValue(double value) throws IOException, DeviceException {
                MSMOTC.this.setPValue(output, value);
            }

            @Override
            public void setIValue(double value) throws IOException, DeviceException {
                MSMOTC.this.setIValue(output, value);
            }

            @Override
            public void setDValue(double value) throws IOException, DeviceException {
                MSMOTC.this.setDValue(output, value);
            }

            @Override
            public double getPValue() throws IOException, DeviceException {
                return MSMOTC.this.getPValue(output);
            }

            @Override
            public double getIValue() throws IOException, DeviceException {
                return MSMOTC.this.getIValue(output);
            }

            @Override
            public double getDValue() throws IOException, DeviceException {
                return MSMOTC.this.getDValue(output);
            }

            @Override
            public void setHeaterRange(double rangePCT) throws IOException, DeviceException {
                MSMOTC.this.setHeaterRange(output, rangePCT);
            }

            @Override
            public double getHeaterRange() throws IOException, DeviceException {
                return MSMOTC.this.getHeaterRange(output);
            }

            @Override
            public Zoner getZoner() {
                return null;
            }

            @Override
            public void setZoner(Zoner zoner) {

            }

            @Override
            public String getIDN() throws IOException, DeviceException {
                return MSMOTC.this.getIDN();
            }

            @Override
            public void close() throws IOException, DeviceException {
                MSMOTC.this.close();
            }

            @Override
            public Address getAddress() {
                return MSMOTC.this.getAddress();
            }

            @Override
            public void setAutoPIDZones(PIDZone... zones) throws IOException, DeviceException {
                MSMOTC.this.setAutoPIDZones(output, zones);
            }

            @Override
            public PIDZone[] getAutoPIDZones() throws IOException, DeviceException {
                return MSMOTC.this.getAutoPIDZones(output);
            }

            @Override
            public void useAutoPID(boolean auto) throws IOException, DeviceException {
                MSMOTC.this.useAutoPID(output, auto);
            }

            @Override
            public boolean isUsingAutoPID() throws IOException, DeviceException {
                return MSMOTC.this.isUsingAutoPID(output);
            }

        };
    }

    class Zoner implements Runnable {

        private final PIDZone[] zones;
        private final int       output;
        private       PIDZone   currentZone;
        private       boolean   running = false;
        private       PIDZone   minZone;
        private       PIDZone   maxZone;
        private       Thread    thread;
        private       MSMOTC    tc;

        public Zoner(MSMOTC tc, int output, PIDZone[] zones) {

            this.tc = tc;

            this.zones = zones;
            this.output = output;
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

                    double T = tc.getTemperature(tc.getUsedSensor(output));

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
                tc.setHeaterRange(output, zone.getRange());
                tc.setPValue(output, zone.getP());
                tc.setIValue(output, zone.getI());
                tc.setDValue(output, zone.getD());
            } else {
                tc.setHeaterRange(output, zone.getRange());
                tc.setManualHeater(output, zone.getPower());
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

}
