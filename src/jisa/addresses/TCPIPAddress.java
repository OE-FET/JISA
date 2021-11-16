package jisa.addresses;

public class TCPIPAddress implements Address {

    private final int    board;
    private final String host;
    private final int    port;

    public TCPIPAddress(int board, String host, int port) {
        this.board = board;
        this.host = host;
        this.port = port;
    }

    public TCPIPAddress(String address, int port) {
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

    public AddressParams createParams() {

        AddressParams params = new TCPIPSocketParams();
        params.set(0, board);
        params.set(1, host);
        params.set(2, port);

        return params;

    }

    public TCPIPAddress toTCPIPSocketAddress() {
        return this;
    }

    public static class TCPIPSocketParams extends AddressParams<TCPIPAddress> {

        public TCPIPSocketParams() {

            addParam("Board", false);
            addParam("Host", true);
            addParam("Port", false);

        }

        @Override
        public TCPIPAddress createAddress() {
            return new TCPIPAddress(getInt(0), getString(1), getInt(2));
        }

        @Override
        public String getName() {
            return "TCP-IP Socket";
        }
    }

}
