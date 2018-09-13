package JISA.Addresses;

public class StrAddress implements InstrumentAddress {

    private String value;

    public StrAddress(String value) {
        this.value = value;
    }

    @Override
    public String getVISAAddress() {
        return value;
    }
}
