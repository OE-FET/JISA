package jisa.addresses;

import java.util.LinkedList;
import java.util.List;
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
            type = Type.LXI;
        } else if (parts[0].contains("TCPIP") && parts[parts.length - 1].contains("SOCKET")) {
            type = Type.TCPIP;
        } else if (parts[0].contains("MODBUS")) {
            type = Type.MODBUS;
        } else if (parts[0].contains("SNID")) {
            type = Type.ID;
        } else if (parts[0].contains("SERIAL")) {
            type = Type.COM;
        } else if (parts[0].contains("PIPE")) {
            type = Type.PIPE;
        }

        return type;

    }

    default GPIBAddress toGPIBAddress() {

        Pattern pattern = Pattern.compile("GPIB([0-9]*?)::([0-9]+)::INSTR");
        Matcher matcher = pattern.matcher(toString().trim());

        if (matcher.matches()) {
            int board   = Integer.parseInt(matcher.group(1));
            int address = Integer.parseInt(matcher.group(2));
            return new GPIBAddress(board, address);
        } else {
            return null;
        }

    }

    default SerialAddress toSerialAddress() {

        Pattern pattern = Pattern.compile("ASRL::(.*?)::INSTR");
        Matcher matcher = pattern.matcher(toString().trim());

        if (matcher.matches()) {
            String board = matcher.group(1);
            return new SerialAddress(board);
        } else {
            return null;
        }

    }

    default LXIAddress toTCPIPAddress() {

        Pattern pattern = Pattern.compile("TCPIP([0-9]*?)::(.*?)::INSTR");
        Matcher matcher = pattern.matcher(toString().trim());

        if (matcher.matches()) {
            int    board = matcher.group(1).equals("") ? -1 : Integer.parseInt(matcher.group(1));
            String host  = matcher.group(2);
            return new LXIAddress(board, host);
        } else {
            return null;
        }


    }

    default TCPIPAddress toTCPIPSocketAddress() {

        Pattern pattern = Pattern.compile("TCPIP([0-9]*?)::(.*?)::([0-9]*?)::SOCKET");
        Matcher matcher = pattern.matcher(toString().trim());

        if (matcher.matches()) {
            int    board = matcher.group(1).equals("") ? -1 : Integer.parseInt(matcher.group(1));
            String host  = matcher.group(2);
            int    port  = Integer.parseInt(matcher.group(3));
            return new TCPIPAddress(board, host, port);
        } else {
            return null;
        }


    }

    default USBAddress toUSBAddress() {

        Pattern pattern = Pattern.compile("USB([0-9]*?)::(.*?)::(.*?)::(.*?)(?:::([0-9]+))?::INSTR");
        Matcher matcher = pattern.matcher(toString().trim());

        if (matcher.matches()) {
            int    board   = matcher.group(1).equals("") ? -1 : Integer.parseInt(matcher.group(1));
            int    vendor  = Integer.decode(matcher.group(2));
            int    product = Integer.decode(matcher.group(3));
            String serial  = matcher.group(4);
            int    intfce  = matcher.groupCount() <= 5 ? -1 : Integer.parseInt(matcher.group(5));
            return new USBAddress(board, vendor, product, serial, intfce);
        } else {
            return null;
        }


    }

    default ModbusAddress toModbusAddress() {

        Pattern pattern = Pattern.compile("MODBUS::(.*?)::([0-9]*?)::INSTR");
        Matcher matcher = pattern.matcher(toString().trim());

        if (matcher.matches()) {
            String port    = matcher.group(1);
            int    address = Integer.parseInt(matcher.group(2));
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

    default PipeAddress toPipeAddress() {

        Pattern pattern = Pattern.compile("PIPE::(.*?)::INSTR");
        Matcher matcher = pattern.matcher(toString().trim());

        if (matcher.matches()) {
            String sn = matcher.group(1);
            return new PipeAddress(sn);
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

            case LXI:
                return toTCPIPAddress().createParams();

            case TCPIP:
                return toTCPIPSocketAddress().createParams();

            case SERIAL:
                return toSerialAddress().createParams();

            case MODBUS:
                return toModbusAddress().createParams();

            case ID:
                return toIDAddress().createParams();

            case PIPE:
                return toPipeAddress().createParams();

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
        LXI,
        TCPIP,
        SERIAL,
        MODBUS,
        ID,
        COM,
        PIPE,
        UNKOWN
    }

    abstract class AddressParams<I extends Address> {

        private List<String>  names  = new LinkedList<>();
        private List<Boolean> texts  = new LinkedList<>();
        private List<String>  values = new LinkedList<>();

        protected void addParam(String name, boolean text) {
            names.add(name);
            texts.add(text);
            values.add("");
        }

        public void forEach(TriConsumer<Integer, String, Boolean> forEach) {

            for (int i = 0; i < names.size(); i++) {
                forEach.accept(i, names.get(i), texts.get(i));
            }

        }

        public abstract I createAddress();

        public abstract String getName();

        public synchronized void set(int i, String val) {
            values.set(i, val);
        }

        public synchronized void set(int i, int val) { set(i, String.valueOf(val)); }

        public synchronized String getString(int i) {
            return values.get(i);
        }

        public synchronized int getInt(int i) {
            try {
                return Integer.parseInt(values.get(i));
            } catch (Exception e) {
                return 0;
            }
        }

    }

    interface TriConsumer<A, B, C> {

        void accept(A a, B b, C c);

    }

}
