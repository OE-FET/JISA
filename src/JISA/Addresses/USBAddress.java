package JISA.Addresses;

import java.util.ArrayList;

public class USBAddress implements Address {

    private int    board;
    private int    manufacturer;
    private int    model;
    private String serialNumber;
    private int    interfaceNumber;

    public USBAddress(int board, int manufacturer, int model, String serialNumber, int interfaceNumber) {
        this.board = board;
        this.manufacturer = manufacturer;
        this.model = model;
        this.serialNumber = serialNumber;
        this.interfaceNumber = interfaceNumber;
    }

    public USBAddress(int board, int manufacturer, int model, String serialNumber) {
        this(board, manufacturer, model, serialNumber, -1);
    }

    public USBAddress(int manufacturer, int model, String serialNumber, int interfaceNumber) {
        this(-1, manufacturer, model, serialNumber, interfaceNumber);
    }

    public USBAddress(int manufacturer, int model, String serialNumber) {
        this(-1, manufacturer, model, serialNumber, -1);
    }

    public USBAddress(int manufacturer, int model) {
        this(-1, manufacturer, model, null);
    }

    public int getBoard() {
        return board;
    }

    public int getManufacturer() {
        return manufacturer;
    }

    public int getModel() {
        return model;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public int getInterfaceNumber() {
        return interfaceNumber;
    }

    public String toString() {

        ArrayList<String> parts = new ArrayList<>();

        if (board == -1) {
            parts.add("USB");
        } else {
            parts.add(String.format("USB%d", board));
        }

        parts.add(String.format("0x%04X", manufacturer));
        parts.add(String.format("0x%04X", model));

        if (serialNumber != null) {
            parts.add(serialNumber);
        }

        if (interfaceNumber != -1) {
            parts.add(String.format("%d", interfaceNumber));
        }

        parts.add("INSTR");

        return String.join("::", parts);

    }

    public USBAddress toUSBAddress() {
        return this;
    }

}
