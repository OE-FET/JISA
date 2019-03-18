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

    public TCPIPAddress toTCPIPAddress() {
        return this;
    }

}
