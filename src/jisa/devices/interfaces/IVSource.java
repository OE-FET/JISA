package jisa.devices.interfaces;

import jisa.devices.DeviceException;
import java.io.IOException;

public interface IVSource extends ISource, VSource {

    public static String getDescription() {
        return "Current and Voltage Source";
    }

    /*
     * On Jisa update add functions below to interface.
     */

    /**
     * Get the default value or the value set by the setCurrent() method.
     * @return current setting [A]
     */
    double getSetCurrent() throws DeviceException, IOException;

    /**
     * Get the default value or the value set by the setVoltage() method.
     * @return voltage setting [V]
     */
    double getSetVoltage() throws DeviceException, IOException;
}
