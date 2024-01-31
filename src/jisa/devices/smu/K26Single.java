package jisa.devices.smu;

import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.IMeter;
import jisa.devices.interfaces.SMU;
import jisa.devices.interfaces.VMeter;
import jisa.enums.AMode;
import jisa.enums.Source;
import jisa.enums.TType;
import jisa.enums.Terminals;
import jisa.experiment.IVPoint;

import java.io.IOException;
import java.util.List;

public abstract class K26Single extends KeithleyTSP implements SMU {

    public final  KSMU      SMU_A    = new KSMU("smua");
    private final List<SMU> channels = List.of(SMU_A);

    public K26Single(Address address, String model) throws IOException, DeviceException {
        super(address, model);
    }

    @Override
    public List<SMU> getSMUChannels() {
        return channels;
    }

    public void waitForStableCurrent(double pctMargin, int interval, int duration) throws IOException, DeviceException, InterruptedException {
        SMU_A.waitForStableCurrent(pctMargin, interval, duration);
    }

    public double getVoltage(double integrationTime) throws DeviceException, IOException {
        return SMU_A.getVoltage(integrationTime);
    }

    public double getCurrent(double integrationTime) throws DeviceException, IOException {
        return SMU_A.getCurrent(integrationTime);
    }

    public double pulseVoltage(double pulseVoltage, double offTime, double measureDelay) throws DeviceException, IOException {
        return SMU_A.pulseVoltage(pulseVoltage, offTime, measureDelay);
    }

    public void setOn(boolean on) throws IOException, DeviceException {
        SMU_A.setOn(on);
    }

    public void setAveraging(AMode mode, int count) throws DeviceException, IOException {
        SMU_A.setAveraging(mode, count);
    }

    public IVPoint getIVPoint() throws DeviceException, IOException {
        return SMU_A.getIVPoint();
    }

    public VMeter asVoltmeter() throws IOException, DeviceException {
        return SMU_A.asVoltmeter();
    }

    public IMeter asAmmeter() throws IOException, DeviceException {
        return SMU_A.asAmmeter();
    }

    public void setRanges(double voltageRange, double currentRange) throws DeviceException, IOException {
        SMU_A.setRanges(voltageRange, currentRange);
    }

    public void useAutoRanges() throws DeviceException, IOException {
        SMU_A.useAutoRanges();
    }

    public void setLimits(double voltageLimit, double currentLimit) throws DeviceException, IOException {
        SMU_A.setLimits(voltageLimit, currentLimit);
    }

    public void waitForStableVoltage(double pctMargin, int duration) throws IOException, DeviceException, InterruptedException {
        SMU_A.waitForStableVoltage(pctMargin, duration);
    }

    public double getSetCurrent() throws DeviceException, IOException {
        return SMU_A.getSetCurrent();
    }

    public double getSetVoltage() throws DeviceException, IOException {
        return SMU_A.getSetVoltage();
    }

    public double measureVoltage() throws IOException {
        return SMU_A.measureVoltage();
    }

    public double measureCurrent() throws IOException {
        return SMU_A.measureCurrent();
    }

    public double getVoltage() throws DeviceException, IOException {
        return SMU_A.getVoltage();
    }

    public void setVoltage(double voltage) throws DeviceException, IOException {
        SMU_A.setVoltage(voltage);
    }

    public double getCurrent() throws DeviceException, IOException {
        return SMU_A.getCurrent();
    }

    public void setCurrent(double current) throws DeviceException, IOException {
        SMU_A.setCurrent(current);
    }

    public void turnOn() throws DeviceException, IOException {
        SMU_A.turnOn();
    }

    public void turnOff() throws DeviceException, IOException {
        SMU_A.turnOff();
    }

    public boolean isOn() throws DeviceException, IOException {
        return SMU_A.isOn();
    }

    public Source getSource() throws DeviceException, IOException {
        return SMU_A.getSource();
    }

    public Source getMeasured() throws DeviceException, IOException {
        return SMU_A.getMeasured();
    }

    public void setSource(Source source) throws DeviceException, IOException {
        SMU_A.setSource(source);
    }

    public void setSourceValue(double level) throws DeviceException, IOException {
        SMU_A.setSourceValue(level);
    }

    public double getSourceValue() throws DeviceException, IOException {
        return SMU_A.getSourceValue();
    }

    public double getMeasureValue() throws DeviceException, IOException {
        return SMU_A.getMeasureValue();
    }

    public boolean isFourProbeEnabled() throws DeviceException, IOException {
        return SMU_A.isFourProbeEnabled();
    }

