package jisa.addresses;

import jisa.Util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static jisa.Util.castOrDefault;

public class PipeAddress implements Address {

    private String pipeName = "";

    public PipeAddress() {}

    public PipeAddress(String pipeName) {
        this.pipeName = pipeName;
    }

    public PipeAddress(Map<String, Object> parameters) {
        setParameters(parameters);
    }

    public String getPipeName() {
        return pipeName;
    }

    public void setPipeName(String pipeName) {
        this.pipeName = pipeName;
    }

    @Override
    public String getTypeName() {
        return "Pipe";
    }

    @Override
    public String getVISAString() {
        return String.format("PIPE::%s::INSTR", pipeName);
    }

    @Override
    public Map<String, Object> getParameters() {
        return Util.buildMap(map -> map.put("Pipe Name", pipeName));
    }

    @Override
    public void setParameters(Map<String, Object> parameters) {
        pipeName = castOrDefault(parameters.getOrDefault("Pipe Name", pipeName), pipeName);
    }

    @Override
    public void parseString(String text) throws InvalidAddressFormatException {

        Pattern pattern = Pattern.compile("PIPE::(.+?)::INSTR");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            pipeName = matcher.group(1);
        } else {
            throw new InvalidAddressFormatException(text, "Pipe");
        }

    }

    public String toString() {
        return getJISAString();
    }

}
