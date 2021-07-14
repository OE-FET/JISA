package jisa.results;

import jisa.maths.matrices.RealMatrix;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ResultTable implements Iterable<ResultTable.Row> {

    private final List<Col<?>>        columns;
    private final Map<String, String> attributes = new LinkedHashMap<>();

    public ResultTable(Col<?>... columns) {
        this.columns = List.of(columns);
    }

    public Col<?> getColumn(int index) {

        if (index < 0 || index >= columns.size()) {
            throw new IndexOutOfBoundsException("Column index is out of bounds. Must be 0 <= index <= " + (columns.size() - 1) + ".");
        }

        return columns.get(index);

    }

    public <T> Col<T> getColumn(Col<T> column) {

        if (columns.contains(column)) {
            return column;
        } else {
            String title = column.getTitle().toLowerCase().trim();
            return (Col<T>) columns.stream().filter(c -> c.getTitle().toLowerCase().trim().equals(title) && c.getType() == column.getType()).findFirst().orElse(null);
        }

    }

    public <T> List<T> getUniqueValues(Col<T> column) {
        return getValues(r -> r.get(column));
    }

    public <T> List<T> getUniqueValues(RowEvaluable<T> expression) {
        return stream().map(expression::evaluate).distinct().collect(Collectors.toList());
    }

    public <T> Map<T, ResultTable> split(RowEvaluable<T> splitBy) {

        Map<T, ResultTable> map = new LinkedHashMap<>();

        for (T value : getUniqueValues(splitBy)) {
            map.put(value, filter(r -> splitBy.evaluate(r).equals(value)));
        }

        return map;

    }

    public ResultTable filter(Predicate<Row> test) {

        ResultTable filtered = new ResultList(columns.toArray(Col[]::new));
        stream().filter(test).forEach(filtered::addRow);
        return filtered;

    }

    public <T> List<T> getValues(Col<T> column) {
        return getValues(r -> r.get(column));
    }

    public <T> List<T> getValues(RowEvaluable<T> expression) {
        return stream().map(expression::evaluate).collect(Collectors.toList());
    }

    public RealMatrix getColumnMatrix(Col<? extends Number>... columns) {

        RealMatrix matrix = new RealMatrix(getRowCount(), columns.length);

        int i = 0;
        for (Row row : this) {

            for (int j = 0; j < columns.length; j++) {

                matrix.set(i, j, row.get(columns[i]).doubleValue());

            }

        }

        return matrix;

    }

    public void setAttribute(String key, String value) {
        attributes.put(key, value);
    }

    public String getAttribute(String key) {
        return attributes.get(key);
    }

    public Map<String, String> getAttributes() {
        return Map.copyOf(attributes);
    }

    public abstract Row getRow(int index);

    protected abstract void addRow(Row row);

    public abstract int getRowCount();

    public int getColumnCount() {
        return columns.size();
    }

    public void addData(Object... data) {

        if (data.length != columns.size()) {
            throw new IllegalArgumentException("The number of values supplied does not match the number of columns.");
        }

        List<Integer>       invalids = new LinkedList<>();
        Map<Col<?>, Object> map      = new LinkedHashMap<>();


        for (int i = 0; i < columns.size(); i++) {

            if (columns.get(i).getType().isAssignableFrom(data[i].getClass())) {
                map.put(columns.get(i), data[i]);
            } else {
                invalids.add(i);
            }

        }

        if (!invalids.isEmpty()) {

            boolean multiple = invalids.size() != 1;

            throw new IllegalArgumentException(
                String.format(
                    "Column%s %s %s the wrong type",
                    multiple ? "s" : "",
                    invalids.stream().map(Object::toString).collect(Collectors.joining(", ")),
                    multiple ? "are" : "is"
                )
            );

        } else {

            addRow(new Row(map));

        }

    }

    public RowBuilder startRow() {
        return new RowBuilder();
    }

    public abstract Stream<Row> stream();

    public class RowBuilder {

        private final Map<Col<?>, Object> map = new LinkedHashMap<>();

        public RowBuilder() {
            columns.forEach(c -> map.put(c, null));
        }

        public <T> RowBuilder add(Col<T> column, T value) {

            column = getColumn(column);

            if (column == null) {
                throw new IllegalArgumentException("Specified column does not exist in table.");
            }

            map.put(column, value);

            return this;

        }

        public void endRow() {
            addRow(new Row(map));
        }

    }

    public class Row {

        private final Map<Col<?>, Object> values;

        public Row(Map<Col<?>, Object> values) {

            this.values = values;

            for (Col<?> column : this.values.keySet()) {

                if (column.isCalculated()) {
                    this.values.put(column, column.calculate(this));
                }

            }

        }

        public Object[] array() {
            return values.values().toArray();
        }

        public List<Object> list() {
            return new ArrayList<>(values.values());
        }

        public <T> T get(Col<T> column) {

            column = getColumn(column);

            if (column == null) {
                return null;
            }

            return (T) values.getOrDefault(column, null);

        }

        public Map<Col<?>, Object> getValues() {
            return Map.copyOf(values);
        }

    }
}
