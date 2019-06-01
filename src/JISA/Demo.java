package JISA;

import JISA.Addresses.SerialAddress;
import JISA.Devices.ADRelay;
import JISA.Devices.DeviceException;
import JISA.Devices.MSwitch;
import JISA.Devices.Switch;

import java.io.IOException;

public class Demo {

    public static void main(String[] args) throws IOException, DeviceException {

        MSwitch relays = new ADRelay(new SerialAddress(0));

        Switch  relay1 = relays.getChannel(0);
        Switch  relay2 = relays.getChannel(1);
        Switch  relay3 = relays.getChannel(2);
        Switch  relay4 = relays.getChannel(3);

        relay2.turnOn();

    }

}
