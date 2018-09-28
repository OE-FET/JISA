package JISA.Addresses;

public class GPIBAddress implements InstrumentAddress {

    private int bus;
    private int address;

    public GPIBAddress(int bus, int address) {
        this.bus = bus;
        this.address = address;
    }

    public int getBus() {
        return bus;
    }

    public int getAddress() {
        return address;
    }

    @Override
    public String getVISAAddress() {
        return String.format("GPIB%d::%d::INSTR", bus, address);
    }
}
