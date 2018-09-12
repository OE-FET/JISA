package JISA;

import JISA.Addresses.GPIBAddress;
import JISA.Addresses.InstrumentAddress;
import JISA.Devices.*;
import JISA.VISA.VISA;
import JISA.VISA.VISAException;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        try {

            for (InstrumentAddress addr : VISA.getInstruments()) {
                System.out.println(addr.getVISAAddress());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
