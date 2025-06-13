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

public class ResultList extends ResultTable {

    private final List<Row> rows;

    public static ResultList emptyCopyOf(ResultTable original) {

        ResultList copy = new ResultList(original.getColumnsAsArray());
        original.getAttributes().forEach(copy::setAttribute);
        return copy;

    }

    public static ResultList copyOf(ResultTable original) {

        ResultList copy = emptyCopyOf(original);
        original.forEach(copy::addRow);
        return copy;

    }

    public static ResultList loadFile(String filePath) throws IOException {

        ResultList list;

        try {
            list = loadBinaryFile(filePath);
        } catch (Throwable e) {
            list = loadCSVFile(filePath);
        }

        return list;

    }

    public static ResultList loadCSVFile(String filePath) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String         header = reader.readLine();

        JSONObject attributes = null;

        if (header.startsWith("% ATTRIBUTES: ")) {
            attributes = new JSONObject(header.replaceFirst("% ATTRIBUTES: ", ""));
            header     = reader.readLine();
        }

        ResultList list = new ResultList(ResultTable.parseColumnHeaderLine(header));

        String line;
        while ((line = reader.readLine()) != null) {
            list.addRow(list.parseCSVLine(line));
        }

        if (attributes != null) {
            attributes.toMap().forEach((k, v) -> list.setAttribute(k, v.toString()));
        }

        return list;

    }

    public static ResultList loadFromCSVStream(InputStream stream) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        String         header = reader.readLine();

        JSONObject attributes = null;

        if (header.startsWith("% ATTRIBUTES: ")) {
            attributes = new JSONObject(header.replaceFirst("% ATTRIBUTES: ", ""));
            header     = reader.readLine();
        }

        ResultList list = new ResultList(ResultTable.parseColumnHeaderLine(header));

        String line;
        while ((line = reader.readLine()) != null) {
            list.addRow(list.parseCSVLine(line));
        }

        if (attributes != null) {
            attributes.toMap().forEach((k, v) -> list.setAttribute(k, v.toString()));
        }

        return list;

    }

    public static ResultList loadBinaryFile(String filePath) throws IOException {

        try {

            try (InputStream stream = new InflaterInputStream(new FileInputStream(filePath))) {
                return loadFromBinaryStream(stream);
            }

        } catch (IOException e) {

            try (InputStream stream = new FileInputStream(filePath)) {
                return loadFromBinaryStream(stream);
            }

        }

    }

    protected static ResultList loadFromBinaryStream(InputStream file) throws IOException {

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

        ResultList list = new ResultList(columns);

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

    public static ResultList fromCSVString(String csv) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(csv.getBytes())));
        String         header = reader.readLine();

        JSONObject attributes = null;

        if (header.startsWith("% ATTRIBUTES: ")) {
            attributes = new JSONObject(header.replaceFirst("% ATTRIBUTES: ", ""));
            header     = reader.readLine();
        }

        ResultList list = new ResultList(ResultTable.parseColumnHeaderLine(header));

        String line;
        while ((line = reader.readLine()) != null) {
            list.addRow(list.parseCSVLine(line));
        }

        if (attributes != null) {
            attributes.toMap().forEach((k, v) -> list.setAttribute(k, v.toString()));
        }

        return list;

    }

    public ResultList(Column... columns) {
        super(columns);
        rows = new LinkedList<>();
    }

    public ResultList(String... names) {
        this(Arrays.stream(names).map(DoubleColumn::new).toArray(DoubleColumn[]::new));
    }

    public ResultList(Collection<Column> columns) {
        this(columns.toArray(Column[]::new));
    }

    protected ResultList(List<Row> rows, boolean dummy) {
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

    public static Collector<Row, ?, ResultList> collect(ResultTable source) {

        return new Collector<Row, ResultList, ResultList>() {

            @Override
            public Supplier<ResultList> supplier() {
                return () -> ResultList.emptyCopyOf(source);
            }

            @Override
            public BiConsumer<ResultList, Row> accumulator() {
                return ResultTable::addRow;
            }

            @Override
            public BinaryOperator<ResultList> combiner() {

                return (left, right) -> {
                    right.forEach(left::addRow);
                    return left;
                };

            }

            @Override
            public Function<ResultList, ResultList> finisher() {
                return (r) -> r;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Set.of(Characteristics.IDENTITY_FINISH);
            }

        };

    }

    public static Collector<Row, ?, ResultList> collect(Column... columns) {

        return new Collector<Row, ResultList, ResultList>() {

            @Override
            public Supplier<ResultList> supplier() {
                return () -> new ResultList(columns);
            }

            @Override
            public BiConsumer<ResultList, Row> accumulator() {
                return ResultTable::addRow;
            }

            @Override
            public BinaryOperator<ResultList> combiner() {

                return (left, right) -> {
                    right.forEach(left::addRow);
                    return left;
                };

            }

            @Override
            public Function<ResultList, ResultList> finisher() {
                return (r) -> r;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Set.of(Characteristics.IDENTITY_FINISH);
            }

        };

    }

    public static Collector<Row, ?, ResultList> collect() {

        return new Collector<Row, List<Row>, ResultList>() {

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
            public Function<List<Row>, ResultList> finisher() {
                return (r) -> new ResultList(r, true);
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.emptySet();
            }

        };

    }

    public static Collector<Map, ?, ResultList> mapCollector() {

        return new Collector<Map, List<Row>, ResultList>() {

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
            public Function<List<Row>, ResultList> finisher() {
                return (r) -> new ResultList(r, true);
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.emptySet();
            }

        };

    }

}
