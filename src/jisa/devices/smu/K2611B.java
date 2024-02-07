package jisa.devices.smu;

import jisa.addresses.Address;
import jisa.devices.DeviceException;

import java.io.IOException;

public class K2611B extends K26Single<K2611B> {

    public static String getDescription() {
        return "Keithley 2611B SMU";
    }

    public K2611B(Address address) throws IOException, DeviceException {
        super(address, "MODEL 2611B");
    }

}
