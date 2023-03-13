package jisa.addresses;

import jisa.Util;
import jisa.visa.connections.SerialConnection;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static jisa.Util.castOrDefault;

public class SerialAddress implements Address {

    private String portName = "";
    private int    asrlNum  = -1;
    private Baud   baudRate = Baud.AUTO;
    private Data   dataBits = Data.AUTO;
    private Parity parity   = Parity.AUTO;
    private Stop   stopBits = Stop.AUTO;

    public SerialAddress() {}

    public SerialAddress(String portName, int asrlNum) {
        this.portName = portName;
        this.asrlNum  = asrlNum;
    }

    public SerialAddress(String portName) {
        this(portName, -1);
    }

    public SerialAddress(int asrlNum) {
        this(null, asrlNum);
    }

    public SerialAddress(Map<String, Object> parameters) {
        setParameters(parameters);
    }

    public String getPortName() {
        return portName;
    }

    public int getASRLNum() {
        return asrlNum;
    }

    public void setPortName(String portName) {
        this.portName = portName;
    }

    public Baud getBaudRate() {
        return baudRate;
    }

    public void setBaudRate(Baud baudRate) {
        this.baudRate = baudRate;
    }

    public Data getDataBits() {
        return dataBits;
    }

    public void setDataBits(Data dataBits) {
        this.dataBits = dataBits;
    }

    public Parity getParity() {
        return parity;
    }

    public void setParity(Parity parity) {
        this.parity = parity;
    }

    public Stop getStopBits() {
        return stopBits;
    }

    public void setStopBits(Stop stopBits) {
        this.stopBits = stopBits;
    }

    @Override
    public String getTypeName() {
        return "Serial";
    }

    @Override
    public String getVISAString() {
        return String.format("ASRL%d::INSTR", getASRLNum());
    }

    public String getJISAString() {

        if (portName.isBlank()) {
            return getVISAString();
        } else {

            if (hasParametersSpecified()) {
                return String.format("ASRL::%s::%s::%s::%s::%s::INSTR", getPortName(), getBaudRate().getTag(), getDataBits().getTag(), getParity().getTag(), getStopBits().getTag());
            } else {
                return String.format("ASRL::%s::INSTR", getPortName());
            }

        }

    }

    public boolean hasParametersSpecified() {
        return getBaudRate() != Baud.AUTO && getDataBits() != Data.AUTO && getParity() != Parity.AUTO && getStopBits() != Stop.AUTO;
    }

    @Override
    public Map<String, Object> getParameters() {

        return Util.buildMap(map -> {
            map.put("Port Name", getPortName());
            map.put("ASRL Number", getASRLNum());
            map.put("Baud Rate", getBaudRate());
            map.put("Data Bits", getDataBits());
            map.put("Parity", getParity());
            map.put("Stop Bits", getStopBits());
        });

    }

    @Override
    public void setParameters(Map<String, Object> parameters) {

        portName = castOrDefault(parameters.getOrDefault("Port Name", portName), portName);
        asrlNum  = castOrDefault(parameters.getOrDefault("ASRL Number", asrlNum), asrlNum);
        baudRate = castOrDefault(parameters.getOrDefault("Baud Rate", baudRate), baudRate);
        dataBits = castOrDefault(parameters.getOrDefault("Data Bits", dataBits), dataBits);
        parity   = castOrDefault(parameters.getOrDefault("Parity", parity), parity);
        stopBits = castOrDefault(parameters.getOrDefault("Stop Bits", stopBits), stopBits);

    }

    @Override
    public void parseString(String text) throws InvalidAddressFormatException {

        Pattern jisa = Pattern.compile("ASRL::(.+?)(?:::([0-9]+)::([1-8])::([NEOMS])::([12]\\.[05]))?::INSTR");
        Pattern visa = Pattern.compile("ASRL([0-9]+)::INSTR");

        Matcher jMatcher = jisa.matcher(text);
        Matcher vMatcher = visa.matcher(text);

        if (jMatcher.find()) {
            portName = jMatcher.group(1).trim();
            baudRate = jMatcher.group(2) != null && !jMatcher.group(2).isBlank() ? Baud.find(jMatcher.group(2)) : baudRate;
            dataBits = jMatcher.group(3) != null && !jMatcher.group(3).isBlank() ? Data.find(jMatcher.group(3)) : dataBits;
            parity   = jMatcher.group(4) != null && !jMatcher.group(4).isBlank() ? Parity.find(jMatcher.group(4)) : parity;
            stopBits = jMatcher.group(5) != null && !jMatcher.group(5).isBlank() ? Stop.find(jMatcher.group(5)) : stopBits;
        } else if (vMatcher.find()) {
            asrlNum = Integer.parseInt(vMatcher.group(1));
        } else {
            throw new InvalidAddressFormatException(text, "Serial");
        }

    }

