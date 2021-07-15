package jisa.results;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class Column<T> {

    private final        String              name;
    private final        String              units;
    private final        Class<T>            type;
    private final        RowEvaluable<T>     evaluable;

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

}
