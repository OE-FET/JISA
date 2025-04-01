package jisa.devices.smu;

import jisa.addresses.Address;
import jisa.devices.DeviceException;

import java.io.IOException;
import java.util.List;

public abstract class K26Dual<T extends K26Dual> extends KeithleyTSP implements MCSMU<KeithleyTSP.KSMU<T>> {

    public final  KSMU<T>       SMU_A;
    public final  KSMU<T>       SMU_B;
    private final List<KSMU<T>> channels;

    public K26Dual(Address address, String model) throws IOException, DeviceException {

        super(address, model);

        SMU_A    = new KSMU<>("smua", (T) this);
        SMU_B    = new KSMU<>("smub", (T) this);
        channels = List.of(SMU_A, SMU_B);

    }

    @Override
    public List<KSMU<T>> getSMUs() {
        return channels;
    }

}
