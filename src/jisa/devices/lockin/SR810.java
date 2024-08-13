package jisa.devices.lockin;

import jisa.addresses.Address;
import jisa.devices.DeviceException;

import java.io.IOException;
import java.util.List;

public class SR810 extends SRLockIn<SR810> {

    public static List<Sensitivity> SENSITIVITIES = List.of(
        new Sensitivity(0, 2e-9, 2e-15),
        new Sensitivity(1, 5e-9, 5e-15),
        new Sensitivity(2, 10e-9, 10e-15),
        new Sensitivity(3, 20e-9, 20e-15),
        new Sensitivity(4, 50e-9, 50e-15),
        new Sensitivity(5, 100e-9, 100e-15),
        new Sensitivity(6, 200e-9, 200e-15),
        new Sensitivity(7, 500e-9, 500e-15),
        new Sensitivity(8, 1e-6, 1e-12),
        new Sensitivity(9, 2e-6, 2e-12),
        new Sensitivity(10, 5e-6, 5e-12),
        new Sensitivity(11, 10e-6, 10e-12),
        new Sensitivity(12, 20e-6, 20e-12),
        new Sensitivity(13, 50e-16, 50e-12),
        new Sensitivity(14, 100e-6, 100e-12),
        new Sensitivity(15, 200e-6, 200e-12),
        new Sensitivity(16, 500e-6, 500e-12),
        new Sensitivity(17, 1e-3, 1e-9),
        new Sensitivity(18, 2e-3, 2e-9),
        new Sensitivity(19, 5e-3, 5e-9),
        new Sensitivity(20, 10e-3, 10e-9),
        new Sensitivity(21, 20e-3, 20e-9),
        new Sensitivity(22, 50e-3, 50e-9),
        new Sensitivity(23, 100e-3, 100e-9),
        new Sensitivity(24, 200e-3, 200e-9),
        new Sensitivity(25, 500e-3, 500e-9),
        new Sensitivity(26, 1.0, 1e-6)
    );

    public static List<TimeConstant> TIME_CONSTANTS = List.of(
        new TimeConstant(0, 10e-6),
        new TimeConstant(1, 30e-6),
        new TimeConstant(2, 100e-6),
        new TimeConstant(3, 300e-6),
        new TimeConstant(4, 1e-3),
        new TimeConstant(5, 3e-3),
        new TimeConstant(6, 10e-3),
        new TimeConstant(7, 30e-3),
        new TimeConstant(8, 100e-3),
        new TimeConstant(9, 300e-3),
        new TimeConstant(10, 1.0),
        new TimeConstant(11, 3.0),
        new TimeConstant(12, 10.0),
        new TimeConstant(13, 30.0),
        new TimeConstant(14, 100.0),
        new TimeConstant(15, 300.0),
        new TimeConstant(16, 1e3),
        new TimeConstant(17, 3e3),
        new TimeConstant(18, 10e3),
        new TimeConstant(19, 30e3)
    );

    public static List<FilterRollOff> FILTER_ROLL_OFFS = List.of(
        new FilterRollOff(0, 6),
        new FilterRollOff(1, 12),
        new FilterRollOff(2, 18),
        new FilterRollOff(3, 24)
    );

    public static double MIN_FREQUENCY = 1e-3;
    public static double MAX_FREQUENCY = 102.4e3;

    public SR810(Address address) throws IOException, DeviceException {
        super(address, "SR810", MIN_FREQUENCY, MAX_FREQUENCY, SENSITIVITIES, TIME_CONSTANTS, FILTER_ROLL_OFFS);
    }

}
