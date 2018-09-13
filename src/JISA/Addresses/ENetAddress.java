package JISA.Addresses;

public class ENetAddress implements InstrumentAddress {

    private String ip;
    private int    port;

    public ENetAddress(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    @Override
    public String getVISAAddress() {
        return String.format("ASLR::%s::%d::INSTR", ip, port);
    }
}
