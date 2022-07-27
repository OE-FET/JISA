package jisa;

import jisa.addresses.Address;
import jisa.addresses.USBAddress;
import jisa.devices.DeviceException;
import jisa.devices.function_generator.K3390;
import jisa.devices.interfaces.FunctionGenerator;
// import jisa.gui.GUI;

import java.io.IOException;

public class MainK3390Testing {

    public static void main(String[] args) throws IOException, DeviceException, InterruptedException {
//        Address address = GUI.browseVISA();
//        System.out.print(address);
        DCOutputTest();
    }

    public static void DCOutputTest() throws IOException, DeviceException, InterruptedException
    {
        K3390 funcGen = new K3390(new USBAddress(0x05E6, 0x3390, "1242550"));
        funcGen.enableLogger("K3390", null);
        funcGen.setStandardImpedanceMode();
        for(int i = -10; i <= 10; i ++) {
            funcGen.outputDC((float)i / 5.0);
            Thread.sleep(5000);
        }

        for(int i = 10; i >= -10; i --) {
            funcGen.outputDC((float)i / 5.0);
            Thread.sleep(5000);
        }
        funcGen.turnOff();
    }

    public static void K3390SquareWaveSineWaveTest() throws IOException, DeviceException, InterruptedException {
        K3390 funcGen = new K3390(new USBAddress(0x05E6, 0x3390, "1242550"));
        funcGen.setHighImpedanceMode();
        for(int i = 1; i <= 10; i ++)
        {
            FunctionGenerator.Waveform waveform = new FunctionGenerator.SquareWave(i/2.0, i*100, i/2.0);
            funcGen.outputWaveform(waveform);
            funcGen.turnOnSynchronizationSignal();
            Thread.sleep(1000);
            funcGen.turnOff();
            funcGen.turnOffSynchronizationSignal();
            Thread.sleep(500);

            FunctionGenerator.Waveform waveform2 = new FunctionGenerator.SineWave(i/2.0, i*1000, 0, 0);
            funcGen.outputWaveform(waveform2);
            Thread.sleep(1000);
            funcGen.turnOff();
            Thread.sleep(500);
        }
    }
}
