package JISA.Devices;

import JISA.Addresses.InstrumentAddress;
import JISA.Util;

import java.io.IOException;

public abstract class MSMOTController extends MSTController {

    protected int defaultOutput = 0;

    public MSMOTController(InstrumentAddress address) throws IOException {
        super(address);
    }

    /**
     * Sets which heater/flow output to assume when not specified in a method call
     *
     * @param output Output to assume
     *
     * @throws DeviceException Upon trying to set an output channel that does not exist
     */
    public void setDefaultOutput(int output) throws DeviceException {

        if (!Util.isBetween(output, 0, getNumOutputs())) {
            throw new DeviceException("That output does not exist!");
        }

        defaultOutput = output;
    }

    /**
     * Returns which heater/flow output is assumed when not specified in a method call
     *
     * @return Default output number
     */
    public int getDefaultOutput() {
        return defaultOutput;
    }

    /**
     * Returns the number of outputs the controller has
     *
     * @return Number of outputs
     */
    public abstract int getNumOutputs();

    /**
     * Configures the controller to use the given sensor for the given output/control-loop.
     *
     * @param output Output number to configure
     * @param sensor Sensor number to set
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract void useSensor(int output, int sensor) throws IOException, DeviceException;

    /**
     * Configures the controller to use the given sensor for all outputs/control-loops.
     *
     * @param sensor Sensor number to set
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public void useSensor(int sensor) throws IOException, DeviceException {
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
    public abstract int getUsedSensor(int output) throws IOException, DeviceException;

    /**
     * Returns which sensor the default output/control-loop is configured to use.
     *
     * @return Sensor number
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public int getUsedSensor() throws IOException, DeviceException {
        return getUsedSensor(defaultOutput);
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
    public abstract void setPValue(int output, double value) throws IOException, DeviceException;

    /**
     * Sets the (manual) proportional co-efficient of the default output/control-loop.
     *
     * @param value P value to use
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public void setPValue(double value) throws IOException, DeviceException {
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
    public abstract void setIValue(int output, double value) throws IOException, DeviceException;

    /**
     * Sets the (manual) integral co-efficient of the default output/control-loop.
     *
     * @param value I value to use
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public void setIValue(double value) throws IOException, DeviceException {
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
    public abstract void setDValue(int output, double value) throws IOException, DeviceException;

    /**
     * Sets the (manual) derivative co-efficient of all outputs/control-loops.
     *
     * @param value D value to use
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public void setDValue(double value) throws IOException, DeviceException {
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
    public abstract double getPValue(int output) throws IOException, DeviceException;

    /**
     * Returns the proportional co-efficient being used by the default output/control-loop.
     *
     * @return P value
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public double getPValue() throws IOException, DeviceException {
        return getPValue(defaultOutput);
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

    public abstract double getIValue(int output) throws IOException, DeviceException;

    /**
     * Returns the integral co-efficient being used by the default output/control-loop.
     *
     * @return I value
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public double getIValue() throws IOException, DeviceException {
        return getPValue(defaultOutput);
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
    public abstract double getDValue(int output) throws IOException, DeviceException;

    /**
     * Returns the derivative co-efficient being used by the default output/control-loop.
     *
     * @return D value
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public double getDValue() throws IOException, DeviceException {
        return getPValue(defaultOutput);
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
    public abstract void setTargetTemperature(int output, double temperature) throws IOException, DeviceException;

    /**
     * Sets the target temperature (set-point) for the all outputs/control-loops.
     *
     * @param temperature Set-point temperature
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public void setTargetTemperature(double temperature) throws IOException, DeviceException {
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
    public abstract double getTargetTemperature(int output) throws IOException, DeviceException;

    /**
     * Returns the target temperature (set-point) for the default output/control-loop.
     *
     * @return Set-point temperature
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public double getTargetTemperature() throws IOException, DeviceException {
        return getTargetTemperature(defaultOutput);
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
    public abstract double getHeaterPower(int output) throws IOException, DeviceException;

    /**
     * Returns the output heater power for the default output/control-loop.
     *
     * @return Heater power, percentage of max
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public double getHeaterPower() throws IOException, DeviceException {
        return getHeaterPower(defaultOutput);
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
    public abstract double getGasFlow(int output) throws IOException, DeviceException;

    /**
     * Returns the gas flow rate for the default output/control-loop.
     *
     * @return Flow rate, in arbitrary units
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public double getGasFlow() throws IOException, DeviceException {
        return getGasFlow(defaultOutput);
    }

    /**
     * Tells the controller to automatically control the heater output power on the given output/control-loop.
     *
     * @param output Output number
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract void useAutoHeater(int output) throws IOException, DeviceException;

    /**
     * Tells the controller to automatically control the heater output power on all outputs/control-loops.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public void useAutoHeater() throws IOException, DeviceException {
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
    public abstract boolean isHeaterAuto(int output) throws IOException, DeviceException;

    /**
     * Returns whether the controller is automatically controlling the heater output on the default output/control-loop.
     *
     * @return Automatic?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public boolean isHeaterAuto() throws IOException, DeviceException {
        return isHeaterAuto(defaultOutput);
    }

    /**
     * Tells the controller to automatically control the gas flow on the given output/control-loop.
     *
     * @param output Output number
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public abstract void useAutoFlow(int output) throws IOException, DeviceException;

    /**
     * Tells the controller to automatically control the gas flow on all outputs/control-loops.
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public void useAutoFlow() throws IOException, DeviceException {
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
    public abstract boolean isFlowAuto(int output) throws IOException, DeviceException;

    /**
     * Returns whether the controller is automatically controlling the gas flow on the default output/control-loop.
     *
     * @return Automatic?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public boolean isFlowAuto() throws IOException, DeviceException {
        return isFlowAuto(defaultOutput);
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
    public abstract void useAutoPID(int output, boolean auto) throws IOException, DeviceException;

    /**
     * Sets whether the controller should use automatic PID control on all outputs/control-loops.
     *
     * @param auto Should it be automatic?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public void useAutoPID(boolean auto) throws IOException, DeviceException {
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
    public abstract boolean isPIDAuto(int output) throws IOException, DeviceException;

    /**
     * Returns whether the controller is automatically selecting PID values on the default output/control-loop.
     *
     * @return Is it automatic?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public boolean isPIDAuto() throws IOException, DeviceException {
        return isPIDAuto(defaultOutput);
    }

    /**
     * Checks whether the specified output number is valid, throws a DeviceException if not.
     *
     * @param output Output number
     *
     * @throws DeviceException Upon an invalid output number being specified
     */
    protected void checkOutput(int output) throws DeviceException {
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
    public abstract void setManualHeater(int output, double powerPCT) throws IOException, DeviceException;

    /**
     * Manually sets the heater output power for all outputs/control-loops.
     *
     * @param powerPCT Heater power, percentage of max
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public void setManualHeater(double powerPCT) throws IOException, DeviceException {
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
    public abstract void setManualFlow(int output, double outputPCT) throws IOException, DeviceException;

    /**
     * Manually sets the flow rate for all outputs/control-loops.
     *
     * @param outputPCT Flow rate, in arbitrary units
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public void setManualFlow(double outputPCT) throws IOException, DeviceException {
        for (int onum = 0; onum < getNumOutputs(); onum++) {
            setManualFlow(onum, outputPCT);
        }
    }

    /**
     * Waits for the temperature reported by the sensor used by the specified output/control-loop to remain within
     * 1% of its target (set-point) temperature for at least 1 minute.
     *
     * @param output Output number
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public void waitForStableTemperature(int output) throws IOException, DeviceException {
        checkOutput(output);
        waitForStableTemperature(getUsedSensor(output), getTargetTemperature(output));
    }

    /**
     * Returns a virtual TController object to control the specified output/control-loop as if it were a separate
     * controller.
     *
     * @param output Output number
     *
     * @return Virtual controller
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon compatibility error
     */
    public TController getOutput(int output) throws DeviceException, IOException {
        checkOutput(output);
        return new VirtualTC(output);
    }

    /**
     * Class for representing an output/control-loop as its own temperature controller.
     */
    public class VirtualTC extends TController {

        private int output;

        /**
         * Connects to the temperature controller at the given address, returning an instrument object to control it.
         *
         * @param output Output number
         *
         * @throws IOException Upon communications error
         */
        public VirtualTC(int output) throws IOException {
            super(null);
        }

        @Override
        public void setTargetTemperature(double temperature) throws IOException, DeviceException {
            MSMOTController.this.setTargetTemperature(output, temperature);
        }

        @Override
        public double getTemperature() throws IOException, DeviceException {
            return MSMOTController.this.getTemperature(getUsedSensor(output));
        }

        @Override
        public double getTargetTemperature() throws IOException, DeviceException {
            return MSMOTController.this.getTargetTemperature(output);
        }

        @Override
        public double getHeaterPower() throws IOException, DeviceException {
            return MSMOTController.this.getHeaterPower(output);
        }

        @Override
        public double getGasFlow() throws IOException, DeviceException {
            return MSMOTController.this.getGasFlow(output);
        }

        @Override
        public void useAutoHeater() throws IOException, DeviceException {
            MSMOTController.this.useAutoHeater(output);
        }

        @Override
        public void setManualHeater(double powerPCT) throws IOException, DeviceException {
            MSMOTController.this.setManualHeater(output, powerPCT);
        }

        @Override
        public boolean isHeaterAuto() throws IOException, DeviceException {
            return MSMOTController.this.isHeaterAuto(output);
        }

        @Override
        public void useAutoFlow() throws IOException, DeviceException {
            MSMOTController.this.useAutoFlow(output);
        }

        @Override
        public void setManualFlow(double outputPCT) throws IOException, DeviceException {
            MSMOTController.this.setManualFlow(output, outputPCT);
        }

        @Override
        public boolean isFlowAuto() throws IOException, DeviceException {
            return MSMOTController.this.isFlowAuto(output);
        }

        @Override
        public void useAutoPID(boolean auto) throws IOException, DeviceException {
            MSMOTController.this.useAutoPID(output, auto);
        }

        @Override
        public boolean isPIDAuto() throws IOException, DeviceException {
            return MSMOTController.this.isPIDAuto(output);
        }

        @Override
        public void setPValue(double value) throws IOException, DeviceException {
            MSMOTController.this.setPValue(output, value);
        }

        @Override
        public void setIValue(double value) throws IOException, DeviceException {
            MSMOTController.this.setIValue(output, value);
        }

        @Override
        public void setDValue(double value) throws IOException, DeviceException {
            MSMOTController.this.setDValue(output, value);
        }

        @Override
        public double getPValue() throws IOException, DeviceException {
            return MSMOTController.this.getPValue(output);
        }

        @Override
        public double getIValue() throws IOException, DeviceException {
            return MSMOTController.this.getIValue(output);
        }

        @Override
        public double getDValue() throws IOException, DeviceException {
            return MSMOTController.this.getDValue(output);
        }
    }

}
