package jisa.addresses;

import jisa.Util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static jisa.Util.castOrDefault;

public class IDAddress implements Address {

    private String id = "";

    public IDAddress() {}

    public IDAddress(String id) {
        this.id = id;
    }

    public IDAddress(Map<String, Object> parameters) {
        setParameters(parameters);
    }

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }

    @Override
    public String getTypeName() {
        return "SN/ID";
    }

    @Override
    public String getVISAString() {
        return String.format("SNID::%s::INSTR", id);
    }

    @Override
    public Map<String, Object> getParameters() {
        return Util.buildMap(map -> map.put("ID", id));
    }

    @Override
    public void setParameters(Map<String, Object> parameters) {
        id = castOrDefault(parameters.getOrDefault("ID", id), id);
    }

    @Override
    public void parseString(String text) throws InvalidAddressFormatException {

        Pattern pattern = Pattern.compile("SNID::(.*?)::INSTR");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            id = matcher.group(1).trim();
        } else {
            throw new InvalidAddressFormatException(text, "Serial Number or ID");
        }

    }

    public String toString() {
        return getJISAString();
    }

}
