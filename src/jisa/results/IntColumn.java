package jisa.results;

import com.google.common.primitives.Ints;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IntColumn extends Column<Integer> {

    public IntColumn(String name, String units) {
        super(name, units, Integer.class);
    }

    public IntColumn(String name) {
        super(name, Integer.class);
    }

    public IntColumn(String name, RowEvaluable<Integer> evaluable) {
        super(name, Integer.class, evaluable);
    }

    public IntColumn(String name, String units, RowEvaluable<Integer> evaluable) {
        super(name, units, Integer.class, evaluable);
    }

    @Override
    public String stringify(Object value) {
        return value.toString();
    }

    @Override
    public Integer parse(String string) {
        return Integer.parseInt(string);
    }

    @Override
    public void writeToStream(OutputStream stream, Integer value) throws IOException {
        stream.write(Ints.toByteArray(value));
    }

    @Override
    public Integer readFromStream(InputStream stream) throws IOException {
        return Ints.fromByteArray(stream.readNBytes(Integer.BYTES));
    }

    @Override
    public void skipBytes(InputStream stream) throws IOException {
        stream.skip(Integer.BYTES);
    }

    public RowEvaluable<Double> pow(double power) {
        return r -> Math.pow(r.get(this), power);
    }

    public RowEvaluable<Integer> abs() {
        return r -> Math.abs(r.get(this));
    }

    public RowEvaluable<Integer> negate() {
        return r -> -r.get(this);
    }

}
