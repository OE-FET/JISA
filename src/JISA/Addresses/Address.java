package JISA.Addresses;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
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
        } else if (parts[0].contains("MODBUS")) {
            type = Type.MODBUS;
        } else if (parts[0].contains("SNID")) {
            type = Type.ID;
        } else if (parts[0].contains("SERIAL")) {
            type = Type.COM;
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

    default ModbusAddress toModbusAddress() {

        Pattern pattern = Pattern.compile("MODBUS::([0-9]*?)::([0-9]*?)::INSTR");
        Matcher matcher = pattern.matcher(toString().trim());

        if (matcher.matches()) {
            int port    = Integer.valueOf(matcher.group(1));
            int address = Integer.valueOf(matcher.group(2));
            return new ModbusAddress(port, address);
        } else {
            return null;
        }

    }

    default IDAddress toIDAddress() {

        Pattern pattern = Pattern.compile("SNID::(.*?)::INSTR");
        Matcher matcher = pattern.matcher(toString().trim());

        if (matcher.matches()) {
            String sn = matcher.group(1);
            return new IDAddress(sn);
        } else {
            return null;
        }

    }

    default COMAddress toCOMAddress() {

        Pattern pattern = Pattern.compile("SERIAL::(.*?)::INSTR");
        Matcher matcher = pattern.matcher(toString().trim());

        if (matcher.matches()) {
            String device = matcher.group(1);
            return new COMAddress(device);
        } else {
            return null;
        }

    }

    default AddressParams createParams() {

        switch (getType()) {

            case GPIB:
                return toGPIBAddress().createParams();

            case USB:
                return toUSBAddress().createParams();

            case TCPIP:
                return toTCPIPAddress().createParams();

            case TCPIP_SOCKET:
                return toTCPIPSocketAddress().createParams();

            case SERIAL:
                return toSerialAddress().createParams();

            case MODBUS:
                return toModbusAddress().createParams();

            case ID:
                return toIDAddress().createParams();

            case COM:
                return toCOMAddress().createParams();

            default:
            case UNKOWN:
                StrAddress.StrParams p = new StrAddress.StrParams();
                p.set(0, toString());
                return p;
        }

    }

    enum Type {
        GPIB,
        USB,
        TCPIP,
        TCPIP_SOCKET,
        SERIAL,
        MODBUS,
        ID,
        COM,
        UNKOWN
    }

    abstract class AddressParams<I extends Address> {

        private List<String>         names  = new LinkedList<>();
        private List<Boolean>        texts  = new LinkedList<>();
        private Map<Integer, Object> values = new HashMap<>();

        protected void addParam(String name, boolean text) {
            names.add(name);
            texts.add(text);
        }

        public void forEach(TriConsumer<Integer, String, Boolean> forEach) {

            for (int i = 0; i < names.size(); i++) {
                forEach.accept(i, names.get(i), texts.get(i));
            }

        }

        public abstract I createAddress();

        public abstract String getName();

        public void set(int i, Object val) {
            values.put(i, val);
        }

        public String getString(int i) {
            return (String) values.getOrDefault(i, "");
        }

        public int getInt(int i) {
            return (int) values.getOrDefault(i, 0);
        }

    }

    interface TriConsumer<A, B, C> {

        void accept(A a, B b, C c);

    }

}
