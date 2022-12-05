package jisa.addresses;

import jisa.Util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static jisa.Util.castOrDefault;

public class GPIBAddress implements Address {

    private int boardNumber      = 0;
    private int primaryAddress   = 0;
    private int secondaryAddress = 0;

    public GPIBAddress() {}

    public GPIBAddress(int boardNumber, int primaryAddress, int secondaryAddress) {
        this.boardNumber      = boardNumber;
        this.primaryAddress   = primaryAddress;
        this.secondaryAddress = secondaryAddress;
    }

    public GPIBAddress(int boardNumber, int primaryAddress) {
        this(boardNumber, primaryAddress, 0);
    }

    public GPIBAddress(int primaryAddress) {
        this(0, primaryAddress);
    }

    public GPIBAddress(Map<String, Object> parameters) {
        setParameters(parameters);
    }

    public int getBoardNumber() {
        return boardNumber;
    }

    public int getPrimaryAddress() {
        return primaryAddress;
    }

    public int getSecondaryAddress() {
        return secondaryAddress;
    }

    public void setBoardNumber(int boardNumber) {
        this.boardNumber = boardNumber;
    }

    public void setPrimaryAddress(int primaryAddress) {
        this.primaryAddress = primaryAddress;
    }

    public void setSecondaryAddress(int secondaryAddress) {
        this.secondaryAddress = secondaryAddress;
    }

    @Override
    public String getTypeName() {
        return "GPIB";
    }

    @Override
    public String getVISAString() {

        if (secondaryAddress > 0) {
            return String.format("GPIB%d::%d::%d::INSTR", boardNumber, primaryAddress, secondaryAddress);
        } else {
            return String.format("GPIB%d::%d::INSTR", boardNumber, primaryAddress);
        }

    }

    @Override
    public Map<String, Object> getParameters() {

        return Util.buildMap(map -> {
            map.put("Board Number", boardNumber);
            map.put("Primary Address", primaryAddress);
            map.put("Secondary Address", secondaryAddress);
        });

    }

    @Override
    public void setParameters(Map<String, Object> parameters) {

        boardNumber      = castOrDefault(parameters.getOrDefault("Board Number", boardNumber), boardNumber);
        primaryAddress   = castOrDefault(parameters.getOrDefault("Primary Address", primaryAddress), primaryAddress);
        secondaryAddress = castOrDefault(parameters.getOrDefault("Secondary Address", secondaryAddress), secondaryAddress);

    }

    @Override
    public void parseString(String text) throws InvalidAddressFormatException {

        Pattern pattern = Pattern.compile("GPIB([0-9]*)::([0-9]+)(?:::([0-9]+))?::INSTR");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {

            String board  = matcher.group(1);
            String first  = matcher.group(2);
            String second = matcher.group(3);

            boardNumber      = board.isBlank() ? 0 : Integer.parseInt(board);
            primaryAddress   = Integer.parseInt(first);
            secondaryAddress = (second == null || second.isBlank()) ? 0 : Integer.parseInt(second);

        } else {
            throw new InvalidAddressFormatException(text, "GPIB");
        }

    }

    public String toString() {
        return getJISAString();
    }

}
