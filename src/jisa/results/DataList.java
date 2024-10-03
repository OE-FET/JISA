package jisa.results;

import com.google.common.primitives.Ints;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.zip.InflaterInputStream;

public class DataList extends DataTable {

    private final List<Row> rows;

    public static DataList emptyCopyOf(DataTable original) {

        DataList copy = new DataList(original.getColumnsAsArray());
        original.getAttributes().forEach(copy::setAttribute);
        return copy;

    }

    public static DataList copyOf(DataTable original) {

        DataList copy = emptyCopyOf(original);
        original.forEach(copy::addRow);
        return copy;

    }

    public static DataList loadFile(String filePath) throws IOException {

        DataList list;

        String type = Files.probeContentType(Paths.get(filePath));

        if (type == null) {
            return loadBinaryFile(filePath);
        }

        type = type.toLowerCase();

        if ((type.contains("text") || type.contains("csv"))) {
            return loadCSVFile(filePath);
        } else {
            return loadBinaryFile(filePath);
        }

    }

    public static DataList loadCSVFile(String filePath) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String         header = reader.readLine();

        JSONObject attributes = null;

        if (header.startsWith("% ATTRIBUTES: ")) {
            attributes = new JSONObject(header.replaceFirst("% ATTRIBUTES: ", ""));
            header     = reader.readLine();
        }

        DataList list = new DataList(DataTable.parseColumnHeaderLine(header));

        String line;
        while ((line = reader.readLine()) != null) {
            list.addRow(list.parseCSVLine(line));
        }

        if (attributes != null) {
            attributes.toMap().forEach((k, v) -> list.setAttribute(k, v.toString()));
        }

