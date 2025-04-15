package jisa.results;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BooleanColumn extends Column<Boolean> {

    public BooleanColumn(String name, String units) {
        super(name, units, Boolean.class);
    }

    public BooleanColumn(String name) {
        super(name, Boolean.class);
    }

    public BooleanColumn(String name, RowEvaluable<Boolean> evaluable) {
        super(name, Boolean.class, evaluable);
    }

    public BooleanColumn(String name, String units, RowEvaluable<Boolean> evaluable) {
        super(name, units, Boolean.class, evaluable);
    }

    @Override
    public String stringify(Object value) {
        return value.toString();
    }

    @Override
    public Boolean parse(String string) {
        return Boolean.parseBoolean(string);
    }

    @Override
    public void writeToStream(DataOutputStream stream, Boolean value) throws IOException {
        stream.write(value ? 1 : 0);
    }

    @Override
    public Boolean readFromStream(InputStream stream) throws IOException {
        return stream.read() == 1;
    }

    @Override
    public void skipBytes(InputStream stream) throws IOException {
        stream.skip(1);
    }

    public RowEvaluable<Boolean> not() {
        return r -> !this.evaluate(r);
    }

    public RowEvaluable<Boolean> and(RowEvaluable<Boolean> other) {
        return r -> this.evaluate(r) && other.evaluate(r);
    }

    public RowEvaluable<Boolean> or(RowEvaluable<Boolean> column) {
        return r -> this.evaluate(r) || column.evaluate(r);
    }

}
