package jisa.addresses;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class USBRawAddress extends USBAddress {

    public USBRawAddress() {}

    public USBRawAddress(int board, int vendorID, int productID, String serialNumber, int interfaceNumber) {
        super(board, vendorID, productID, serialNumber, interfaceNumber);
    }

    public USBRawAddress(int vendorID, int productID, String serialNumber, int interfaceNumber) {
        super(vendorID, productID, serialNumber, interfaceNumber);
    }

    public USBRawAddress(int vendorID, int productID, String serialNumber) {
        super(vendorID, productID, serialNumber);
    }

    public USBRawAddress(int vendorID, int productID, int interfaceNumber) {
        super(vendorID, productID, interfaceNumber);
    }

    public USBRawAddress(int vendorID, int productID) {
        super(vendorID, productID);
    }

    @Override
    public String getTypeName() {
        return "USB Raw";
    }

    @Override
    public String getVISAString() {

        List<String> parts = new ArrayList<>();

        if (board < 0) {
            parts.add("USB");
        } else {
            parts.add(String.format("USB%d", board));
        }

        parts.add(String.format("0x%04X", vendorID));
        parts.add(String.format("0x%04X", productID));

        parts.add(serialNumber);

        if (interfaceNumber >= 0) {
            parts.add(String.format("%d", interfaceNumber));
        }

        parts.add("RAW");

        return String.join("::", parts);

    }

    @Override
    public void parseString(String text) throws InvalidAddressFormatException {

        Pattern pattern = Pattern.compile("USB([0-9]*)::(0[xX][0-9a-fA-F]+)::(0[xX][0-9a-fA-F]+)::(.*?)(?:::([0-9]+))?::RAW");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {

            board           = (matcher.group(1) == null || matcher.group(1).isBlank()) ? -1 : Integer.parseInt(matcher.group(1));
            vendorID        = Integer.decode(matcher.group(2));
            productID       = Integer.decode(matcher.group(3));
            serialNumber    = matcher.group(4).trim();
            interfaceNumber = (matcher.group(5) == null || matcher.group(5).isBlank()) ? -1 : Integer.parseInt(matcher.group(5));

        } else {
            throw new InvalidAddressFormatException(text, "USB-Raw");
        }

    }
}
