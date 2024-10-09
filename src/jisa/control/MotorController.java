package jisa.control;

import jisa.Util;
import jisa.devices.DeviceException;
import org.apache.commons.math.stat.regression.SimpleRegression;

import java.io.IOException;

public class MotorController {

    private final Returnable<Double>   frequency;
    private final SetGettable<Double>  voltage;
    private final SetGettable<Boolean> power;
    private       SimpleRegression     fit = null;

    public MotorController(Returnable<Double> frequency, SetGettable<Double> voltage, SetGettable<Boolean> power) {
        this.frequency = frequency;
        this.voltage   = voltage;
        this.power     = power;
    }

    public double getVoltage() throws IOException, DeviceException {
        return voltage.get();
    }

    public double getFrequency() throws IOException, DeviceException {
        return frequency.get();
    }

    public void setVoltage(double v) throws IOException, DeviceException {
        voltage.set(v);
    }

    public void calibrate(double minV, double maxV, long maxPeriod) throws IOException, DeviceException, InterruptedException {

        double[] values = Util.makeLinearArray(maxV, minV, 5);
        voltage.set(maxV);
        start();

        Sync.waitForParamStable(frequency::get, 1.0, 100, 2 * maxPeriod);

        fit = new SimpleRegression();

        for (double v : values) {

            voltage.set(v);
            Sync.waitForParamStable(frequency::get, 1.0, 100, 2 * maxPeriod);
            fit.addData(frequency.get(), voltage.get());

        }

        stop();

    }

    public void setFrequency(double frequency) throws IOException, DeviceException {

        if (fit == null) {
            throw new DeviceException("You need to perform a calibration first!");
        }

        voltage.set(fit.predict(frequency));
    }

    public void waitForFrequency(double f, double pctMargin) throws IOException, DeviceException, InterruptedException {
        start();
        Sync.waitForParamWithinError(frequency::get, f, pctMargin, 100);
    }

    public void waitForFrequency(double f) throws IOException, DeviceException, InterruptedException {
        waitForFrequency(f, 1.0);
    }

    public void setFrequencyAndWait(double f, double pctMargin) throws IOException, DeviceException, InterruptedException {
        setFrequency(f);
        waitForFrequency(f, pctMargin);
    }

    public void setFrequencyAndWait(double f) throws IOException, DeviceException, InterruptedException {
        setFrequency(f);
        waitForFrequency(f);
    }

    public void stop() throws IOException, DeviceException {
        power.set(false);
    }

    public void start() throws IOException, DeviceException {
        power.set(true);
    }

    public boolean isRunning() throws IOException, DeviceException {
        return power.get();
    }

}
