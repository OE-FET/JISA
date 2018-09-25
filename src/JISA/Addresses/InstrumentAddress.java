package JISA.Addresses;

public interface InstrumentAddress {

    public String getVISAAddress();

    public enum Type {
        GPIB,
        USB,
        TCPIP,
        SERIAL,
        UNKOWN
    }

}
