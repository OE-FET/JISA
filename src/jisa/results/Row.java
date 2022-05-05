package jisa.results;

import org.python.antlr.ast.Num;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Row {

    private final Map<Column<?>, Object> values;

    public Row(List<Column<?>> columns, Map<Column<?>, Object> values) {

        this.values = values;

        this.values.putAll(
            columns.stream()
                  .filter(Column::isCalculated)
                  .map(c -> Map.entry(c, c.calculate(this)))
                  .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );

    }

    public Object[] array() {
        return values.values().toArray();
    }

    public List<Object> list() {
        return new ArrayList<>(values.values());
    }

    public List<String> stringList() {
        return values.entrySet().stream().map(e -> e.getKey().stringify(e.getValue())).collect(Collectors.toList());
    }

    public <T> T get(Column<T> column) {

        column = findColumn(column);

        if (column == null) {
            return null;
        }

        return (T) values.getOrDefault(column, null);

    }

    public <T> T get(String columnName, Class<T> type) {

        Column<T> column = findColumn(columnName, type);

        if (column == null) {
            return null;
        }

        return (T) values.getOrDefault(column, null);

    }

    public double get(String columnName) {

        Number value = get(columnName, Number.class);

        if (value != null) {
            return value.doubleValue();
        } else {
            throw new IndexOutOfBoundsException("No numerical column with that name was found.");
        }

    }

    public double get(int column) {
        return (Double) get(values.keySet().toArray(Column[]::new)[column]);
    }

    private <T> Column<T> findColumn(Column<T> column) {

        if (values.containsKey(column)) {
            return column;
        }

        String title = column.getMatcherTitle();
        return (Column<T>) values.keySet().stream()
                                 .filter(c -> c.getMatcherTitle().equals(title) && c.getType() == column.getType())
                                 .findFirst().orElse(null);

    }

    private <T> Column<T> findColumn(String name, Class<T> type) {

        String title = name.toLowerCase().trim();
        return (Column<T>) values.keySet().stream()
                                 .filter(c -> c.getMatcherName().equals(title) && type.isAssignableFrom(c.getType()))
                                 .findFirst().orElse(null);

    }

    public Map<Column<?>, Object> getValues() {
        return Map.copyOf(values);
    }

}
