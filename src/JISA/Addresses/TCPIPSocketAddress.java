package JISA.Addresses;

public class TCPIPSocketAddress implements InstrumentAddress {

    private int    board;
    private String host;
    private int    port;

    public TCPIPSocketAddress(int board, String host, int port) {
        this.board = board;
        this.host = host;
        this.port = port;
    }

    public TCPIPSocketAddress(String address, int port) {
        this(-1, address, port);
    }

    @Override
    public String getVISAAddress() {

        if (board == -1) {
            return String.format("TCPIP::%s::%d::SOCKET", host, port);
        } else {
            return String.format("TCPIP%d::%s::%d::SOCKET", board, host, port);
        }

    }

    public int getBoard() {
        return board;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

}
