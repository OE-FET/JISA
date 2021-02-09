package jisa.devices.interfaces;

import jisa.addresses.Address;
import jisa.Util;
import jisa.devices.DeviceException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface MSMOTC extends MSTC, MultiOutput<MSTC> {

    public static String getDescription() {
        return "Multi-Sensor Multi-Output Temperature Controller";
    }

    default Class<MSTC> getOutputType() {
        return MSTC.class;
    }

    /**
     * Returns the number of outputs the controller has
     *
     * @return Number of outputs
     */
    int getNumOutputs();

    @Override
    default List<MSTC> getOutputs() {

        List<MSTC> list = new ArrayList<>();

        for (int i = 0; i < getNumOutputs(); i++) {

            try {
                list.add(getOutput(i));
            } catch (DeviceException e) {
                e.printStackTrace();
            }

        }

        return list;

    }

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
            setIValue(onum, value);
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
            setDValue(onum, value);
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
        return getIValue(0);
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
        return getDValue(0);
    }

    /**
     * Sets whether auto PID-zoning should be used.
     *
     * @param flag Should it be used?
     */
    void useAutoPID(int output, boolean flag) throws IOException, DeviceException;

    default void useAutoPID(boolean flag) throws IOException, DeviceException {

        for (int i = 0; i < getNumOutputs(); i++) {
            useAutoPID(i, flag);
        }

    }

    /**
     * Returns whether auto PID-zoning is being used currently.
     *
     * @return Is it being used?
     */
    boolean isUsingAutoPID(int output) throws IOException, DeviceException;

    default boolean isUsingAutoPID() throws IOException, DeviceException {
        return isUsingAutoPID(0);
    }

    /**
     * Returns a list of all PID zones currently set.
     *
     * @return List of zones
     */
    List<PID.Zone> getAutoPIDZones(int output) throws IOException, DeviceException;

    default List<PID.Zone> getAutoPIDZones() throws IOException, DeviceException {
        return getAutoPIDZones(0);
    }

    /**
     * Sets the PID zones to use for auto-zoning.
     *
     * @param zones Zones to use
     */
    void setAutoPIDZones(int output, PID.Zone... zones) throws IOException, DeviceException;

    default void setAutoPIDZones(PID.Zone... zones) throws IOException, DeviceException {

        for (int i = 0; i < getNumOutputs(); i++) {
            setAutoPIDZones(i, zones);
        }

    }

    /**
     * Sets the PID zones to use for auto-zoning.
     *
     * @param zones Zones to use
     */
    default void setAutoPIDZones(int output, Collection<PID.Zone> zones) throws IOException, DeviceException {
        setAutoPIDZones(output, zones.toArray(new PID.Zone[0]));
    }

    default void setAutoPIDZones(Collection<PID.Zone> zones) throws IOException, DeviceException {

        for (int i = 0; i < getNumOutputs(); i++) {
            setAutoPIDZones(i, zones);
        }

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

    void setTemperatureRampRate(int output, double kPerMin) throws IOException, DeviceException;

    default void setTemperatureRampRate(double kPerMin) throws IOException, DeviceException {

        for (int onum = 0; onum < getNumOutputs(); onum++) {
            setTemperatureRampRate(onum, kPerMin);
        }

    }

    double getTemperatureRampRate(int output) throws IOException, DeviceException;

    default double getTemperatureRampRate() throws IOException, DeviceException {
        return getTemperatureRampRate(0);
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
    double getFlow(int output) throws IOException, DeviceException;

    /**
     * Returns the gas flow rate for the default output/control-loop.
     *
     * @return Flow rate, in arbitrary units
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    default double getFlow() throws IOException, DeviceException {
        return getFlow(0);
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
    boolean isUsingAutoFlow(int output) throws IOException, DeviceException;

    /**
     * Returns whether the controller is automatically controlling the gas flow on the default output/control-loop.
     *
     * @return Automatic?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    default boolean isUsingAutoFlow() throws IOException, DeviceException {
        return isUsingAutoFlow(0);
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
    void setHeaterPower(int output, double powerPCT) throws IOException, DeviceException;

    /**
     * Manually sets the heater output power for all outputs/control-loops.
     *
     * @param powerPCT Heater power, percentage of max
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    default void setHeaterPower(double powerPCT) throws IOException, DeviceException {
        for (int onum = 0; onum < getNumOutputs(); onum++) {
            setHeaterPower(onum, powerPCT);
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
    void setFlow(int output, double outputPCT) throws IOException, DeviceException;

    /**
     * Manually sets the flow rate for all outputs/control-loops.
     *
     * @param outputPCT Flow rate, in arbitrary units
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    default void setFlow(double outputPCT) throws IOException, DeviceException {
        for (int onum = 0; onum < getNumOutputs(); onum++) {
            setFlow(onum, outputPCT);
        }
    }

    default void updateAutoPID(int output) throws IOException, DeviceException {

        if (isUsingAutoPID(output)) {

            double temp = getTargetTemperature(output);

            for (PID.Zone zone : getAutoPIDZones(output)) {

                if (zone.matches(temp)) {
                    usePIDZone(zone);
                    break;
                }

            }

        }

    }

    default void usePIDZone(int output, PID.Zone zone) throws IOException, DeviceException {

        setPValue(output, zone.getP());
        setIValue(output, zone.getI());
        setDValue(output, zone.getD());
        setHeaterRange(output, zone.getRange());

        if (zone.isAuto()) {
            useAutoHeater(output);
        } else {
            setHeaterPower(output, zone.getPower());
        }

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
            public String getSensorName() {

                try {
                    return getSensorName(getUsedSensor());
                } catch (Exception e) {
                    return "Unknown Sensor";
                }

            }

            @Override
            public void setTargetTemperature(double temperature) throws IOException, DeviceException {
                MSMOTC.this.setTargetTemperature(output, temperature);
            }

            @Override
            public double getTemperatureRampRate() throws IOException, DeviceException {
                return MSMOTC.this.getTemperatureRampRate(output);
            }

            @Override
            public void setTemperatureRampRate(double kPerMin) throws IOException, DeviceException {
                MSMOTC.this.setTemperatureRampRate(output, kPerMin);
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
            public String getSensorName(int sensorNumber) {
                return MSMOTC.this.getSensorName(sensorNumber);
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
            public String getOutputName() {
                return MSMOTC.this.getOutputName(output);
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
            public double getFlow() throws IOException, DeviceException {
                return MSMOTC.this.getFlow(output);
            }

            @Override
            public void useAutoHeater() throws IOException, DeviceException {
                MSMOTC.this.useAutoHeater(output);
            }

            @Override
            public void setHeaterPower(double powerPCT) throws IOException, DeviceException {
                MSMOTC.this.setHeaterPower(output, powerPCT);
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
            public void setFlow(double outputPCT) throws IOException, DeviceException {
                MSMOTC.this.setFlow(output, outputPCT);
            }

            @Override
            public boolean isUsingAutoFlow() throws IOException, DeviceException {
                return MSMOTC.this.isUsingAutoFlow(output);
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
            public void useAutoPID(boolean flag) throws IOException, DeviceException {
                MSMOTC.this.useAutoPID(output, flag);
            }

            @Override
            public boolean isUsingAutoPID() throws IOException, DeviceException {
                return MSMOTC.this.isUsingAutoPID(output);
            }

            @Override
            public List<PID.Zone> getAutoPIDZones() throws IOException, DeviceException {
                return MSMOTC.this.getAutoPIDZones(output);
            }

            @Override
            public void setAutoPIDZones(PID.Zone... zones) throws IOException, DeviceException {
                MSMOTC.this.setAutoPIDZones(output, zones);
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


        };
    }

}
