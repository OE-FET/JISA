package jisa.addresses;

import jisa.Util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static jisa.Util.castOrDefault;

public class SerialAddress implements Address {

    private String portName = "";
    private int    asrlNum  = -1;

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
            return String.format("ASRL::%s::INSTR", getPortName());
        }

    }

    @Override
    public Map<String, Object> getParameters() {

        return Util.buildMap(map -> {
            map.put("Port Name", getPortName());
            map.put("ASRL Number", getASRLNum());
        });

    }

    @Override
    public void setParameters(Map<String, Object> parameters) {

        portName = castOrDefault(parameters.getOrDefault("Port Name", portName), portName);
        asrlNum  = castOrDefault(parameters.getOrDefault("ASRL Number", asrlNum), asrlNum);

    }

    @Override
    public void parseString(String text) throws InvalidAddressFormatException {

        Pattern jisa = Pattern.compile("ASRL::(.+?)::INSTR");
        Pattern visa = Pattern.compile("ASRL([0-9]+)::INSTR");

        Matcher jMatcher = jisa.matcher(text);
        Matcher vMatcher = visa.matcher(text);

        if (jMatcher.find()) {
            portName = jMatcher.group(1).trim();
        } else if (vMatcher.find()) {
            asrlNum  = Integer.parseInt(vMatcher.group(1));
        } else {
            throw new InvalidAddressFormatException(text, "Serial");
        }

    }

    public String toString() {
        return getJISAString();
    }

}
