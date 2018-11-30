package JISA.Experiment;

import JISA.GUI.Clearable;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Table-like structure for holding numerical data
 */
public class ResultList extends ResultTable {

    private String[]          names;
    private String[]          units = null;
    private int               cols;
    private ArrayList<Result> rows  = new ArrayList<>();

    public ResultList(String... names) {
        this.names = names;
        cols = this.names.length;
    }

    @Override
    public void setUnits(String... units) {

        if (units.length != cols) {
            return;
        }

        this.units = units;
    }

    @Override
    public String getName(int i) {
        return names[i];
    }

    @Override
    public String getUnits(int i) {
        return units[i];
    }

    @Override
    public String[] getNames() {
        return names.clone();
    }

    @Override
    public boolean hasUnits() {
        return units != null;
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
    public int getNumCols() {
        return cols;
    }

    @Override
    public Result getRow(int i) {
        return rows.get(i);
    }

    @Override
    public Iterator<Result> iterator() {
        return rows.iterator();
    }
}
