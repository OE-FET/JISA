package jisa.devices.smu;

import jisa.addresses.Address;
import jisa.control.*;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.SMU;
import jisa.devices.interfaces.SubInstrument;
import jisa.enums.AMode;
import jisa.enums.Source;
import jisa.enums.TType;
import jisa.enums.Terminals;
import jisa.visa.VISADevice;
import jisa.visa.drivers.TCPIPDriver;

import java.io.IOException;

public abstract class KeithleyTSP extends VISADevice {

    private static final String   C_QUERY_VOLT               = "print(%s.measure.v())";
    private static final String   C_QUERY_CURR               = "print(%s.measure.i())";
    private static final String   C_QUERY_FUNC               = "print(%s.source.func)";
    private static final String   C_QUERY_OUTPUT             = "print(%s.source.output)";
    private static final String   C_QUERY_SENSE              = "print(%s.sense)";
    private static final String   C_SET_SOURCE               = "%s.source.func = %s";
    private static final String   C_SET_VOLT                 = "%s.source.levelv = %e";
    private static final String   C_SET_CURR                 = "%s.source.leveli = %e";
    private static final String   C_QUERY_SET_VOLT           = "print(%s.source.levelv)";
    private static final String   C_QUERY_SET_CURR           = "print(%s.source.leveli)";
    private static final String   C_SET_OUTPUT               = "%s.source.output = %s";
    private static final String   C_SET_SENSE                = "%s.sense = %s";
    private static final String   C_SET_AVG_COUNT            = "%s.measure.filter.count = %d";
    private static final String   C_QUERY_AVG_COUNT          = "print(%s.measure.filter.count)";
    private static final String   C_SET_AVG_MODE             = "%s.measure.filter.type = %s";
    private static final String   C_QUERY_AVG_MODE           = "print(%s.measure.filter.type)";
    private static final String   C_SET_AVG_STATE            = "%s.measure.filter.enable = %s";
    private static final String   C_QUERY_AVG_STATE          = "print(%s.measure.filter.enable)";
    private static final String   C_SET_LIMIT                = "%s.source.limit%s = %e";
    private static final String   C_QUERY_LIMIT              = "print(%s.source.limit%s)";
    private static final String   C_SET_SOURCE_RANGE         = "%s.source.range%s = %e";
    private static final String   C_QUERY_SOURCE_RANGE       = "print(%s.source.range%s)";
    private static final String   C_SET_MEASURE_RANGE        = "%s.measure.range%s = %e";
    private static final String   C_QUERY_MEASURE_RANGE      = "print(%s.measure.range%s)";
    private static final String   C_SET_SOURCE_AUTO_RANGE    = "%s.source.autorange%s = %s";
    private static final String   C_QUERY_SOURCE_AUTO_RANGE  = "print(%s.source.autorange%s)";
    private static final String   C_SET_MEASURE_AUTO_RANGE   = "%s.measure.autorange%s = %s";
    private static final String   C_QUERY_MEASURE_AUTO_RANGE = "print(%s.measure.autorange%s)";
    private static final String   C_SET_NPLC                 = "%s.measure.nplc = %f";
    private static final String   C_QUERY_NPLC               = "print(%s.measure.nplc)";
    private static final String   C_QUERY_LFR                = "print(localnode.linefreq)";
    private static final String   C_SET_OFF_MODE             = "%s.source.offmode = %d";
    private static final String   C_QUERY_OFF_MODE           = "print(%s.source.offmode)";
    private static final String   C_SET_OFF_FUNC             = "%s.source.offfunc = %d";
    private static final String   C_QUERY_OFF_FUNC           = "print(%s.source.offfunc)";
    private static final String   C_SET_OFF_LIMIT            = "%s.source.offlimit%s = %e";
    private static final String   C_QUERY_OFF_LIMIT          = "print(%s.source.offlimit%s)";
    private static final String   SENSE_LOCAL                = "0";
    private static final String   SENSE_REMOTE               = "1";
    private static final String   OUTPUT_ON                  = "1";
    private static final String   OUTPUT_OFF                 = "0";
    private static final String   FILTER_MOVING_MEAN         = "0";
    private static final String   FILTER_REPEAT_MEAN         = "1";
    private static final String   FILTER_MOVING_MEDIAN       = "2";
    private static final String   VOLTAGE                    = "v";
    private static final String   CURRENT                    = "i";
    private static final int      OFF_MODE_NORMAL            = 0;
    private static final int      OFF_MODE_ZERO              = 1;
    private static final int      OFF_MODE_HIGH_Z            = 2;
    private static final int      OFF_SOURCE_CURR            = 0;
    private static final int      OFF_SOURCE_VOLT            = 1;
    private final        double   LINE_FREQUENCY;

