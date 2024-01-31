package jisa.devices.smu;

import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.MCSMU;
import jisa.devices.interfaces.SMU;
import jisa.enums.AMode;
import jisa.enums.Source;
import jisa.enums.TType;
import jisa.enums.Terminals;
import jisa.maths.Range;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class DummyMCSMU implements MCSMU {

    public static String getDescription() {
        return "Dummy SMU";
    }

    @Override
    public List<SMU> getSMUChannels() {
        return null;
    }

    private final List<SMU> channels = Range.count(0, 3).stream().map(DSMU::new).collect(Collectors.toUnmodifiableList());
    private final Random    random   = new Random();
    private final Double[]  current  = {null, null, null, null};
    private final Double[]  voltage  = {null, null, null, null};
    private final Source[]  mode     = {Source.VOLTAGE, Source.VOLTAGE, Source.VOLTAGE, Source.VOLTAGE};
    private final boolean[] probes   = {true, true, true, true};
    private final double[]  R        = {random.nextDouble() * 500, random.nextDouble() * 500, random.nextDouble() * 500, random.nextDouble() * 500};

    public class DSMU implements SMU {

        private final int channel;

        public DSMU(int channel) { this.channel = channel; }

        @Override
        public double getSetCurrent() throws DeviceException, IOException {
            throw new DeviceException("Not implemented.");
        }

        @Override
        public double getSetVoltage() throws DeviceException, IOException {
            throw new DeviceException("Not implemented.");
        }

        @Override
        public double getVoltage() throws DeviceException, IOException {

            if (voltage[channel] == null && current[channel] == null) {
                setVoltage(0.0);
            }

            return voltage[channel] == null ? getCurrent(channel) * (R[channel] + (1 - 2 * random.nextDouble()) * 0.05 * R[channel]) : voltage[channel];

        }

        @Override
        public double getCurrent() throws DeviceException, IOException {

            if (voltage[channel] == null && current[channel] == null) {
                setVoltage(0.0);
            }

            return current[channel] == null ? getVoltage(channel) / (R[channel] + (1 - 2 * random.nextDouble()) * 0.05 * R[channel]) : current[channel];

        }

        @Override
        public void setVoltage(double v) throws DeviceException, IOException {
            voltage[channel] = v;
            current[channel] = null;
        }

        @Override
        public void setCurrent(double i) throws DeviceException, IOException {
            current[channel] = i;
            voltage[channel] = null;
        }

        @Override
        public void turnOn() throws DeviceException, IOException {

        }

        @Override
        public void turnOff() throws DeviceException, IOException {

        }

        @Override
        public boolean isOn() throws DeviceException, IOException {
            return true;
        }

        @Override
        public void setSourceValue(double level) throws DeviceException, IOException {

        }

        @Override
        public Source getSource() throws DeviceException, IOException {
            return mode[channel];
        }

        @Override
        public void setSource(Source source) throws DeviceException, IOException {

        }

        @Override
        public double getSourceValue() throws DeviceException, IOException {

            switch (getSource()) {

                case VOLTAGE:
                    getVoltage();
                    break;

                case CURRENT:
                    getCurrent();
                    break;

            }

            return getVoltage(channel);
        }

        @Override
        public double getMeasureValue() throws DeviceException, IOException {
            switch (getSource()) {

                case VOLTAGE:
                    getCurrent();
                    break;

                case CURRENT:
                    getVoltage();
                    break;

            }

            return getVoltage();
        }

        @Override
        public void setFourProbeEnabled(boolean fourProbes) throws DeviceException, IOException {
            probes[channel] = fourProbes;
        }

        @Override
        public boolean isFourProbeEnabled() throws DeviceException, IOException {
            return probes[channel];
        }

        @Override
        public void setAverageMode(AMode mode) throws DeviceException, IOException {

        }

        @Override
        public void setAverageCount(int count) throws DeviceException, IOException {

        }

        @Override
        public int getAverageCount() throws DeviceException, IOException {
            return 0;
        }

        @Override
        public AMode getAverageMode() throws DeviceException, IOException {
            return null;
        }

        @Override
        public void setSourceRange(double value) throws DeviceException, IOException {

        }

        @Override
        public double getSourceRange() throws DeviceException, IOException {
            return 0;
        }

        @Override
        public void useAutoSourceRange() throws DeviceException, IOException {

        }

        @Override
        public boolean isAutoRangingSource() throws DeviceException, IOException {
            return false;
        }

        @Override
        public void setMeasureRange(double value) throws DeviceException, IOException {

        }

        @Override
        public double getMeasureRange() throws DeviceException, IOException {
            return 0;
        }

        @Override
        public void useAutoMeasureRange() throws DeviceException, IOException {

        }

        @Override
        public boolean isAutoRangingMeasure() throws DeviceException, IOException {
            return false;
        }

        @Override
        public void setVoltageRange(double value) throws DeviceException, IOException {

        }

        @Override
        public double getVoltageRange() throws DeviceException, IOException {
            return 0;
        }

        @Override
        public void useAutoVoltageRange() throws DeviceException, IOException {

        }

        @Override
        public boolean isAutoRangingVoltage() throws DeviceException, IOException {
            return false;
        }

        @Override
        public void setCurrentRange(double value) throws DeviceException, IOException {

        }

        @Override
        public double getCurrentRange() throws DeviceException, IOException {
            return 0;
        }

        @Override
        public void useAutoCurrentRange() throws DeviceException, IOException {

        }

        @Override
        public boolean isAutoRangingCurrent() throws DeviceException, IOException {
            return false;
        }

        @Override
        public void setOutputLimit(double value) throws DeviceException, IOException {

        }

        @Override
        public double getOutputLimit() throws DeviceException, IOException {
            return 0;
        }

        @Override
        public void setVoltageLimit(double value) throws DeviceException, IOException {

        }

        @Override
        public double getVoltageLimit() throws DeviceException, IOException {
            return 0;
        }

        @Override
        public void setCurrentLimit(double value) throws DeviceException, IOException {

        }

        @Override
        public double getCurrentLimit() throws DeviceException, IOException {
            return 0;
        }

        @Override
        public void setIntegrationTime(double time) throws DeviceException, IOException {

        }

        @Override
        public double getIntegrationTime() throws DeviceException, IOException {
            return 100e-6;
        }

        @Override
        public TType getTerminalType(Terminals terminals) throws DeviceException, IOException {
            return TType.TRIAX;
        }

        @Override
        public void setTerminals(Terminals terminals) throws DeviceException, IOException {

        }

        @Override
        public Terminals getTerminals() throws DeviceException, IOException {
            return Terminals.FRONT;
        }

        @Override
        public void setOffMode(OffMode mode) throws DeviceException, IOException {

        }

        @Override
        public OffMode getOffMode() throws DeviceException, IOException {
            return null;
        }

        @Override
        public boolean isLineFilterEnabled() throws DeviceException, IOException {
            return false;
        }

        @Override
        public void setLineFilterEnabled(boolean enabled) throws DeviceException, IOException {

        }

        @Override
        public String getIDN() throws IOException {
            return null;
        }

        @Override
        public void close() throws IOException, DeviceException {

        }

        @Override
        public Address getAddress() {
            return null;
        }

        @Override
        public String getName() {
            return "Dummy MCSMU";
        }

    }


}
