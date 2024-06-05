package jisa.devices.smu;

import jisa.addresses.Address;
import jisa.devices.DeviceException;

import java.io.IOException;

public class K2612A extends K26Dual<K2612A> {

    public static String getDescription() {
        return "Keithley 2612A Dual-Channel SMU";
    }

    public K2612A(Address address) throws IOException, DeviceException {
        super(address, "MODEL 2612A");
    }

}
