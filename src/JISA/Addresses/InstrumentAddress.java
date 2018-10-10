package JISA.Addresses;

public interface InstrumentAddress {

    String getVISAAddress();

    enum Type {
        GPIB,
        USB,
        TCPIP,
        SERIAL,
        UNKOWN
    }

}
