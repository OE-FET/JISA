package jisa.visa.drivers;

import jisa.Util;
import jisa.addresses.Address;
import jisa.addresses.TCPIPAddress;
import jisa.visa.VISAException;
import jisa.visa.connections.Connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TCPIPDriver implements Driver {

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
    public List<Address> search() throws VISAException {
        return Collections.emptyList();
    }

    @Override
    public boolean worksWith(Address address) {
        return address.getType() == Address.Type.TCPIP;
    }

    public static class TCPIPConnection implements jisa.visa.connections.TCPIPConnection {

        private final Socket       socket;
        private final OutputStream out;
        private final InputStream  in;
        private       byte[]       terminationSequence;
        private       Charset      charset = StandardCharsets.UTF_8;

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
        public void setEncoding(Charset charset) {
            this.charset = charset;
        }

        @Override
        public Charset getEncoding() {
            return charset;
        }

        @Override
        public byte[] readBytes(int bufferSize) throws VISAException {

            ByteBuffer buffer    = ByteBuffer.allocate(bufferSize);
            byte       single;
            byte[]     lastBytes = new byte[terminationSequence.length];

            try {

                for (int i = 0; i < bufferSize; i++) {

                    single = (byte) in.read();

                    buffer.put(single);

                    if (terminationSequence.length > 0) {

                        System.arraycopy(lastBytes, 1, lastBytes, 0, lastBytes.length - 1);

                        lastBytes[lastBytes.length - 1] = single;

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
        public void setReadTerminator(long character) {

            if (character == 0) {
                terminationSequence = new byte[0];
            }

            ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
            buffer.putLong(character);

            buffer.rewind();

            byte value;
            do {
                value = buffer.get();
            } while (value == 0);

            buffer.position(buffer.position() - 1);

            terminationSequence = buffer.slice().array();

        }

        @Override
        public void setReadTerminator(String character) {
            terminationSequence = character.getBytes(charset);
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
