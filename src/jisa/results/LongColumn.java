package jisa.results;

import com.google.common.primitives.Longs;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class LongColumn extends Column<Long> {

    public LongColumn(String name, String units) {
        super(name, units, Long.class);
    }

    public LongColumn(String name) {
        super(name, Long.class);
    }

    public LongColumn(String name, RowEvaluable<Long> evaluable) {
        super(name, Long.class, evaluable);
    }

    public LongColumn(String name, String units, RowEvaluable<Long> evaluable) {
        super(name, units, Long.class, evaluable);
    }

    @Override
    public String stringify(Object value) {
        return value.toString();
    }

    @Override
    public Long parse(String string) {
        return Long.parseLong(string);
    }

    @Override
    public void writeToStream(DataOutputStream stream, Long value) throws IOException {
        stream.writeLong(value != null ? value : Long.MIN_VALUE);
    }

    @Override
    public Long readFromStream(InputStream stream) throws IOException {
        return Longs.fromByteArray(stream.readNBytes(Long.BYTES));
    }

    @Override
    public void skipBytes(InputStream stream) throws IOException {
        stream.skip(Long.BYTES);
    }

    public RowEvaluable<Double> pow(double power) {
        return r -> Math.pow(r.get(this), power);
    }

    public RowEvaluable<Long> abs() {
        return r -> Math.abs(r.get(this));
    }

    public RowEvaluable<Long> negate() {
        return r -> -r.get(this);
    }

}
