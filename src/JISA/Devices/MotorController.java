package JISA.Devices;

import JISA.Control.Returnable;
import JISA.Control.SetGettable;
import JISA.Control.Synch;
import JISA.Util;
import org.apache.commons.math.stat.regression.SimpleRegression;

import java.io.IOException;
import java.util.ArrayList;

public class MotorController {

    private Returnable<Double>   frequency;
    private SetGettable<Double>  voltage;
    private SetGettable<Boolean> power;
    private SimpleRegression     fit;

    public double getVoltage() throws IOException, DeviceException {
        return voltage.get();
    }

    public double getFrequency() throws IOException, DeviceException {
        return frequency.get();
    }

    public void setVoltage(double v) throws IOException, DeviceException {
        voltage.set(v);
    }

    public void calibrate(double minV, double maxV, long maxPeriod) throws IOException, DeviceException {

        double[] values = Util.makeLinearArray(maxV, minV, 5);
        voltage.set(maxV);
        start();

        Synch.waitForParamStable(() -> frequency.get(), 0.1, 100, 2 * maxPeriod);

        fit = new SimpleRegression();

        for (double v : values) {

            voltage.set(v);
            Synch.waitForParamStable(() -> frequency.get(), 0.1, 100, 2 * maxPeriod);
            fit.addData(frequency.get(), voltage.get());

        }

        stop();

    }

    public void setFrequency(double frequency) throws IOException, DeviceException {
        voltage.set(fit.predict(frequency));
    }

    public void waitForFrequency(double f, double pctMargin) throws IOException, DeviceException {
        Synch.waitForParamWithinError(() -> frequency.get(), f, pctMargin, 100);
    }

    public void waitForFrequency(double f) throws IOException, DeviceException {
        waitForFrequency(f, 0.1);
    }

    public void setFrequencyAndWait(double f, double pctMargin) throws IOException, DeviceException {
        setFrequency(f);
        waitForFrequency(f, pctMargin);
    }

    public void setFrequencyAndWait(double f) throws IOException, DeviceException {
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
