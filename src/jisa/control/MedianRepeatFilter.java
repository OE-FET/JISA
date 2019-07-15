package jisa.control;

import jisa.devices.DeviceException;
import org.apache.commons.math.stat.descriptive.rank.Median;

import java.io.IOException;

public class MedianRepeatFilter implements ReadFilter {

    protected int                count  = 1;
    protected Returnable<Double> value;
    protected Median             median = new Median();
    protected Setupable          setUp;

    public MedianRepeatFilter(Returnable<Double> v, Setupable s) {
        value = v;
        setUp = s;
    }

    @Override
    public double getValue() throws IOException, DeviceException {

        double[] values = new double[count];

        for (int i = 0; i < count; i++) {
            values[i] = value.get();
        }

        return median.evaluate(values);

    }

    @Override
    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public void clear() {

    }

    @Override
    public void setUp() throws IOException, DeviceException {
        setUp.run(getCount());
    }
}
