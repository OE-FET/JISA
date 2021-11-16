package jisa.addresses;

public class StrAddress implements Address {

    private final String value;

    public StrAddress(String value) {
        this.value = value.trim();
    }

    @Override
    public String toString() {
        return value;
    }

    public StrAddress toStrAddress() {
        return this;
    }

    public static class StrParams extends AddressParams<StrAddress> {

        public StrParams() {

            addParam("VISA ID", true);

        }

        @Override
        public StrAddress createAddress() {
            return new StrAddress(getString(0));
        }

        @Override
        public String getName() {
            return "Custom VISA";
        }
    }

}
