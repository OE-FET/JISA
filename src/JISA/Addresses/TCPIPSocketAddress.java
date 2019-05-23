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

    public AddressParams createParams() {

        AddressParams params = new TCPIPSocketParams();
        params.set(0, board);
        params.set(1, host);
        params.set(2, port);

        return params;

    }

    public TCPIPSocketAddress toTCPIPSocketAddress() {
        return this;
    }

    public static class TCPIPSocketParams extends AddressParams<TCPIPSocketAddress> {

        public TCPIPSocketParams() {

            addParam("Board", false);
            addParam("Host", true);
            addParam("Port", false);

        }

        @Override
        public TCPIPSocketAddress createAddress() {
            return new TCPIPSocketAddress(getInt(0), getString(1), getInt(2));
        }

        @Override
        public String getName() {
            return "TCP-IP (Raw Socket)";
        }
    }

}
