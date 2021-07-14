package jisa.results;

import java.util.List;
import java.util.stream.Collectors;

public class Col<T> {

    private final        String          name;
    private final        String          units;
    private final        Class<T>        type;
    private final        RowEvaluable<T> evaluable;
    private static final List<Class<?>>  availableTypes = List.of(String.class, Double.class, Integer.class, Boolean.class);

    public Col(String name, String units, Class<T> type) {
        this(name, units, type, null);
    }

    public Col(String name, Class<T> type) {
        this(name, null, type, null);
    }

    public Col(String name, Class<T> type, RowEvaluable<T> evaluable) {
        this(name, null, type, evaluable);
    }

    public Col(String name, String units, Class<T> type, RowEvaluable<T> evaluable) {

        if (!availableTypes.contains(type)) {
            throw new IllegalArgumentException(String.format("ResultTable columns can only be of the following types: %s", availableTypes.stream().map(Class::getSimpleName).collect(Collectors.joining(", "))));
        }

        this.name      = name;
        this.units     = units;
        this.type      = type;
        this.evaluable = evaluable;

    }

    public String getName() {
        return name;
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

    public T calculate(ResultTable.Row row) {

        if (evaluable == null) {
            return null;
        }

        return evaluable.evaluate(row);

    }

}
