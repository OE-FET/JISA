package jisa.addresses;

import jisa.Util;
import jisa.devices.DeviceException;

import java.util.Map;

import static jisa.Util.castOrDefault;

public abstract class USBAddress implements Address {

    protected int    board           = -1;
    protected int    vendorID        = -1;
    protected int    productID       = -1;
    protected String serialNumber    = "";
    protected int    interfaceNumber = -1;

    public USBAddress() {}

    public USBAddress(int board, int vendorID, int productID, String serialNumber, int interfaceNumber) {
        this.board           = board;
        this.vendorID        = vendorID;
        this.productID       = productID;
        this.serialNumber    = serialNumber;
        this.interfaceNumber = interfaceNumber;
    }

    public USBAddress(int vendorID, int productID, String serialNumber, int interfaceNumber) {
        this.vendorID        = vendorID;
        this.productID       = productID;
        this.serialNumber    = serialNumber;
        this.interfaceNumber = interfaceNumber;
    }

    public USBAddress(int vendorID, int productID, String serialNumber) {
        this.vendorID     = vendorID;
        this.productID    = productID;
        this.serialNumber = serialNumber;
    }

    public USBAddress(int vendorID, int productID, int interfaceNumber) {
        this.vendorID        = vendorID;
        this.productID       = productID;
        this.interfaceNumber = interfaceNumber;
    }

    public USBAddress(int vendorID, int productID) {
        this.vendorID     = vendorID;
        this.productID    = productID;
    }

    public USBAddress(String address) throws DeviceException
    {
        if (!address.contains("USB") && !address.contains("INSTR")) {
            throw new DeviceException("Address is not not USB address, does not contain USB and INSTR");
        }

        String[] addressSplit = address.split("::");

        int manufacturer = Integer.decode(addressSplit[1]);
        int model = Integer.decode(addressSplit[2]);
        String serialNumber = addressSplit[3];

        this.vendorID = manufacturer;
        this.productID = model;
        this.serialNumber = serialNumber;
    }

    public int getInterfaceNumber() {
        return interfaceNumber;
    }

    public void setInterfaceNumber(int interfaceNumber) {
        this.interfaceNumber = interfaceNumber;
    }

    public int getBoard() {
        return board;
    }

    public void setBoard(int board) {
        this.board = board;
    }

    public int getVendorID() {
        return vendorID;
    }

    public void setVendorID(int vendorID) {
        this.vendorID = vendorID;
    }

    public int getProductID() {
        return productID;
    }

    public void setProductID(int productID) {
        this.productID = productID;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    @Override
    public Map<String, Object> getParameters() {

        return Util.buildMap(map -> {
            map.put("Board", board);
            map.put("Vendor ID", vendorID);
            map.put("Product ID", productID);
            map.put("Serial Number", serialNumber);
            map.put("Interface Number", interfaceNumber);
        });

    }

    @Override
    public void setParameters(Map<String, Object> parameters) {

        board           = castOrDefault(parameters.getOrDefault("Board", board), board);
        vendorID        = castOrDefault(parameters.getOrDefault("Vendor ID", vendorID), vendorID);
        productID       = castOrDefault(parameters.getOrDefault("Product ID", productID), productID);
        serialNumber    = castOrDefault(parameters.getOrDefault("Serial Number", serialNumber), serialNumber);
        interfaceNumber = castOrDefault(parameters.getOrDefault("Interface Number", interfaceNumber), interfaceNumber);

    }

    public String toString() {
        return getJISAString();
    }

}
