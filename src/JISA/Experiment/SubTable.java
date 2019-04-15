package JISA.Experiment;

import java.util.Iterator;

public class SubTable extends ResultTable {

    final ResultTable source;
    final int[]       columns;

    public SubTable(ResultTable source, int... columns) {
        this.source = source;
        this.columns = columns;
    }

    @Override
    public void setUnits(String... units) {

        if (units.length != columns.length) {
            throw new IllegalArgumentException("Number of units does not match number of columns.");
        }

        String[] newUnits = new String[source.getNumCols()];

        for (int i = 0; i < source.getNumCols(); i++) {
            newUnits[i] = source.getUnits(i);
        }

        for (int i = 0; i < units.length; i++) {
            newUnits[columns[i]] = units[i];
        }

        source.setUnits(newUnits);

    }

    @Override
    public String getName(int i) {
        return source.getName(columns[i]);
    }

    @Override
    public String getUnits(int i) {
        return source.getUnits(columns[i]);
    }

    @Override
    public String[] getNames() {

        String[] allNames = source.getNames();
        String[] names    = new String[columns.length];

        for (int i = 0; i < names.length; i++) {
            names[i] = allNames[columns[i]];
        }

        return names;

    }

    @Override
    public boolean hasUnits() {
        return source.hasUnits();
    }

    @Override
    protected void addRow(Result row) {
        throw new IllegalStateException("Cannot add data to a read-only SubTable");
    }

    @Override
    protected void clearData() {
        throw new IllegalStateException("Cannot remove data from a read-only SubTable");
    }

    @Override
    public int getNumRows() {
        return source.getNumRows();
    }

    @Override
    public int getNumCols() {
        return columns.length;
    }

    @Override
    public Result getRow(int i) {

        double[] row  = new double[columns.length];
        Result   full = source.getRow(i);

        for (int j = 0; j < row.length; j++) {
            row[j] = full.get(columns[j]);
        }

        return new Result(row);

    }

    @Override
    public void close() {

    }

    @Override
    public Iterator<Result> iterator() {

        return new Iterator<Result>() {

            int rows = getNumRows();
            int row = 0;

            @Override
            public boolean hasNext() {
                return row < rows;
            }

            @Override
            public Result next() {
                return getRow(row++);
            }
        };

    }

}
