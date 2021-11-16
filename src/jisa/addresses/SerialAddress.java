package jisa.addresses;

public class SerialAddress implements Address {

    private final String port;

    public SerialAddress(String port) {
        this.port = port;
    }

    public String getPort() {
        return port;
    }

    @Override
    public String toString() {
        return String.format("ASRL::%s::INSTR", port);
    }

    public SerialAddress toSerialAddress() {
        return this;
    }

    public AddressParams createParams() {

        AddressParams params = new SerialParams();
        params.set(0, port);
        return params;

    }

    public static class SerialParams extends AddressParams<SerialAddress> {

        public SerialParams() {

            addParam("Port", true);

        }

        @Override
        public SerialAddress createAddress() {
            return new SerialAddress(getString(0));
        }

        @Override
        public String getName() {
            return "Serial";
        }
    }

}
