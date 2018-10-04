package JISA.Addresses;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StrAddress implements InstrumentAddress {

    private String value;

    public StrAddress(String value) {
        this.value = value;
    }

    @Override
    public String getVISAAddress() {
        return value;
    }

    public Type getType() {

        Type type = Type.UNKOWN;

        if (value.length() >= 4 && value.substring(0, 4).equals("GPIB")) {
            type = Type.GPIB;
        } else if (value.length() >= 3 && value.substring(0, 3).equals("USB")) {
            type = Type.USB;
        } else if (value.length() >= 4 && value.substring(0, 4).equals("ASRL")) {
            type = Type.SERIAL;
        } else if (value.length() >= 5 && value.substring(0, 5).equals("TCPIP")) {
            type = Type.TCPIP;
        }

        return type;

    }

    public GPIBAddress toGPIBAddress() {

        Pattern pattern = Pattern.compile("GPIB([0-9]*?)::([0-9]+)::INSTR");
        Matcher matcher = pattern.matcher(value.trim());

        if (matcher.matches()) {
            int board   = Integer.valueOf(matcher.group(1));
            int address = Integer.valueOf(matcher.group(2));
            return new GPIBAddress(board, address);
        } else {
            return null;
        }

    }

    public SerialAddress toSerialAddress() {

        Pattern pattern = Pattern.compile("ASRL([0-9]+)::INSTR");
        Matcher matcher = pattern.matcher(value.trim());

        if (matcher.matches()) {
            int board = Integer.valueOf(matcher.group(1));
            return new SerialAddress(board);
        } else {
            return null;
        }

    }

    public TCPIPAddress toTCPIPAddress() {

        Pattern pattern = Pattern.compile("TCPIP([0-9]*?)::(.*?)::INSTR");
        Matcher matcher = pattern.matcher(value.trim());

        if (matcher.matches()) {
            int    board = Integer.valueOf(matcher.group(1));
            String host  = matcher.group(2);
            return new TCPIPAddress(board, host);
        } else {
            return null;
        }


    }

    public USBAddress toUSBAddress() {

        Pattern pattern = Pattern.compile("USB([0-9]*?)::(.*?)::(.*?)::(.*?)(?:::([0-9]+))?::INSTR");
        Matcher matcher = pattern.matcher(value.trim());

        if (matcher.matches()) {
            int    board   = matcher.group(1).equals("") ? -1 : Integer.valueOf(matcher.group(1));
            String vendor  = matcher.group(2);
            String product = matcher.group(3);
            String serial  = matcher.group(4);
            int    intfce  = matcher.group(5).equals("") ? -1 : Integer.valueOf(matcher.group(5));
            return new USBAddress(board, vendor, product, serial, intfce);
        } else {
            return null;
        }


    }

}
