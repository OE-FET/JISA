package jisa.results;

import java.util.Map;

public abstract class Column<T> {

    private final        String              name;
    private final        String              units;
    private final        Class<T>            type;
    private final        RowEvaluable<T>     evaluable;

    public static <T> Column<T> of(String name, String units, T example) {

        if (example instanceof Double) {
            return (Column<T>) ofDecimals(name, units);
        }

        if (example instanceof Integer) {
            return (Column<T>) ofIntegers(name, units);
        }

        if (example instanceof Boolean) {
            return (Column<T>) ofBooleans(name, units);
        }

        if (example instanceof String) {
            return (Column<T>) ofText(name, units);
        }

        throw new IllegalArgumentException("Columns cannot be made for objects of type " + example.getClass().getName());

    }

    public static Column<String> ofStrings(String name, String units) {
        return new StringColumn(name, units);
    }

    public static Column<String> ofStrings(String name) {
        return new StringColumn(name);
    }

    public static Column<String> ofStrings(String name, RowEvaluable<String> evaluable) {
        return new StringColumn(name, evaluable);
    }

    public static Column<String> ofStrings(String name, String units, RowEvaluable<String> evaluable) {
        return new StringColumn(name, units, evaluable);
    }

    public static Column<String> ofText(String name, String units) {
        return ofStrings(name, units);
    }

    public static Column<String> ofText(String name) {
        return ofStrings(name);
    }

    public static Column<String> ofText(String name, RowEvaluable<String> evaluable) {
        return ofStrings(name, evaluable);
    }

    public static Column<String> ofText(String name, String units, RowEvaluable<String> evaluable) {
        return ofStrings(name, units, evaluable);
    }

    public static Column<Double> ofDoubles(String name, String units) {
        return new DoubleColumn(name, units);
    }

    public static Column<Double> ofDoubles(String name) {
        return new DoubleColumn(name);
    }

    public static Column<Double> ofDoubles(String name, RowEvaluable<Double> evaluable) {
        return new DoubleColumn(name, evaluable);
    }

    public static Column<Double> ofDoubles(String name, String units, RowEvaluable<Double> evaluable) {
        return new DoubleColumn(name, units, evaluable);
    }

    public static Column<Double> ofDecimals(String name, String units) {
        return ofDoubles(name, units);
    }

    public static Column<Double> ofDecimals(String name) {
        return ofDoubles(name);
    }

    public static Column<Double> ofDecimals(String name, RowEvaluable<Double> evaluable) {
        return ofDoubles(name, evaluable);
    }

    public static Column<Double> ofDecimals(String name, String units, RowEvaluable<Double> evaluable) {
        return ofDoubles(name, units, evaluable);
    }

    public static Column<Integer> ofIntegers(String name, String units) {
        return new IntColumn(name, units);
    }

    public static Column<Integer> ofIntegers(String name) {
        return new IntColumn(name);
    }

    public static Column<Integer> ofIntegers(String name, RowEvaluable<Integer> evaluable) {
        return new IntColumn(name, evaluable);
    }

    public static Column<Integer> ofIntegers(String name, String units, RowEvaluable<Integer> evaluable) {
        return new IntColumn(name, units, evaluable);
    }

    public static Column<Long> ofLongs(String name, String units) {
        return new LongColumn(name, units);
    }

    public static Column<Long> ofLongs(String name) {
        return new LongColumn(name);
    }

    public static Column<Long> ofLongs(String name, RowEvaluable<Long> evaluable) {
        return new LongColumn(name, evaluable);
    }

    public static Column<Long> ofLongs(String name, String units, RowEvaluable<Long> evaluable) {
        return new LongColumn(name, units, evaluable);
    }

    public static Column<Boolean> ofBooleans(String name, String units) {
        return new BooleanColumn(name, units);
    }

    public static Column<Boolean> ofBooleans(String name) {
        return new BooleanColumn(name);
    }

    public static Column<Boolean> ofBooleans(String name, RowEvaluable<Boolean> evaluable) {
        return new BooleanColumn(name, evaluable);
    }

    public static Column<Boolean> ofBooleans(String name, String units, RowEvaluable<Boolean> evaluable) {
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
