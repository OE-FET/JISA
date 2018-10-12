package JISA.Devices;

import JISA.Addresses.InstrumentAddress;

import java.io.IOException;
import java.util.HashMap;

public class K2600B extends MCSMU {

    private static final String[] CHANNELS       = {"smua", "smub"};
    private static final String   C_QUERY_VOLT   = "print(%s.measure.v())";
    private static final String   C_QUERY_CURR   = "print(%s.measure.i())";
    private static final String   C_QUERY_FUNC   = "print(%s.source.func)";
    private static final String   C_QUERY_OUTPUT = "print(%s.source.output)";
    private static final String   C_QUERY_SENSE  = "print(%s.sense)";
    private static final String   C_SET_SOURCE   = "%s.source.func = %s";
    private static final String   C_SET_VOLT     = "%s.source.levelv = %f";
    private static final String   C_SET_CURR     = "%s.source.leveli = %f";
    private static final String   C_SET_OUTPUT   = "%s.source.output = %s";
    private static final String   C_SET_SENSE    = "%s.sense = %s";
    private static final String   SENSE_LOCAL    = "0";
    private static final String   SENSE_REMOTE   = "1";
    private static final String   OUTPUT_ON      = "1";
    private static final String   OUTPUT_OFF     = "0";

    public K2600B(InstrumentAddress address) throws IOException, DeviceException {

        super(address);

        // TODO: Check that this IDN check actually works
        try {
            String[] idn = getIDN().split(", ");
            if (!idn[1].trim().substring(0 ,8).equals("Model 26")) {
                throw new DeviceException("The instrument at address %s is not a Keithley 2600 series!", address.getVISAAddress());
            }
        } catch (IOException e) {
            throw new DeviceException("The instrument at address %s is not responding!", address.getVISAAddress());
        }

    }

    @Override
    public double getVoltage(int channel) throws DeviceException, IOException {

        if (channel >= getNumChannels() || channel < 0) {
            throw new DeviceException("Channel does not exist!");
        }

        return queryDouble(C_QUERY_VOLT, CHANNELS[channel]);

    }

    @Override
    public double getCurrent(int channel) throws DeviceException, IOException {

        if (channel >= getNumChannels() || channel < 0) {
            throw new DeviceException("Channel does not exist!");
        }

        return queryDouble(C_QUERY_CURR, CHANNELS[channel]);

    }

    @Override
    public void setVoltage(int channel, double voltage) throws DeviceException, IOException {

        if (channel >= getNumChannels() || channel < 0) {
            throw new DeviceException("Channel does not exist!");
        }

        write(C_SET_VOLT, voltage);
        setSource(channel, Source.VOLTAGE);

    }

    @Override
    public void setCurrent(int channel, double current) throws DeviceException, IOException {

        if (channel >= getNumChannels() || channel < 0) {
            throw new DeviceException("Channel does not exist!");
        }

        write(C_SET_CURR, current);
        setSource(channel, Source.CURRENT);

    }

    @Override
    public void turnOn(int channel) throws DeviceException, IOException {

        if (channel >= getNumChannels() || channel < 0) {
            throw new DeviceException("Channel does not exist!");
        }

        write(C_SET_OUTPUT, CHANNELS[channel], OUTPUT_ON);

    }

    @Override
    public void turnOff(int channel) throws DeviceException, IOException {

        if (channel >= getNumChannels() || channel < 0) {
            throw new DeviceException("Channel does not exist!");
        }

        write(C_SET_OUTPUT, CHANNELS[channel], OUTPUT_OFF);

    }

    @Override
    public boolean isOn(int channel) throws DeviceException, IOException {

        if (channel >= getNumChannels() || channel < 0) {
            throw new DeviceException("Channel does not exist!");
        }

        return query(C_QUERY_OUTPUT, CHANNELS[channel]).trim().equals(OUTPUT_ON);
    }

    @Override
    public void setSource(int channel, Source source) throws DeviceException, IOException {

        if (channel >= getNumChannels() || channel < 0) {
            throw new DeviceException("Channel does not exist!");
        }

        write(C_SET_SOURCE, CHANNELS[channel], SFunc.fromSMU(source).toString());

    }

    @Override
    public Source getSource(int channel) throws DeviceException, IOException {

        if (channel >= getNumChannels() || channel < 0) {
            throw new DeviceException("Channel does not exist!");
        }

        return SFunc.fromString(query(C_QUERY_FUNC, CHANNELS[channel])).toSMU();

    }

    @Override
    public void setBias(int channel, double level) throws DeviceException, IOException {

        if (channel >= getNumChannels() || channel < 0) {
            throw new DeviceException("Channel does not exist!");
        }

        switch (getSource(channel)) {

            case VOLTAGE:
                setVoltage(channel, level);
                break;

            case CURRENT:
                setCurrent(channel, level);
                break;

            default:
                setVoltage(channel, level);
                break;

        }

    }

    @Override
    public double getSourceValue(int channel) throws DeviceException, IOException {

        if (channel >= getNumChannels() || channel < 0) {
            throw new DeviceException("Channel does not exist!");
        }

        switch (getSource(channel)) {

            case VOLTAGE:
                return getVoltage(channel);

            case CURRENT:
                return getCurrent(channel);

            default:
                return getVoltage(channel);

        }

    }

    @Override
    public double getMeasureValue(int channel) throws DeviceException, IOException {

        if (channel >= getNumChannels() || channel < 0) {
            throw new DeviceException("Channel does not exist!");
        }

        switch (getSource(channel)) {

            case VOLTAGE:
                return getCurrent(channel);

            case CURRENT:
                return getVoltage(channel);

            default:
                return getCurrent(channel);

        }
    }

    @Override
    public int getNumChannels() {
        return 2;
    }

    @Override
    public void useFourProbe(int channel, boolean fourProbes) throws DeviceException, IOException {

        if (channel >= getNumChannels() || channel < 0) {
            throw new DeviceException("Channel does not exist!");
        }

        write(C_SET_SENSE, CHANNELS[channel], fourProbes ? SENSE_REMOTE : SENSE_LOCAL);

    }

    @Override
    public boolean isUsingFourProbe(int channel) throws DeviceException, IOException {

        if (channel >= getNumChannels() || channel < 0) {
            throw new DeviceException("Channel does not exist!");
        }

        return query(C_QUERY_SENSE, CHANNELS[channel]).trim().equals(SENSE_REMOTE);

    }

    private enum SFunc {

        VOLTAGE("1", Source.VOLTAGE),
        CURRENT("0", Source.CURRENT);

        private static HashMap<String, SFunc>     fMap = new HashMap<>();
        private static HashMap<SMU.Source, SFunc> sMap = new HashMap<>();

        static {
            for (SFunc f : values()) {
                fMap.put(f.toString(), f);
                sMap.put(f.toSMU(), f);
            }
        }

        private String     tag;
        private SMU.Source smu;

        public static SFunc fromString(String tag) {
            return fMap.getOrDefault(tag, null);
        }

        public static SFunc fromSMU(SMU.Source s) {
            return sMap.getOrDefault(s, null);
        }

        SFunc(String tag, SMU.Source smu) {
            this.tag = tag;
            this.smu = smu;
        }

        public String toString() {
            return tag;
        }

        public SMU.Source toSMU() {
            return smu;
        }

    }

}
