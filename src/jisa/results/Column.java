package jisa.results;

import java.util.Map;

public abstract class Column<T> {

    private final        String              name;
    private final        String              units;
    private final        Class<T>            type;
    private final        RowEvaluable<T>     evaluable;

    public static <T> Column<T> forValue(String name, String units, T example) {

        if (example instanceof Double) {
            return (Column<T>) forDecimal(name, units);
        }

        if (example instanceof Integer) {
            return (Column<T>) forInteger(name, units);
        }

        if (example instanceof Boolean) {
            return (Column<T>) forBoolean(name, units);
        }

        if (example instanceof String) {
            return (Column<T>) forText(name, units);
        }

        throw new IllegalArgumentException("Columns cannot be made for objects of type " + example.getClass().getName());

    }

    public static Column<String> forText(String name, String units) {
        return new StringColumn(name, units);
    }

    public static Column<String> forText(String name) {
        return new StringColumn(name);
    }

    public static Column<String> forText(String name, RowEvaluable<String> evaluable) {
        return new StringColumn(name, evaluable);
    }

    public static Column<String> forText(String name, String units, RowEvaluable<String> evaluable) {
        return new StringColumn(name, units, evaluable);
    }

    public static Column<Double> forDecimal(String name, String units) {
        return new DoubleColumn(name, units);
    }

    public static Column<Double> forDecimal(String name) {
        return new DoubleColumn(name);
    }

    public static Column<Double> forDecimal(String name, RowEvaluable<Double> evaluable) {
        return new DoubleColumn(name, evaluable);
    }

    public static Column<Double> forDecimal(String name, String units, RowEvaluable<Double> evaluable) {
        return new DoubleColumn(name, units, evaluable);
    }

    public static Column<Double> forInteger(String name, String units) {
        return new DoubleColumn(name, units);
    }

    public static Column<Integer> forInteger(String name) {
        return new IntColumn(name);
    }

    public static Column<Integer> forInteger(String name, RowEvaluable<Integer> evaluable) {
        return new IntColumn(name, evaluable);
    }

    public static Column<Integer> forInteger(String name, String units, RowEvaluable<Integer> evaluable) {
        return new IntColumn(name, units, evaluable);
    }

    public static Column<Boolean> forBoolean(String name, String units) {
        return new BooleanColumn(name, units);
    }

    public static Column<Boolean> forBoolean(String name) {
        return new BooleanColumn(name);
    }

    public static Column<Boolean> forBoolean(String name, RowEvaluable<Boolean> evaluable) {
        return new BooleanColumn(name, evaluable);
    }

    public static Column<Boolean> forBoolean(String name, String units, RowEvaluable<Boolean> evaluable) {
        return new BooleanColumn(name, units, evaluable);
    }

    public Column(String name, String units, Class<T> type) {
        this(name, units, type, null);
    }

    public Column(String name, Class<T> type) {
        this(name, null, type, null);
    }

    public Column(String name, Class<T> type, RowEvaluable<T> evaluable) {
        this(name, null, type, evaluable);
    }

    public Column(String name, String units, Class<T> type, RowEvaluable<T> evaluable) {

        this.name      = name;
        this.units     = units;
        this.type      = type;
        this.evaluable = evaluable;

    }

    public abstract String stringify(Object value);

    public abstract T parse(String string);

    public String getName() {
        return name;
    }

    public String getMatcherName() {
        return name.toLowerCase().trim();
    }

    public String getMatcherTitle() {
        return getTitle().toLowerCase().trim();
    }

    public String getUnits() {
        return units;
    }

    public boolean hasUnits() {
        return units != null;
    }

    public String getTitle() {

        if (hasUnits()) {
            return String.format("%s [%s]", name, units);
        } else {
            return name;
        }

    }

    public Class<T> getType() {
        return type;
    }

    public boolean isCalculated() {
        return evaluable != null;
    }

    public T calculate(Row row) {

        if (evaluable == null) {
            return null;
        }

        return evaluable.evaluate(row);

    }

    public Map.Entry<Column, Object> to(T value) {
        return Map.entry(this, value);
    }

}