    public KeithleyTSP(Address address, String model) throws IOException, DeviceException {

        // Connect and set-up terminators
        super(address, TCPIPDriver.class);

        configSerial(con -> con.setSerialParameters(9600, 8));
        configGPIB(con -> con.setEOIEnabled(true));
        configTCPIP(con -> con.setKeepAliveEnabled(true));

        setReadTerminator("\n");
        setWriteTerminator("\n");
        addAutoRemove("\n", "\r");

        // Check that this is a Keithley 2600B series
        String idn = getIDN().toUpperCase();
        if (!(idn.contains("KEITHLEY") && idn.contains(model.toUpperCase()))) {
            throw new DeviceException(
                "The instrument at address \"%s\" is not compatible with the %s driver",
                address.toString(),
                getClass().getSimpleName()
            );
        }

        // Store the power-line frequency
        LINE_FREQUENCY = queryDouble(C_QUERY_LFR);

    }

    public boolean errorsEmpty() throws IOException {
        return ((int) queryDouble("print(errorqueue.count)")) == 0;
    }

    public int getNumErrors() throws IOException {
        return (int) queryDouble("print(errorqueue.count)");
    }

    public String getNextError() throws IOException {
        return query("print(errorqueue.next())");
    }

    public void clearErrorQueue() throws IOException {
        write("errorqueue.clear()");
    }

    /**
     * Subclass to represent each channel of the K2600B SMU
     */
    public class KSMU implements SMU, SubInstrument<KeithleyTSP> {

        private final String     channel;
        private       AMode      avMode        = AMode.NONE;
        private       int        avCount       = 1;
        private       ReadFilter voltageFilter = new BypassFilter(this::measureVoltage, c -> { });
        private       ReadFilter currentFilter = new BypassFilter(this::measureCurrent, c -> { });

        public KSMU(String channel) { this.channel = channel; }

        @Override
        public double getSetCurrent() throws DeviceException, IOException {
            return queryDouble(C_QUERY_SET_CURR, channel);
        }

        @Override
        public double getSetVoltage() throws DeviceException, IOException {
            return queryDouble(C_QUERY_SET_VOLT, channel);
        }

        @Override
        public String getIDN() throws IOException, DeviceException {
            return KeithleyTSP.this.getIDN();
        }

        @Override
        public String getName() {
            return channel.toUpperCase().replace("SMU", "SMU ");
        }

        @Override
        public Address getAddress() {
            return KeithleyTSP.this.getAddress();
        }

        protected double measureVoltage() throws IOException {
            return queryDouble(C_QUERY_VOLT, channel);
        }

        protected double measureCurrent() throws IOException {
            return queryDouble(C_QUERY_CURR, channel);
        }

        @Override
        public double getVoltage() throws DeviceException, IOException {
            return voltageFilter.getValue();
        }

        @Override
        public void setVoltage(double voltage) throws DeviceException, IOException {
            write(C_SET_VOLT, channel, voltage);
            setSource(Source.VOLTAGE);
        }

        @Override
        public double getCurrent() throws DeviceException, IOException {
            return currentFilter.getValue();
        }

        @Override
        public void setCurrent(double current) throws DeviceException, IOException {
            write(C_SET_CURR, channel, current);
            setSource(Source.CURRENT);
        }

        @Override
        public void turnOn() throws DeviceException, IOException {
            write(C_SET_OUTPUT, channel, OUTPUT_ON);
        }

        @Override
        public void turnOff() throws DeviceException, IOException {
            write(C_SET_OUTPUT, channel, OUTPUT_OFF);
        }

        @Override
        public boolean isOn() throws DeviceException, IOException {
            return queryInt(C_QUERY_OUTPUT, channel) == 1;
        }

