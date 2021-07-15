package jisa.results;

public class DoubleColumn extends Column<Double> {

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

}
