package jisa.devices.smu;

import jisa.addresses.Address;
import jisa.devices.DeviceException;

import java.io.IOException;

public class K2612B extends K26Dual<K2612B> {

    public static String getDescription() {
        return "Keithley 2612B Dual-Channel SMU";
    }

    public K2612B(Address address) throws IOException, DeviceException {
        super(address, "MODEL 2612B");
    }

}
