package jisa.results;

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
