package jisa.devices.smu;

import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.MCSMU;
import jisa.devices.interfaces.SMU;

import java.io.IOException;
import java.util.List;

public abstract class K26Dual extends KeithleyTSP implements MCSMU {

    public final  KSMU      SMU_A    = new KSMU("smua");
    public final  KSMU      SMU_B    = new KSMU("smub");
    private final List<SMU> channels = List.of(SMU_A, SMU_B);

    public K26Dual(Address address, String model) throws IOException, DeviceException {
        super(address, model);
    }

    @Override
    public List<SMU> getSMUChannels() {
        return channels;
    }

}
