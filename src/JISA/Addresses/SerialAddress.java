package JISA.Addresses;

public class SerialAddress implements Address {

    private int board;

    public SerialAddress(int board) {
        this.board = board;
    }

    public int getBoard() {
        return board;
    }

    @Override
    public String toString() {
        return String.format("ASRL%d::INSTR", board);
    }

    public SerialAddress toSerialAddress() {
        return this;
    }

}
