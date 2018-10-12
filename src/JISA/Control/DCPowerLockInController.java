package JISA.Control;

import JISA.Devices.DCPower;
import JISA.Devices.DeviceException;
import JISA.Devices.LockIn;

import java.io.IOException;

public class DCPowerLockInController extends MotorController {

    public DCPowerLockInController(DCPower power, LockIn lockIn) {
        super(
                lockIn::getFrequency,
                new SetGettable<Double>() {
                    @Override
                    public void set(Double value) throws IOException, DeviceException {
                        power.setVoltage(value);
                    }

                    @Override
                    public Double get() throws IOException, DeviceException {
                        return power.getVoltage();
                    }
                },
                new SetGettable<Boolean>() {
                    @Override
                    public void set(Boolean value) throws IOException, DeviceException {
                        if (value) {
                            power.turnOn();
                        } else {
                            power.turnOff();
                        }
                    }

                    @Override
                    public Boolean get() throws IOException, DeviceException {
                        return power.isOn();
                    }
                }
        );
    }

}