        return list;

    }

    public static DataList loadBinaryFile(String filePath) throws IOException {

        try {

            try (InputStream stream = new InflaterInputStream(new FileInputStream(filePath))) {
                return loadFromStream(stream);
            }

        } catch (IOException e) {

            try (InputStream stream = new FileInputStream(filePath)) {
                return loadFromStream(stream);
            }

        }

    }

    protected static DataList loadFromStream(InputStream file) throws IOException {

        Map<String, Object> attributes = null;
        List<Column>        columns    = new LinkedList<>();

        int code = file.read();

        while (code != 3) {

            switch (code) {

                case 1:

                    int length = Ints.fromByteArray(file.readNBytes(4));
                    attributes = new JSONObject(new String(file.readNBytes(length), StandardCharsets.UTF_8)).toMap();
                    break;

                case 2:

                    int nameLength = Ints.fromByteArray(file.readNBytes(4));
                    String name = new String(file.readNBytes(nameLength), StandardCharsets.UTF_8);
                    int unitsLength = Ints.fromByteArray(file.readNBytes(4));
                    String units = new String(file.readNBytes(unitsLength), StandardCharsets.UTF_8);
                    int typeLength = Ints.fromByteArray(file.readNBytes(4));
                    String type = new String(file.readNBytes(typeLength), StandardCharsets.UTF_8);

                    columns.add(STANDARD_TYPES.get(type).create(name, units));

                    break;

                default:
                    throw new IOException("Not a valid binary ResultTable file!");

            }

            code = file.read();

        }

        DataList list = new DataList(columns);

        if (attributes != null) {
            attributes.forEach((k, v) -> list.setAttribute(k, v.toString()));
        }

        while (file.available() > 0) {

            RowBuilder builder = list.startRow();

            for (Column column : columns) {
                builder.set(column, column.readFromStream(file));
            }

            builder.endRow();

        }

        return list;

    }

    public static DataList fromCSVString(String csv) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(csv.getBytes())));
        String         header = reader.readLine();

        JSONObject attributes = null;

        if (header.startsWith("% ATTRIBUTES: ")) {
            attributes = new JSONObject(header.replaceFirst("% ATTRIBUTES: ", ""));
            header     = reader.readLine();
        }

        DataList list = new DataList(DataTable.parseColumnHeaderLine(header));

        String line;
        while ((line = reader.readLine()) != null) {
            list.addRow(list.parseCSVLine(line));
        }

        if (attributes != null) {
            attributes.toMap().forEach((k, v) -> list.setAttribute(k, v.toString()));
        }

        return list;

    }

    public DataList(Column... columns) {
        super(columns);
        rows = new LinkedList<>();
    }

    public DataList(String... names) {
        this(Arrays.stream(names).map(DoubleColumn::new).toArray(DoubleColumn[]::new));
    }

    public DataList(Collection<Column> columns) {
        this(columns.toArray(Column[]::new));
    }

    protected DataList(List<Row> rows, boolean dummy) {
        super(rows.stream().flatMap(r -> r.getColumnSet().stream()).distinct().toArray(Column[]::new));
        this.rows = rows;
    }

    @Override
    public Row getRow(int index) {

        if (index < 0 || index >= rows.size()) {
            throw new IndexOutOfBoundsException("Row index out of bounds. 0 <= index <= " + (rows.size() - 1) + ".");
        }

        return rows.get(index);
    }

    @Override
    protected void addRowData(Row row) {
        rows.add(row);
    }

    @Override
    protected void clearData() {
        rows.clear();
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public Stream<Row> stream() {
        return rows.stream();
    }

    @Override
    public Iterator<Row> iterator() {
        return rows.iterator();
    }

    public static Collector<Row, ?, DataList> collect(DataTable source) {

        return new Collector<Row, DataList, DataList>() {

            @Override
            public Supplier<DataList> supplier() {
                return () -> DataList.emptyCopyOf(source);
            }

            @Override
            public BiConsumer<DataList, Row> accumulator() {
                return DataTable::addRow;
            }

            @Override
            public BinaryOperator<DataList> combiner() {

                return (left, right) -> {
                    right.forEach(left::addRow);
                    return left;
                };

            }

            @Override
            public Function<DataList, DataList> finisher() {
                return (r) -> r;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Set.of(Characteristics.IDENTITY_FINISH);
            }

        };

    }

    public static Collector<Row, ?, DataList> collect(Column... columns) {

        return new Collector<Row, DataList, DataList>() {

            @Override
            public Supplier<DataList> supplier() {
                return () -> new DataList(columns);
            }

            @Override
            public BiConsumer<DataList, Row> accumulator() {
                return DataTable::addRow;
            }

            @Override
            public BinaryOperator<DataList> combiner() {

                return (left, right) -> {
                    right.forEach(left::addRow);
                    return left;
                };

            }

            @Override
            public Function<DataList, DataList> finisher() {
                return (r) -> r;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Set.of(Characteristics.IDENTITY_FINISH);
            }

        };

    }

    public static Collector<Row, ?, DataList> collect() {

        return new Collector<Row, List<Row>, DataList>() {

            @Override
            public Supplier<List<Row>> supplier() {
                return LinkedList::new;
            }

            @Override
            public BiConsumer<List<Row>, Row> accumulator() {
                return List::add;
            }

            @Override
            public BinaryOperator<List<Row>> combiner() {

                return (left, right) -> {
                    left.addAll(right);
                    return left;
                };

            }

            @Override
            public Function<List<Row>, DataList> finisher() {
                return (r) -> new DataList(r, true);
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.emptySet();
            }

        };

    }

    public static Collector<Map, ?, DataList> mapCollector() {

        return new Collector<Map, List<Row>, DataList>() {

            @Override
            public Supplier<List<Row>> supplier() {
                return LinkedList::new;
            }

            @Override
            public BiConsumer<List<Row>, Map> accumulator() {
                return (l, m) -> l.add(new Row(m));
            }

            @Override
            public BinaryOperator<List<Row>> combiner() {

                return (left, right) -> {
                    left.addAll(right);
                    return left;
                };

            }

            @Override
            public Function<List<Row>, DataList> finisher() {
                return (r) -> new DataList(r, true);
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.emptySet();
            }

        };

    }

}
