package jisa.devices.smu;

import jisa.addresses.Address;
import jisa.devices.DeviceException;

import java.io.IOException;

public class K2635A extends K26Single<K2635A> {

    public static String getDescription() {
        return "Keithley 2635A SMU";
    }

    public K2635A(Address address) throws IOException, DeviceException {
        super(address, "MODEL 2635A");
    }

}
