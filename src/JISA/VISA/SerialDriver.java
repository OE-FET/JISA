package JISA.VISA;

import JISA.Addresses.InstrumentAddress;
import JISA.Addresses.SerialAddress;
import JISA.Addresses.StrAddress;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;
import jssc.SerialPortTimeoutException;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

public class SerialDriver implements Driver {

    protected static long                      counter = 0;
    protected static HashMap<Long, SerialPort> ports   = new HashMap<>();
    protected static HashMap<Long, Integer>    tmos    = new HashMap<>();
    protected static HashMap<Long, String>     terms   = new HashMap<>();

    @Override
    public long open(InstrumentAddress address) throws VISAException {

        SerialAddress addr = (new StrAddress(address.getVISAAddress())).toSerialAddress();

        if (addr == null) {
            throw new VISAException("Can only open serial connections with the serial driver!");
        }

        SerialPort port   = new SerialPort(String.format("COM%d", addr.getBoard()));
        boolean    result = false;
        try {
            result = port.openPort();
        } catch (SerialPortException e) {
            throw new VISAException(e.getMessage());
        }

        if (!result) {
            throw new VISAException("Error opening port!");
        }

        long handle = counter++;
        ports.put(handle, port);

        return handle;

    }

    @Override
    public void close(long instrument) throws VISAException {

        if (!ports.containsKey(instrument)) {
            throw new VISAException("That instrument has not been opened!");
        }

        boolean result = false;

        try {
            result = ports.get(instrument).closePort();
        } catch (SerialPortException e) {
            throw new VISAException(e.getMessage());
        }

        if (!result) {
            throw new VISAException("Error closing port!");
        }

        ports.remove(instrument);

    }

    @Override
    public void write(long instrument, String toWrite) throws VISAException {

        if (!ports.containsKey(instrument)) {
            throw new VISAException("That instrument has not been opened!");
        }

        boolean result = false;
        try {
            result = ports.get(instrument).writeString(toWrite);
        } catch (SerialPortException e) {
            e.printStackTrace();
        }

        if (!result) {
            throw new VISAException("Error writing to port!");
        }

    }

    @Override
    public String read(long instrument, int bufferSize) throws VISAException {

        SerialPort    port = ports.get(instrument);
        StringBuilder read = new StringBuilder();

        while (!read.toString().contains(terms.get(instrument))) {
            try {
                read.append(port.readString(1, tmos.get(instrument)));
            } catch (SerialPortException | SerialPortTimeoutException e) {
                throw new VISAException(e.getMessage());
            }
        }

        return read.toString();

    }

    @Override
    public void setEOI(long instrument, boolean set) throws VISAException {

        if (!ports.containsKey(instrument)) {
            throw new VISAException("That instrument has not been opened!");
        }

    }

    @Override
    public void setEOS(long instrument, long character) throws VISAException {

        if (!ports.containsKey(instrument)) {
            throw new VISAException("That instrument has not been opened!");
        }

        byte[] bytes = ByteBuffer.allocate(Long.BYTES).putLong(character).array();

        int offset = 0;
        for (int i = 0; i < bytes.length; i++) {

            if (bytes[i] > (byte) 0) {
                offset = i;
                break;
            }

        }

        ByteBuffer buffer = ByteBuffer.allocate(bytes.length - offset);

        for (int i = offset; i < bytes.length; i ++) {
            buffer.put(bytes[i]);
        }

        String term = new String(buffer.array());

        terms.put(instrument, term);
    }

    @Override
    public void setTMO(long instrument, long duration) throws VISAException {

        if (!ports.containsKey(instrument)) {
            throw new VISAException("That instrument has not been opened!");
        }

        tmos.put(instrument, (int) duration);
    }

    @Override
    public void setSerial(long instrument, int baud, int data, int parity, int stop, int flow) throws VISAException {

        if (!ports.containsKey(instrument)) {
            throw new VISAException("That instrument has not been opened!");
        }

        try {
            ports.get(instrument).setParams(baud, data, stop, parity, false, false);
        } catch (SerialPortException e) {
            throw new VISAException(e.getMessage());
        }
    }

    @Override
    public StrAddress[] search() throws VISAException {

        String[] names = SerialPortList.getPortNames();

        ArrayList<StrAddress> addresses = new ArrayList<>();

        for (String name : names) {

            if (name.substring(0, 3).equals("COM")) {
                int board = Integer.valueOf(name.substring(3));
                addresses.add(new StrAddress(new SerialAddress(board).getVISAAddress()));
            }

        }

        return addresses.toArray(new StrAddress[0]);

    }
}
