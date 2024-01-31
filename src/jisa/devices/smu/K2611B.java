package jisa.devices.smu;

import jisa.addresses.Address;
import jisa.devices.DeviceException;

import java.io.IOException;

public class K2611B extends K26Single {

    public K2611B(Address address) throws IOException, DeviceException {
        super(address, "MODEL 2611B");
    }

}
