package jisa.devices;

import jisa.addresses.Address;
import jisa.visa.Connection;
import jisa.visa.VISADevice;

import java.io.IOException;
import java.util.HashMap;

public class ILM200 extends VISADevice {

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
        setTerminator(TERMINATOR);
        write(C_SET_COMM_MODE);
        setReadTerminationCharacter(EOS_RETURN);

        clearRead();

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
