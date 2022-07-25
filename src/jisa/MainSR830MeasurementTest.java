package jisa;

import jisa.addresses.GPIBAddress;
import jisa.addresses.USBAddress;
import jisa.devices.DeviceException;
import jisa.devices.amp.SR830;
import jisa.devices.function_generator.K3390;
import jisa.devices.interfaces.FunctionGenerator;
import jisa.devices.interfaces.LockIn;
import jisa.enums.Input;
import jisa.enums.Shield;
import jisa.enums.Source;

import java.io.IOException;
import java.util.Scanner;

public class MainSR830MeasurementTest {

    public static void main(String[] args) throws IOException, DeviceException, InterruptedException {

        // set up the function generator
        K3390 funcGen = new K3390(new USBAddress(0x05E6, 0x3390, "1242550"));
        funcGen.setHighImpedanceMode();
        funcGen.enableLogger(null, null);
        funcGen.turnOnSynchronizationSignal();
        //output a sine wave
        double rmsAmplitude = 2;
        double amplitudeV = rmsAmplitude*Math.sqrt(2);
        double freqHz = 97;
        FunctionGenerator.SineWave sineWave = new FunctionGenerator.SineWave(amplitudeV, freqHz, 0, 0);
        funcGen.outputWaveform(sineWave);

        // set up the SR830 lock-in amplifier
        SR830 lockInAmp = new SR830(new GPIBAddress(8));
        lockInAmp.enableLogger(null, null);
        lockInAmp.setRefMode(LockIn.RefMode.EXTERNAL);
        lockInAmp.setExternalTriggerMode(LockIn.TrigMode.POS_TTL);
        // wait for the PLL to get locked.
        // there seems to be no way to check if the amplifier is locked
        Thread.sleep(1000);
        lockInAmp.setOscPhase(0);

        // configure differential A-B input
        lockInAmp.setSource(Source.VOLTAGE);
        lockInAmp.setInput(Input.DIFF);
        // how should I set this ?
        lockInAmp.setShielding(Shield.FLOAT);

        // configure filters
        // disable the line frequency notch filter for now.
        lockInAmp.setLineFilterHarmonics();
        if (freqHz < 200)
            lockInAmp.setSyncFilterEnabled(true);

        // configure measurement parameters
        lockInAmp.setTimeConstant(SR830.TimeConst.T_300ms);
        lockInAmp.autoGain();

        Scanner in = new Scanner(System.in);
        // measure
        while (true) {
            SR830.DataPacket dataPacket = lockInAmp.getAll();
            System.out.print("r = " + dataPacket.r + "V");

            String s = in.nextLine();
            if (s.charAt(0) == 'q')
                break;;
        }
    }
}
