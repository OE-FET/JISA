package JISA.VISA;

import JISA.Addresses.Address;
import JISA.Addresses.SerialAddress;
import JISA.Addresses.StrAddress;
import JISA.Util;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

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

        int board = addr.getBoard();

        String[] portNames = SerialPortList.getPortNames();

        if (portNames.length <= board) {
            throw new VISAException(String.format("Serial port %d does not exist.", board));
        }

        SerialPort port   = new SerialPort(portNames[board]);
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
        private byte[]     terminationSequence = {0x0A};

        public SerialConnection(SerialPort comPort) throws VISAException {
            port = comPort;
            setSerial(9600, 8, Parity.NONE, StopBits.ONE, Flow.NONE);
        }

        @Override
        public void writeBytes(byte[] bytes) throws VISAException {

            boolean result;

            try {
                result = port.writeBytes(bytes);
            } catch (SerialPortException e) {
                throw new VISAException(e.getMessage());
            }

            if (!result) {
                throw new VISAException("Error writing to port!");
            }

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
        public byte[] readBytes(int bufferSize) throws VISAException {

            ByteBuffer buffer    = ByteBuffer.allocate(bufferSize);
            byte[]     lastBytes = new byte[terminationSequence.length];
            byte[]     single;

            try {

                for (int i = 0; i < bufferSize; i++) {

                    single = port.readBytes(1, tmo);

                    if (single.length != 1) {
                        throw new VISAException("Error reading from input stream!");
                    }

                    buffer.put(single[0]);

                    if (terminationSequence.length > 0) {

                        System.arraycopy(lastBytes, 1, lastBytes, 0, lastBytes.length - 1);

                        lastBytes[lastBytes.length - 1] = single[0];

                        if (Arrays.equals(lastBytes, terminationSequence)) {
                            break;
                        }

                    }

                }

                return Util.trimArray(buffer.array());

            } catch (Exception e) {
                throw new VISAException(e.getMessage());
            }

        }

        @Override
        public void setEOI(boolean set) {
            // Nothing to do here
        }

        @Override
        public void setEOS(long character) {

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
        public void setTMO(int duration) throws VISAException {
            tmo = duration;
        }

        @Override
        public void setSerial(int baud, int data, Parity parity, StopBits stop, Flow flow) throws VISAException {

            int stopBits = 1;

            switch (stop) {

                case ONE:
                    stopBits = SerialPort.STOPBITS_1;
                    break;

                case ONE_HALF:
                    stopBits = SerialPort.STOPBITS_1_5;
                    break;

                case TWO:
                    stopBits = SerialPort.STOPBITS_2;
                    break;

            }

            try {

                port.setParams(baud, data, stopBits, parity.toInt());

                switch (flow) {

                    case RTS_CTS:
                        port.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | SerialPort.FLOWCONTROL_RTSCTS_OUT);
                        break;

                    case XON_XOFF:
                        port.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_IN | SerialPort.FLOWCONTROL_XONXOFF_OUT);
                        break;

                    default:
                    case NONE:
                        port.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
                        break;

                }

            } catch (SerialPortException e) {
                throw new VISAException(e.getMessage());
            }

        }

        @Override
        public void close() throws VISAException {

            boolean result;

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
    public StrAddress[] search() {

        String[] names = SerialPortList.getPortNames();

        ArrayList<StrAddress> addresses = new ArrayList<>();

        for (int i = 0; i < names.length; i++) {
            addresses.add(new SerialAddress(i).toStrAddress());
        }

        return addresses.toArray(new StrAddress[0]);

    }
}
