package JISA.Addresses;

public class TCPIPAddress implements InstrumentAddress {

    private int    board;
    private String host;

    public TCPIPAddress(int board, String host) {
        this.board = board;
        this.host = host;
    }

    public TCPIPAddress(String host) {
        this(-1, host);
    }

    @Override
    public String getVISAAddress() {

        if (board == -1) {

            return String.format("TCPIP::%s::INSTR", host);

        } else {

            return String.format("TCPIP%d::%s::INSTR", board, host);

        }
    }

}
