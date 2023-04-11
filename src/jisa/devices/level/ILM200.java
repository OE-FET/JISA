package jisa.devices.level;

import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.Instrument;
import jisa.devices.interfaces.LevelMeter;
import jisa.devices.temperature.ITC503;
import jisa.visa.VISADevice;
import jisa.visa.connections.Connection;
import jisa.visa.connections.GPIBConnection;
import jisa.visa.connections.SerialConnection;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ILM200 extends VISADevice implements LevelMeter {

    private static final String TERMINATOR       = "\r";
    private static final String C_SET_COMM_MODE  = "Q2";
    private static final String C_READ_CHANNEL   = "R%d";
    private static final String C_SET_REM_STATUS = "C%d";
    private static final String C_SET_FAST       = "T%d";
    private static final String C_SET_SLOW       = "S%d";
    private static final int CHANNEL_1_LEVEL = 1;

    public ILM200(Address address) throws IOException {

        super(address);

        Connection connection = getConnection();

        if (connection instanceof SerialConnection) {
            ((SerialConnection) connection).setSerialParameters(9600, 8, SerialConnection.Parity.NONE, SerialConnection.Stop.BITS_10);
        }

        if (connection instanceof GPIBConnection) {
            ((GPIBConnection) connection).setEOIEnabled(false);
        }

        setWriteTerminator(TERMINATOR);
        write(C_SET_COMM_MODE);
        setReadTerminator(EOS_RETURN);

        manuallyClearReadBuffer();

        setMode(Mode.REMOTE_UNLOCKED);

    }

    public static String getDescription() {
        return "Oxford Instruments ILM-200";
    }

    public String getIDN() throws IOException {
        return query("V");
    }

    private double readChannel(int channel) throws IOException {

        String response = "";
        int    count    = 0;

        do {
            response = query(C_READ_CHANNEL, channel);
            count ++;
        } while (count < 3 && !response.startsWith("R"));

        if (response.startsWith("R")) {
            return Double.parseDouble(response.substring(1));
        } else {
            throw new IOException("Invalid response from ILM200!");
        }

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

    public int getNumChannels() {
        return 2;
    }

    public String getName(int channelNumber) {
        return String.format("Channel %d", channelNumber + 1);
    }

    public List<LevelMeter> getChannels() {
        return List.of(getChannel(0), getChannel(1));
    }

    @Override
    public <I extends Instrument> List<I> get(Class<I> type) {

        if (type.isAssignableFrom(LevelMeter.class)) {
            return (List<I>) List.of(getChannel(0));
        } else {
            return Collections.emptyList();
        }

    }

    public <I extends Instrument> I getSubInstrument(Class<I> type, int index) {
        if (type.isAssignableFrom(LevelMeter.class)) {
            return (I) getChannel(index);
        } else {
            return null;
        }
    }

    public enum Mode {

        LOCAL_LOCKED(0),
        REMOTE_LOCKED(1),
        LOCAL_UNLOCKED(2),
        REMOTE_UNLOCKED(3);

        private static final HashMap<Integer, ITC503.Mode> lookup = new HashMap<>();

        static {
            for (ITC503.Mode mode : ITC503.Mode.values()) {
                lookup.put(mode.toInt(), mode);
            }
        }

        private final int c;

        Mode(int code) {
            c = code;
        }

        static ITC503.Mode fromInt(int i) {
            return lookup.getOrDefault(i, null);
        }

        int toInt() {
            return c;
        }

    }


}
