package JISA;

import JISA.Addresses.GPIBAddress;
import JISA.Addresses.InstrumentAddress;
import JISA.Addresses.StrAddress;
import JISA.Addresses.TCPIPAddress;
import JISA.Devices.K236;
import JISA.Devices.K2450;
import JISA.Devices.SMU;
import JISA.VISA.VISA;

public class Examples {

    public static void connectingToDevices() throws Exception {

        // You can see which instruments are detectable by VISA by use of:
        VISA.getInstruments();

        // This will return an array of instrument addresses (ie InstrumentAddress[]) therefor
        InstrumentAddress[] addresses = VISA.getInstruments();

        // You can then output each address as its VISA resource address by using getVISAAddress():
        for (InstrumentAddress address : addresses) {
            System.out.println(address.getVISAAddress());
        }

        // If you don't know what address your device is using, this can be a handy way of looking it up

        // You can establish a connection to an instrument through VISA by creating an object of the relevant class.
        // For example, let's say we have a Keithley 2450 connected via GPIB, on GPIB board 0, address 15. To
        // establish contact we would write:
        K2450 smu1 = new K2450(new GPIBAddress(0, 15));

        // Note that we have specified how the device is connected to the computer by providing a GPIBAddress object.
        // In the constructor of the GPIBAddress object we have specified board 0, address 15 (ie (0, 15)).
        // A similar method should be used for other connection types as well. For example, if we had another connected
        // via an ethernet cable using TCPIP with address 192.168.0.5:
        K2450 smu2 = new K2450(new TCPIPAddress("192.168.0.5"));

        // There are different "Address" classes for different connection types, but I would imagine these two are
        // the most common.

        // Alternatively, if you know the VISA resource address you can just specify it directly using StrAddress:
        K2450 smu3 = new K2450(new StrAddress("ASRL5::INSTR"));

        // In this case smu3 is connected via serial port 5

    }

    public static void usingDevices() throws Exception {

        K2450 smu1 = new K2450(new GPIBAddress(0, 15));

        // Now that we have established a connection you can command and query the device using the in-built methods
        // supplied by the device class like so:

        // Sets the SMU to source 5 V
        smu1.setVoltage(5.0);

        // Enables the output of the SMU
        smu1.turnOn();

        // Reads the current being measured by the SMU
        double current = smu1.getCurrent();

        // All classes representing SMU instruments are built upon the SMU class, meaning that they all implement
        // at least a standard set of functions. If you stick to using only those functions, then you can swap out
        // any SMU for any other and your program will not even notice.
        K236 smu4 = new K236(new GPIBAddress(0, 14));

        smu4.setVoltage(5.0);
        smu4.turnOn();
        current = smu4.getCurrent();

        // This is despite the fact that a Keithley 236 operates in a fundamentally different way when it comes to
        // setting and getting voltages/currents at the communication level. Essentially the K2450 and K236 classes
        // have hidden the complexity of how communication works to/from these devices and have provided the user
        // with a standard way of controlling them.

        // In fact, when connecting to an SMU you can tell Java to only consider it as an SMU and not a K2450 or K236:
        SMU smu5 = new K2450(new GPIBAddress(0, 15));
        SMU smu6 = new K236(new GPIBAddress(0, 14));

        // The result is that now smu5 and smu6 are both of type "SMU" meaning you can only use the standard SMU functions
        // common to all SMU implementations, making sure that you program will work no-matter what SMU make/model you use.
        // As far as Java is concerned they are simply both an "SMU" not a Keithley this or Keysight that.
        // As far as the user is concerned, all SMUs work the same way.


    }

}
