package jisa.results;

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
}
