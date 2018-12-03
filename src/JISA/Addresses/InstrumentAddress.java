package JISA.Addresses;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface InstrumentAddress {

    String getVISAAddress();

    enum Type {
        GPIB,
        USB,
        TCPIP,
        TCPIP_SOCKET,
        SERIAL,
        UNKOWN
    }

    public default StrAddress toStrAddress() {
        return new StrAddress(getVISAAddress());
    }

    public default Type getType() {

        Type type = Type.UNKOWN;

        String[] parts = getVISAAddress().split("::");

        if (parts[0].contains("GPIB")) {
            type = Type.GPIB;
        } else if (parts[0].contains("USB")) {
            type = Type.USB;
        } else if (parts[0].contains("ASRL")) {
            type = Type.SERIAL;
        } else if (parts[0].contains("TCPIP") && parts[parts.length-1 ].contains("INSTR")) {
            type = Type.TCPIP;
        } else if (parts[0].contains("TCPIP") && parts[parts.length-1 ].contains("SOCKET")) {
            type = Type.TCPIP_SOCKET;
        }

        return type;

    }

    public default GPIBAddress toGPIBAddress() {

        Pattern pattern = Pattern.compile("GPIB([0-9]*?)::([0-9]+)::INSTR");
        Matcher matcher = pattern.matcher(getVISAAddress().trim());

        if (matcher.matches()) {
            int board   = Integer.valueOf(matcher.group(1));
            int address = Integer.valueOf(matcher.group(2));
            return new GPIBAddress(board, address);
        } else {
            return null;
        }

    }

    public default SerialAddress toSerialAddress() {

        Pattern pattern = Pattern.compile("ASRL([0-9]+)::INSTR");
        Matcher matcher = pattern.matcher(getVISAAddress().trim());

        if (matcher.matches()) {
            int board = Integer.valueOf(matcher.group(1));
            return new SerialAddress(board);
        } else {
            return null;
        }

    }

    public default TCPIPAddress toTCPIPAddress() {

        Pattern pattern = Pattern.compile("TCPIP([0-9]*?)::(.*?)::INSTR");
        Matcher matcher = pattern.matcher(getVISAAddress().trim());

        if (matcher.matches()) {
            int    board = matcher.group(1).equals("") ? -1 : Integer.valueOf(matcher.group(1));
            String host  = matcher.group(2);
            return new TCPIPAddress(board, host);
        } else {
            return null;
        }


    }

    public default TCPIPSocketAddress toTCPIPSocketAddress() {

        Pattern pattern = Pattern.compile("TCPIP([0-9]*?)::(.*?)::([0-9]*?)::SOCKET");
        Matcher matcher = pattern.matcher(getVISAAddress().trim());

        if (matcher.matches()) {
            int    board = matcher.group(1).equals("") ? -1 :Integer.valueOf(matcher.group(1));
            String host  = matcher.group(2);
            int    port  = Integer.valueOf(matcher.group(3));
            return new TCPIPSocketAddress(board, host, port);
        } else {
            return null;
        }


    }

    public default USBAddress toUSBAddress() {

        Pattern pattern = Pattern.compile("USB([0-9]*?)::(.*?)::(.*?)::(.*?)(?:::([0-9]+))?::INSTR");
        Matcher matcher = pattern.matcher(getVISAAddress().trim());

        if (matcher.matches()) {
            int    board   = matcher.group(1).equals("") ? -1 : Integer.valueOf(matcher.group(1));
            String vendor  = matcher.group(2);
            String product = matcher.group(3);
            String serial  = matcher.group(4);
            int    intfce  = matcher.groupCount() <= 5 ? -1 : Integer.valueOf(matcher.group(5));
            return new USBAddress(board, vendor, product, serial, intfce);
        } else {
            return null;
        }


    }

}
