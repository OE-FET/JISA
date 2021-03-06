package jisa.control;

import jisa.devices.DeviceException;
import org.apache.commons.math.stat.descriptive.rank.Median;

import java.io.IOException;
import java.util.ArrayList;

public class MedianMovingFilter implements ReadFilter {

    protected int                count  = 1;
    protected Returnable<Double> value;
    protected ArrayList<Double>  queue  = new ArrayList<>();
    protected Median             median = new Median();
    protected Setupable          setUp;

    public MedianMovingFilter(Returnable<Double> v, Setupable s) {
        value = v;
        setUp = s;
    }

    @Override
    public double getValue() throws IOException, DeviceException {

        queue.subList(0, Math.max(0, queue.size() - (count - 1))).clear();

        int toFill = (count - queue.size());
        for (int i = 0; i < toFill; i++) {
            queue.add(value.get());
        }

        double[] values = new double[count];

        for (int i = 0; i < count; i++) {
            values[i] = queue.get(i);
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
        queue.clear();
    }

    @Override
    public void setUp() throws IOException, DeviceException {
        setUp.run(getCount());
    }
}
