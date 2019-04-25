package JISA.Addresses;

public class GPIBAddress implements Address {

    private int bus;
    private int address;

    public GPIBAddress(int bus, int address) {
        this.bus = bus;
        this.address = address;
    }

    public GPIBAddress(int address) {
        this(0, address);
    }

    public int getBus() {
        return bus;
    }

    public int getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return String.format("GPIB%d::%d::INSTR", bus, address);
    }

    public GPIBAddress toGPIBAddress() {
        return this;
    }

}
