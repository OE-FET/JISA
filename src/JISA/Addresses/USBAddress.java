package JISA.Addresses;

import java.util.ArrayList;

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

    public USBAddress(String manufacturer, String model) {
        this(-1, manufacturer, model, null);
    }

    public int getBoard() {
        return board;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public String getModel() {
        return model;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public int getInterfaceNumber() {
        return interfaceNumber;
    }

    public String getVISAAddress() {

        ArrayList<String> parts = new ArrayList<>();

        if (board == -1) {
            parts.add("USB");
        } else {
            parts.add(String.format("USB%d", board));
        }

        parts.add(manufacturer);
        parts.add(model);

        if (serialNumber != null) {
            parts.add(serialNumber);
        }

        if (interfaceNumber != -1) {
            parts.add(String.format("%d", interfaceNumber));
        }

        parts.add("INSTR");

        return String.join("::", parts);

    }

}
