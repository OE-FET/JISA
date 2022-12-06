package jisa.addresses;

import jisa.Util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static jisa.Util.castOrDefault;

public class TCPIPAddress implements Address {

    private int    board = -1;
    private String host  = "";
    private int    port  = 0;

    public TCPIPAddress() {}

    public TCPIPAddress(int board, String host, int port) {
        this.board = board;
        this.host  = host;
        this.port  = port;
    }

    public TCPIPAddress(String host, int port) {
        this(-1, host, port);
    }

    public TCPIPAddress(Map<String, Object> parameters) {
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

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String getTypeName() {
        return "TCP-IP Socket";
    }

    @Override
    public String getVISAString() {

        if (board < 0) {
            return String.format("TCPIP::%s::%d::SOCKET", host, port);
        } else {
            return String.format("TCPIP%d::%s::%d::SOCKET", board, host, port);
        }

    }

    @Override
    public Map<String, Object> getParameters() {

        return Util.buildMap(map -> {
            map.put("Board Number", board);
            map.put("Host", host);
            map.put("Port", port);
        });

    }

    @Override
    public void setParameters(Map<String, Object> parameters) {

        board = castOrDefault(parameters.getOrDefault("Board", board), board);
        host  = castOrDefault(parameters.getOrDefault("Host", host), host);
        port  = castOrDefault(parameters.getOrDefault("Port", port), port);

    }

    @Override
    public void parseString(String text) throws InvalidAddressFormatException {

        Pattern pattern = Pattern.compile("TCPIP([0-9]*)::(.*?)::([0-9]+)::SOCKET");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {

            board = matcher.group(1).isBlank() ? -1 : Integer.parseInt(matcher.group(1));
            host  = matcher.group(2);
            port  = Integer.parseInt(matcher.group(3));

        } else {
            throw new InvalidAddressFormatException(text, "TCPIP Socket");
        }

    }

    public String toString() {
        return getJISAString();
    }

}
