package JISA.Devices;

import JISA.Control.Returnable;

import java.io.IOException;

public class MotorController {

   private Returnable<Double> frequency;
   private Returnable<Double> voltage;

   public double getVoltage() throws IOException, DeviceException {
       return voltage.getValue();
   }

   public double getFrequency() throws IOException, DeviceException {
       return frequency.getValue();
   }

}
