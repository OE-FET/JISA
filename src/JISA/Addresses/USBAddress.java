package JISA.Addresses;

public class USBAddress implements InstrumentAddress {

    private int    board;
    private String manufacturer;
    private String model;
    private String serialNumber;
    private int    interfaceNumber;

    public USBAddress(int board, String manufacturer, String model, String serialNumber, int interfaceNumber) {
        this.board = board;
        this.manufacturer = manufacturer;
        this.model = model;
        this.serialNumber = serialNumber;
        this.interfaceNumber = interfaceNumber;
    }

    public USBAddress(int board, String manufacturer, String model, String serialNumber) {
        this(board, manufacturer, model, serialNumber, -1);
    }

    public USBAddress(String manufacturer, String model, String serialNumber, int interfaceNumber) {
        this(-1, manufacturer, model, serialNumber, interfaceNumber);
    }

    public USBAddress(String manufacturer, String model, String serialNumber) {
        this(-1, manufacturer, model, serialNumber, -1);
    }

    public String getVISAAddress() {

        if (board == -1 && interfaceNumber == -1) {
            return String.format("USB::%s::%s::%s::INSTR", manufacturer, model, serialNumber);
        } else if (board == -1) {
            return String.format("USB::%s::%s::%s::%d::INSTR", manufacturer, model, serialNumber, interfaceNumber);
        } else if (interfaceNumber == -1) {
            return String.format("USB%d::%s::%s::%s::INSTR", board, manufacturer, model, serialNumber);
        } else {
            return String.format("USB%d::%s::%s::%s::%d::INSTR", board, manufacturer, model, serialNumber, interfaceNumber);
        }

    }

}
