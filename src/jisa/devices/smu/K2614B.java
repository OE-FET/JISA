package jisa.devices.smu;

import jisa.addresses.Address;
import jisa.devices.DeviceException;

import java.io.IOException;

public class K2614B extends K26Dual<K2614B> {

    public static String getDescription() {
        return "Keithley 2614B Dual-Channel SMU";
    }

    public K2614B(Address address) throws IOException, DeviceException {
        super(address, "MODEL 2614B");
    }

}
