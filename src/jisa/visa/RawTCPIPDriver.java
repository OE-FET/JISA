package jisa.visa;

import jisa.Util;
import jisa.addresses.Address;
import jisa.addresses.StrAddress;
import jisa.addresses.TCPIPAddress;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class RawTCPIPDriver implements Driver {
    @Override
    public Connection open(Address address) throws VISAException {

        TCPIPAddress addr = address.toTCPIPSocketAddress();

        if (addr == null) {
            throw new VISAException("Raw TCP-IP driver can only be used to open raw TCP-IP sockets!");
        }
        try {
            Socket socket = new Socket(InetAddress.getByName(addr.getHost()), addr.getPort());
            socket.setSoTimeout(2000);
            return new TCPIPConnection(socket);
        } catch (IOException e) {
            throw new VISAException(e.getMessage());
        }

    }

    @Override
    public StrAddress[] search() throws VISAException {
        return new StrAddress[0];
    }

    @Override
    public boolean worksWith(Address address) {
        return address.getType() == Address.Type.TCPIP;
    }

    public static class TCPIPConnection implements Connection {

        private Socket       socket;
        private OutputStream out;
        private InputStream  in;
        private byte[]       terminationSequence;

        public TCPIPConnection(Socket tcpipSocket) throws IOException {
            socket = tcpipSocket;
            out    = socket.getOutputStream();
            in     = socket.getInputStream();
        }

        @Override
        public void writeBytes(byte[] bytes) throws VISAException {

            try {
                out.write(bytes);
            } catch (Exception e) {
                throw new VISAException(e.getMessage());
            }

        }

        @Override
        public void clear() throws VISAException {

            try {
                out.flush();
                in.readAllBytes();
            } catch (IOException e) {
                throw new VISAException(e.getMessage());
            }

        }

        @Override
        public byte[] readBytes(int bufferSize) throws VISAException {

            ByteBuffer buffer    = ByteBuffer.allocate(bufferSize);
            byte[]     single    = new byte[1];
            byte[]     lastBytes = new byte[terminationSequence.length];

            try {

                for (int i = 0; i < bufferSize; i++) {

                    int readCount = in.read(single);

                    if (readCount != 1) {
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

            } catch (IOException e) {
                throw new VISAException(e.getMessage());
            }

        }

        @Override
        public void setEOI(boolean set) throws VISAException {

        }

        @Override
        public void setReadTerminator(long character) {

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
        public void setTimeout(int duration) throws VISAException {
            try {
                socket.setSoTimeout(duration);
            } catch (Exception e) {
                throw new VISAException(e.getMessage());
            }
        }

        @Override
        public void setSerial(int baud, int data, Parity parity, StopBits stop, Flow flow) {
            // Nothing to do here
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

}
