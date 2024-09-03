package jisa.results;

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
