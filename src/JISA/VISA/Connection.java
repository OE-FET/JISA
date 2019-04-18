package JISA.VISA;

import java.nio.ByteBuffer;

public interface Connection {

    void writeBytes(byte[] bytes) throws VISAException;

    /**
     * Writes the specified string over the connection.
     *
     * @param toWrite String to write
     *
     * @throws VISAException Upon something going wrong with VISA
     */
    default void write(String toWrite) throws VISAException {
        writeBytes(toWrite.getBytes());
    }

    /**
     * Reads from the connection, using the specified maximum buffer size. Returns data as a String
     *
     * @param bufferSize Buffer size, in bytes
     *
     * @return Read data, as a String
     *
     * @throws VISAException Upon something going wrong with VISA
     */
    default String read(int bufferSize) throws VISAException {
        return new String(readBytes(bufferSize));
    }

    /**
     * Reads from the connection (max buffer of 1024 bytes), returning the data as a String
     *
     * @return Read data, as a String
     *
     * @throws VISAException Upon something going wrong
     */
    default String read() throws VISAException {
        return read(1024);
    }

    /**
     * Attempts to read the given number of bytes from the connection. Will return if either the number of bytes is
     * reached or the specified EOS terminator is found. Will time-out if neither of these conditions are met within
     * the value specified by setTMO(...).
     *
     * @param bufferSize The number of bytes to read
     *
     * @return Array of bytes read (trimmed)
     *
     * @throws VISAException Upon something going wrong
     */
    byte[] readBytes(int bufferSize) throws VISAException;

    /**
     * Sets whether to use the EOI (End or Identify) line to terminate messages over GPIB.
     * Has no effect on connections that aren't over GPIB.
     *
     * @param set Use EOI?
     *
     * @throws VISAException Upon something going wrong
     */
    void setEOI(boolean set) throws VISAException;

    /**
     * Sets the "End of String" terminator, in numerical form. This is the terminator used for reading from the
     * connection, to specify the end of a message from the instrument. For example:
     * <p>
     * line-feed:      \n   = 0x0A
     * carriage-return \r   = 0x0D
     * CR-LF           \r\n = 0x0D0A
     *
     * @param terminator The terminator to look for (numerical representation)
     *
     * @throws VISAException Upon something going wrong
     */
    void setEOS(long terminator) throws VISAException;

    /**
     * Sets the "End of String" terminator, in String form. This is the terminator used for reading from the connection,
     * to specify the end of a message from the instrument.
     *
     * @param terminator The terminator to look from (String representation)
     *
     * @throws VISAException Upon something going wrong
     */
    default void setEOS(String terminator) throws VISAException {

        byte[] bytes = terminator.getBytes();

        if (bytes.length > Long.BYTES) {
            throw new VISAException("EOS terminator too long!");
        }

        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);

        for (int i = 0; i < Long.BYTES - bytes.length; i++) {
            buffer.put((byte) 0);
        }

        buffer.put(bytes);
        buffer.rewind();
        setEOS(buffer.getLong());

    }

    /**
     * Sets the time-out, in milli-seconds, to use on this connection.
     *
     * @param duration Time-out, in milli-seconds
     *
     * @throws VISAException Upon something going wrong
     */
    void setTMO(long duration) throws VISAException;

    /**
     * Sets the parameters for a serial connection.
     * Has no effect on connections that aren't over serial.
     *
     * @param baud   Baud rate
     * @param data   Data bits
     * @param parity Parity type
     * @param stop   Stop bits
     * @param flow   Flow control
     *
     * @throws VISAException Upon something going wrong
     */
    void setSerial(int baud, int data, Parity parity, StopBits stop, Flow flow) throws VISAException;

    /**
     * Closes the connection, freeing up the resources used by it.
     *
     * @throws VISAException Upon something going wrong
     */
    void close() throws VISAException;

    enum Parity {
        NONE(0),
        ODD(1),
        EVEN(2),
        MARK(3),
        SPACE(4);

        private int value;

        Parity(int v) {
            value = v;
        }

        public int toInt() {
            return value;
        }

    }

    enum StopBits {
        ONE(10),
        ONE_HALF(15),
        TWO(20);

        private int value;

        StopBits(int v) {
            value = v;
        }

        public int toInt() {
            return value;
        }

    }

    enum Flow {

        NONE(0),
        XON_XOFF(1),
        RTS_CTS(2),
        DTR_DSR(4);

        private int value;

        Flow(int v) {
            value = v;
        }

        public int toInt() {
            return value;
        }

    }

}
