package JISA.Devices;

import JISA.Devices.K2200;
import JISA.Devices.SR830;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class MotorController {

    private LockIn  lockIn;
    private DCPower power;
    private double  targetFrequency;
    private double  errorPCT;

    private static final double KNOWN_VOLTAGE   = 10.0;
    private static final double KNOWN_FREQUENCY = 1.0;
    private static final double KNOWN_GRADIENT  = KNOWN_FREQUENCY / KNOWN_VOLTAGE;
    private static final Timer  timer           = new Timer();
    private final        PID    pid             = new PID();


    private class PID extends TimerTask {

        private long   lastTime  = 0;
        private double integral  = 0;
        private double lastError = 0;

        // TODO: These are to be tuned when we have the assembly
        private static final double KP     = -KNOWN_GRADIENT;
        private static final double KI     = -KNOWN_GRADIENT;
        private static final double KD     = -KNOWN_GRADIENT;
        private static final double OFFSET = 1.0;

        @Override
        public void run() {

            long tDiff = 0;
            try {

                double error = getFieldFrequency() - targetFrequency;

                if (lastTime > 0) {
                    tDiff = System.currentTimeMillis() - lastTime;
                    lastTime += tDiff;
                } else {
                    lastError = error;
                }

                double diff = (error - lastError) / tDiff;
                integral += error * tDiff;

                double voltage = (KP * error) + (KD * diff) + (KI * integral) + OFFSET;

                lastError = error;

                power.setVoltage(voltage);

            } catch (Exception e) {
                System.err.printf("Timer exception: %s", e.getMessage());
                e.printStackTrace(System.err);
            }

        }

        public void reset() {
            lastTime = 0;
            integral = 0;
            lastError = 0;
        }

    }

    public MotorController(LockIn lockInAmplifier, DCPower dcPowerSupply) {
        lockIn = lockInAmplifier;
        power = dcPowerSupply;
    }

    public double getFieldFrequency() throws IOException, DeviceException {
        return lockIn.getFrequency();
    }

    public double getVoltage() throws IOException, DeviceException {
        return power.getVoltage();
    }

    public void setTargetFrequency(double frequency) {
        this.targetFrequency = frequency;
    }

    public void start() throws IOException, DeviceException {

        power.setVoltage(0.0);
        power.setCurrent(5.0);
        power.turnOn();

        pid.cancel();
        pid.reset();
        timer.scheduleAtFixedRate(pid, 0, 100);

    }

    public void stop() throws IOException, DeviceException {

        pid.cancel();
        power.turnOff();

    }

}
