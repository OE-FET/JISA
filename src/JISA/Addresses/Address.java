package JISA.Addresses;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface Address {

    String toString();

    default StrAddress toStrAddress() {
        return new StrAddress(toString());
    }

    default Type getType() {

        Type type = Type.UNKOWN;

        String[] parts = toString().split("::");

        if (parts[0].contains("GPIB")) {
            type = Type.GPIB;
        } else if (parts[0].contains("USB")) {
            type = Type.USB;
        } else if (parts[0].contains("ASRL")) {
            type = Type.SERIAL;
        } else if (parts[0].contains("TCPIP") && parts[parts.length - 1].contains("INSTR")) {
            type = Type.TCPIP;
        } else if (parts[0].contains("TCPIP") && parts[parts.length - 1].contains("SOCKET")) {
            type = Type.TCPIP_SOCKET;
        }

        return type;

    }

    default GPIBAddress toGPIBAddress() {

        Pattern pattern = Pattern.compile("GPIB([0-9]*?)::([0-9]+)::INSTR");
        Matcher matcher = pattern.matcher(toString().trim());

        if (matcher.matches()) {
            int board   = Integer.valueOf(matcher.group(1));
            int address = Integer.valueOf(matcher.group(2));
            return new GPIBAddress(board, address);
        } else {
            return null;
        }

    }

    default SerialAddress toSerialAddress() {

        Pattern pattern = Pattern.compile("ASRL([0-9]+)::INSTR");
        Matcher matcher = pattern.matcher(toString().trim());

        if (matcher.matches()) {
            int board = Integer.valueOf(matcher.group(1));
            return new SerialAddress(board);
        } else {
            return null;
        }

    }

    default TCPIPAddress toTCPIPAddress() {

        Pattern pattern = Pattern.compile("TCPIP([0-9]*?)::(.*?)::INSTR");
        Matcher matcher = pattern.matcher(toString().trim());

        if (matcher.matches()) {
            int    board = matcher.group(1).equals("") ? -1 : Integer.valueOf(matcher.group(1));
            String host  = matcher.group(2);
            return new TCPIPAddress(board, host);
        } else {
            return null;
        }


    }

    default TCPIPSocketAddress toTCPIPSocketAddress() {

        Pattern pattern = Pattern.compile("TCPIP([0-9]*?)::(.*?)::([0-9]*?)::SOCKET");
        Matcher matcher = pattern.matcher(toString().trim());

        if (matcher.matches()) {
            int    board = matcher.group(1).equals("") ? -1 : Integer.valueOf(matcher.group(1));
            String host  = matcher.group(2);
            int    port  = Integer.valueOf(matcher.group(3));
            return new TCPIPSocketAddress(board, host, port);
        } else {
            return null;
        }


    }

    default USBAddress toUSBAddress() {

        Pattern pattern = Pattern.compile("USB([0-9]*?)::(.*?)::(.*?)::(.*?)(?:::([0-9]+))?::INSTR");
        Matcher matcher = pattern.matcher(toString().trim());

        if (matcher.matches()) {
            int    board   = matcher.group(1).equals("") ? -1 : Integer.valueOf(matcher.group(1));
            int    vendor  = Integer.decode(matcher.group(2));
            int    product = Integer.decode(matcher.group(3));
            String serial  = matcher.group(4);
            int    intfce  = matcher.groupCount() <= 5 ? -1 : Integer.valueOf(matcher.group(5));
            return new USBAddress(board, vendor, product, serial, intfce);
        } else {
            return null;
        }


    }

    enum Type {
        GPIB,
        USB,
        TCPIP,
        TCPIP_SOCKET,
        SERIAL,
        UNKOWN
    }

}
