package JISA.Devices;

import JISA.Addresses.Address;
import JISA.Control.*;
import JISA.Enums.AMode;
import JISA.Enums.TType;
import JISA.Enums.Terminals;
import JISA.VISA.Driver;
import JISA.VISA.VISADevice;

import java.io.IOException;

public class K2182 extends VISADevice implements VMeter {

    private ReadFilter filter      = new BypassFilter(this::measureVoltage, (c) -> disableAveraging());
    private AMode      filterMode  = AMode.NONE;
    private int        filterCount = 1;

    public K2182(Address address) throws IOException, DeviceException {
        this(address, null);
    }

    public K2182(Address address, Class<? extends Driver> prefDriver) throws IOException, DeviceException {

        super(address, prefDriver);

        String idn = getIDN();

        if (!idn.contains("MODEL 2182")) {
            throw new DeviceException("Instrument at \"%s\" is not a Keithley 2182!", address.toString());
        }

        resetFilter();

        write(":SENSE:FUNC \"VOLT\"");

    }

    private void disableAveraging() throws IOException {
        write(":SENSE:VOLT:DFILTER OFF");
    }

    private double measureVoltage() throws IOException {
        return queryDouble(":MEAS:VOLT?");
    }

    @Override
    public double getVoltage() throws IOException, DeviceException {
        return filter.getValue();
    }

    @Override
    public void setIntegrationTime(double time) throws IOException {
        write(":SENSE:VOLT:APERTURE %e", time);
    }

    @Override
    public double getIntegrationTime() throws IOException {
        return queryDouble(":SENSE:VOLT:APERTURE?");
    }

    @Override
    public void setVoltageRange(double range) throws IOException {
        write(":SENSE:VOLT:RANGE %e", range);
    }

    @Override
    public double getVoltageRange() throws IOException {
        return queryDouble(":SENSE:VOLT:RANGE?");
    }

    @Override
    public void useAutoVoltageRange() throws IOException {
        write(":SENSE:VOLT:RANGE:AUTO 1");
    }

    @Override
    public boolean isAutoRangingVoltage() throws IOException {
        return queryInt(":SENSE:VOLT:RANGE:AUTO?") == 1;
    }

    @Override
    public void setAverageMode(AMode mode) throws IOException, DeviceException {

        switch (mode) {

            case NONE:
                filter = new BypassFilter(this::measureVoltage, (c) -> disableAveraging());
                break;

            case MEAN_REPEAT:
                filter = new MeanRepeatFilter(this::measureVoltage, (c) -> disableAveraging());
                break;

            case MEAN_MOVING:
                filter = new MeanMovingFilter(this::measureVoltage, (c) -> disableAveraging());
                break;

            case MEDIAN_REPEAT:
                filter = new MedianRepeatFilter(this::measureVoltage, (c) -> disableAveraging());
                break;

            case MEDIAN_MOVING:
                filter = new MedianMovingFilter(this::measureVoltage, (c) -> disableAveraging());
                break;

        }

        filterMode = mode;

        resetFilter();

    }

    @Override
    public void setAverageCount(int count) throws IOException, DeviceException {
        filterCount = count;
        resetFilter();
    }

    private void resetFilter() throws IOException, DeviceException {

        filter.setCount(filterCount);
        filter.setUp();
        filter.clear();

    }

    @Override
    public AMode getAverageMode() {
        return filterMode;
    }

    @Override
    public int getAverageCount() {
        return filterCount;
    }

    @Override
    public void turnOn() {

    }

    @Override
    public void turnOff() {

    }

    @Override
    public boolean isOn() {
        return true;
    }

    @Override
    public TType getTerminalType(Terminals terminals) {

        if (terminals == Terminals.FRONT) {
            return TType.BANANA;
        } else {
            return TType.NONE;
        }

    }

    @Override
    public void setTerminals(Terminals terminals) {

    }

    @Override
    public Terminals getTerminals() {
        return Terminals.FRONT;
    }

}
