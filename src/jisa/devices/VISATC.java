package jisa.devices;

import jisa.addresses.Address;
import jisa.visa.Driver;
import jisa.visa.VISADevice;

import java.io.IOException;

public abstract class VISATC extends VISADevice implements TC {

    private Zoner zoner = null;

    public VISATC(Address address) throws IOException {
        super(address);
    }

    public VISATC(Address address, Class<? extends Driver> prefDriver) throws IOException {
        super(address, prefDriver);
    }

    public Zoner getZoner() {
        return zoner;
    }

    public void setZoner(Zoner zoner) {
        this.zoner = zoner;
    }

}
