package JPIB;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class MotorController {

    private SR830  lockIn;
    private K2200  power;
    private double targetFrequency;
    private double errorPCT;

    private static final double KNOWN_VOLTAGE   = 10.0;
    private static final double KNOWN_FREQUENCY = 1.0;
    private static final double KNOWN_GRADIENT  = KNOWN_FREQUENCY / KNOWN_VOLTAGE;
    private static final Timer  timer           = new Timer();
    private final        PID    pid             = new PID();


    private class PID extends TimerTask {

        private long   lastTime  = 0;
        private double integral  = 0;
        private double lastError = 0;

        private static final double Kp = -KNOWN_GRADIENT;
        private static final double Ki = -KNOWN_GRADIENT;
        private static final double Kd = -KNOWN_GRADIENT;

        @Override
        public void run() {

            long tDiff = 0;
            try {

                double error = getFieldFrequency() - targetFrequency;

                if (lastTime > 0) {
                    tDiff = System.currentTimeMillis() - lastTime;
                    lastTime = System.currentTimeMillis();
                } else {
                    lastError = error;
                }

                double diff = (error - lastError) / tDiff;
                integral += error * tDiff;

                double voltage = Kp * error + Kd * diff + Ki * integral;

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
        }

    }

    public MotorController(SR830 lockInAmplifier, K2200 dcPowerSupply) {
        lockIn = lockInAmplifier;
        power = dcPowerSupply;
    }

    public double getFieldFrequency() throws IOException {
        return lockIn.getRefFrequency();
    }

    public double getVoltage() throws IOException {
        return power.getVoltage();
    }

    public void setTargetFrequency(double frequency, double errorPCT) throws IOException {
        this.targetFrequency = frequency;
        this.errorPCT = errorPCT;
    }

    public void start() throws IOException {

        power.setVoltage(0.0);
        power.setCurrent(5.0);
        power.turnOn();

        pid.cancel();
        pid.reset();
        timer.scheduleAtFixedRate(pid, 0, 100);

    }

    public void stop() throws IOException {

        pid.cancel();
        power.turnOff();

    }

}
