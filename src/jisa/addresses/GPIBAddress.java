package jisa.addresses;

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

    public AddressParams createParams() {

        AddressParams params = new GPIBParams();
        params.set(0, bus);
        params.set(1, address);

        return params;

    }

    public static class GPIBParams extends AddressParams<GPIBAddress> {

        public GPIBParams() {

            addParam("Board", false);
            addParam("Address", false);

        }

        @Override
        public GPIBAddress createAddress() {
            return new GPIBAddress(getInt(0), getInt(1));
        }

        @Override
        public String getName() {
            return "GPIB";
        }
    }

}
