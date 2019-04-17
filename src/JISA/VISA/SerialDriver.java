package JISA.VISA;

import JISA.Addresses.Address;
import JISA.Addresses.SerialAddress;
import JISA.Addresses.StrAddress;
import JISA.Util;
import com.sun.security.ntlm.Server;
import jssc.*;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class SerialDriver implements Driver {

    private static final String PATTERN_LINUX_MAC = "/dev/ttyS%d";
    private static final String PATTERN_WINDOWS   = "COM%d";

    protected String pattern;

    public SerialDriver() {

        String osName = System.getProperty("os.name").trim().toLowerCase();

        if (osName.contains("linux") || osName.contains("mac")) {
            pattern = PATTERN_LINUX_MAC;
        } else {
            pattern = PATTERN_WINDOWS;
        }

    }

    @Override
    public Connection open(Address address) throws VISAException {

        SerialAddress addr = (new StrAddress(address.toString())).toSerialAddress();

        if (addr == null) {
            throw new VISAException("Can only open serial connections with the serial driver!");
        }

        SerialPort port   = new SerialPort(String.format(pattern, addr.getBoard()));
        boolean    result = false;

        try {
            result = port.openPort();
        } catch (SerialPortException e) {
            throw new VISAException(e.getMessage());
        }

        if (!result) {
            throw new VISAException("Error opening port!");
        }

        return new SerialConnection(port);

    }

    public class SerialConnection implements Connection {

        private SerialPort port;
        private int        tmo;
        private String     terms;
        private byte[]     terminationSequence;

        public SerialConnection(SerialPort comPort) {
            port = comPort;
        }

        @Override
        public void write(String toWrite) throws VISAException {

            boolean result = false;

            try {
                result = port.writeString(toWrite);
            } catch (SerialPortException e) {
                e.printStackTrace();
            }

            if (!result) {
                throw new VISAException("Error writing to port!");
            }

        }

        @Override
        public String read(int bufferSize) throws VISAException {
            return new String(readBytes(bufferSize));
        }

        @Override
        public byte[] readBytes(int bufferSize) throws VISAException {

            ByteBuffer buffer    = ByteBuffer.allocate(bufferSize);
            byte[]     lastBytes = new byte[terminationSequence.length];
            byte[]     single;

            try {

                do {

                    single = port.readBytes(1, tmo);

                    if (single.length != 1) {
                        throw new VISAException("Error reading from input stream!");
                    }

                    if (terminationSequence.length > 0) {
                        System.arraycopy(lastBytes, 1, lastBytes, 0, lastBytes.length - 1);
                        lastBytes[lastBytes.length - 1] = single[0];
                    }

                    buffer.put(single[0]);

                } while (terminationSequence.length == 0 || !Arrays.equals(lastBytes, terminationSequence));

                return Util.trimArray(buffer.array());

            } catch (Exception e) {
                throw new VISAException(e.getMessage());
            }

        }

        @Override
        public void setEOI(boolean set) throws VISAException {

        }

        @Override
        public void setEOS(long character) throws VISAException {

            ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
            buffer.putLong(character);

            int pos = 0;

            for (int i = 0; i < Long.BYTES; i++) {
                if (buffer.get(i) > 0) {
                    pos = i;
                    break;
                }
            }

            terminationSequence = new byte[Long.BYTES - pos];
            System.arraycopy(buffer.array(), pos, terminationSequence, 0, terminationSequence.length);

        }

        @Override
        public void setTMO(long duration) throws VISAException {
            tmo = (int) duration;
        }

        @Override
        public void setSerial(int baud, int data, Parity parity, StopBits stop, Flow flow) throws VISAException {

            int stopBits = 1;

            switch (stop) {

                case ONE:
                    stopBits = 1;
                    break;

                case ONE_HALF:
                    stopBits = 3;
                    break;

                case TWO:
                    stopBits = 2;
                    break;

            }

            try {
                port.setParams(baud, data, stopBits, parity.toInt(), false, false);
            } catch (SerialPortException e) {
                throw new VISAException(e.getMessage());
            }

        }

        @Override
        public void close() throws VISAException {

            boolean result = false;

            try {
                port.purgePort(1);
                port.purgePort(2);
                result = port.closePort();
            } catch (SerialPortException e) {
                throw new VISAException(e.getMessage());
            }

            if (!result) {
                throw new VISAException("Error closing port!");
            }

        }
    }

    @Override
    public StrAddress[] search() throws VISAException {

        String[] names = SerialPortList.getPortNames();

        ArrayList<StrAddress> addresses = new ArrayList<>();

        for (String name : names) {

            if (name.substring(0, 3).equals("COM")) {
                int board = Integer.valueOf(name.substring(3));
                addresses.add((new SerialAddress(board)).toStrAddress());
            } else if (name.substring(0, 9).equals("/dev/ttyS")) {
                int board = Integer.valueOf(name.substring(9));
                addresses.add((new SerialAddress(board)).toStrAddress());
            }

        }

        return addresses.toArray(new StrAddress[0]);

    }
}
