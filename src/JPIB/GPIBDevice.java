package JPIB;

import java.io.IOException;

public class GPIBDevice {

    private int bus;
    private int address;
    private int device;
    private String terminator  = "";
    private String lastCommand = null;
    private String lastRead    = null;

    public final static int DEFAULT_TIMEOUT = 13;
    public final static int DEFAULT_EOI     = 1;
    public final static int DEFAULT_EOS     = 0;
    public final static int EOS_RETURN      = 5130;

    public GPIBDevice(int bus, int address, int timeout, int EOI, int EOS) throws IOException {

        // Check that we've initialised this bus first
        if (!GPIB.isInitialised(bus)) {
            throw new IOException("GPIB bus has not been initialised!");
        }

        // Store the bus and address
        this.bus = bus;
        this.address = address;

        // Open the device and get its handle
        this.device = GPIB.openDevice(bus, address, timeout, EOI, EOS);

    }

    public GPIBDevice(int bus, int address) throws IOException {
        this(bus, address, DEFAULT_TIMEOUT, DEFAULT_EOI, DEFAULT_EOS);
    }

    public void setTerminator(String term) {
        terminator = term;
    }

    public synchronized void write(String command, Object... args) throws IOException {
        String commandParsed = String.format(command, args).concat(terminator);
        lastCommand = commandParsed;
        GPIB.writeCommand(device, String.format(commandParsed, args));
    }

    public synchronized String read() throws IOException {
        lastRead = GPIB.readDevice(device);
        return lastRead;
    }

    public synchronized String query(String command, Object... args) throws IOException {
        write(command, args);
        return read();
    }

}
