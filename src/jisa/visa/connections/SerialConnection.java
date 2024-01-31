package jisa.visa.connections;

import jisa.visa.VISANativeInterface;
import jisa.visa.exceptions.VISAException;

public interface SerialConnection extends Connection {

    void setSerialParameters(int baud, int data, Parity parity, Stop stop, FlowControl... flow) throws VISAException;

    default void setSerialParameters(int baud, int data) throws VISAException {
        setSerialParameters(baud, data, Parity.NONE, Stop.BITS_10, FlowControl.NONE);
    }

    default void setSerialParameters(int baud, int data, Parity parity, int stop, FlowControl... flow) throws VISAException {

        Stop stopBits;

        if (stop == 1) {
            stopBits = Stop.BITS_10;
        } else if (stop == 2) {
            stopBits = Stop.BITS_20;
        } else if (stop == 10) {
            stopBits = Stop.BITS_10;
        } else if (stop == 15) {
            stopBits = Stop.BITS_15;
        } else if (stop == 20) {
            stopBits = Stop.BITS_20;
        } else {
            stopBits = Stop.BITS_10;
        }

        setSerialParameters(baud, data, parity, stopBits, flow);

    }

    default void setSerialParameters(int baud, int data, Parity parity, double stop, FlowControl... flow) throws VISAException {

        Stop stopBits;

        if (stop == 1) {
            stopBits = Stop.BITS_10;
        } else if (stop == 1.5) {
            stopBits = Stop.BITS_15;
        } else if (stop == 2) {
            stopBits = Stop.BITS_20;
        } else if (stop == 10) {
            stopBits = Stop.BITS_10;
        } else if (stop == 15) {
            stopBits = Stop.BITS_15;
        } else if (stop == 20) {
            stopBits = Stop.BITS_20;
        } else {
            stopBits = Stop.BITS_10;
        }

        setSerialParameters(baud, data, parity, stopBits, flow);

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
