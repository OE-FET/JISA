package jisa.visa;

import jisa.addresses.Address;
import jisa.addresses.LXIAddress;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.Instrument;

import java.io.IOException;

public class PseudoDevice implements Instrument {

    public PseudoDevice() {

    }

    public PseudoDevice(Address address) {

    }

    @Override
    public String getIDN() throws IOException, DeviceException {
        return "PseudoDevice";
    }

    @Override
    public void close() throws IOException, DeviceException {

    }

    @Override
    public Address getAddress() {
        return new LXIAddress("0.0.0.0");
    }

}
