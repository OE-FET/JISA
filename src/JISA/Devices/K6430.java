package JISA.Devices;

import JISA.Addresses.Address;
import JISA.Util;

import java.io.IOException;

public class K6430 extends KeithleySCPI {

    public K6430(Address address) throws IOException, DeviceException {

        super(address);

        if (!getIDN().contains("MODEL 6430")) {
            throw new DeviceException("Instrument at \"%s\" is not a Keithley 6430", address.toString());
        }

    }

    @Override
    public TType getTerminalType(Terminals terminals) {

        switch (terminals) {

            case FRONT:
                return TType.NONE;

            case REAR:
                return TType.BANANA;

            default:
                return TType.BANANA;

        }

    }

    public void setTerminals(Terminals terminals) {}

    public Terminals getTerminals() {
        return Terminals.REAR;
    }


    @Override
    public void setOffMode(OffMode mode) throws IOException {

        switch (mode) {

            case NORMAL:
                write(C_SET_OFF_STATE, OFF_NORMAL);
                break;

            case ZERO:
                write(C_SET_OFF_STATE, OFF_ZERO);
                break;

            case GUARD:
                write(C_SET_OFF_STATE, OFF_GUARD);
                break;

            case HIGH_IMPEDANCE:
                Util.errLog.println("Keithley 6430 SMUs do not have a HIGH_IMPEDANCE off-mode. Switching to NORMAL instead.");
                write(C_SET_OFF_STATE, OFF_NORMAL);
                break;

        }

    }


}
