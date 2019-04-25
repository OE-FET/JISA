package JISA.Control;

import JISA.Devices.DeviceException;

import java.io.IOException;
import java.util.ArrayList;

public class MeanMovingFilter implements ReadFilter {

    protected int                count = 1;
    protected Returnable<Double> value;
    protected ArrayList<Double>  queue = new ArrayList<>();
    protected Setupable          setUp;

    public MeanMovingFilter(Returnable<Double> v, Setupable s) {
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

        double total = 0;
        for (double v : queue) {
            total += v;
        }

        return total / ((double) queue.size());

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
