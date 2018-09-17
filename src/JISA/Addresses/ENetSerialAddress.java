package JISA.Addresses;

public class ENetSerialAddress implements InstrumentAddress {

    private String ip;
    private int    port;

    public ENetSerialAddress(String host, int port) {
        this.ip = host;
        this.port = port;
    }

    @Override
    public String getVISAAddress() {
        return String.format("ASRL::%s::%d::INSTR", ip, port);
    }
}
