package JISA.Devices;

import JISA.Addresses.Address;
import JISA.VISA.VISADevice;

import java.io.IOException;

public class AB1500A extends VISADevice {

    private static final String C_MEAS_MODE = "MM %d %s";

    public AB1500A(Address address) throws IOException {
        super(address);
    }
}
