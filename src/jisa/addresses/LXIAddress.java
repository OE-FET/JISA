package jisa.addresses;

import jisa.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static jisa.Util.castOrDefault;

public class LXIAddress implements Address {

    private int    board   = -1;
    private String host    = "";
    private String lanName = "";

    public LXIAddress() {}

    public LXIAddress(int board, String host, String lanName) {
        this.board   = board;
        this.host    = host;
        this.lanName = lanName;
    }

    public LXIAddress(int board, String host) {
        this.board = board;
        this.host  = host;
    }

    public LXIAddress(String host, String lanName) {
        this.host    = host;
        this.lanName = lanName;
    }

    public LXIAddress(String host) {
        this.host = host;
    }

    public LXIAddress(Map<String, Object> parameters) {
        setParameters(parameters);
    }

    public int getBoard() {
        return board;
    }

    public void setBoard(int board) {
        this.board = board;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getLanName() {
        return lanName;
    }

    public void setLanName(String lanName) {
        this.lanName = lanName;
    }

    @Override
    public String getTypeName() {
        return "VXI/LXI/VX-11";
    }

    @Override
    public String getVISAString() {

        List<String> parts = new ArrayList<>(4);

        if (board < 0) {
            parts.add("TCPIP");
        } else {
            parts.add(String.format("TCPIP%d", board));
        }

        parts.add(host);

        if (!lanName.isBlank()) {
            parts.add(lanName);
        }

        parts.add("INSTR");

        return String.join("::", parts);

    }

    @Override
    public Map<String, Object> getParameters() {

        return Util.buildMap(map -> {
            map.put("Board Number", board);
            map.put("Host", host);
            map.put("LAN Name", lanName);
        });

    }

    @Override
    public void setParameters(Map<String, Object> parameters) {

        board   = castOrDefault(parameters.getOrDefault("Board Number", board), board);
        host    = castOrDefault(parameters.getOrDefault("Host", host), host);
        lanName = castOrDefault(parameters.getOrDefault("LAN Name", lanName), lanName);

    }

    @Override
    public void parseString(String text) throws InvalidAddressFormatException {

        Pattern pattern = Pattern.compile("TCPIP([0-9]*)::(.*?)(?:::(.*?))?::INSTR");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {

            board   = matcher.group(1).isBlank() ? -1 : Integer.parseInt(matcher.group(1));
            host    = matcher.group(2);
            lanName = (matcher.group(3) == null || matcher.group(3).isBlank()) ? "" : matcher.group(3);

        } else {
            throw new InvalidAddressFormatException(text, "LXI");
        }

    }

    public String toString() {
        return getJISAString();
    }

}
