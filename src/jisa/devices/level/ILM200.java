package jisa.devices.level;

import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.LevelMeter;
import jisa.devices.temperature.ITC503;
import jisa.visa.Connection;
import jisa.visa.VISADevice;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class ILM200 extends VISADevice implements LevelMeter {

    public static String getDescription() {
        return "Oxford Instruments ILM-200";
    }

    private static final String TERMINATOR       = "\r";
    private static final String C_SET_COMM_MODE  = "Q2";
    private static final String C_READ_CHANNEL   = "R%d";
    private static final String C_SET_REM_STATUS = "C%d";
    private static final String C_SET_FAST       = "T%d";
    private static final String C_SET_SLOW       = "S%d";

    private static final int CHANNEL_1_LEVEL = 1;

    public ILM200(Address address) throws IOException {

        super(address);
        setSerialParameters(9600, 8, Connection.Parity.NONE, Connection.StopBits.ONE, Connection.Flow.NONE);
        setEOI(false);
        setWriteTerminator(TERMINATOR);
        write(C_SET_COMM_MODE);
        setReadTerminator(EOS_RETURN);

        clearReadBuffer();

        setMode(Mode.REMOTE_UNLOCKED);

    }


    public String getIDN() throws IOException {
        return query("V");
    }

    private double readChannel(int channel) throws IOException {
        String response = query(C_READ_CHANNEL, channel);
        return Double.valueOf(response.substring(1));
    }

    public double getLevel(int channel) throws DeviceException, IOException {

        if (channel < 0 || channel > 2) {
            throw new DeviceException("That channel does not exist!");
        }

        return readChannel(CHANNEL_1_LEVEL + channel) / 10.0;

    }

    public void setMode(Mode mode) throws IOException {
        query(C_SET_REM_STATUS, mode.toInt());
    }

    public void setFastRate(int channel, boolean fast) throws IOException, DeviceException {

        if (channel < 0 || channel > 2) {
            throw new DeviceException("That channel does not exist!");
        }

        query(fast ? C_SET_FAST : C_SET_SLOW, channel + 1);

    }

    @Override
    public int getNumChannels() {
        return 2;
    }

    @Override
    public String getChannelName(int channelNumber) {
        return String.format("Channel %d", channelNumber + 1);
    }

    @Override
    public List<LevelMeter> getChannels() {
        return List.of(getChannel(0), getChannel(1));
    }

    public enum Mode {

        LOCAL_LOCKED(0),
        REMOTE_LOCKED(1),
        LOCAL_UNLOCKED(2),
        REMOTE_UNLOCKED(3);

        private        int                           c;
        private static HashMap<Integer, ITC503.Mode> lookup = new HashMap<>();

        static ITC503.Mode fromInt(int i) {
            return lookup.getOrDefault(i, null);
        }

        static {
            for (ITC503.Mode mode : ITC503.Mode.values()) {
                lookup.put(mode.toInt(), mode);
            }
        }

        Mode(int code) {
            c = code;
        }

        int toInt() {
            return c;
        }

    }


}
