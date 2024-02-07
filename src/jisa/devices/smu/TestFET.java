package jisa.devices.smu;

import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.MCSMU;
import jisa.devices.interfaces.SMU;
import jisa.enums.AMode;
import jisa.enums.Source;
import jisa.enums.TType;
import jisa.enums.Terminals;

import java.io.IOException;
import java.util.List;
import java.util.Random;

public class TestFET implements MCSMU<SMU> {

    public static String getDescription() {
        return "Virtual FET Simulator";
    }

    public final SMU SD_CHANNEL = new SMU(0);
    public final SMU SG_CHANNEL = new SMU(1);

    private final List<jisa.devices.interfaces.SMU> channels = List.of(SD_CHANNEL, SG_CHANNEL);

    private static final double VT     = 1.0;
    private static final double MU     = 1.0;
    private static final double C      = 100e-6;
    private static final double W      = 105e-6;
    private static final double L      = 240e-6;
    private static final Random random = new Random();

    private       double vSD = 0;
    private       double iSD = 0;
    private       double vSG = 0;
    private final double iSG = 0;

    public static final int CHANNEL_SD = 0;
    public static final int CHANNEL_SG = 1;

    @Override
    public String getIDN() throws IOException, DeviceException {
        return "Test FET";
    }

    @Override
    public String getName() {
        return "Test FET";
    }

    @Override
    public void close() throws IOException, DeviceException {

    }

    @Override
    public Address getAddress() {
        return null;
    }

    public class SMU implements jisa.devices.interfaces.SMU {

        private final int channel;

        public SMU(int channel) { this.channel = channel; }

        @Override
        public double getSetCurrent() throws DeviceException, IOException {
            throw new DeviceException("Not implemented.");
        }

        @Override
        public double getSetVoltage() throws DeviceException, IOException {
            throw new DeviceException("Not implemented.");
        }

        @Override
        public double getVoltage() throws DeviceException {

            switch (channel) {

                case CHANNEL_SD:
                    return vSD;

                case CHANNEL_SG:
                    return vSG;

                default:
                    throw new DeviceException("Invalid Channel");

            }

        }

        @Override
        public double getCurrent() throws DeviceException {

            iSD = calculate(vSD, vSG);

            switch (channel) {

                case CHANNEL_SD:
                    return iSD;

                case CHANNEL_SG:
                    return iSG;

                default:
                    throw new DeviceException("Invalid Channel");

            }
        }

        @Override
        public void setVoltage(double voltage) throws DeviceException {

            switch (channel) {

                case CHANNEL_SD:
                    vSD = voltage;
                    break;

                case CHANNEL_SG:
                    vSG = voltage;
                    break;

                default:
                    throw new DeviceException("Invalid Channel");

            }

        }

        @Override
        public void setCurrent(double current) throws DeviceException, IOException {

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
        public void setSource(Source source) throws DeviceException, IOException {

        }

        @Override
        public Source getSource() throws DeviceException, IOException {
            return null;
        }

        @Override
        public double getSourceValue() throws DeviceException, IOException {
            return 0;
        }

        @Override
        public double getMeasureValue() throws DeviceException, IOException {
            return 0;
        }

        @Override
        public void setFourProbeEnabled(boolean fourProbes) throws DeviceException, IOException {

        }

        @Override
        public boolean isFourProbeEnabled() throws DeviceException, IOException {
            return false;
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
            return 0;
        }

        @Override
        public TType getTerminalType(Terminals terminals) throws DeviceException, IOException {
            return null;
        }

        @Override
        public void setTerminals(Terminals terminals) throws DeviceException, IOException {

        }

        @Override
        public Terminals getTerminals() throws DeviceException, IOException {
            return null;
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
        public String getIDN() throws IOException, DeviceException {
            return "FET";
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
            
            switch (channel) {

                case 0:
                    return "SD Channel";

                case 1:
                    return "SG Channel";

                default:
                    return "Unknown";

            }

        }

        @Override
        public void setSourceValue(double level) throws DeviceException, IOException {
            
            switch (getSource()) {

                case VOLTAGE:
                    setVoltage(level);
                    break;

                case CURRENT:
                    setCurrent(level);
                    break;

            }

        }

    }

    protected static double calculate(double vSD, double vSG) {

        double vT = vSD < 0 ? VT * -1 : VT;

        boolean nSat = Math.abs(vSD) < Math.abs(vSG - vT);

        if (nSat) {

            return MU * C * (W / L) * ((vSG - vT) * vSD - (Math.pow(vSD, 2) / 2)) + 10e-9 * random.nextDouble();

        } else {

            return MU * C * (W / L) * (Math.pow((vSG - vT), 2) / 2) + 10e-9 * random.nextDouble();

        }

    }

    public TestFET(Address address) {

    }

    @Override
    public List<jisa.devices.interfaces.SMU> getSMUChannels() {
        return channels;
    }

}
