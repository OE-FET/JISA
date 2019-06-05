package JISA.Addresses;

public class COMAddress implements Address {

    private String device;

    public COMAddress(String device) {
        this.device = device;
    }

    public String getDevice() {
        return device;
    }

    @Override
    public String toString() {
        return String.format("SERIAL::%s::INSTR", device);
    }

    public COMAddress toCOMAddress() {
        return this;
    }

    public AddressParams createParams() {

        AddressParams params = new COMParams();
        params.set(1, device);

        return params;

    }

    public static class COMParams extends AddressParams<COMAddress> {

        public COMParams() {

            addParam("Port", true);

        }

        @Override
        public COMAddress createAddress() {
            return new COMAddress(getString(0));
        }

        @Override
        public String getName() {
            return "Serial (Native)";
        }
    }

}
