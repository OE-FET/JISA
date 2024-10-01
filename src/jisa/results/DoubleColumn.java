package jisa.results;

import com.google.common.primitives.Longs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DoubleColumn extends Column<Double> implements DoubleRowEvaluable {

    public DoubleColumn(String name, String units) {
        super(name, units, Double.class);
    }

    public DoubleColumn(String name) {
        super(name, Double.class);
    }

    public DoubleColumn(String name, RowEvaluable<Double> evaluable) {
        super(name, Double.class, evaluable);
    }

    public DoubleColumn(String name, String units, RowEvaluable<Double> evaluable) {
        super(name, units, Double.class, evaluable);
    }

    @Override
    public String stringify(Object value) {
        return value.toString();
    }

    @Override
    public Double parse(String string) {
        return Double.parseDouble(string);
    }

    @Override
    public void writeToStream(OutputStream stream, Double value) throws IOException {
        stream.write(Longs.toByteArray(Double.doubleToLongBits(value)));
    }

    @Override
    public Double readFromStream(InputStream stream) throws IOException {
        return Double.longBitsToDouble(Longs.fromByteArray(stream.readNBytes(Double.BYTES)));
    }

}
