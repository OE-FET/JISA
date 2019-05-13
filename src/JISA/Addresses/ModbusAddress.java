package JISA.Addresses;

public class ModbusAddress implements Address {

    private final int port;
    private final int address;

    public ModbusAddress(int port, int address) {
        this.port = port;
        this.address = address;
    }

    public String toString() {
        return String.format("MODBUS::%d::%d::INSTR", port, address);
    }

    public int getPort() {
        return port;
    }

    public int getAddress() {
        return address;
    }

}
