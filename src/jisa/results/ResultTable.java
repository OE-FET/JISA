package jisa.results;

import com.google.common.collect.Lists;
import jisa.maths.matrices.RealMatrix;
import org.json.JSONObject;

import java.io.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ResultTable implements Iterable<Row> {

    public static final Map<String, ResultList.ColumnBuilder> STANDARD_TYPES = Map.of(
        "String",  StringColumn::new,
        "Double",  DoubleColumn::new,
        "Integer", IntColumn::new,
        "Boolean", BooleanColumn::new,
        "Long",    LongColumn::new
    );

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
            throw new IndexOutOfBoundsException("Column index is out of bounds. Must be 0 <= index <= " + (columns.size() - 1) + ".");
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

    public List<Column<? extends Number>> getNumericColumns() {

        return columns.stream()
                      .filter(c -> Number.class.isAssignableFrom(c.getType()))
                      .map(c -> (Column<? extends Number>) c)
                      .collect(Collectors.toList());

    }

    public Column<? extends Number> getFirstNumericColumn() {

        return columns.stream()
                      .filter(c -> Number.class.isAssignableFrom(c.getType()))
                      .map(c -> (Column<? extends Number>) c)
                      .findFirst().orElse(null);

    }

    public Column<? extends Number> getNthNumericColumn(int n) {

        try {
            return columns.stream()
                          .filter(c -> Number.class.isAssignableFrom(c.getType()))
                          .map(c -> (Column<? extends Number>) c)
                          .toArray(Column[]::new)[n];
        } catch (IndexOutOfBoundsException e) {
            return null;
        }

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
     * Returns all values in the given column in a List.
     *
     * @param column Column to extract
     * @param <T>    Data type of column
     *
     * @return Column values, as a list
     */
    public <T> List<T> toList(Column<T> column) {
        return toList(r -> r.get(column));
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

                if (newDirection != direction && newDirection != 0) {
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

    public List<ResultList> directionalSplit(Column<? extends Number> splitBy) {
        return directionalSplit(r -> r.get(splitBy));
    }

    public ResultList filter(Predicate<Row> test) {
        return stream().filter(test).collect(ResultList.collect(this));
    }

    public ResultList sorted(RowEvaluable<?> expression) {

        if (getRowCount() < 2) {
            return ResultList.copyOf(this);
        }

        Object value = expression.evaluate(getRow(0));

        if (value instanceof Number) {
            RowEvaluable<? extends Number> byExpression = (RowEvaluable<? extends Number>) expression;
            return stream().sorted(Comparator.comparingDouble(r -> byExpression.evaluate(r).doubleValue())).collect(ResultList.collect(this));
        } else {
            return stream().sorted(Comparator.comparing(r -> expression.evaluate(r).toString())).collect(ResultList.collect(this));
        }

    }

    public ResultList sorted(Column<?> byColumn) {
        return sorted(r -> r.get(byColumn));
    }

    public ResultList reversed() {
        return Lists.reverse(getRows()).stream().collect(ResultList.collect(this));
    }

    public List<Row> getRows() {
        return stream().collect(Collectors.toList());
    }

    public Row findRow(Predicate<Row> test) {
        return stream().filter(test).findFirst().orElse(null);
    }

    public double getMean(RowEvaluable<? extends Number> expression) {
        return stream().mapToDouble(r -> expression.evaluate(r).doubleValue()).average().orElse(0.0);
    }

    public double getMean(Column<? extends Number> column) {
        return getMean(r -> r.get(column));
    }

    public <T extends Number> T getMin(RowEvaluable<T> expression) {
        return stream().map(expression::evaluate).min(Comparator.comparingDouble(Number::doubleValue)).orElse(null);
    }

    public <T extends Number> T getMin(Column<T> column) {
        return getMin(r -> r.get(column));
    }

    public <T extends Number> T getMax(RowEvaluable<T> expression) {
        return stream().map(expression::evaluate).max(Comparator.comparingDouble(Number::doubleValue)).orElse(null);
    }

    public <T extends Number> T getMax(Column<T> column) {
        return getMax(r -> r.get(column));
    }

    public boolean allMatch(Predicate<Row> test) {
        return stream().allMatch(test);
    }

    public boolean noneMatch(Predicate<Row> test) {
        return stream().noneMatch(test);
    }

    public RealMatrix toMatrix(Column<? extends Number>... columns) {
        return toMatrix(Arrays.stream(columns).map(c -> (RowEvaluable<? extends Number>) (row -> row.get(c))).toArray(RowEvaluable[]::new));
    }

    public RealMatrix toMatrix(RowEvaluable<? extends Number>... columns) {

        RealMatrix matrix = new RealMatrix(getRowCount(), columns.length);

        int i = 0;
        for (Row row : this) {

            for (int j = 0; j < columns.length; j++) {

                matrix.set(i, j, columns[j].evaluate(row).doubleValue());

            }

            i++;

        }

        return matrix;

    }

    public void setAttribute(String key, Object value) {
        setAttribute(key, value.toString());
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

    public Row get(int index) {
        return getRow(index);
    }

    protected abstract void addRowData(Row row);

    public void addRow(Row row) {
        addRowData(row);
        rowListeners.forEach(l -> l.added(row));
    }

    protected abstract void clearData();

    public void clear() {
        clearData();
        clearListeners.forEach(ClearListener::cleared);
    }

    public abstract int getRowCount();

    public int size() {
        return getRowCount();
    }

    public int getColumnCount() {
        return columns.size();
    }

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

    public void output() {
        output(System.out);
    }

    public void output(String file) throws IOException {
        PrintStream writer = new PrintStream(new FileOutputStream(file));
        output(writer);
        writer.close();
    }

    protected String getAttributeLine() {
        return String.format("%% ATTRIBUTES: %s", new JSONObject(getAttributes()));
    }

    protected String getColumnHeaderLine() {

        String[] header = new String[columns.size()];

        for (int i = 0; i < header.length; i++) {
            header[i] = String.format("\"%s\" {%s}", columns.get(i).getTitle().replace("\"", "\\\""), columns.get(i).getType().getSimpleName());
        }

        return String.join(", ", header);

    }

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

    public void output(PrintStream out) {

        out.println(getAttributeLine());
        out.println(getColumnHeaderLine());

        for (Row row : this) {
            out.println(getCSVLine(row));
        }

    }

    public void outputTable(PrintStream stream, String formatter) {

        int[] widths = new int[getColumnCount()];

        for (int i = 0; i < widths.length; i++) {

            Column<?> column = getColumn(i);
            int       max    = column.getTitle().length();

            for (Row r : this) {
                max = Math.max(max, r.get(column).toString().length());
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
                String value = r.get(column).toString();
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

    public void outputTable(PrintStream stream) {
        outputTable(stream, "%s");
    }


    public void outputTable() {
        outputTable(System.out);
    }

    public void outputTable(String path) throws FileNotFoundException {
        PrintStream writer = new PrintStream(new FileOutputStream(path));
        outputTable(writer);
        writer.close();
    }

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

    public void outputHTML() {
        outputHTML(System.out);
    }

    public void outputHTML(String file) throws FileNotFoundException {
        PrintStream stream = new PrintStream(new FileOutputStream(file));
        outputHTML(stream);
        stream.close();
    }

    public String getHTML() {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        outputHTML(new PrintStream(stream));

        return stream.toString();

    }

    public RowBuilder startRow() {
        return new RowBuilder();
    }

    public void addRow(Rowable rowable) {

        RowSetter builder = new RowSetter();
        rowable.build(builder);
        builder.endRow();

    }

    public void mapRow(Map<Column<?>, Object> data) {
        addRow(new Row(columns, data));
    }

    public void mapRow(Map.Entry<Column, Object>... values) {
        addRow(new Row(columns, Arrays.stream(values).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))));
    }

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

    public abstract Stream<Row> stream();

    public interface Rowable {
        void build(RowSetter row);
    }

    public class RowSetter {

        private final Map<Column<?>, Object> map = new LinkedHashMap<>();

        public RowSetter() {
            columns.forEach(c -> map.put(c, null));
        }

        public <T> RowSetter set(Column<T> column, T value) {

            column = findColumn(column);

            if (column == null) {
                throw new IllegalArgumentException("Specified column does not exist in table.");
            }

            map.put(column, value);

            return this;

        }

        public <T> T get(Column<T> column) {
            return (T) map.getOrDefault(column, null);
        }

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
