package JISA.Experiment;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Table-like structure for holding numerical data
 */
public class ResultList extends ResultTable {

    private String[]          names;
    private String[]          units = null;
    private ArrayList<Result> rows  = new ArrayList<>();

    public ResultList(Col... columns) {
        super(columns);
    }

    public ResultList(String... names) {
        super(names);
    }

    @Override
    public void updateColumns() {

    }

    @Override
    protected void addRow(Result row) {
        rows.add(row);
    }

    @Override
    protected void clearData() {
        rows.clear();
    }

    @Override
    public int getNumRows() {
        return rows.size();
    }

    @Override
    public Result getRow(int i) {
        return rows.get(i);
    }

    @Override
    public void close() {

    }

    @Override
    public Iterator<Result> iterator() {
        return rows.iterator();
    }
}