        @Override
        public Source getSource() throws DeviceException, IOException {

            switch (queryInt(C_QUERY_FUNC, channel)) {

                case 0:
                    return Source.CURRENT;

                case 1:
                    return Source.VOLTAGE;

                default:
                    throw new IOException("Invalid response from SMU when querying source function.");

            }

        }

        public Source getMeasured() throws DeviceException, IOException {

            switch (getSource()) {

                case CURRENT:
                    return Source.VOLTAGE;

                case VOLTAGE:
                    return Source.CURRENT;

                default:
                    throw new IOException("Invalid response from SMU when querying source function.");

            }

        }

        @Override
        public void setSource(Source source) throws DeviceException, IOException {

            switch (source) {

                case CURRENT:
                    write(C_SET_SOURCE, channel, "0");
                    break;

                case VOLTAGE:
                    write(C_SET_SOURCE, channel, "1");
                    break;

                default:
                    throw new DeviceException("Unrecognised source function.");

            }

        }

        @Override
        public void setSourceValue(double level) throws DeviceException, IOException {

            switch (getSource()) {

                case CURRENT:
                    setCurrent(level);
                    break;

                case VOLTAGE:
                    setVoltage(level);
                    break;

            }

        }

        @Override
        public double getSourceValue() throws DeviceException, IOException {

            switch (getSource()) {

                case CURRENT:
                    return getCurrent();

                case VOLTAGE:
                    return getVoltage();

                default:
                    throw new IOException("Unknown function state.");

            }

        }

        @Override
        public double getMeasureValue() throws DeviceException, IOException {

            switch (getMeasured()) {

                case CURRENT:
                    return getCurrent();

                case VOLTAGE:
                    return getVoltage();

                default:
                    throw new IOException("Unknown function state.");

            }

        }

        @Override
        public boolean isFourProbeEnabled() throws DeviceException, IOException {
            return query(C_QUERY_SENSE, channel).trim().equals(SENSE_REMOTE);
        }

        @Override
        public void setFourProbeEnabled(boolean fourProbes) throws DeviceException, IOException {
            write(C_SET_SENSE, channel, fourProbes ? SENSE_REMOTE : SENSE_LOCAL);
        }

        @Override
        public AMode getAverageMode() throws DeviceException, IOException {
            return avMode;
        }

        protected void disableAveraging(int count) throws IOException {
            write(C_SET_AVG_COUNT, channel, 1);
            write(C_SET_AVG_MODE, channel, FILTER_REPEAT_MEAN);
            write(C_SET_AVG_STATE, channel, OUTPUT_OFF);
        }

        protected void resetFilters() throws IOException, DeviceException {

            voltageFilter.setCount(getAverageCount());
            currentFilter.setCount(getAverageCount());

            voltageFilter.setUp();
            currentFilter.setUp();

            voltageFilter.clear();
            currentFilter.clear();

        }

        @Override
        public void setAverageMode(AMode mode) throws DeviceException, IOException {

            switch (mode) {

                case NONE:
                    voltageFilter = new BypassFilter(this::measureVoltage, this::disableAveraging);
                    currentFilter = new BypassFilter(this::measureCurrent, this::disableAveraging);
                    break;

                case MEAN_REPEAT:
                    voltageFilter = new MeanRepeatFilter(this::measureVoltage, this::disableAveraging);
                    currentFilter = new MeanRepeatFilter(this::measureCurrent, this::disableAveraging);
                    break;

                case MEAN_MOVING:
                    voltageFilter = new MeanMovingFilter(this::measureVoltage, this::disableAveraging);
                    currentFilter = new MeanMovingFilter(this::measureCurrent, this::disableAveraging);
                    break;

                case MEDIAN_REPEAT:
                    voltageFilter = new MedianRepeatFilter(this::measureVoltage, this::disableAveraging);
                    currentFilter = new MedianRepeatFilter(this::measureCurrent, this::disableAveraging);
                    break;

                case MEDIAN_MOVING:
                    voltageFilter = new MedianMovingFilter(this::measureVoltage, this::disableAveraging);
                    currentFilter = new MedianMovingFilter(this::measureCurrent, this::disableAveraging);
                    break;

            }

            resetFilters();

        }

        @Override
        public int getAverageCount() throws DeviceException, IOException {
            return avCount;
        }

