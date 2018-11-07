package JISA.Devices;

import JISA.Addresses.InstrumentAddress;
import JISA.VISA.VISADevice;

import java.io.IOException;

public class AB1500A extends VISADevice {

    private static final String C_MEAS_MODE = "MM %d %s";

    public AB1500A(InstrumentAddress address) throws IOException {
        super(address);
    }
}
