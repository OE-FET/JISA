package jisa.devices.smu;

import jisa.addresses.Address;
import jisa.control.*;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.*;
import jisa.enums.AMode;
import jisa.enums.Source;
import jisa.enums.TType;
import jisa.enums.Terminals;
import jisa.visa.VISADevice;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import static jisa.devices.interfaces.SMU.OffMode.HIGH_IMPEDANCE;
import static jisa.devices.interfaces.SMU.OffMode.ZERO;
import static jisa.enums.AMode.NONE;
import static jisa.enums.Source.CURRENT;
import static jisa.enums.Source.VOLTAGE;

public abstract class AgilentSPA extends VISADevice implements SPA {

    public static final String C_RESET = "*RST";
    public static final String C_FLEX  = "US";
    public static final String C_FMT   = "FMT 2";

    private double lastIntTime = 0.0;

    public AgilentSPA(Address address, boolean setFLEX) throws IOException, DeviceException {

        super(address);

        configGPIB(gpib -> gpib.setEOIEnabled(true));

        clearBuffers();
        manuallyClearReadBuffer();

        setWriteTerminator("\n");
        setReadTerminator("\n");
        addAutoRemove("\n", "\r");

        write(C_RESET);
        
        if (setFLEX) {
            write(C_FLEX);
        }

        boolean  match       = false;
        int      attempts    = 0;
        String[] terminators = {"\n", "\r", "\r\n"};

        for (String term : terminators) {

            try {
                setWriteTerminator(term);
                setReadTerminator(term);
                clearBuffers();
                manuallyClearReadBuffer();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (confirmIdentity()) {
                match = true;
                break;
            }

        }

        if (!match) {
            throw new DeviceException("Instrument at \"%s\" is not correctly identifying as an Agilent 415XX/B1500 series SPA.", address.toString());
        }

        // For some reason, the SPA sometimes forgets that this happened, so need to do it again?
        if (setFLEX) {
            write(C_FLEX);
        }

        write(C_FMT);

    }

    protected abstract boolean confirmIdentity();

    public class ASMU implements SMU, SubInstrument<AgilentSPA> {

        private final String     name;
        private final int        channel;
        private final Range[]    vRanges;
        private final Range[]    iRanges;
        private       AMode      filterMode    = NONE;
        private       int        filterCount   = 1;
        private       Source     mode          = VOLTAGE;
        private       double     value         = 0.0;
        private       double     voltageComp   = 200.0;
        private       double     currentComp   = 100e-3;
        private       ReadFilter voltageFilter = makeVoltageFilter(NONE);
        private       ReadFilter currentFilter = makeCurrentFilter(NONE);
        private       boolean    state         = false;
        private       Range      voltageRange  = Range.AUTO_RANGING;
        private       Range      currentRange  = Range.AUTO_RANGING;
        private       double     intTime       = 0.02;
        private       OffMode    offMode       = OffMode.NORMAL;

        public ASMU(String name, int channel, Range[] vRanges, Range[] iRanges) {

            if (vRanges == null) {
                vRanges = new Range[0];
            }

            if (iRanges == null) {
                iRanges = new Range[0];
            }

            this.name    = name;
            this.channel = channel;
            this.vRanges = Arrays.stream(vRanges).sorted(Comparator.comparing(Range::getRange)).toArray(Range[]::new);
            this.iRanges = Arrays.stream(iRanges).sorted(Comparator.comparing(Range::getRange)).toArray(Range[]::new);

        }

        protected void updateIntTime() throws IOException {

            if (lastIntTime != intTime) {

                lastIntTime = intTime;
                int mode = lastIntTime <= 10.16e-3 ? 1 : 3;

                if (lastIntTime <= 10.16e-3) {
                    write("SIT 1,%e", lastIntTime);
                    write("SLI 1");
                } else if (lastIntTime >= 16.7e-3) {
                    write("SIT 3,%e", lastIntTime);
                    write("SLI 3");
                } else {
                    write("SIT 1,10.16E-3");
                    write("SIT 3,%e", 2.0 * lastIntTime - 10.16e-3);
                    write("SLI 2");                                   // Basically one power-line cycle
                }

                write("SIT %d,%e", mode, lastIntTime);
                write("SLI %d", mode);
            }

        }

        protected Range rangeFromVoltage(double voltage) {

            for (Range range : vRanges) {

                if (range.getRange() >= Math.abs(value)) {
                    return range;
                }

            }

            return Range.AUTO_RANGING;

        }

        protected Range rangeFromCurrent(double currentComp) {

            for (Range range : iRanges) {

                if (range.getRange() >= Math.abs(value)) {
                    return range;
                }

            }

            return Range.AUTO_RANGING;

        }

        protected ReadFilter makeVoltageFilter(AMode type) {

            switch (type) {

                case MEAN_REPEAT:
                    return new MeanRepeatFilter(this::measureVoltage, (c) -> {});

                case MEAN_MOVING:
                    return new MeanMovingFilter(this::measureVoltage, (c) -> {});

                case MEDIAN_REPEAT:
                    return new MedianRepeatFilter(this::measureVoltage, (c) -> {});

                case MEDIAN_MOVING:
                    return new MedianMovingFilter(this::measureVoltage, (c) -> {});

                default:
                case NONE:
                    return new BypassFilter(this::measureVoltage, (c) -> {});

            }

        }

        protected ReadFilter makeCurrentFilter(AMode type) {

            switch (type) {

                case MEAN_REPEAT:
                    return new MeanRepeatFilter(this::measureCurrent, (c) -> {});

                case MEAN_MOVING:
                    return new MeanMovingFilter(this::measureCurrent, (c) -> {});

                case MEDIAN_REPEAT:
                    return new MedianRepeatFilter(this::measureCurrent, (c) -> {});

                case MEDIAN_MOVING:
                    return new MedianMovingFilter(this::measureCurrent, (c) -> {});

                default:
                case NONE:
                    return new BypassFilter(this::measureCurrent, (c) -> {});

            }

        }

        @Override
        public double getSetCurrent() throws DeviceException, IOException {
            return queryDouble("DI? %d", channel);
        }

        @Override
        public double getSetVoltage() throws DeviceException, IOException {
            return queryDouble("DV? %d", channel);
        }

        @Override
        public String getIDN() throws IOException, DeviceException {
            return AgilentSPA.this.getIDN();
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void close() throws IOException, DeviceException {

        }

        @Override
        public AgilentSPA getParentInstrument() {
            return AgilentSPA.this;
        }

        @Override
        public Address getAddress() {
            return AgilentSPA.this.getAddress();
        }

        protected double measureVoltage() throws DeviceException, IOException {

            if (state) {
                return queryDouble("TV? %d,%d", channel, voltageRange.toInt());
            } else {
                return 0.0;
            }

        }

        @Override
        public double getVoltage() throws DeviceException, IOException {
            updateIntTime();
            return voltageFilter.getValue();
        }

        private double getCurrComp(double range) {
            return Math.min(currentComp, rangeFromVoltage(range).getCompliance());
        }

        @Override
        public void setVoltage(double voltage) throws DeviceException, IOException {

            mode  = VOLTAGE;
            value = voltage;

            if (state) {
                write("DV %d,%d,%e,%e", channel, voltageRange.toInt(), voltage, getCurrComp(voltage));
            }

        }

        protected double measureCurrent() throws DeviceException, IOException {

            if (state) {
                return queryDouble("TI? %d,%d", channel, currentRange.toInt());
            } else {
                return 0.0;
            }

        }

        @Override
        public double getCurrent() throws DeviceException, IOException {

            updateIntTime();
            return currentFilter.getValue();

        }

        private double getVoltComp(double range) {
            return Math.min(voltageComp, rangeFromCurrent(range).getCompliance());
        }

        @Override
        public void setCurrent(double current) throws DeviceException, IOException {

            mode  = CURRENT;
            value = current;

            if (state) {
                write("DV %d,%d,%e,%e", channel, currentRange.toInt(), current, getVoltComp(current));
            }

        }

        @Override
        public void turnOn() throws DeviceException, IOException {

            write("CN %d", channel);
            state = true;

            switch (mode) {

                case VOLTAGE:
                    setVoltage(value);
                    break;

                case CURRENT:
                    setCurrent(value);
                    break;

            }

        }

        @Override
        public void turnOff() throws DeviceException, IOException {

            switch (offMode) {

                case HIGH_IMPEDANCE:
                    write("CL %d", channel);
                    break;

                case GUARD:
                case ZERO:
                case NORMAL:
                    write("DZ %d", channel);
                    break;

            }

            state = false;

        }

        @Override
        public boolean isOn() throws DeviceException, IOException {
            return state;
        }

        @Override
        public Source getSource() throws DeviceException, IOException {
            return mode;
        }

        @Override
        public void setSource(Source source) throws DeviceException, IOException {

            turnOff();

            switch (source) {

                case VOLTAGE:
                    setVoltage(value);
                    break;

                case CURRENT:
                    setCurrent(value);
                    break;

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

        @Override
        public double getSourceValue() throws DeviceException, IOException {

            switch (getSource()) {

                default:
                case VOLTAGE:
                    return getVoltage();

                case CURRENT:
                    return getCurrent();

            }

        }

        @Override
        public double getMeasureValue() throws DeviceException, IOException {

            switch (getSource()) {

                default:
                case VOLTAGE:
                    return getCurrent();

                case CURRENT:
                    return getVoltage();

            }

        }

        @Override
        public boolean isFourProbeEnabled() throws DeviceException, IOException {
            return false;
        }

        @Override
        public void setFourProbeEnabled(boolean fourProbes) throws DeviceException, IOException {

        }

        @Override
        public AMode getAverageMode() throws DeviceException, IOException {
            return filterMode;
        }

        @Override
        public void setAverageMode(AMode mode) throws DeviceException, IOException {

            voltageFilter = makeVoltageFilter(mode);
            voltageFilter.setCount(filterCount);
            voltageFilter.setUp();
            voltageFilter.clear();

            currentFilter = makeCurrentFilter(mode);
            currentFilter.setCount(filterCount);
            currentFilter.setUp();
            currentFilter.clear();

            filterMode = mode;

        }

        @Override
        public int getAverageCount() throws DeviceException, IOException {
            return filterCount;
        }

        @Override
        public void setAverageCount(int count) throws DeviceException, IOException {

            filterCount = count;
            voltageFilter.setCount(filterCount);
            voltageFilter.setUp();
            voltageFilter.clear();

            currentFilter.setCount(filterCount);
            currentFilter.setUp();
            currentFilter.clear();

        }

        @Override
        public double getSourceRange() throws DeviceException, IOException {

            switch (getSource()) {

                case VOLTAGE:
                    return getVoltageRange();

                case CURRENT:
                    return getCurrentRange();

                default:
                    throw new IOException("Invalid source type reported");

            }

        }

        @Override
        public void setSourceRange(double value) throws DeviceException, IOException {

            switch (getSource()) {

                case VOLTAGE:
                    setVoltageRange(value);
                    break;

                case CURRENT:
                    setCurrentRange(value);
                    break;

            }

        }

        @Override
        public void useAutoSourceRange() throws DeviceException, IOException {

            switch (getSource()) {

                case VOLTAGE:
                    useAutoVoltageRange();
                    break;

                case CURRENT:
                    useAutoCurrentRange();
                    break;

            }

        }

        @Override
        public boolean isAutoRangingSource() throws DeviceException, IOException {

            switch (getSource()) {

                case CURRENT:
                    return isAutoRangingCurrent();

                default:
                case VOLTAGE:
                    return isAutoRangingVoltage();

            }

        }

        @Override
        public double getMeasureRange() throws DeviceException, IOException {

            switch (getSource()) {

                case CURRENT:
                    return getVoltageRange();

                default:
                case VOLTAGE:
                    return getCurrentRange();

            }

        }

        @Override
        public void setMeasureRange(double value) throws DeviceException, IOException {

            switch (getSource()) {

                case CURRENT:
                    setVoltageRange(value);
                    break;

                case VOLTAGE:
                    setCurrentRange(value);
                    break;

            }

        }

        @Override
        public void useAutoMeasureRange() throws DeviceException, IOException {

            switch (getSource()) {

                case CURRENT:
                    useAutoVoltageRange();
                    break;

                case VOLTAGE:
                    useAutoCurrentRange();
                    break;

            }

        }

        @Override
        public boolean isAutoRangingMeasure() throws DeviceException, IOException {

            switch (getSource()) {

                case CURRENT:
                    return isAutoRangingVoltage();

                default:
                case VOLTAGE:
                    return isAutoRangingCurrent();

            }

        }

        @Override
        public double getVoltageRange() throws DeviceException, IOException {
            return voltageRange.getRange();
        }

        @Override
        public void setVoltageRange(double value) throws DeviceException, IOException {
            voltageRange = rangeFromVoltage(value);
        }

        @Override
        public void useAutoVoltageRange() throws DeviceException, IOException {
            voltageRange = Range.AUTO_RANGING;
        }

        @Override
        public boolean isAutoRangingVoltage() throws DeviceException, IOException {
            return voltageRange == Range.AUTO_RANGING;
        }

        @Override
        public double getCurrentRange() throws DeviceException, IOException {
            return currentRange.getRange();
        }

        @Override
        public void setCurrentRange(double value) throws DeviceException, IOException {
            currentRange = rangeFromCurrent(value);
        }

        @Override
        public void useAutoCurrentRange() throws DeviceException, IOException {
            currentRange = Range.AUTO_RANGING;
        }

        @Override
        public boolean isAutoRangingCurrent() throws DeviceException, IOException {
            return currentRange == Range.AUTO_RANGING;
        }

        @Override
        public double getOutputLimit() throws DeviceException, IOException {

            switch (getSource()) {

                case CURRENT:
                    return getVoltageLimit();

                default:
                case VOLTAGE:
                    return getCurrentLimit();

            }

        }

        @Override
        public void setOutputLimit(double value) throws DeviceException, IOException {

            switch (getSource()) {

                case CURRENT:
                    setVoltageLimit(value);
                    break;

                case VOLTAGE:
                    setCurrentLimit(value);
                    break;

            }

        }

        @Override
        public double getVoltageLimit() throws DeviceException, IOException {
            return voltageComp;
        }

        @Override
        public void setVoltageLimit(double voltage) throws DeviceException, IOException {
            voltageComp = voltage;
        }

        @Override
        public double getCurrentLimit() throws DeviceException, IOException {
            return currentComp;
        }

        @Override
        public void setCurrentLimit(double current) throws DeviceException, IOException {
            currentComp = current;
        }

        @Override
        public double getIntegrationTime() throws DeviceException, IOException {
            return intTime;
        }

        @Override
        public void setIntegrationTime(double time) throws DeviceException, IOException {
            intTime = time;
        }

        @Override
        public TType getTerminalType(Terminals terminals) throws DeviceException, IOException {
            return terminals == Terminals.REAR ? TType.TRIAX : TType.NONE;
        }

        @Override
        public Terminals getTerminals() throws DeviceException, IOException {
            return Terminals.REAR;
        }

        @Override
        public void setTerminals(Terminals terminals) throws DeviceException, IOException {
            // Nothing to do
        }

        @Override
        public OffMode getOffMode() throws DeviceException, IOException {
            return offMode;
        }

        @Override
        public void setOffMode(OffMode mode) throws DeviceException, IOException {

            switch (mode) {

                case HIGH_IMPEDANCE:
                    offMode = HIGH_IMPEDANCE;
                    break;

                default:
                case GUARD:
                case ZERO:
                case NORMAL:
                    offMode = ZERO;
                    break;

            }

            if (!isOn()) {
                write("CN %d", channel);
                turnOff();
            }

        }

        @Override
        public boolean isLineFilterEnabled() throws DeviceException, IOException {
            return false;
        }

        @Override
        public void setLineFilterEnabled(boolean enabled) throws DeviceException, IOException {
            // No line filter
        }

    }

    public class AVMU implements VMeter, SubInstrument<AgilentSPA> {

        private final String     name;
        private final int        channel;
        private final Range[]    vRanges;
        private       AMode      filterMode    = NONE;
        private       int        filterCount   = 1;
        private       ReadFilter voltageFilter = makeVoltageFilter(NONE);
        private       boolean    state         = false;
        private       Range      voltageRange  = Range.AUTO_RANGING;
        private       double     intTime       = 0.02;

        public AVMU(String name, int channel, Range[] vRanges) throws IOException {
            this.name    = name;
            this.channel = channel;
            this.vRanges = vRanges;
            write("VM %d,1", channel);
        }

        protected ReadFilter makeVoltageFilter(AMode type) {

            switch (type) {

                case MEAN_REPEAT:
                    return new MeanRepeatFilter(this::measureVoltage, (c) -> {});

                case MEAN_MOVING:
                    return new MeanMovingFilter(this::measureVoltage, (c) -> {});

                case MEDIAN_REPEAT:
                    return new MedianRepeatFilter(this::measureVoltage, (c) -> {});

                case MEDIAN_MOVING:
                    return new MedianMovingFilter(this::measureVoltage, (c) -> {});

                default:
                case NONE:
                    return new BypassFilter(this::measureVoltage, (c) -> {});

            }

        }

        protected void updateIntTime() throws IOException {

            if (lastIntTime != intTime) {

                lastIntTime = intTime;
                int mode = lastIntTime <= 10.16e-3 ? 1 : 3;

                if (lastIntTime <= 10.16e-3) {
                    write("SIT 1,%e", lastIntTime);
                    write("SLI 1");
                } else if (lastIntTime >= 16.7e-3) {
                    write("SIT 3,%e", lastIntTime);
                    write("SLI 3");
                } else {
                    write("SIT 1,10.16E-3");
                    write("SIT 3,%e", 2.0 * lastIntTime - 10.16e-3);
                    write("SLI 2");                                   // Basically one power-line cycle
                }

                write("SIT %d,%e", mode, lastIntTime);
                write("SLI %d", mode);
            }

        }

        protected double measureVoltage() throws IOException {

            if (state) {
                return queryDouble("TV? %d,%d", channel, voltageRange.toInt());
            } else {
                return 0.0;
            }

        }

        protected Range rangeFromVoltage(double value) {

            for (Range range : vRanges) {

                if (range.getRange() >= Math.abs(value)) {
                    return range;
                }

            }

            return Range.AUTO_RANGING;

        }

        @Override
        public String getIDN() throws IOException, DeviceException {
            return AgilentSPA.this.getIDN();
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void close() throws IOException, DeviceException {

        }

        @Override
        public AgilentSPA getParentInstrument() {
            return AgilentSPA.this;
        }

        @Override
        public Address getAddress() {
            return AgilentSPA.this.getAddress();
        }

        @Override
        public double getVoltage() throws IOException, DeviceException {
            updateIntTime();
            return voltageFilter.getValue();
        }

        @Override
        public double getIntegrationTime() throws IOException, DeviceException {
            return intTime;
        }

        @Override
        public void setIntegrationTime(double time) throws IOException, DeviceException {
            intTime = time;
        }

        @Override
        public double getVoltageRange() throws IOException, DeviceException {
            return voltageRange.getRange();
        }

        @Override
        public void setVoltageRange(double range) throws IOException, DeviceException {
            voltageRange = rangeFromVoltage(range);
        }

        @Override
        public void useAutoVoltageRange() throws IOException, DeviceException {
            voltageRange = Range.AUTO_RANGING;
        }

        @Override
        public boolean isAutoRangingVoltage() throws IOException, DeviceException {
            return voltageRange == Range.AUTO_RANGING;
        }

        @Override
        public AMode getAverageMode() throws IOException, DeviceException {
            return filterMode;
        }


        @Override
        public void setAverageMode(AMode mode) throws DeviceException, IOException {

            filterMode = mode;

            voltageFilter = makeVoltageFilter(filterMode);
            voltageFilter.setCount(filterCount);
            voltageFilter.setUp();
            voltageFilter.clear();

        }

        @Override
        public int getAverageCount() throws IOException, DeviceException {
            return filterCount;
        }

        @Override
        public void setAverageCount(int count) throws IOException, DeviceException {

            filterCount = count;

            voltageFilter.setCount(filterCount);
            voltageFilter.setUp();
            voltageFilter.clear();

        }

        @Override
        public void turnOn() throws DeviceException, IOException {
            write("CN %d", channel);
            state = true;

        }

        @Override
        public void turnOff() throws IOException, DeviceException {
            write("CL %d", channel);
            state = false;
        }

        @Override
        public boolean isOn() throws IOException, DeviceException {
            return state;
        }

        @Override
        public TType getTerminalType(Terminals terminals) throws DeviceException, IOException {
            return terminals == Terminals.REAR ? TType.TRIAX : TType.NONE;
        }

        @Override
        public Terminals getTerminals() throws DeviceException, IOException {
            return Terminals.REAR;
        }

        @Override
        public void setTerminals(Terminals terminals) throws DeviceException, IOException {
            // Nothing to do
        }

    }

    public class AVSU implements VSource, SubInstrument<AgilentSPA> {

        private final String      name;
        private final int         channel;
        private       double      value   = 0.0;
        private       boolean     state   = false;
        private       SMU.OffMode offMode = SMU.OffMode.NORMAL;

        public AVSU(String name, int channel) {
            this.name    = name;
            this.channel = channel;
        }

        @Override
        public String getIDN() throws IOException, DeviceException {
            return AgilentSPA.this.getIDN();
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void close() throws IOException, DeviceException {

        }

        @Override
        public AgilentSPA getParentInstrument() {
            return AgilentSPA.this;
        }

        @Override
        public Address getAddress() {
            return AgilentSPA.this.getAddress();
        }

        @Override
        public void setVoltage(double voltage) throws DeviceException, IOException {

            value = voltage;

            if (state) {
                write("DV %d,0,%e", channel, voltage);
            }

        }

        @Override
        public double getVoltage() throws IOException, DeviceException {
            return isOn() ? value : 0.0;
        }

        @Override
        public void turnOn() throws IOException, DeviceException {
            state = true;
            write("CN %d", channel);
            setVoltage(value);
        }

        public void setOffMode(SMU.OffMode mode) throws DeviceException, IOException {

            switch (mode) {

                case HIGH_IMPEDANCE:
                    offMode = HIGH_IMPEDANCE;
                    break;

                default:
                case GUARD:
                case ZERO:
                case NORMAL:
                    offMode = ZERO;
                    break;

            }

            if (!isOn()) {
                write("CN %d", channel);
                turnOff();
            }

        }

        public SMU.OffMode getOffMode() {
            return offMode;
        }

        @Override
        public void turnOff() throws DeviceException, IOException {

            switch (offMode) {

                case HIGH_IMPEDANCE:
                    write("CL %d", channel);
                    break;

                case GUARD:
                case ZERO:
                case NORMAL:
                    write("DZ %d", channel);
                    break;

            }

            state = false;

        }

        @Override
        public boolean isOn() throws IOException, DeviceException {
            return state;
        }

    }

    public class GNDU implements Switch, SubInstrument<AgilentSPA> {

        private final String  name;
        private final int     channel;
        private       boolean state = false;

        public GNDU(String name, int channel) throws IOException, DeviceException {
            this.name    = name;
            this.channel = channel;
            turnOff();
        }

        @Override
        public String getIDN() throws IOException, DeviceException {
            return AgilentSPA.this.getIDN();
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void close() throws IOException, DeviceException {

        }

        @Override
        public AgilentSPA getParentInstrument() {
            return AgilentSPA.this;
        }

        @Override
        public Address getAddress() {
            return AgilentSPA.this.getAddress();
        }

        @Override
        public void turnOn() throws IOException, DeviceException {
            write("CN %d", channel);
            state = true;
        }

        @Override
        public void turnOff() throws IOException, DeviceException {
            write("CL %d", channel);
            state = false;
        }

        @Override
        public boolean isOn() throws IOException, DeviceException {
            return state;
        }

    }

    public interface Range {

        Range AUTO_RANGING = new Range() {

            @Override
            public int toInt() {
                return 0;
            }

            @Override
            public double getRange() {
                return -1;
            }

            @Override
            public double getCompliance() {
                return -1;
            }

        };

        int toInt();

        double getRange();

        double getCompliance();

    }

}
