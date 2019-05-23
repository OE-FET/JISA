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

    public AddressParams createParams() {

        AddressParams params = new SerialParams();
        params.set(0, board);

        return params;

    }

    public static class SerialParams extends AddressParams<SerialAddress> {

        public SerialParams() {

            addParam("Port", false);

        }

        @Override
        public SerialAddress createAddress() {
            return new SerialAddress(getInt(0));
        }

        @Override
        public String getName() {
            return "Serial";
        }
    }

}
