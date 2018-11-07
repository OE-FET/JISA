package JISA.Addresses;

public interface InstrumentAddress {

    String getVISAAddress();

    enum Type {
        GPIB,
        USB,
        TCPIP,
        TCPIP_SOCKET,
        SERIAL,
        UNKOWN
    }

    public default StrAddress toStrAddress() {
        return new StrAddress(getVISAAddress());
    }

}
