package jisa.addresses;

import jisa.Util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static jisa.Util.castOrDefault;

public class ModbusAddress implements Address {

    private String portName = "";
    private int    address  = -1;

    public ModbusAddress() {}

    public ModbusAddress(String portName, int address) {
        this.portName = portName;
        this.address  = address;
    }

    public ModbusAddress(Map<String, Object> parameters) {
        setParameters(parameters);
    }

    public String getPortName() {
        return portName;
    }

    public void setPortName(String portName) {
        this.portName = portName;
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    @Override
    public String getTypeName() {
        return "MODBUS-RTU";
    }

    @Override
    public String getVISAString() {
        return String.format("MODBUS::%s::%d::INSTR", portName, address);
    }

    @Override
    public Map<String, Object> getParameters() {

        return Util.buildMap(map -> {
            map.put("Port Name", portName);
            map.put("Address", address);
        });

    }

    @Override
    public void setParameters(Map<String, Object> parameters) {

        portName = castOrDefault(parameters.getOrDefault("Port Name", portName), portName);
        address  = castOrDefault(parameters.getOrDefault("Address", address), address);

    }

    @Override
    public void parseString(String text) throws InvalidAddressFormatException {

        Pattern pattern = Pattern.compile("MODBUS::(.+?)::([0-9]+)::INSTR");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {

            portName = matcher.group(1);
            address  = Integer.parseInt(matcher.group(2));

        } else {
            throw new InvalidAddressFormatException(text, "MODBUS");
        }

    }

    public String toString() {
        return getJISAString();
    }
}
