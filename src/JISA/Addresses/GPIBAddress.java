package JISA.Addresses;

import JISA.InstrumentAddress;

public class GPIBAddress implements InstrumentAddress {

    private int bus;
    private int address;

    public GPIBAddress(int bus, int address) {
        this.bus = bus;
        this.address = address;
    }

    @Override
    public String getVISAAddress() {
        return String.format("GPIB[%d]::%d::INSTR", bus, address);
    }
}
