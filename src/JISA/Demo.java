package JISA;

import JISA.Addresses.SerialAddress;
import JISA.Devices.ADRelay;
import JISA.Devices.DeviceException;
import JISA.Devices.MSwitch;
import JISA.Devices.Switch;
import JISA.GUI.GUI;
import JISA.VISA.VISADevice;
import JISA.VISA.VISADriver;

import java.io.IOException;

public class Demo {

    public static void main(String[] args) throws IOException, DeviceException {

        ADRelay device = new ADRelay(new SerialAddress(0));

        device.turnOn();
        Util.sleep(1500);
        device.turnOff();

    }

}