        @Override
        public void setAverageCount(int count) throws DeviceException, IOException {
            this.avCount = count;
            resetFilters();
        }

        @Override
        public double getSourceRange() throws DeviceException, IOException {

            switch (getSource()) {

                case CURRENT:
                    return getCurrentRange();

                case VOLTAGE:
                    return getVoltageRange();

                default:
                    throw new IOException("Unknown source function");

            }

        }

        @Override
        public void setSourceRange(double value) throws DeviceException, IOException {

            switch (getSource()) {

                case CURRENT:
                    setCurrentRange(value);
                    break;

                case VOLTAGE:
                    setVoltageRange(value);
                    break;

                default:
                    throw new IOException("Unknown source function.");

            }

        }

        @Override
        public void useAutoSourceRange() throws DeviceException, IOException {

            switch (getSource()) {

                case CURRENT:
                    useAutoCurrentRange();
                    break;

                case VOLTAGE:
                    useAutoVoltageRange();
                    break;

                default:
                    throw new IOException("Unknown source function.");

            }

        }

        @Override
        public boolean isAutoRangingSource() throws DeviceException, IOException {

            switch (getSource()) {

                case CURRENT:
                    return isAutoRangingCurrent();

                case VOLTAGE:
                    return isAutoRangingVoltage();

                default:
                    throw new IOException("Unknown source function.");

            }

        }

        @Override
        public double getMeasureRange() throws DeviceException, IOException {

            switch (getMeasured()) {

                case CURRENT:
                    return getCurrentRange();

                case VOLTAGE:
                    return getVoltageRange();

                default:
                    throw new IOException("Unknown source function");

            }

        }

        @Override
        public void setMeasureRange(double value) throws DeviceException, IOException {

            switch (getMeasured()) {

                case CURRENT:
                    setCurrentRange(value);
                    break;

                case VOLTAGE:
                    setVoltageRange(value);
                    break;

                default:
                    throw new IOException("Unknown source function.");

            }

        }

        @Override
        public void useAutoMeasureRange() throws DeviceException, IOException {

            switch (getMeasured()) {

                case CURRENT:
                    useAutoCurrentRange();
                    break;

                case VOLTAGE:
                    useAutoVoltageRange();
                    break;

                default:
                    throw new IOException("Unknown source function.");

            }

        }

        @Override
        public boolean isAutoRangingMeasure() throws DeviceException, IOException {

            switch (getMeasured()) {

                case CURRENT:
                    return isAutoRangingCurrent();

                case VOLTAGE:
                    return isAutoRangingVoltage();

                default:
                    throw new IOException("Unknown source function.");

            }

        }

        @Override
        public double getVoltageRange() throws DeviceException, IOException {
            return queryDouble(C_QUERY_MEASURE_RANGE, channel, VOLTAGE);
        }

        @Override
        public void setVoltageRange(double value) throws DeviceException, IOException {
            write(C_SET_MEASURE_AUTO_RANGE, channel, VOLTAGE, OUTPUT_OFF);
            write(C_SET_SOURCE_AUTO_RANGE, channel, VOLTAGE, OUTPUT_OFF);
            write(C_SET_SOURCE_RANGE, channel, VOLTAGE, value);
            write(C_SET_MEASURE_RANGE, channel, VOLTAGE, value);
        }

        @Override
        public void useAutoVoltageRange() throws DeviceException, IOException {
            write(C_SET_MEASURE_AUTO_RANGE, channel, VOLTAGE, OUTPUT_ON);
            write(C_SET_SOURCE_AUTO_RANGE, channel, VOLTAGE, OUTPUT_ON);
        }

        @Override
        public boolean isAutoRangingVoltage() throws DeviceException, IOException {
            return query(C_QUERY_MEASURE_AUTO_RANGE, channel, VOLTAGE).trim().equals(OUTPUT_ON);
        }

        @Override
        public double getCurrentRange() throws DeviceException, IOException {
            return queryDouble(C_QUERY_MEASURE_RANGE, channel, CURRENT);
        }

