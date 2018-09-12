package JISA.VISA;

import JISA.Addresses.InstrumentAddress;

import java.io.IOException;

public class VISADevice {

    private long              device;
    private InstrumentAddress address;
    private String            terminator  = "";
    private String            lastCommand = null;
    private String            lastRead    = null;

    public final static int    DEFAULT_TIMEOUT = 13;
    public final static int    DEFAULT_EOI     = 1;
    public final static int    DEFAULT_EOS     = 0;
    public final static int    EOS_RETURN      = 5130;
    public final static String C_IDN           = "*IDN?";

    public VISADevice(InstrumentAddress address) throws IOException {

        try {
            this.device = VISA.openInstrument(address.getVISAAddress());
            this.address = address;
        } catch (VISAException e) {
            throw new IOException(e.getMessage());
        }

    }

    public void setEOI(boolean flag) throws IOException {

        try {
            VISA.setEOI(device, flag);
        } catch (VISAException e) {
            throw new IOException(e.getMessage());
        }

    }

    public void setReadTerminationCharacter(long character) throws IOException {

        try {
            VISA.setTerminationCharacter(device, character);
        } catch (VISAException e) {
            throw new IOException(e.getMessage());
        }

    }

    public void enabledReadTerminationCharacter(boolean flag) throws IOException {
        try {
            VISA.enableTerminationCharacter(device, flag);
        } catch (VISAException e) {
            throw new IOException(e.getMessage());
        }
    }

    public void setTimeout(long timeoutMSec) throws IOException {
        try {
            VISA.setTimeout(device, timeoutMSec);
        } catch (VISAException e) {
            throw new IOException(e.getMessage());
        }
    }

    public InstrumentAddress getAddress() {
        return address;
    }

    public void setTerminator(String term) {
        terminator = term;
    }

    public synchronized void write(String command, Object... args) throws IOException {
        String commandParsed = String.format(command, args).concat(terminator);
        lastCommand = commandParsed;
        try {
            VISA.write(device, commandParsed);
        } catch (VISAException e) {
            throw new IOException(e.getMessage());
        }
    }

    public synchronized String read() throws IOException {
        try {
            lastRead = VISA.read(device);
        } catch (VISAException e) {
            throw new IOException(e.getMessage());
        }
        return lastRead;
    }

    public synchronized double readDouble() throws IOException {
        return Double.parseDouble(read());
    }

    public synchronized int readInt() throws IOException {
        return Integer.parseInt(read());
    }

    public synchronized double queryDouble(String command, Object... args) throws IOException {
        write(command, args);
        return readDouble();
    }

    public synchronized int queryInt(String command, Object... args) throws IOException {
        write(command, args);
        return readInt();
    }

    public synchronized String query(String command, Object... args) throws IOException {
        write(command, args);
        return read();
    }

    public synchronized String getIDN() throws IOException {
        return query(C_IDN);
    }

}
