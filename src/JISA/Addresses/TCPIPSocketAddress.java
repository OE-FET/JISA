package JISA.Addresses;

public class TCPIPSocketAddress implements Address {

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
    public String toString() {

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

    public TCPIPSocketAddress toTCPIPSocketAddress() {
        return this;
    }

}
