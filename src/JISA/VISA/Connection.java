package JISA.VISA;

public interface Connection {

    void write(String toWrite) throws VISAException;

    String read(int bufferSize) throws VISAException;

    default String read() throws VISAException {
        return read(1024);
    }

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
