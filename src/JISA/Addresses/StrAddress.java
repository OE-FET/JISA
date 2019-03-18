package JISA.Addresses;

public class StrAddress implements Address {

    private String value;

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

}
