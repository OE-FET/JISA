package JISA.VISA;

import JISA.Addresses.Address;
import JISA.Addresses.StrAddress;
import JISA.Addresses.TCPIPSocketAddress;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class RawTCPIPDriver implements Driver {
    @Override
    public Connection open(Address address) throws VISAException {

        TCPIPSocketAddress addr = address.toStrAddress().toTCPIPSocketAddress();

        if (addr == null) {
            throw new VISAException("Raw TCP-IP driver can only be used to open raw TCP-IP sockets!");
        }
        try {
            Socket socket = new Socket(InetAddress.getByName(addr.getHost()), addr.getPort());
            return new TCPIPConnection(socket);
        } catch (IOException e) {
            throw new VISAException(e.getMessage());
        }

    }

    public class TCPIPConnection implements Connection {

        private Socket      socket;
        private PrintWriter out;
        private Scanner     in;

        public TCPIPConnection(Socket tcpipSocket) throws IOException {
            socket = tcpipSocket;
            out = new PrintWriter(socket.getOutputStream());
            in = new Scanner(socket.getInputStream());
            in.useDelimiter("\n");
        }

        @Override
        public void write(String toWrite) throws VISAException {
            try {
                out.print(toWrite);
            } catch (Exception e) {
                throw new VISAException(e.getMessage());
            }
        }

        @Override
        public String read(int bufferSize) throws VISAException {
            try {
                return in.next();
            } catch (Exception e) {
                throw new VISAException(e.getMessage());
            }
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

            in.useDelimiter(new String(buffer.array()));
        }

        @Override
        public void setTMO(long duration) throws VISAException {
            try {
                socket.setSoTimeout((int) duration);
            } catch (Exception e) {
                throw new VISAException(e.getMessage());
            }
        }

        @Override
        public void setSerial(int baud, int data, Parity parity, StopBits stop, Flow flow) throws VISAException {

        }

        @Override
        public void close() throws VISAException {
            try {
                socket.close();
                in.close();
                out.close();
            } catch (Exception e) {
                throw new VISAException(e.getMessage());
            }
        }
    }

    @Override
    public StrAddress[] search() throws VISAException {
        return new StrAddress[0];
    }
}
