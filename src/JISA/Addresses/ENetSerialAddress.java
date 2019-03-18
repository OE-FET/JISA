package JISA.Addresses;

public class ENetSerialAddress implements Address {

    private String ip;
    private int    port;

    public ENetSerialAddress(String host, int port) {
        this.ip = host;
        this.port = port;
    }

    @Override
    public String toString() {
        return String.format("ASRL::%s::%d::INSTR", ip, port);
    }

    public ENetSerialAddress toENetSerialAddress() {
        return this;
    }

}
