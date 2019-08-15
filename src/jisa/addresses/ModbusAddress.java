package jisa.addresses;

public class ModbusAddress implements Address {

    private final String port;
    private final int    address;

    public ModbusAddress(String port, int address) {
        this.port    = port;
        this.address = address;
    }

    public String toString() {
        return String.format("MODBUS::%s::%d::INSTR", port, address);
    }

    public String getPort() {
        return port;
    }

    public int getAddress() {
        return address;
    }

    public ModbusAddress toModbusAddress() {
        return this;
    }

    public AddressParams createParams() {

        AddressParams params = new ModbusParams();
        params.set(0, port);
        params.set(1, address);

        return params;

    }

    public static class ModbusParams extends AddressParams<ModbusAddress> {

        public ModbusParams() {

            addParam("Port", true);
            addParam("Unit", false);

        }

        @Override
        public ModbusAddress createAddress() {
            return new ModbusAddress(getString(0), getInt(1));
        }

        @Override
        public String getName() {
            return "MODBUS";
        }

    }

}
