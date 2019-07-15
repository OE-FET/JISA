package jisa.addresses;

public class IDAddress implements Address {

    private String value;

    public IDAddress(String value) {
        this.value = value.trim();
    }

    @Override
    public String toString() {
        return String.format("SNID::%s::INSTR", value);
    }

    public IDAddress toIDAddress() {
        return this;
    }

    public AddressParams createParams() {

        AddressParams params = new IDParams();
        params.set(0, value);

        return params;

    }

    public String getID() {
        return value;
    }

    public static class IDParams extends AddressParams<IDAddress> {

        public IDParams() {

            addParam("Serial No.", true);

        }

        @Override
        public IDAddress createAddress() {
            return new IDAddress(getString(0));
        }

        @Override
        public String getName() {
            return "Serial No.";
        }
    }

}
