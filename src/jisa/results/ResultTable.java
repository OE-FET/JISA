package jisa.results;

import com.google.common.collect.Lists;
import com.google.common.primitives.Booleans;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import io.jhdf.HdfFile;
import io.jhdf.WritableHdfFile;
import io.jhdf.api.WritableGroup;
import io.jhdf.api.WritiableDataset;
import jisa.Util;
import jisa.maths.matrices.RealMatrix;
import jisa.results.ResultList.ColumnBuilder;
import kotlin.Pair;
import kotlin.jvm.JvmClassMappingKt;
import kotlin.reflect.KClass;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ResultTable implements Iterable<Row> {

    public static final Map<String, ColumnBuilder> STANDARD_TYPES = Util.buildMap(types -> {

        types.put("String", StringColumn::new);
        types.put("Double", DoubleColumn::new);
        types.put("Integer", IntColumn::new);
        types.put("Boolean", BooleanColumn::new);
        types.put("Long", LongColumn::new);

    });

    private final List<Column<?>>     columns;
    private final Map<String, String> attributes     = new LinkedHashMap<>();
    private final List<RowListener>   rowListeners   = new LinkedList<>();
    private final List<ClearListener> clearListeners = new LinkedList<>();

    public interface RowListener {
        void added(Row row);
    }

    public interface ClearListener {
        void cleared();
    }

    public static Column[] parseColumnHeaderLine(String header) {

        header = header.replace("\\\"", "&quot;").replace("\\,", "&comma;");

        String[] columns = Arrays.stream(header.split(",")).map(String::trim).toArray(String[]::new);
        Column[] cols    = new Column[columns.length];

        Pattern pattern = Pattern.compile("(?:\"(.*?)(?:\\s\\[(.*?)\\])?\"(?:\\s\\{(.*?)\\})?)|(?:(.*)\\s\\[(.*)\\])");

        for (int i = 0; i < cols.length; i++) {

            Matcher   matcher = pattern.matcher(columns[i]);
            Column<?> column;

            if (matcher.find()) {

                String name        = matcher.group(1) != null ? matcher.group(1).replace("&quot;", "\"").replace("&comma;", ",") : "";
                String units       = matcher.group(2) != null ? matcher.group(2) : "";
                String type        = matcher.group(3) != null ? matcher.group(3) : "";
                String legacyName  = matcher.group(4) != null ? matcher.group(4).replace("&quot;", "\"").replace("&comma;", ",") : "";
                String legacyUnits = matcher.group(5) != null ? matcher.group(5) : "";

                if (!type.isBlank()) {
                    column = STANDARD_TYPES.getOrDefault(type, StringColumn::new).create(name, units.isBlank() ? null : units);
                } else if (!name.isBlank()) {
                    column = new DoubleColumn(name, units.isBlank() ? null : units);
                } else if (!legacyName.isBlank()) {
                    column = new DoubleColumn(legacyName, legacyUnits.isBlank() ? null : legacyUnits);
                } else {
                    column = new DoubleColumn(columns[i].replace("&quot;", "\"").replace("&comma;", ","));
                }

            } else {
                column = new DoubleColumn(columns[i].replace("&quot;", "\"").replace("&comma;", ","));
            }

            cols[i] = column;

        }

        return cols;

    }

    public ResultTable(Column<?>... columns) {
        this.columns = List.of(columns);
    }

    public RowListener addRowListener(RowListener listener) {
        rowListeners.add(listener);
        return listener;
    }

    public void removeRowListener(RowListener listener) {
        rowListeners.remove(listener);
    }

    public ClearListener addClearListener(ClearListener listener) {
        clearListeners.add(listener);
        return listener;
    }

    public void removeClearListener(ClearListener listener) {
        clearListeners.remove(listener);
    }

    /**
     * Returns the column with the given index as a Column object.
     *
     * @param index Column index
     *
     * @return Column object
     */
    public Column<?> getColumn(int index) {

        if (index < 0 || index >= columns.size()) {
            throw new IndexOutOfBoundsException(String.format("Column index is out of bounds. Must be 0 <= index <= %d.", columns.size() - 1));
        }

        return columns.get(index);

    }

    /**
     * Returns an unmodifiable list of all columns in this ResultTable.
     *
     * @return Unmodifiable list of columns
     */
    public List<Column<?>> getColumns() {
        return List.copyOf(columns);
    }

    /**
     * Returns a list of columns in this table that have a numerical data type.
     *
     * @return List of all numeric columns in table
     */
    public List<Column<? extends Number>> getNumericColumns() {

        return columns.stream()
                      .filter(c -> Number.class.isAssignableFrom(c.getType()))
                      .map(c -> (Column<? extends Number>) c)
                      .collect(Collectors.toList());

    }

    /**
     * Returns the first column in this table that has a numerical data type.
     *
     * @return First numerical column, null if none
     */
    public Column<? extends Number> getFirstNumericColumn() {

        return columns.stream()
                      .filter(c -> Number.class.isAssignableFrom(c.getType()))
                      .map(c -> (Column<? extends Number>) c)
                      .findFirst().orElse(null);

    }

    /**
     * Returns the nth column in this table that has a numerical data type.
     *
     * @param n Index of column
     *
     * @return nth numerical column, null if none
     */
    public Column<? extends Number> getNthNumericColumn(int n) {

        return columns.stream()
                      .filter(c -> Number.class.isAssignableFrom(c.getType()))
                      .map(c -> (Column<? extends Number>) c)
                      .skip(n).findFirst().orElse(null);

    }

    /**
     * Returns an array of all columns in this ResultTable.
     *
     * @return Array of columns
     */
    public Column[] getColumnsAsArray() {
        return columns.toArray(Column[]::new);
    }

    /**
     * Tries to find the column in this ResultTable that matches the given column in name, units and type
     *
     * @param column Column to match
     * @param <T>    Data type of Column object
     *
     * @return Matching column if found, otherwise null
     */
    public <T> Column<T> findColumn(Column<T> column) {

        if (columns.contains(column)) {

            return column;

        } else {

            String title = column.getMatcherTitle();

            return (Column<T>) columns.stream()
                                      .filter(c -> c.getMatcherTitle().equals(title) && c.getType() == column.getType())
                                      .findFirst().orElse(null);

        }

    }

    /**
     * Tries to find the column in this ResultTable that matches the given name and type
     *
     * @param name Name to match
     * @param type Data type class of the column to match
     * @param <T>  Data type of Column object
     *
     * @return Matching column if found, otherwise null
     */
    public <T> Column<T> findColumn(String name, Class<T> type) {

        String nameLower = name.toLowerCase().trim();

        return (Column<T>) columns.stream()
                                  .filter(c -> c.getMatcherName().equals(nameLower) && type.isAssignableFrom(c.getType()))
                                  .findFirst().orElse(null);

    }

    /**
     * Tries to find a column of doubles in this ResultTable that matches the given name.
     *
     * @param name Name to match
     *
     * @return Matching column if found, otherwise null
     */
    public Column<Double> findDoubleColumn(String name) {
        return findColumn(name, Double.class);
    }

    /**
     * Tries to find a column of doubles in this ResultTable that matches the given name. Alias for findDoubleColumn(...).
     *
     * @param name Name to match
     *
     * @return Matching column if found, otherwise null
     */
    public Column<Double> findDecimalColumn(String name) {
        return findDoubleColumn(name);
    }

    /**
     * Tries to find a column of integers in this ResultTable that matches the given name.
     *
     * @param name Name to match
     *
     * @return Matching column if found, otherwise null
     */
    public Column<Integer> findIntegerColumn(String name) {
        return findColumn(name, Integer.class);
    }

    /**
     * Tries to find a column of long integers in this ResultTable that matches the given name.
     *
     * @param name Name to match
     *
     * @return Matching column if found, otherwise null
     */
    public Column<Long> findLongColumn(String name) {
        return findColumn(name, Long.class);
    }

    /**
     * Tries to find a column of booleans in this ResultTable that matches the given name.
     *
     * @param name Name to match
     *
     * @return Matching column if found, otherwise null
     */
    public Column<Boolean> findBooleanColumn(String name) {
        return findColumn(name, Boolean.class);
    }

    /**
     * Tries to find a column of Strings in this ResultTable that matches the given name.
     *
     * @param name Name to match
     *
     * @return Matching column if found, otherwise null
     */
    public Column<String> findStringColumn(String name) {
        return findColumn(name, String.class);
    }

    /**
     * Tries to find a column of String in this ResultTable that matches the given name. Alias for findStringColumn(...).
     *
     * @param name Name to match
     *
     * @return Matching column if found, otherwise null
     */
    public Column<String> findTextColumn(String name) {
        return findStringColumn(name);
    }

    /**
     * Tries to find the column in this ResultTable that matches the given name and type
     *
     * @param name Name to match
     * @param type Data type class of the column to match
     * @param <T>  Data type of Column object
     *
     * @return Matching column if found, otherwise null
     */
    public <T> Column<T> findColumn(String name, KClass<T> type) {
        return findColumn(name, JvmClassMappingKt.getJavaObjectType(type));
    }

    /**
     * Tries to find a numerical column in this ResultTable that matches the given name.
     *
     * @param name Name to match
     *
     * @return Matching column if found, otherwise null
     */
    public Column<? extends Number> findColumn(String name) {
        return findColumn(name, Number.class);
    }

    /**
     * Returns all values returned by the supplied expression on each row as a List.
     *
     * @param expression Expression to pass each row through
     * @param <T>        Data type of column
     *
     * @return Expression values, as a list
     */
    public <T> List<T> toList(RowEvaluable<T> expression) {
        return stream().map(expression::evaluate).collect(Collectors.toList());
    }

    /**
     * Returns a list of all unique values returned by the given expression applied to each row in this ResultTable.
     *
     * @param expression Expression to evaluate on each row
     * @param <T>        Data type returned by expression
     *
     * @return Unique values, as a List
     */
    public <T> List<T> getUniqueValues(RowEvaluable<T> expression) {
        return stream().map(expression::evaluate).distinct().collect(Collectors.toList());
    }

    /**
     * Returns a list of all unique values in the given column in this ResultTable.
     *
     * @param column Column to check
     * @param <T>    Data type returned by expression
     *
     * @return Unique values, as a List
     */
    public <T> List<T> getUniqueValues(Column<T> column) {
        return getUniqueValues(r -> r.get(column));
    }

    /**
     * Splits this ResultTable into separate tables based on the value given by the specified expression for each row.
     *
     * @param splitBy Expression to split by
     * @param <T>     Data type of expression
     *
     * @return Mapping of unique values of expression to sub-tables
     */
    public <T> Map<T, ResultList> split(RowEvaluable<T> splitBy) {

        Map<T, ResultList> map = new LinkedHashMap<>();

        for (T value : getUniqueValues(splitBy)) {
            map.put(value, filter(r -> splitBy.evaluate(r).equals(value)));
        }

        return map;

    }

    /**
     * Splits this ResultTable into separate tables based on the value of the specified column for each row.
     *
     * @param splitBy Column to split by
     * @param <T>     Data type of column
     *
     * @return Mapping of unique values of column to sub-tables
     */
    public <T> Map<T, ResultList> split(Column<T> splitBy) {
        return split(r -> r.get(splitBy));
    }

    /**
     * Splits this ResultTable into separate tables based on the direction of change of the specified expression for
     * each row.
     *
     * @param splitBy Expression to split by
     *
     * @return List of resulting tables, in alternating order of direction
     */
    public List<ResultList> directionalSplit(RowEvaluable<? extends Number> splitBy) {

        Integer          direction = null;
        Double           lastValue = null;
        List<ResultList> list      = new LinkedList<>();
        ResultList       table     = ResultList.emptyCopyOf(this);
        list.add(table);

        for (Row row : this) {

            double value = splitBy.evaluate(row).doubleValue();

            if (lastValue == null) {

                table.addRow(row);

            } else if (direction == null) {

                table.addRow(row);
                direction = Double.compare(value, lastValue);

            } else {

                int newDirection = Double.compare(value, lastValue);

                if (newDirection != direction && direction != 0) {
                    table = ResultList.emptyCopyOf(this);
                    list.add(table);
                }

                table.addRow(row);
                direction = newDirection;

            }

            lastValue = value;

        }

        return list;

    }

    /**
     * Splits this ResultTable into separate tables based on the direction of change of the specified column for
     * each row.
     *
     * @param splitBy Column to split by
     *
     * @return List of resulting tables, in alternating order of direction
     */
    public List<ResultList> directionalSplit(Column<? extends Number> splitBy) {
        return directionalSplit(r -> r.get(splitBy));
    }

    /**
     * Returns a filtered copy of this ResultTable containing only the rows that pass the provided test expression.
     *
     * @param test Test expression
     *
     * @return Filtered table
     */
    public ResultList filter(Predicate<Row> test) {
        return stream().filter(test).collect(collector());
    }

    /**
     * Returns a copy of this ResultTable, sorted by the provided expression evaluated for each row.
     *
     * @param expression Expression to sort by
     *
     * @return Sorted table
     */
    public ResultList sorted(RowEvaluable<?> expression) {

        if (getRowCount() < 2) {
            return ResultList.copyOf(this);
        }

        Object value = expression.evaluate(getRow(0));

        if (value instanceof Number) {
            RowEvaluable<? extends Number> byExpression = (RowEvaluable<? extends Number>) expression;
            return stream().sorted(Comparator.comparingDouble(r -> byExpression.evaluate(r).doubleValue())).collect(collector());
        } else {
            return stream().sorted(Comparator.comparing(r -> expression.evaluate(r).toString())).collect(collector());
        }

    }

    /**
     * Returns a copy of this ResultTable, sorted by the specified column.
     *
     * @param byColumn Column to sort by
     *
     * @return Sorted table
     */
    public ResultList sorted(Column<?> byColumn) {
        return sorted(r -> r.get(byColumn));
    }

    /**
     * Performs a multi-column mapping operation, returning a new ResultList of the results (with only the columns that
     * have mappings specified).
     *
     * @param mappings Map of column mappings
     *
     * @return Transformed table
     */
    public ResultList transform(Map<Column<?>, RowEvaluable<?>> mappings) {

        ResultList list = ResultList.emptyCopyOf(this);

        for (Row row : this) {

            RowBuilder builder = list.startRow();

            for (Column column : columns) {

                if (mappings.containsKey(column)) {
                    builder.set(column, mappings.get(column).evaluate(row));
                } else {
                    builder.set(column, row.get(column));
                }

            }

            builder.endRow();

        }

        return list;

    }

    /**
     * Returns a copy of this table with only the specified columns present.
     *
     * @param columns Columns to copy
     *
     * @return Copied sub-table
     */
    public ResultList subTable(Column... columns) {

        ResultList list = new ResultList(columns);

        getAttributes().forEach(list::setAttribute);

        for (Row row : this) {

            RowBuilder builder = list.startRow();

            for (Column column : columns) {
                builder.set(column, row.get(column));
            }

            builder.endRow();

        }

        return list;

    }

    /**
     * Returns a copy of this table with only the specified range of rows present.
     *
     * @param start Index to start at
     * @param to    Index to end at
     *
     * @return Copied sub-table
     */
    public ResultList subTable(int start, int to) {

        ResultList list = ResultList.emptyCopyOf(this);

        for (int i = start; i <= to; i++) {
            list.addRow(get(i));
        }

        return list;

    }

    /**
     * Returns a copy of this table with only the specified range of rows and columns present.
     *
     * @param start   Index to start at
     * @param to      Index to end at
     * @param columns Columns to copy
     *
     * @return Copied sub-table
     */
    public ResultList subTable(int start, int to, Column... columns) {

        ResultList list = new ResultList(columns);

        getAttributes().forEach(list::setAttribute);

        for (int i = 0; i <= to; i++) {

            RowBuilder builder = list.startRow();
            Row        row     = get(i);

            for (Column column : columns) {
                builder.set(column, row.get(column));
            }

            builder.endRow();

        }

        return list;

    }

    /**
     * Returns a copy of this table with its rows in reverse order.
     *
     * @return Reversed table
     */
    public ResultList reverse() {
        return Lists.reverse(getRows()).stream().collect(collector());
    }

    /**
     * Returns all rows in this table as a List of Row objects.
     *
     * @return List of all rows
     */
    public List<Row> getRows() {
        return stream().collect(Collectors.toList());
    }

    /**
     * Finds the first row that passes the given predicate.
     *
     * @param test Predicate to test each row with
     *
     * @return First matching row
     */
    public Row findRow(Predicate<Row> test) {
        return stream().filter(test).findFirst().orElse(null);
    }

    /**
     * Returns the mean value of the given expression evaluated across all rows.
     *
     * @param expression Expression to take the mean of
     *
     * @return Mean value
     */
    public double mean(RowEvaluable<? extends Number> expression) {
        return stream().mapToDouble(r -> expression.evaluate(r).doubleValue()).average().orElse(0.0);
    }

    /**
     * Returns the mean value of the given column.
     *
     * @param column Column to take the mean of
     *
     * @return Mean value
     */
    public double mean(Column<? extends Number> column) {
        return mean(r -> r.get(column));
    }

    /**
     * Returns the minimum value of the provided expression when evaluated for each row.
     *
     * @param expression Expression to find minimum of
     * @param <T>        Data type
     *
     * @return Min value
     */
    public <T extends Number> T min(RowEvaluable<T> expression) {
        return stream().map(expression::evaluate).min(Comparator.comparingDouble(Number::doubleValue)).orElse(null);
    }

    /**
     * Returns the minimum value of the provided column in the table.
     *
     * @param column Column to find minimum of
     * @param <T>    Data type
     *
     * @return Min value
     */
    public <T extends Number> T min(Column<T> column) {
        return min(r -> r.get(column));
    }

    /**
     * Returns the minimum value of the provided expression when evaluated for each row. Alias for min(...).
     *
     * @param expression Expression to find minimum of
     * @param <T>        Data type
     *
     * @return Min value
     */
    public <T extends Number> T getMin(RowEvaluable<T> expression) {
        return min(expression);
    }

    /**
     * Returns the minimum value of the provided column in the table. Alias for min(...).
     *
     * @param column Column to find minimum of
     * @param <T>    Data type
     *
     * @return Min value
     */
    public <T extends Number> T getMin(Column<T> column) {
        return min(column);
    }


    /**
     * Returns the maximum value of the provided expression when evaluated for each row.
     *
     * @param expression Expression to find maximum of
     * @param <T>        Data type
     *
     * @return Max value
     */
    public <T extends Number> T max(RowEvaluable<T> expression) {
        return stream().map(expression::evaluate).max(Comparator.comparingDouble(Number::doubleValue)).orElse(null);
    }

    /**
     * Returns the maximum value of the provided column in the table.
     *
     * @param column Column to find maximum of
     * @param <T>    Data type
     *
     * @return Max value
     */
    public <T extends Number> T max(Column<T> column) {
        return max(r -> r.get(column));
    }

    /**
     * Returns the maximum value of the provided expression when evaluated for each row. Alias for max(...).
     *
     * @param expression Expression to find maximum of
     * @param <T>        Data type
     *
     * @return Max value
     */
    public <T extends Number> T getMax(RowEvaluable<T> expression) {
        return max(expression);
    }

    /**
     * Returns the maximum value of the provided column in the table. Alias for max(...).
     *
     * @param column Column to find maximum of
     * @param <T>    Data type
     *
     * @return Max value
     */
    public <T extends Number> T getMax(Column<T> column) {
        return max(column);
    }

    /**
     * Returns the row for which the given expression has its maximum value.
     *
     * @param expression Expression to evaluate
     * @param <T>        Data type
     *
     * @return Max row
     */
    public <T extends Number> Row maxBy(RowEvaluable<T> expression) {
        return stream().max(Comparator.comparingDouble(r -> expression.evaluate(r).doubleValue())).orElse(null);
    }

    /**
     * Returns the row for which the given column has its maximum value.
     *
     * @param column Column to maximise
     * @param <T>    Data type
     *
     * @return Max row
     */
    public <T extends Number> Row maxBy(Column<T> column) {
        return maxBy(r -> r.get(column));
    }

    /**
     * Returns the row for which the given expression has its minimum value.
     *
     * @param expression Expression to evaluate
     * @param <T>        Data type
     *
     * @return Min row
     */
    public <T extends Number> Row minBy(RowEvaluable<T> expression) {
        return stream().min(Comparator.comparingDouble(r -> expression.evaluate(r).doubleValue())).orElse(null);
    }

    /**
     * Returns the row for which the given column has its minimum value.
     *
     * @param column Column to minimise
     * @param <T>    Data type
     *
     * @return Min row
     */
    public <T extends Number> Row minBy(Column<T> column) {
        return minBy(r -> r.get(column));
    }

    /**
     * Tests whether all rows in this table pass the given predicate.
     *
     * @param test Predicate to test each row with
     *
     * @return Do all rows pass?
     */
    public boolean allMatch(Predicate<Row> test) {
        return stream().allMatch(test);
    }

    /**
     * Tests whether no rows in this table pass the given predicate.
     *
     * @param test Predicate to test each row with
     *
     * @return Do no rows pass?
     */
    public boolean noneMatch(Predicate<Row> test) {
        return stream().noneMatch(test);
    }

    /**
     * Returns a Matrix of the given numerical columns.
     *
     * @param columns Columns to use
     *
     * @return Matrix
     */
    public RealMatrix toMatrix(Column<? extends Number>... columns) {
        return toMatrix(Arrays.stream(columns).map(c -> (RowEvaluable<? extends Number>) (row -> row.get(c))).toArray(RowEvaluable[]::new));
    }

    /**
     * Returns a Matrix of the given expressions.
     *
     * @param expressions Expressions to use for each column
     *
     * @return Matrix
     */
    public RealMatrix toMatrix(RowEvaluable<? extends Number>... expressions) {

        RealMatrix matrix = new RealMatrix(getRowCount(), expressions.length);

        int i = 0;
        for (Row row : this) {

            for (int j = 0; j < expressions.length; j++) {

                matrix.set(i, j, expressions[j].evaluate(row).doubleValue());

            }

            i++;

        }

        return matrix;

    }

    /**
     * Stores the given value in this table's header, using the specified key to identify it.
     *
     * @param key   Key (unique identifier of attribute)
     * @param value Attribute value
     */
    public void setAttribute(String key, Object value) {
        setAttribute(key, value.toString());
    }

    /**
     * Stores the given value in this table's header, using the specified key to identify it.
     *
     * @param key   Key (unique identifier of attribute)
     * @param value Attribute value
     */
    public void setAttribute(String key, String value) {
        attributes.put(key, value);
    }

    /**
     * Retrieves the value identified by the given key in the table's header.
     *
     * @param key Key (unique identifier of attribute)
     *
     * @return Attribute value
     */
    public String getAttribute(String key) {
        return attributes.get(key);
    }

    /**
     * Returns a Key -> Value map of all attributes as Strings.
     *
     * @return Map of all attributes
     */
    public Map<String, String> getAttributes() {
        return Map.copyOf(attributes);
    }

    /**
     * Returns the row with the given index as a Row object.
     *
     * @param index Row index
     *
     * @return Row object for given index
     */
    public abstract Row getRow(int index);

    /**
     * Returns the row with the given index as a Row object. Alias for getRow(...).
     *
     * @param index Row index
     *
     * @return Row object for given index
     */
    public Row get(int index) {
        return getRow(index);
    }

    public <T> List<T> get(RowEvaluable<T> column) {
        return toList(column);
    }

    public <T> T get(int index, RowEvaluable<T> column) {
        return column.evaluate(get(index));
    }

    /**
     * Implementation method for storing a given Row of data.
     *
     * @param row Row to store
     */
    protected abstract void addRowData(Row row);

    /**
     * Add a new row to the table, by supplying a Row object.
     *
     * @param row Row to add
     */
    public void addRow(Row row) {
        addRowData(row);
        rowListeners.forEach(l -> l.added(row));
    }

    /**
     * Implementation method for clearing all data from the table.
     */
    protected abstract void clearData();

    /**
     * Clears this table of all data.
     */
    public void clear() {
        clearData();
        clearListeners.forEach(ClearListener::cleared);
    }

    /**
     * Returns the number of rows in the table.
     *
     * @return Number of rows
     */
    public abstract int getRowCount();

    /**
     * Returns the number of rows in the table. Alias of getRowCount().
     *
     * @return Number of rows
     */
    public int size() {
        return getRowCount();
    }

    /**
     * Returns the number of columns in the table.
     *
     * @return Numnber of columns
     */
    public int getColumnCount() {
        return columns.size();
    }

    /**
     * Adds a new row to the table by specifying data in column order.
     *
     * @param data Values to add, in column order
     */
    public void addData(Object... data) {

        if (data.length != columns.size()) {
            throw new IllegalArgumentException("The number of values supplied does not match the number of columns.");
        }

        List<Integer>          invalids = new LinkedList<>();
        Map<Column<?>, Object> map      = new LinkedHashMap<>();

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

            addRow(new Row(columns, map));

        }

    }

    /**
     * Outputs this table in CSV format to the standard output stream.
     */
    public void output() {
        output(System.out);
    }

    /**
     * Outputs this table in CSV format to the specified file.
     *
     * @param file Path to file
     *
     * @throws IOException Upon error opening/writing file
     */
    public void output(String file) throws IOException {
        PrintStream writer = new PrintStream(new FileOutputStream(file));
        output(writer);
        writer.close();
    }

    /**
     * Returns the attribute header line required for writing to file.
     *
     * @return Attribute header line
     */
    protected String getAttributeLine() {
        return String.format("%% ATTRIBUTES: %s", new JSONObject(getAttributes()));
    }

    /**
     * Returns the column header line required for writing to file.
     *
     * @return Column header line
     */
    protected String getColumnHeaderLine() {

        String[] header = new String[columns.size()];

        for (int i = 0; i < header.length; i++) {
            header[i] = String.format("\"%s\" {%s}", columns.get(i).getTitle().replace("\"", "\\\""), columns.get(i).getType().getSimpleName());
        }

        return String.join(", ", header);

    }

    /**
     * Returns the CSV line for the given row, as required for writing to file.
     *
     * @param row Row to CSV-ify
     *
     * @return CSV line
     */
    protected String getCSVLine(Row row) {

        String[] parts = new String[columns.size()];
        Arrays.fill(parts, "null");

        for (int i = 0; i < parts.length; i++) {

            if (row.get(columns.get(i)) == null) {
                parts[i] = "null";
            } else {
                parts[i] = columns.get(i).stringify(row.get(columns.get(i))).replace(",", "\\,");
            }

        }

        return String.join(", ", parts);

    }

    /**
     * Parses a CSV-formatted line and converts it into a Row object, as required when reading from file.
     *
     * @param line Line to parse
     *
     * @return Parsed Row object
     */
    protected Row parseCSVLine(String line) {

        if (line == null) {
            return null;
        }

        String[] parts = Arrays.stream(line.replace("\\,", "&comma;").split(","))
                               .map(s -> s.trim().replace("&comma;", ","))
                               .toArray(String[]::new);

        int                    length = Math.min(parts.length, columns.size());
        Map<Column<?>, Object> map    = new LinkedHashMap<>();

        for (int i = 0; i < length; i++) {
            map.put(columns.get(i), parts[i].equals("null") ? null : columns.get(i).parse(parts[i]));
        }

        return new Row(columns, map);

    }

    /**
     * Outputs this table in CSV format to the given output (print) stream.
     *
     * @param out Output PrintStream
     */
    public void output(PrintStream out) {

        out.println(getAttributeLine());
        out.println(getColumnHeaderLine());

        for (Row row : this) {
            out.println(getCSVLine(row));
        }

    }

    /**
     * Outputs this table as a terminal-friendly ASCII table, to the given output (print) stream, using the given format
     * specifier for all columns.
     *
     * @param stream    Output stream to write to
     * @param formatter Format specifier
     */
    public void outputTable(PrintStream stream, String formatter) {

        int[] widths = new int[getColumnCount()];

        for (int i = 0; i < widths.length; i++) {

            Column<?> column = getColumn(i);
            int       max    = column.getTitle().length();

            for (Row r : this) {
                max = Math.max(max, String.format(formatter, r.get(column)).length());
            }

            widths[i] = max;

        }

        stream.print("=");

        for (int w : widths) {

            for (int i = 0; i < w + 2; i++) {
                stream.print("=");
            }

            stream.print("=");

        }

        stream.println();
        stream.print("|");

        for (int i = 0; i < getColumnCount(); i++) {

            Column<?> column = getColumn(i);

            stream.print(" ");
            String title = column.getTitle();
            stream.print(title);

            for (int n = 0; n < widths[i] - title.length(); n++) {
                stream.print(" ");
            }

            stream.print(" |");

        }

        stream.print("\n=");

        for (int w : widths) {

            for (int i = 0; i < w + 2; i++) {
                stream.print("=");
            }

            stream.print("=");

        }

        stream.println();

        for (Row r : this) {

            stream.print("|");

            for (int i = 0; i < getColumnCount(); i++) {

                Column<?> column = getColumn(i);

                stream.print(" ");
                String value = String.format(formatter, r.get(column));
                stream.print(value);

                for (int n = 0; n < widths[i] - value.length(); n++) {
                    stream.print(" ");
                }

                stream.print(" |");

            }

            stream.println();
            stream.print("+");

            for (int w : widths) {

                for (int i = 0; i < w + 2; i++) {
                    stream.print("-");
                }

                stream.print("+");

            }

            stream.println();

        }

    }

    /**
     * Outputs this table as a terminal-friendly ASCII table, to the given output (print) stream.
     *
     * @param stream Output stream to write to
     */
    public void outputTable(PrintStream stream) {
        outputTable(stream, "%s");
    }

    /**
     * Outputs this table as a terminal-friendly ASCII table, to the standard output stream.
     */
    public void outputTable() {
        outputTable(System.out);
    }

    /**
     * Outputs this table as a terminal-friendly ASCII table, to the specified file.
     *
     * @param path Path to file
     */
    public void outputTable(String path) throws FileNotFoundException {
        PrintStream writer = new PrintStream(new FileOutputStream(path));
        outputTable(writer);
        writer.close();
    }

    public String getCSV() {

        OutputStream os     = new ByteArrayOutputStream();
        PrintStream  writer = new PrintStream(os);
        outputTable(writer);
        writer.close();

        return os.toString();

    }

    /**
     * Outputs this table as an HTML table node, to the given output (print) stream.
     *
     * @param stream Output stream to write to
     */
    public void outputHTML(PrintStream stream) {

        stream.println("<table>");
        stream.println("<thead><tr>");

        for (Column column : columns) {

            stream.print("<th>");
            stream.print(column.getTitle());
            stream.println("</th>");

        }

        stream.println("</tr></thead>");

        stream.println("<tbody>");

        for (Row row : this) {

            stream.println("<tr>");

            for (Column column : columns) {

                stream.print("<td>");
                stream.print(row.get(column).toString());
                stream.println("</td>");

            }

            stream.println("</tr>");

        }

        stream.println("</tbody>");
        stream.println("</table>");

    }

    /**
     * Outputs this table as an HTML table node, to the standard output stream.
     */
    public void outputHTML() {
        outputHTML(System.out);
    }

    /**
     * Outputs this table as an HTML table node, to the specified file.
     *
     * @param file Path to file
     */
    public void outputHTML(String file) throws FileNotFoundException {
        PrintStream stream = new PrintStream(new FileOutputStream(file));
        outputHTML(stream);
        stream.close();
    }

    /**
     * Returns this table as an HTML String.
     *
     * @return HTML table
     */
    public String getHTML() {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        outputHTML(new PrintStream(stream));

        return stream.toString();

    }

    /**
     * Starts a row-building chain call.
     *
     * @return RowBuilder object
     */
    public RowBuilder startRow() {
        return new RowBuilder();
    }

    /**
     * Adds a new row by using a lambda expression.
     *
     * @param rowable Lambda expression to set each column
     *
     * @throws Exception Forwarded exceptions from lambda
     */
    public void addRow(Rowable rowable) throws Exception {

        RowSetter builder = new RowSetter();
        rowable.build(builder);
        builder.endRow();

    }

    /**
     * Adds a new row based on Column -> Value mappings.
     *
     * @param data Map of data to add
     */
    public void mapRow(Map<Column<?>, Object> data) {
        addRow(new Row(columns, data));
    }

    /**
     * Adds a new row based on Column -> Value mappings.
     *
     * @param values Map entries of data to add
     */
    public void mapRow(Map.Entry<Column, Object>... values) {
        mapRow(Arrays.stream(values).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    /**
     * Adds a new row based on Column -> Value mappings.
     *
     * @param values Map entries of data to add
     */
    public void mapRow(Pair<Column, Object>... values) {
        mapRow(Arrays.stream(values).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));
    }

    /**
     * Adds multiple new rows based on Column -> Iterable<Value> mappings. Each map entry should be of a column mapped
     * to an iterable collection of values to add in that column.
     *
     * @param rows Map of columns
     */
    public void mapRows(Map<Column, Iterable> rows) {

        List<Map.Entry<Column, Iterator>> iterators = rows
            .entrySet()
            .stream()
            .map(e -> Map.entry(e.getKey(), e.getValue().iterator()))
            .collect(Collectors.toList());

        while (iterators.stream().allMatch(e -> e.getValue().hasNext())) {
            mapRow(iterators.stream().map(e -> Map.entry(e.getKey(), e.getValue().next())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }

    }

    /**
     * Adds multiple new rows based on Column -> Iterable<Value> mappings. Each map entry should be of a column mapped
     * to an iterable collection of values to add in that column.
     *
     * @param rows Map entries of columns
     */
    public void mapRows(Pair<Column, Iterable>... rows) {
        mapRows(Arrays.stream(rows).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));
    }

    /**
     * Returns a sequential Stream of Row objects with this table as its source
     *
     * @return Stream of rows
     */
    public abstract Stream<Row> stream();

    /**
     * Returns a collector object for a stream sourced from this table, to collect its rows into a table with the same
     * structure.
     *
     * @return Collector of ResultTable rows
     */
    public Collector<Row, ?, ResultList> collector() {
        return ResultList.collect(this);
    }

    public void outputHDF(WritableGroup group) {

        for (Column column : columns) {

            Class type = column.getType();
            List  data = toList(column);

            if (type == Double.class) {
                group.putDataset(column.getTitle(), Doubles.toArray(data));
            } else if (type == Integer.class) {
                group.putDataset(column.getTitle(), Ints.toArray(data));
            } else if (type == Long.class) {
                group.putDataset(column.getTitle(), Longs.toArray(data));
            } else if (type == Boolean.class) {
                group.putDataset(column.getTitle(), Booleans.toArray(data));
            } else {
                group.putDataset(column.getTitle(), data.stream().map(Object::toString).toArray(String[]::new));
            }

        }

        attributes.forEach(group::putAttribute);

    }

    public void outputHDF(String filePath, String groupPath) {

        try (WritableHdfFile file = HdfFile.write(Path.of(filePath))) {

            if (groupPath == null || groupPath.isBlank()) {
                outputHDF(file);
            } else {

                WritableGroup group = file;

                for (String part : groupPath.split("/")) {
                    group = group.putGroup(part);
                }

                outputHDF(group);

            }

        }

    }

    public void outputHDF(String filePath) {
        outputHDF(filePath, null);
    }

    public void outputHDFDataset(WritableGroup group, String name, Column<? extends Number>... columns) {

        if (columns == null || columns.length == 0) {
            columns = getNumericColumns().toArray(Column[]::new);
        }

        double[][] data = new double[size()][columns.length];

        int i = 0;

        for (Row row : this) {

            for (int j = 0; j < columns.length; j++) {

                data[i][j] = row.get(columns[j]).doubleValue();

            }

            i++;

        }

        WritiableDataset set = group.putDataset(name, data);

        set.putAttribute("Columns", Arrays.stream(columns).map(Column::getTitle).toArray(String[]::new));

    }

    public void outputHDFDataset(String filePath, String datasetPath, Column<? extends Number>... columns) {

        try (WritableHdfFile file = HdfFile.write(Path.of(filePath))) {

            if (datasetPath == null || datasetPath.isBlank()) {
                throw new IllegalArgumentException("datasetPath is null or empty");
            } else {

                WritableGroup group = file;
                String[]      parts = datasetPath.split("/");

                for (int i = 0; i < parts.length - 1; i++) {
                    group = group.putGroup(parts[i]);
                }

                outputHDFDataset(group, parts[parts.length - 1], columns);

            }

        }

    }

    public void outputHDFDataset(String filePath, Column<? extends Number>... columns) {
        outputHDFDataset(filePath, "Data", columns);
    }

    /**
     * Interface for building a row by use of a RowSetter
     */
    public interface Rowable {

        /**
         * Build a new row using the given RowSetter.
         *
         * @param row RowSetter to build with
         *
         * @throws Exception Forwarded exception
         */
        void build(RowSetter row) throws Exception;

    }

    public class RowSetter {

        private final Map<Column<?>, Object> map = new LinkedHashMap<>();

        public RowSetter() {
            columns.forEach(c -> map.put(c, null));
        }

        /**
         * Set the specified column value for this new row to the given value.
         *
         * @param column Column to set
         * @param value  Value to set column to
         * @param <T>    Data type
         *
         * @return Self reference
         */
        public <T> RowSetter set(Column<T> column, T value) {

            column = findColumn(column);

            if (column == null) {
                throw new IllegalArgumentException("Specified column does not exist in table.");
            }

            map.put(column, value);

            return this;

        }

        /**
         * Gets the specified column value for this new row.
         *
         * @param column Column to get value of
         * @param <T>    Data type
         *
         * @return Column value
         */
        public <T> T get(Column<T> column) {
            return (T) map.getOrDefault(column, null);
        }

        /**
         * Ends the row-building process and adds the new row to the table that spawned it.
         *
         * @return ResultTable reference
         */
        protected ResultTable endRow() {
            addRow(new Row(columns, map));
            return ResultTable.this;
        }

    }

    public class RowBuilder extends RowSetter {

        public ResultTable endRow() {
            return super.endRow();
        }

        public <T> RowBuilder set(Column<T> column, T value) {
            super.set(column, value);
            return this;
        }

    }

}
