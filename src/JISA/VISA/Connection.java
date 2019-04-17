package JISA.VISA;

public interface Connection {

    /**
     * Writes the specified string over the connection.
     *
     * @param toWrite String to write
     *
     * @throws VISAException Upon something going wrong with VISA
     */
    void write(String toWrite) throws VISAException;

    /**
     * Reads from the connection, using the specified maximum buffer size. Returns data as a String
     *
     * @param bufferSize Buffer size, in bytes
     *
     * @return Read data, as a String
     *
     * @throws VISAException Upon something going wrong with VISA
     */
    String read(int bufferSize) throws VISAException;

    /**
     * Reads from the connection (max buffer of 1024 bytes), returning the data as a String
     *
     * @return Read data, as a String
     *
     * @throws VISAException Upon something going wrong with VISA
     */
    default String read() throws VISAException {
        return read(1024);
    }

    byte[] readBytes(int bufferSize) throws VISAException;

    /**
     *
     * @param set
     * @throws VISAException
     */
    void setEOI(boolean set) throws VISAException;

    void setEOS(long character) throws VISAException;

    void setTMO(long duration) throws VISAException;

    void setSerial(int baud, int data, Parity parity, StopBits stop, Flow flow) throws VISAException;

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
