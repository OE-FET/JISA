package JISA.Control;

import JISA.Devices.DeviceException;

import java.io.IOException;

public class MeanRepeatFilter implements ReadFilter {

    protected int                count = 1;
    protected Returnable<Double> value;
    protected Setupable          setUp;

    public MeanRepeatFilter(Returnable<Double> v, Setupable s) {
        value = v;
        setUp = s;
    }

    @Override
    public double getValue() throws IOException, DeviceException {

        double total = 0;

        for (int i = 0; i < count; i++) {
            total += value.get();
        }

        return total / ((double) count);

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
