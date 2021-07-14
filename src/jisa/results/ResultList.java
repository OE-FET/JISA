package jisa.results;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class ResultList extends ResultTable {

    private final List<Row> rows = new LinkedList<>();

    public ResultList(Col<?>... columns) {
        super(columns);
    }

    @Override
    public Row getRow(int index) {

        if (index < 0  || index >= rows.size()) {
            throw new IndexOutOfBoundsException("Row index out of bounds. 0 <= index <= " + (rows.size() - 1) + ".");
        }

        return rows.get(index);
    }

    @Override
    protected void addRow(Row row) {
        rows.add(row);
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

}
