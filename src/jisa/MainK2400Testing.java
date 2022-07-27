package jisa;

import jisa.addresses.GPIBAddress;
import jisa.devices.DeviceException;
import jisa.devices.smu.K2400;
import jisa.enums.Source;

import java.io.IOException;

public class MainK2400Testing {
    public static void main(String[] args) throws IOException, DeviceException, InterruptedException {
        K2400 smu = new K2400(new GPIBAddress(28));
        smu.setSource(Source.VOLTAGE);
        smu.setVoltage(0);
        smu.turnOn();
        for(int i = -10; i < 10; i ++){
            smu.setVoltage(i);
            double current = smu.getCurrent();
            System.out.println("Voltage = " + i + "    Current = " + current);
            Thread.sleep(1000);
        }

        // it seems that the SMU automatically turns off when the mode is changed.
        smu.setSource(Source.CURRENT);
        smu.turnOn();
        smu.setCurrent(0);
        smu.setVoltageLimit(2);
        for(int i = -10; i < 10; i ++){
            smu.setCurrent(i*1e-6);
            double voltage = smu.getVoltage();
            System.out.println("Voltage = " + voltage + "    Current = " + i*1e-6);
            Thread.sleep(1000);
        }
    }
}
