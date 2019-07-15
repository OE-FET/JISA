package jisa.control;

import jisa.devices.DeviceException;

import java.io.IOException;

public class BypassFilter implements ReadFilter {

    protected Returnable<Double> value;
    protected int                count = 1;
    protected Setupable          setUp;

    public BypassFilter(Returnable<Double> v, Setupable s) {
        value = v;
        setUp = s;
    }

    @Override
    public double getValue() throws IOException, DeviceException {
        return value.get();
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