    public String toString() {
        return getJISAString();
    }

    public enum Baud {

        AUTO("Auto", -1),
        BAUD_110("110", 110),
        BAUD_300("300", 300),
        BAUD_1200("1200", 1200),
        BAUD_2400("2400", 2400),
        BAUD_4800("4800", 4800),
        BAUD_9600("9600", 9600),
        BAUD_19200("19200", 19200),
        BAUD_38400("38400", 38400),
        BAUD_57600("57600", 57600),
        BAUD_115200("115200", 115200);

        public static Baud find(String tag) {
            return Arrays.stream(values()).filter(e -> e.getTag().equalsIgnoreCase(tag)).findFirst().orElse(AUTO);
        }

        private final String text;
        private final int    value;

        Baud(String text, int value) {
            this.text  = text;
            this.value = value;
        }

        public String toString() {
            return text;
        }

        public String getTag() {
            return text;
        }

        public int getValue() {
            return value;
        }

    }

    public enum Data {

        AUTO("Auto", "", -1),
        DATA_1("1 Bit", "1", 1),
        DATA_2("2 Bits", "2", 2),
        DATA_3("3 Bits", "3", 3),
        DATA_4("4 Bits", "4", 4),
        DATA_5("5 Bits", "5", 5),
        DATA_6("6 Bits", "6", 6),
        DATA_7("7 Bits", "7", 7),
        DATA_8("8 Bits", "8", 8);

        public static Data find(String tag) {
            return Arrays.stream(values()).filter(e -> e.getTag().equalsIgnoreCase(tag)).findFirst().orElse(AUTO);
        }

        private final String text;
        private final String tag;
        private final int    value;

        Data(String text, String tag, int value) {
            this.text  = text;
            this.tag   = tag;
            this.value = value;
        }

        public String toString() {
            return text;
        }

        public String getTag() {
            return tag;
        }

        public int getValue() {
            return value;
        }

    }

    public enum Parity {

        AUTO("Auto", "", null),
        NONE("None", "N", SerialConnection.Parity.NONE),
        EVEN("Even", "E", SerialConnection.Parity.EVEN),
        ODD("Odd", "O", SerialConnection.Parity.ODD),
        MARK("Mark", "M", SerialConnection.Parity.MARK),
        SPACE("Space", "S", SerialConnection.Parity.SPACE);

        public static Parity find(String tag) {
            return Arrays.stream(values()).filter(e -> e.getTag().equalsIgnoreCase(tag)).findFirst().orElse(AUTO);
        }

        private final String                  text;
        private final String                  tag;
        private final SerialConnection.Parity value;

        Parity(String text, String tag, SerialConnection.Parity value) {
            this.text  = text;
            this.tag   = tag;
            this.value = value;
        }

        public String toString() {
            return text;
        }

        public String getTag() {
            return tag;
        }

        public SerialConnection.Parity getValue() {
            return value;
        }

    }

    public enum Stop {

        AUTO("Auto", "", null),
        STOP_1_0("1 Bit", "1.0", SerialConnection.Stop.BITS_10),
        STOP_1_5("1.5 Bits", "1.5", SerialConnection.Stop.BITS_15),
        STOP_2_0("2 Bits", "2.0", SerialConnection.Stop.BITS_20);

        public static Stop find(String tag) {
            return Arrays.stream(values()).filter(e -> e.getTag().equalsIgnoreCase(tag)).findFirst().orElse(AUTO);
        }

        private final String                text;
        private final String                tag;
        private final SerialConnection.Stop value;

        Stop(String text, String tag, SerialConnection.Stop value) {
            this.text  = text;
            this.tag   = tag;
            this.value = value;
        }

        public String toString() {
            return text;
        }

        public String getTag() {
            return tag;
        }

        public SerialConnection.Stop getValue() {
            return value;
        }

    }

}
