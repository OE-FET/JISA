package jisa.addresses;

import jisa.Util;

import java.util.Map;

public class VISAAddress implements Address {

    private String address = "";

    public VISAAddress() {}

    public VISAAddress(String address) {
        setAddress(address);
    }

    public VISAAddress(Map<String, Object> parameters) {
        setParameters(parameters);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String getTypeName() {
        return "VISA Address";
    }

    @Override
    public String getVISAString() {
        return address;
    }

    @Override
    public Map<String, Object> getParameters() {
        return Util.buildMap(map -> map.put("Address", address));
    }

    @Override
    public void setParameters(Map<String, Object> parameters) {
        address = Util.castOrDefault(parameters.getOrDefault("Address", address), address);
    }

    @Override
    public void parseString(String text) throws InvalidAddressFormatException {
        setAddress(text);
    }

    public String toString() {
        return getJISAString();
    }

}
