package jisa.devices.meter;

import jisa.addresses.Address;
import jisa.control.*;
import jisa.devices.DeviceException;
import jisa.devices.features.LineFilter;
import jisa.enums.AMode;
import jisa.enums.TType;
import jisa.enums.Terminals;
import jisa.visa.VISADevice;
import jisa.visa.drivers.Driver;

import java.io.IOException;

public class K2182 extends VISADevice implements VMeter, LineFilter {

    public static String getDescription() {
        return "Keithley 2182";
    }

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
    public boolean isLineFilterEnabled() throws IOException {

        switch (query(":SENSE:VOLT:LPASS?")) {

            case "1":
            case "ON":
                return true;

            case "0":
            case "OFF":
                return false;

            default:
                throw new IOException("Invalid response from Keithley 2182 Voltmeter.");

        }

    }

    @Override
    public void setLineFilterEnabled(boolean enabled) throws IOException {
        write(":SENSE:VOLT:LPASS %s", enabled ? "ON" : "OFF");
    }

    @Override
    public Terminals getTerminals() {
        return Terminals.FRONT;
    }

}
