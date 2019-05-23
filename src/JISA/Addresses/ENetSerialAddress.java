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

    public AddressParams createParams() {

        AddressParams params = new ENetParams();
        params.set(0, ip);
        params.set(1, port);

        return params;

    }

    public static class ENetParams extends AddressParams<ENetSerialAddress> {

        public ENetParams() {

            addParam("Host", true);
            addParam("Port", false);

        }

        @Override
        public ENetSerialAddress createAddress() {
            return new ENetSerialAddress(getString(0), getInt(1));
        }

        @Override
        public String getName() {
            return "Ethernet Serial";
        }
    }

}
