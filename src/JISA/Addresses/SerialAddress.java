package JISA.Addresses;

public class SerialAddress implements InstrumentAddress {

    private int board;

    public SerialAddress(int board) {
        this.board = board;
    }

    @Override
    public String getVISAAddress() {
        return String.format("ASRL%d::INSTR", board);
    }
}
