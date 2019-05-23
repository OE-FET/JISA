package JISA.Addresses;

public class TCPIPAddress implements Address {

    private int    board;
    private String host;

    public TCPIPAddress(int board, String host) {
        this.board = board;
        this.host = host;
    }

    public TCPIPAddress(String host) {
        this(-1, host);
    }

    public int getBoard() {
        return board;
    }

    public String getHost() {
        return host;
    }

    @Override
    public String toString() {

        if (board == -1) {

            return String.format("TCPIP::%s::INSTR", host);

        } else {

            return String.format("TCPIP%d::%s::INSTR", board, host);

        }
    }

    public AddressParams createParams() {

        AddressParams params = new TCPIPParams();
        params.set(0, board);
        params.set(1, host);

        return params;

    }

    public TCPIPAddress toTCPIPAddress() {
        return this;
    }

    public static class TCPIPParams extends AddressParams<TCPIPAddress> {

        public TCPIPParams() {

            addParam("Board", false);
            addParam("Host", true);

        }

        @Override
        public TCPIPAddress createAddress() {
            return new TCPIPAddress(getInt(0), getString(1));
        }

        @Override
        public String getName() {
            return "TCP-IP (VXI-11)";
        }
    }

}
