package JISA;

import JISA.Addresses.GPIBAddress;
import JISA.Addresses.InstrumentAddress;
import JISA.Devices.DeviceException;
import JISA.Devices.ITC503;
import JISA.Devices.SR830;
import JISA.VISA.VISA;
import JISA.VISA.VISAException;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        try {

            for (InstrumentAddress addr : VISA.getInstruments()) {
                System.out.println(addr.getVISAAddress());
            }

            ITC503 itc503 = new ITC503(new GPIBAddress(0, 17));

        } catch (VISAException | DeviceException | IOException e) {
            e.printStackTrace();
        }

    }

}
