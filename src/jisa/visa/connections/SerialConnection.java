package jisa.visa.connections;

import jisa.visa.VISAException;
import jisa.visa.VISANativeInterface;

public interface SerialConnection extends Connection {

    void setSerialParameters(int baud, int data, Parity parity, Stop stop, FlowControl... flow) throws VISAException;

    default void setSerialParameters(int baud, int data) throws VISAException {
        setSerialParameters(baud, data, Parity.NONE, Stop.BITS_10, FlowControl.NONE);
    }

    enum Parity {

        NONE(0),
        ODD(1),
        EVEN(2),
        MARK(3),
        SPACE(4);

        private final int value;

        Parity(int v) {
            value = v;
        }

        public int toInt() {
            return value;
        }

    }

    enum Stop {

        BITS_10(VISANativeInterface.VI_ASRL_STOP_ONE),
        BITS_15(VISANativeInterface.VI_ASRL_STOP_ONE5),
        BITS_20(VISANativeInterface.VI_ASRL_STOP_TWO);

        private final int value;

        Stop(int v) {
            value = v;
        }

        public int toInt() {
            return value;
        }

    }

    enum FlowControl {

        NONE(0),
        XON_XOFF(1),
        RTS_CTS(2),
        DTR_DSR(4);

        private final int value;

        FlowControl(int v) {
            value = v;
        }

        public int toInt() {
            return value;
        }

    }
}
