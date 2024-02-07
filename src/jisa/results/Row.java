package jisa.results;

import java.util.List;
import java.util.Map;
import java.util.Set;
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

    public Row(Map<Column<?>, Object> values) {
        this.values = values;
    }

    /**
     * Returns all columns contained in this row as an array.
     *
     * @return Array of columns
     */
    public Column[] getColumnArray() {
        return values.keySet().toArray(Column[]::new);
    }

    /**
     * Returns all columns contained in this row as a Set.
     *
     * @return Set of columns
     */
    public Set<Column<?>> getColumnSet() {
        return values.keySet();
    }

    /**
     * Returns all values in this row as a List of their String representations.
     *
     * @return List of String values
     */
    public List<String> stringList() {
        return values.entrySet().stream().map(e -> e.getKey().stringify(e.getValue())).collect(Collectors.toList());
    }

    /**
     * Returns the value in this row for the given column.
     *
     * @param column Column
     * @param <T>    Data Type
     *
     * @return Value of column in this row, if it exists, null otherwise
     */
    public <T> T get(Column<T> column) {

        column = findColumn(column);

        if (column == null) {
            return null;
        }

        return (T) values.getOrDefault(column, null);

    }

    public <T> T __getitem__(Column<T> column) {
        return get(column);
    }

    /**
     * Returns the value in this row for the given column name and data type.
     *
     * @param columnName Column name
     * @param type       Column data type class
     * @param <T>        Data Type
     *
     * @return Value of column in this row, if it exists, null otherwise
     */
    public <T> T get(String columnName, Class<T> type) {

        Column<T> column = findColumn(columnName, type);

        if (column == null) {
            return null;
        }

        return (T) values.getOrDefault(column, null);

    }

    /**
     * Returns a numerical value in this row for the given column name.
     *
     * @param columnName Column name
     *
     * @return Value of column in this row, if it exists
     *
     * @throws IndexOutOfBoundsException If no numerical column exists with the given name
     */
    public double get(String columnName) {

        Number value = get(columnName, Number.class);

        if (value != null) {
            return value.doubleValue();
        } else {
            throw new IndexOutOfBoundsException("No numerical column with that name was found.");
        }

    }

    /**
     * Returns a double value in this row for the given column index.
     *
     * @param column Column index
     *
     * @return Value of column in this row, if it exists and is castable to Double
     */
    public double get(int column) {
        return (Double) get(values.keySet().toArray(Column[]::new)[column]);
    }

    /**
     * Finds a column reference stored within this row that matches the one supplied in name and data type.
     *
     * @param column Column to find match for
     * @param <T>    Data Type
     *
     * @return Matching column, if found, null otherwise
     */
    private <T> Column<T> findColumn(Column<T> column) {

        if (values.containsKey(column)) {
            return column;
        }

        String title = column.getMatcherTitle();
        return (Column<T>) values.keySet().stream()
                                 .filter(c -> c.getMatcherTitle().equals(title) && c.getType() == column.getType())
                                 .findFirst().orElse(null);

    }

    /**
     * Finds a column reference stored within this row that matches the supplied name and data type.
     *
     * @param name Column name
     * @param type Column data type class
     * @param <T>  Data Type
     *
     * @return Matching column, if found, null otherwise
     */
    private <T> Column<T> findColumn(String name, Class<T> type) {

        String title = name.toLowerCase().trim();
        return (Column<T>) values.keySet().stream()
                                 .filter(c -> c.getMatcherName().equals(title) && type.isAssignableFrom(c.getType()))
                                 .findFirst().orElse(null);

    }

    /**
     * Returns this row's values as a Column -> Value map.
     *
     * @return Map of values
     */
    public Map<Column<?>, Object> getValues() {
        return Map.copyOf(values);
    }

}
