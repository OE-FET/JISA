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

    public int getDefaultOutput() {
        return defaultOutput;
    }

    public abstract int getNumOutputs();

    public abstract void useSensor(int output, int sensor) throws IOException, DeviceException;

    public void useSensor(int sensor) throws IOException, DeviceException {
        useSensor(defaultOutput, sensor);
    }

    public abstract int getUsedSensor(int output) throws IOException, DeviceException;

    public int getUsedSensor() throws IOException, DeviceException {
        return getUsedSensor(defaultOutput);
    }

    public abstract void setPValue(int output, double value) throws IOException, DeviceException;

    public void setPValue(double value) throws IOException, DeviceException {
        setPValue(defaultOutput, value);
    }

    public abstract void setIValue(int output, double value) throws IOException, DeviceException;

    public void setIValue(double value) throws IOException, DeviceException {
        setPValue(defaultOutput, value);
    }

    public abstract void setDValue(int output, double value) throws IOException, DeviceException;

    public void setDValue(double value) throws IOException, DeviceException {
        setPValue(defaultOutput, value);
    }

    public abstract double getPValue(int output) throws IOException, DeviceException;

    public double getPValue() throws IOException, DeviceException {
        return getPValue(defaultOutput);
    }

    public abstract double getIValue(int output) throws IOException, DeviceException;

    public double getIValue() throws IOException, DeviceException {
        return getPValue(defaultOutput);
    }

    public abstract double getDValue(int output) throws IOException, DeviceException;

    public double getDValue() throws IOException, DeviceException {
        return getPValue(defaultOutput);
    }

    public abstract void setTargetTemperature(int output, double temperature) throws IOException, DeviceException;

    public void setTargetTemperature(double temperature) throws IOException, DeviceException {
        setTargetTemperature(defaultOutput, temperature);
    }

    public abstract double getTargetTemperature(int output) throws IOException, DeviceException;

    public double getTargetTemperature() throws IOException, DeviceException {
        return getTargetTemperature(defaultOutput);
    }

    public abstract double getHeaterPower(int output) throws IOException, DeviceException;

    public double getHeaterPower() throws IOException, DeviceException {
        return getHeaterPower(defaultOutput);
    }

    public abstract double getGasFlow(int output) throws IOException, DeviceException;

    public double getGasFlow() throws IOException, DeviceException {
        return getGasFlow(defaultOutput);
    }

    public abstract void useAutoHeater(int output) throws IOException, DeviceException;

    public void useAutoHeater() throws IOException, DeviceException {
        useAutoHeater(defaultOutput);
    }

    public abstract boolean isHeaterAuto(int output) throws IOException, DeviceException;

    public boolean isHeaterAuto() throws IOException, DeviceException {
        return isHeaterAuto(defaultOutput);
    }

    public abstract void useAutoFlow(int output) throws IOException, DeviceException;

    public void useAutoFlow() throws IOException, DeviceException {
        useAutoFlow(defaultOutput);
    }

    public abstract boolean isFlowAuto(int output) throws IOException, DeviceException;

    public boolean isFlowAuto() throws IOException, DeviceException {
        return isFlowAuto(defaultOutput);
    }

    public abstract void useAutoPID(int output, boolean auto) throws IOException, DeviceException;

    public void useAutoPID(boolean auto) throws IOException, DeviceException {
        useAutoPID(defaultOutput, auto);
    }

    public abstract boolean isPIDAuto(int output) throws IOException, DeviceException;

    public boolean isPIDAuto() throws IOException, DeviceException {
        return isPIDAuto(defaultOutput);
    }

    public void checkOutput(int output) throws DeviceException {
        if (!Util.isBetween(output, 0, getNumOutputs() - 1)) {
            throw new DeviceException("This temperature controller only has %d outputs.", getNumOutputs());
        }
    }

    public abstract void setManualHeater(int output, double powerPCT) throws IOException, DeviceException;

    public void setManualHeater(double powerPCT) throws IOException, DeviceException {
        setManualHeater(defaultOutput, powerPCT);
    }

    public abstract void setManualFlow(int output, double outputPCT) throws IOException, DeviceException;

    public void setManualFlow(double outputPCT) throws IOException, DeviceException {
        setManualFlow(defaultOutput, outputPCT);
    }

    public void waitForStableTemperature(int output) throws IOException, DeviceException {

        checkOutput(output);
        waitForStableTemperature(getUsedSensor(output), getTargetTemperature(output));

    }

    public void setTargetAndWait(int output, double temperature) throws IOException, DeviceException {
        checkOutput(output);
        setTargetTemperature(output, temperature);
        waitForStableTemperature(output);
    }

    public TController getOutput(int output) throws DeviceException, IOException {
        checkOutput(output);
        return new VirtualTC(output);
    }

    public class VirtualTC extends TController {

        private int output;

        /**
         * Connects to the temperature controller at the given address, returning an instrument object to control it.
         *
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
