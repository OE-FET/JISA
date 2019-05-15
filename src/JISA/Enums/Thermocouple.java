package JISA.Enums;

/**
 * Enumeration of TC-08 thermocouple types
 */
public enum Thermocouple {

    NONE((char) 0),
    B('B'),
    E('E'),
    J('J'),
    K('K'),
    N('N'),
    R('R'),
    S('S'),
    T('T'),
    X('X');

    private final byte code;

    Thermocouple(char code) {
        this.code = (byte) code;
    }

    public byte getCode() {
        return code;
    }

}
