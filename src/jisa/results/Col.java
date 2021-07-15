package jisa.results;

public class Col extends DoubleColumn {

    public Col(String name, String units) {
        super(name, units);
    }

    public Col(String name) {
        super(name);
    }

    public Col(String name, RowEvaluable<Double> evaluable) {
        super(name, evaluable);
    }

    public Col(String name, String units, RowEvaluable<Double> evaluable) {
        super(name, units, evaluable);
    }

}
