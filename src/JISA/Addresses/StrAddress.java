package JISA.Addresses;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StrAddress implements InstrumentAddress {

    private String value;

    public StrAddress(String value) {
        this.value = value.trim();
    }

    @Override
    public String getVISAAddress() {
        return value;
    }

}
