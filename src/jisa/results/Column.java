package jisa.results;

import kotlin.jvm.JvmClassMappingKt;
import kotlin.reflect.KClass;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public abstract class Column<T> implements RowEvaluable<T> {

    private final String          name;
    private final String          units;
    private final Class<T>        type;
    private final RowEvaluable<T> evaluable;

    /**
     * Creates a column for data of a given type.
     *
     * @param type  Type class of the data to hold: Double/Long/Integer/Boolean/String
     * @param name  Name of the column
     * @param units Units of the column
     * @param <T>   Type
     *
     * @return The created column
     */
    public static <T> Column<T> of(Class<T> type, String name, String units) {

        if (type == Double.class) {
            return (Column<T>) ofDoubles(name, units);
        }

        if (type == Long.class) {
            return (Column<T>) ofLongs(name, units);
        }

        if (type == Integer.class) {
            return (Column<T>) ofIntegers(name, units);
        }

        if (type == Boolean.class) {
            return (Column<T>) ofBooleans(name, units);
        }

        if (type == String.class) {
            return (Column<T>) ofStrings(name, units);
        }

        throw new IllegalArgumentException("Columns cannot be made for objects of type " + type.getName());

    }

    /**
     * Creates a column for data of a given type.
     *
     * @param type  Type class of the data to hold: Double/Long/Integer/Boolean/String
     * @param name  Name of the column
     * @param units Units of the column
     * @param <T>   Type
     *
     * @return The created column
     */
    public static <T> Column<T> of(KClass<T> type, String name, String units) {
        return of(JvmClassMappingKt.getJavaClass(type), name, units);
    }

    /**
     * Creates a column for data of the same type as a supplied example.
     *
     * @param example The example data
     * @param name    Name of column
     * @param units   Units of column
     * @param <T>     Data Type
     *
     * @return The created column
     */
    public static <T> Column<T> byExample(T example, String name, String units) {

        if (example instanceof Double) {
            return (Column<T>) ofDoubles(name, units);
        }

        if (example instanceof Long) {
            return (Column<T>) ofLongs(name, units);
        }

        if (example instanceof Integer) {
            return (Column<T>) ofIntegers(name, units);
        }

        if (example instanceof Boolean) {
            return (Column<T>) ofBooleans(name, units);
        }

        if (example instanceof String) {
            return (Column<T>) ofStrings(name, units);
        }

        throw new IllegalArgumentException("Columns cannot be made for objects of type " + example.getClass().getName());

    }

    /**
     * Creates a column for storing String values.
     *
     * @param name  Name of the column
     * @param units Units of the column
     *
     * @return The created column
     */
    public static StringColumn ofStrings(String name, String units) {
        return new StringColumn(name, units);
    }

    /**
     * Creates a column for storing String values.
     *
     * @param name Name of the column
     *
     * @return The created column
     */
    public static StringColumn ofStrings(String name) {
        return new StringColumn(name);
    }


    /**
     * Creates an auto-evaluating column for storing String values.
     *
     * @param name      Name of the column
     * @param evaluable Lambda for calculating column value
     *
     * @return The created column
     */
    public static StringColumn ofStrings(String name, RowEvaluable<String> evaluable) {
        return new StringColumn(name, evaluable);
    }

    /**
     * Creates an auto-evaluating column for storing String values.
     *
     * @param name      Name of the column
     * @param units     Units of the column
     * @param evaluable Lambda for calculating column value
     *
     * @return The created column
     */
    public static StringColumn ofStrings(String name, String units, RowEvaluable<String> evaluable) {
        return new StringColumn(name, units, evaluable);
    }

    /**
     * Creates a column for storing String values. Alias for Column.ofStrings(...).
     *
     * @param name  Name of the column
     * @param units Units of the column
     *
     * @return The created column
     */
    public static StringColumn ofText(String name, String units) {
        return ofStrings(name, units);
    }

    /**
     * Creates a column for storing String values. Alias for Column.ofStrings(...).
     *
     * @param name Name of the column
     *
     * @return The created column
     */
    public static StringColumn ofText(String name) {
        return ofStrings(name);
    }

    /**
     * Creates an auto-evaluating column for storing String values. Alias for Column.ofStrings(...).
     *
     * @param name      Name of the column
     * @param evaluable Lambda for calculating column value
     *
     * @return The created column
     */
    public static StringColumn ofText(String name, RowEvaluable<String> evaluable) {
        return ofStrings(name, evaluable);
    }

    /**
     * Creates an auto-evaluating column for storing String values. Alias for Column.ofStrings(...).
     *
     * @param name      Name of the column
     * @param units     Units of the column
     * @param evaluable Lambda for calculating column value
     *
     * @return The created column
     */
    public static StringColumn ofText(String name, String units, RowEvaluable<String> evaluable) {
        return ofStrings(name, units, evaluable);
    }

    /**
     * Creates a column for storing double values.
     *
     * @param name  Name of the column
     * @param units Units of the column
     *
     * @return The created column
     */
    public static DoubleColumn ofDoubles(String name, String units) {
        return new DoubleColumn(name, units);
    }

    /**
     * Creates a column for storing double values.
     *
     * @param name Name of the column
     *
     * @return The created column
     */
    public static DoubleColumn ofDoubles(String name) {
        return new DoubleColumn(name);
    }

    /**
     * Creates an auto-evaluating column for storing double values.
     *
     * @param name      Name of the column
     * @param evaluable Lambda for calculating column value
     *
     * @return The created column
     */
    public static DoubleColumn ofDoubles(String name, RowEvaluable<Double> evaluable) {
        return new DoubleColumn(name, evaluable);
    }

    /**
     * Creates an auto-evaluating column for storing double values.
     *
     * @param name      Name of the column
     * @param units     Units of the column
     * @param evaluable Lambda for calculating column value
     *
     * @return The created column
     */
    public static DoubleColumn ofDoubles(String name, String units, RowEvaluable<Double> evaluable) {
        return new DoubleColumn(name, units, evaluable);
    }

    /**
     * Creates a column for storing double values. Alias for Column.ofDoubles(...).
     *
     * @param name  Name of the column
     * @param units Units of the column
     *
     * @return The created column
     */
    public static DoubleColumn ofDecimals(String name, String units) {
        return ofDoubles(name, units);
    }

    /**
     * Creates a column for storing double values. Alias for Column.ofDoubles(...).
     *
     * @param name Name of the column
     *
     * @return The created column
     */
    public static DoubleColumn ofDecimals(String name) {
        return ofDoubles(name);
    }

    /**
     * Creates an auto-evaluating column for storing double values. Alias for Column.ofDoubles(...).
     *
     * @param name      Name of the column
     * @param evaluable Lambda for calculating column value
     *
     * @return The created column
     */
    public static DoubleColumn ofDecimals(String name, RowEvaluable<Double> evaluable) {
        return ofDoubles(name, evaluable);
    }

    /**
     * Creates an auto-evaluating column for storing double values. Alias for Column.ofDoubles(...).
     *
     * @param name      Name of the column
     * @param units     Units of the column
     * @param evaluable Lambda for calculating column value
     *
     * @return The created column
     */
    public static DoubleColumn ofDecimals(String name, String units, RowEvaluable<Double> evaluable) {
        return ofDoubles(name, units, evaluable);
    }

    /**
     * Creates a column for storing integer values.
     *
     * @param name  Name of the column
     * @param units Units of the column
     *
     * @return The created column
     */
    public static Column<Integer> ofIntegers(String name, String units) {
        return new IntColumn(name, units);
    }

    /**
     * Creates a column for storing integer values.
     *
     * @param name Name of the column
     *
     * @return The created column
     */
    public static Column<Integer> ofIntegers(String name) {
        return new IntColumn(name);
    }

    /**
     * Creates an auto-evaluating column for storing integer values.
     *
     * @param name      Name of the column
     * @param evaluable Lambda for calculating column value
     *
     * @return The created column
     */
    public static Column<Integer> ofIntegers(String name, RowEvaluable<Integer> evaluable) {
        return new IntColumn(name, evaluable);
    }

    /**
     * Creates an auto-evaluating column for storing integer values.
     *
     * @param name      Name of the column
     * @param units     Units of the column
     * @param evaluable Lambda for calculating column value
     *
     * @return The created column
     */
    public static Column<Integer> ofIntegers(String name, String units, RowEvaluable<Integer> evaluable) {
        return new IntColumn(name, units, evaluable);
    }

    /**
     * Creates a column for storing long integer values.
     *
     * @param name  Name of the column
     * @param units Units of the column
     *
     * @return The created column
     */
    public static Column<Long> ofLongs(String name, String units) {
        return new LongColumn(name, units);
    }

    /**
     * Creates a column for storing long integer values.
     *
     * @param name Name of the column
     *
     * @return The created column
     */
    public static Column<Long> ofLongs(String name) {
        return new LongColumn(name);
    }

    /**
     * Creates an auto-evaluating column for storing long integer values.
     *
     * @param name      Name of the column
     * @param evaluable Lambda for calculating column value
     *
     * @return The created column
     */
    public static Column<Long> ofLongs(String name, RowEvaluable<Long> evaluable) {
        return new LongColumn(name, evaluable);
    }

    /**
     * Creates an auto-evaluating column for storing long integer values.
     *
     * @param name      Name of the column
     * @param units     Units of the column
     * @param evaluable Lambda for calculating column value
     *
     * @return The created column
     */
    public static Column<Long> ofLongs(String name, String units, RowEvaluable<Long> evaluable) {
        return new LongColumn(name, units, evaluable);
    }

    /**
     * Creates a column for storing boolean values.
     *
     * @param name  Name of the column
     * @param units Units of the column
     *
     * @return The created column
     */
    public static Column<Boolean> ofBooleans(String name, String units) {
        return new BooleanColumn(name, units);
    }

    /**
     * Creates a column for storing boolean values.
     *
     * @param name Name of the column
     *
     * @return The created column
     */
    public static Column<Boolean> ofBooleans(String name) {
        return new BooleanColumn(name);
    }

    /**
     * Creates an auto-evaluating column for storing boolean values.
     *
     * @param name      Name of the column
     * @param evaluable Lambda for calculating column value
     *
     * @return The created column
     */
    public static Column<Boolean> ofBooleans(String name, RowEvaluable<Boolean> evaluable) {
        return new BooleanColumn(name, evaluable);
    }

    /**
     * Creates an auto-evaluating column for storing boolean values.
     *
     * @param name      Name of the column
     * @param units     Units of the column
     * @param evaluable Lambda for calculating column value
     *
     * @return The created column
     */
    public static Column<Boolean> ofBooleans(String name, String units, RowEvaluable<Boolean> evaluable) {
        return new BooleanColumn(name, units, evaluable);
    }

    /**
     * Constructs a column with given name, units, and data type.
     *
     * @param name  The name of the column
     * @param units The units of the column
     * @param type  The data type that the column will hold
     */
    public Column(String name, String units, Class<T> type) {
        this(name, units, type, null);
    }

    /**
     * Constructs a column with given name, and data type.
     *
     * @param name The name of the column
     * @param type The data type that the column will hold
     */
    public Column(String name, Class<T> type) {
        this(name, null, type, null);
    }

    /**
     * Constructs an auto-evaluating column with given name, and data type.
     *
     * @param name      The name of the column
     * @param type      The data type that the column will hold
     * @param evaluable Lambda for auto-evaluation
     */
    public Column(String name, Class<T> type, RowEvaluable<T> evaluable) {
        this(name, null, type, evaluable);
    }

    /**
     * Constructs an auto-evaluating column with given name, units, and data type.
     *
     * @param name      The name of the column
     * @param units     The units of the column
     * @param type      The data type that the column will hold
     * @param evaluable Lambda for auto-evaluation
     */
    public Column(String name, String units, Class<T> type, RowEvaluable<T> evaluable) {

        this.name      = name;
        this.units     = units;
        this.type      = type;
        this.evaluable = evaluable;

    }

    /**
     * Converts a value of this column's data type into a String, for outputting.
     *
     * @param value Value to convert
     *
     * @return String representation of value
     */
    public abstract String stringify(Object value);

    /**
     * Parses a string representation of a value of this column's data type into its true representation.
     *
     * @param string String to parse
     *
     * @return Parsed data
     */
    public abstract T parse(String string);

    public abstract void writeToStream(DataOutputStream stream, T value) throws IOException;

    public abstract T readFromStream(InputStream stream) throws IOException;

    public abstract void skipBytes(InputStream stream) throws IOException;

    /**
     * Returns the name of this column.
     *
     * @return Name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns whether this column has units or not.
     *
     * @return Does it have units?
     */
    public boolean hasUnits() {
        return units != null;
    }

    /**
     * Returns the units of this column.
     *
     * @return Units
     */
    public String getUnits() {
        return units;
    }

    /**
     * Returns a formatted string of both the column's name and units.
     *
     * @return Name and [units]
     */
    public String getTitle() {

        if (hasUnits()) {
            return String.format("%s [%s]", name, units);
        } else {
            return name;
        }

    }

    /**
     * Returns a specially formatted instance of this column's name for findColumn(...) purposes.
     *
     * @return Lower-case, trimmed name
     */
    public String getMatcherName() {
        return name.toLowerCase().trim();
    }

    /**
     * Returns a specially formatted instance of this column's title for findColumn(...) purposes.
     *
     * @return Lower-case, trimmed title
     */
    public String getMatcherTitle() {
        return getTitle().toLowerCase().trim();
    }

    /**
     * Returns the class of this column's data type.
     *
     * @return Data type class
     */
    public Class<T> getType() {
        return type;
    }

    /**
     * Returns whether this column is auto-evaluated or not.
     *
     * @return Auto-evaluated?
     */
    public boolean isCalculated() {
        return evaluable != null;
    }

    /**
     * For auto-evaluable columns, this will return their calculated value based on the supplied row.
     *
     * @param row Row to calculate from
     *
     * @return Calculated value
     */
    public T calculate(Row row) {

        if (evaluable == null) {
            return null;
        }

        return evaluable.evaluate(row);

    }

    /**
     * Maps this column to a value, for ResultTable::mapRow.
     *
     * @param value Value to map to
     *
     * @return Map entry
     */
    public Map.Entry<Column, Object> to(T value) {
        return Map.entry(this, value);
    }

    @Override
    public T evaluate(Row row) {
        return row.get(this);
    }



}