    public void setFourProbeEnabled(boolean fourProbes) throws DeviceException, IOException {
        SMU_A.setFourProbeEnabled(fourProbes);
    }

    public AMode getAverageMode() throws DeviceException, IOException {
        return SMU_A.getAverageMode();
    }

    public void disableAveraging(int count) throws IOException {
        SMU_A.disableAveraging(count);
    }

    public void resetFilters() throws IOException, DeviceException {
        SMU_A.resetFilters();
    }

    public void setAverageMode(AMode mode) throws DeviceException, IOException {
        SMU_A.setAverageMode(mode);
    }

    public int getAverageCount() throws DeviceException, IOException {
        return SMU_A.getAverageCount();
    }

    public void setAverageCount(int count) throws DeviceException, IOException {
        SMU_A.setAverageCount(count);
    }

    public double getSourceRange() throws DeviceException, IOException {
        return SMU_A.getSourceRange();
    }

    public void setSourceRange(double value) throws DeviceException, IOException {
        SMU_A.setSourceRange(value);
    }

    public void useAutoSourceRange() throws DeviceException, IOException {
        SMU_A.useAutoSourceRange();
    }

    public boolean isAutoRangingSource() throws DeviceException, IOException {
        return SMU_A.isAutoRangingSource();
    }

    public double getMeasureRange() throws DeviceException, IOException {
        return SMU_A.getMeasureRange();
    }

    public void setMeasureRange(double value) throws DeviceException, IOException {
        SMU_A.setMeasureRange(value);
    }

    public void useAutoMeasureRange() throws DeviceException, IOException {
        SMU_A.useAutoMeasureRange();
    }

    public boolean isAutoRangingMeasure() throws DeviceException, IOException {
        return SMU_A.isAutoRangingMeasure();
    }

    public double getVoltageRange() throws DeviceException, IOException {
        return SMU_A.getVoltageRange();
    }

    public void setVoltageRange(double value) throws DeviceException, IOException {
        SMU_A.setVoltageRange(value);
    }

    public void useAutoVoltageRange() throws DeviceException, IOException {
        SMU_A.useAutoVoltageRange();
    }

    public boolean isAutoRangingVoltage() throws DeviceException, IOException {
        return SMU_A.isAutoRangingVoltage();
    }

    public double getCurrentRange() throws DeviceException, IOException {
        return SMU_A.getCurrentRange();
    }

    public void setCurrentRange(double value) throws DeviceException, IOException {
        SMU_A.setCurrentRange(value);
    }

    public void useAutoCurrentRange() throws DeviceException, IOException {
        SMU_A.useAutoCurrentRange();
    }

    public boolean isAutoRangingCurrent() throws DeviceException, IOException {
        return SMU_A.isAutoRangingCurrent();
    }

    public double getOutputLimit() throws DeviceException, IOException {
        return SMU_A.getOutputLimit();
    }

    public void setOutputLimit(double value) throws DeviceException, IOException {
        SMU_A.setOutputLimit(value);
    }

    public double getVoltageLimit() throws DeviceException, IOException {
        return SMU_A.getVoltageLimit();
    }

    public void setVoltageLimit(double voltage) throws DeviceException, IOException {
        SMU_A.setVoltageLimit(voltage);
    }

    public double getCurrentLimit() throws DeviceException, IOException {
        return SMU_A.getCurrentLimit();
    }

    public void setCurrentLimit(double current) throws DeviceException, IOException {
        SMU_A.setCurrentLimit(current);
    }

    public double getIntegrationTime() throws DeviceException, IOException {
        return SMU_A.getIntegrationTime();
    }

    public void setIntegrationTime(double time) throws DeviceException, IOException {
        SMU_A.setIntegrationTime(time);
    }

    public TType getTerminalType(Terminals terminals) throws DeviceException, IOException {
        return SMU_A.getTerminalType(terminals);
    }

    public Terminals getTerminals() throws DeviceException, IOException {
        return SMU_A.getTerminals();
    }

    public void setTerminals(Terminals terminals) throws DeviceException, IOException {
        SMU_A.setTerminals(terminals);
    }

    public SMU.OffMode getOffMode() throws DeviceException, IOException {
        return SMU_A.getOffMode();
    }

    public void setOffMode(SMU.OffMode mode) throws DeviceException, IOException {
        SMU_A.setOffMode(mode);
    }

    public boolean isLineFilterEnabled() throws DeviceException, IOException {
        return SMU_A.isLineFilterEnabled();
    }

    public void setLineFilterEnabled(boolean enabled) throws DeviceException, IOException {
        SMU_A.setLineFilterEnabled(enabled);
    }


}
