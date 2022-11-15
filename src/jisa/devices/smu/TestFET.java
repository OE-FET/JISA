package jisa.devices.smu;

import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.MCSMU;
import jisa.enums.*;

import java.io.IOException;
import java.util.Random;

public class TestFET implements MCSMU {

    public static String getDescription() {
        return "Virtual FET Simulator";
    }

    private static final double VT     = 1.0;
    private static final double MU     = 1.0;
    private static final double C      = 100e-6;
    private static final double W      = 105e-6;
    private static final double L      = 240e-6;
    private static final Random random = new Random();

    private double vSD = 0;
    private double iSD = 0;
    private double vSG = 0;
    private double iSG = 0;

    public static final int CHANNEL_SD = 0;
    public static final int CHANNEL_SG = 1;

    private static double calculate(double vSD, double vSG) {

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
    public double getSetCurrent() throws DeviceException, IOException
    {
        throw new DeviceException("Not implemented.");
    }

    @Override
    public double getSetVoltage() throws DeviceException, IOException
    {
        throw new DeviceException("Not implemented.");
    }

    @Override
    public String getChannelName(int channel) {

        switch (channel) {

            case 0:
                return "Source-Drain Channel";

            case 1:
                return "Source-Gate Channel";

            default:
                return "Unknown Channel";

        }

    }

    @Override
    public double getVoltage(int channel) throws DeviceException {

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
    public double getCurrent(int channel) throws DeviceException {

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
    public void setVoltage(int channel, double voltage) throws DeviceException {

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
    public void setCurrent(int channel, double current) throws DeviceException, IOException {

    }

    @Override
    public void turnOn(int channel) throws DeviceException, IOException {

    }

    @Override
    public void turnOff(int channel) throws DeviceException, IOException {

    }

    @Override
    public boolean isOn(int channel) throws DeviceException, IOException {
        return true;
    }

    @Override
    public void setSource(int channel, Source source) throws DeviceException, IOException {

    }

    @Override
    public Source getSource(int channel) throws DeviceException, IOException {
        return null;
    }

    @Override
    public void setBias(int channel, double level) throws DeviceException, IOException {

    }

    @Override
    public double getSourceValue(int channel) throws DeviceException, IOException {
        return 0;
    }

    @Override
    public double getMeasureValue(int channel) throws DeviceException, IOException {
        return 0;
    }

    @Override
    public int getNumChannels() {
        return 2;
    }

    @Override
    public void setFourProbeEnabled(int channel, boolean fourProbes) throws DeviceException, IOException {

    }

    @Override
    public boolean isFourProbeEnabled(int channel) throws DeviceException, IOException {
        return false;
    }

    @Override
    public void setAverageMode(int channel, AMode mode) throws DeviceException, IOException {

    }

    @Override
    public void setAverageCount(int channel, int count) throws DeviceException, IOException {

    }

    @Override
    public int getAverageCount(int channel) throws DeviceException, IOException {
        return 0;
    }

    @Override
    public AMode getAverageMode(int channel) throws DeviceException, IOException {
        return null;
    }

    @Override
    public void setSourceRange(int channel, double value) throws DeviceException, IOException {

    }

    @Override
    public double getSourceRange(int channel) throws DeviceException, IOException {
        return 0;
    }

    @Override
    public void useAutoSourceRange(int channel) throws DeviceException, IOException {

    }

    @Override
    public boolean isAutoRangingSource(int channel) throws DeviceException, IOException {
        return false;
    }

    @Override
    public void setMeasureFunction(int channel, Function function) throws IOException {

    }

    @Override
    public void setMeasureRange(int channel, double value) throws DeviceException, IOException {

    }

    @Override
    public double getMeasureRange(int channel) throws DeviceException, IOException {
        return 0;
    }

    @Override
    public void useAutoMeasureRange(int channel) throws DeviceException, IOException {

    }

    @Override
    public boolean isAutoRangingMeasure(int channel) throws DeviceException, IOException {
        return false;
    }

    @Override
    public void setVoltageRange(int channel, double value) throws DeviceException, IOException {

    }

    @Override
    public double getVoltageRange(int channel) throws DeviceException, IOException {
        return 0;
    }

    @Override
    public void useAutoVoltageRange(int channel) throws DeviceException, IOException {

    }

    @Override
    public boolean isAutoRangingVoltage(int channel) throws DeviceException, IOException {
        return false;
    }

    @Override
    public void setCurrentRange(int channel, double value) throws DeviceException, IOException {

    }

    @Override
    public double getCurrentRange(int channel) throws DeviceException, IOException {
        return 0;
    }

    @Override
    public void useAutoCurrentRange(int channel) throws DeviceException, IOException {

    }

    @Override
    public boolean isAutoRangingCurrent(int channel) throws DeviceException, IOException {
        return false;
    }

    @Override
    public void setOutputLimit(int channel, double value) throws DeviceException, IOException {

    }

    @Override
    public double getOutputLimit(int channel) throws DeviceException, IOException {
        return 0;
    }

    @Override
    public void setVoltageLimit(int channel, double value) throws DeviceException, IOException {

    }

    @Override
    public double getVoltageLimit(int channel) throws DeviceException, IOException {
        return 0;
    }

    @Override
    public void setCurrentLimit(int channel, double value) throws DeviceException, IOException {

    }

    @Override
    public double getCurrentLimit(int channel) throws DeviceException, IOException {
        return 0;
    }

    @Override
    public void setIntegrationTime(int channel, double time) throws DeviceException, IOException {

    }

    @Override
    public double getIntegrationTime(int channel) throws DeviceException, IOException {
        return 0;
    }

    @Override
    public TType getTerminalType(int channel, Terminals terminals) throws DeviceException, IOException {
        return null;
    }

    @Override
    public void setTerminals(int channel, Terminals terminals) throws DeviceException, IOException {

    }

    @Override
    public void setProbeMode(int channel, Function funcType, boolean enableSense) throws DeviceException, IOException {
        throw new DeviceException("Need to implement");
    }

    @Override
    public Terminals getTerminals(int channel) throws DeviceException, IOException {
        return null;
    }

    @Override
    public void setOffMode(int channel, OffMode mode) throws DeviceException, IOException {

    }

    @Override
    public OffMode getOffMode(int channel) throws DeviceException, IOException {
        return null;
    }

    @Override
    public boolean isLineFilterEnabled(int channel) throws DeviceException, IOException {
        return false;
    }

    @Override
    public void setLineFilterEnabled(int channel, boolean enabled) throws DeviceException, IOException {

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
    public String getChannelName() {
        return "Test FET SMU";
    }

    @Override
    public void setMeasureFunction(Function func) throws IOException
    {

    }
}
