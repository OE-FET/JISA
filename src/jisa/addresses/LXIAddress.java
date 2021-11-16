package jisa.addresses;

public class LXIAddress implements Address {

    private final int    board;
    private final String host;

    public LXIAddress(int board, String host) {
        this.board = board;
        this.host = host;
    }

    public LXIAddress(String host) {
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

    public LXIAddress toTCPIPAddress() {
        return this;
    }

    public static class TCPIPParams extends AddressParams<LXIAddress> {

        public TCPIPParams() {

            addParam("Board", false);
            addParam("Host", true);

        }

        @Override
        public LXIAddress createAddress() {
            return new LXIAddress(getInt(0), getString(1));
        }

        @Override
        public String getName() {
            return "LXI / VXI-11 (TCP-IP)";
        }
    }

}
