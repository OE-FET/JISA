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

    protected static long counter = 0;

    @Override
    public Connection open(InstrumentAddress address) throws VISAException {

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

        return new SerialConnection(port);

    }


    public class SerialConnection implements Connection {

        private SerialPort port;
        private int        tmo;
        private String     terms;

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

            StringBuilder read = new StringBuilder();

            while (!read.toString().contains(terms)) {
                try {
                    read.append(port.readString(1, tmo));
                } catch (SerialPortException | SerialPortTimeoutException e) {
                    throw new VISAException(e.getMessage());
                }
            }

            return read.toString();

        }

        @Override
        public void setEOI(boolean set) throws VISAException {

        }

        @Override
        public void setEOS(long character) throws VISAException {

            byte[] bytes = ByteBuffer.allocate(Long.BYTES).putLong(character).array();

            int offset = 0;
            for (int i = 0; i < bytes.length; i++) {

                if (bytes[i] > (byte) 0) {
                    offset = i;
                    break;
                }

            }

            ByteBuffer buffer = ByteBuffer.allocate(bytes.length - offset);

            for (int i = offset; i < bytes.length; i++) {
                buffer.put(bytes[i]);
            }

            terms = new String(buffer.array());

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
                addresses.add(new StrAddress(new SerialAddress(board).getVISAAddress()));
            }

        }

        return addresses.toArray(new StrAddress[0]);

    }
}