        @Override
        public void setCurrentRange(double value) throws DeviceException, IOException {
            write(C_SET_MEASURE_AUTO_RANGE, channel, CURRENT, OUTPUT_OFF);
            write(C_SET_SOURCE_AUTO_RANGE, channel, CURRENT, OUTPUT_OFF);
            write(C_SET_SOURCE_RANGE, channel, CURRENT, value);
            write(C_SET_MEASURE_RANGE, channel, CURRENT, value);
        }

        @Override
        public void useAutoCurrentRange() throws DeviceException, IOException {
            write(C_SET_MEASURE_AUTO_RANGE, channel, CURRENT, OUTPUT_ON);
            write(C_SET_SOURCE_AUTO_RANGE, channel, CURRENT, OUTPUT_ON);
        }

        @Override
        public boolean isAutoRangingCurrent() throws DeviceException, IOException {
            return query(C_QUERY_MEASURE_AUTO_RANGE, channel, CURRENT).trim().equals(OUTPUT_ON);
        }

        @Override
        public double getOutputLimit() throws DeviceException, IOException {

            switch (getMeasured()) {

                case CURRENT:
                    return getCurrentLimit();

                case VOLTAGE:
                    return getVoltageLimit();

                default:
                    throw new IOException("Unrecognised source function.");

            }

        }

        @Override
        public void setOutputLimit(double value) throws DeviceException, IOException {

            switch (getMeasured()) {

                case CURRENT:
                    setCurrentLimit(value);

                case VOLTAGE:
                    setVoltageLimit(value);

                default:
                    throw new IOException("Unrecognised source function.");

            }

        }

        @Override
        public double getVoltageLimit() throws DeviceException, IOException {
            return queryDouble(C_QUERY_LIMIT, channel, VOLTAGE);
        }

        @Override
        public void setVoltageLimit(double voltage) throws DeviceException, IOException {
            write(C_SET_LIMIT, channel, VOLTAGE, voltage);
        }

        @Override
        public double getCurrentLimit() throws DeviceException, IOException {
            return queryDouble(C_QUERY_LIMIT, channel, CURRENT);
        }

        @Override
        public void setCurrentLimit(double current) throws DeviceException, IOException {
            write(C_SET_LIMIT, channel, CURRENT, current);
        }

        @Override
        public double getIntegrationTime() throws DeviceException, IOException {
            return queryDouble(C_QUERY_NPLC, channel) / LINE_FREQUENCY;
        }

        @Override
        public void setIntegrationTime(double time) throws DeviceException, IOException {
            write(C_SET_NPLC, channel, time * LINE_FREQUENCY);
        }

        @Override
        public TType getTerminalType(Terminals terminals) throws DeviceException, IOException {

            if (terminals == Terminals.REAR) {
                return TType.PHOENIX;
            } else {
                return TType.NONE;
            }

        }

        @Override
        public Terminals getTerminals() throws DeviceException, IOException {
            return Terminals.REAR;
        }

        @Override
        public void setTerminals(Terminals terminals) throws DeviceException, IOException {
            /* nothing to do */
        }

        @Override
        public OffMode getOffMode() throws DeviceException, IOException {

            switch (queryInt(C_QUERY_OFF_MODE, channel)) {

                case OFF_MODE_NORMAL:
                    return OffMode.NORMAL;

                case OFF_MODE_ZERO:
                    return OffMode.ZERO;

                case OFF_MODE_HIGH_Z:
                    return OffMode.HIGH_IMPEDANCE;

                default:
                    throw new IOException("Unrecognised response from Keithley 2600B");

            }

        }

        @Override
        public void setOffMode(OffMode mode) throws DeviceException, IOException {

            switch (mode) {

                case ZERO:
                    write(C_SET_OFF_MODE, channel, OFF_MODE_ZERO);
                    break;

                case HIGH_IMPEDANCE:
                    write(C_SET_OFF_MODE, channel, OFF_MODE_HIGH_Z);
                    break;

                default:
                case NORMAL:
                    write(C_SET_OFF_MODE, channel, OFF_MODE_NORMAL);
                    break;

            }

        }

        @Override
        public boolean isLineFilterEnabled() throws DeviceException, IOException {
            return false;
        }

        @Override
        public void setLineFilterEnabled(boolean enabled) throws DeviceException, IOException {
            /* no line filtering */
        }

        @Override
        public KeithleyTSP getParentInstrument() {
            return KeithleyTSP.this;
        }
    }

}
