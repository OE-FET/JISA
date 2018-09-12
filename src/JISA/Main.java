package JISA;

import JISA.Addresses.GPIBAddress;
import JISA.Devices.SR830;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        try {

            for (InstrumentAddress addr : VISA.getInstruments()) {
                System.out.println(addr.getVISAAddress());
            }

            SR830 lockin = new SR830(new GPIBAddress(0, 30));

        } catch (VISAException e) {
            e.printStackTrace();
        } catch (DeviceException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
