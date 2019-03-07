package JISA.Experiment;

import java.io.IOException;
import java.util.Iterator;
import java.util.ListIterator;

public class OrderedStream extends ResultStream {

    public OrderedStream(String path) throws IOException {
        super(path, "X", "Y");
    }


    public int addDataIndex(double x, double y) {

        int index = findSlot(x);
        addData(x,y);
        return index;

    }

    protected void addRow(Result row) {

        addBefore(findSlot(row.get(0)), row.getOutput(","));

    }

    protected int findSlot(double value) {

        try {

            if (getNumRows() == 0) {
                return 0;
            } else {
                file.seek(0);
                file.readLine();

                String line = "";
                double read = Double.NEGATIVE_INFINITY;
                int i = -1;
                while (read < value) {

                    line = file.readLine();
                    if (!line.trim().equals("")) {
                        read = Double.valueOf(line.split(",")[0]);
                        i++;
                    } else {
                        return i+1;
                    }
                }

                return i;

            }

        } catch (IOException ignored) {
            return 0;
        }

    }

    public ListIterator<Result> iterateFrom(double value) {

        try {
            file.seek(0);
            file.readLine();

            final int index = findSlot(value);

            for (int i = 0; i < index; i ++) {
                file.readLine();
            }

            return new ListIterator<Result>() {

                int rows = getNumRows();
                int row = index;

                @Override
                public boolean hasNext() {
                    return row < rows;
                }

                @Override
                public Result next() {
                    try {
                        String[] values = file.readLine().split(",");
                        row++;
                        Double[] dVals  = new Double[values.length];
                        for (int j = 0; j < values.length; j++) {
                            dVals[j] = Double.valueOf(values[j]);
                        }
                        return new Result(dVals);
                    } catch (IOException e) {
                        return null;
                    }
                }

                @Override
                public boolean hasPrevious() {
                    return false;
                }

                @Override
                public Result previous() {
                    return null;
                }

                @Override
                public int nextIndex() {
                    return row;
                }

                @Override
                public int previousIndex() {
                    return 0;
                }

                @Override
                public void remove() {

                }

                @Override
                public void set(Result result) {

                }

                @Override
                public void add(Result result) {

                }

            };

        } catch (IOException e) {
            return new ListIterator<Result>() {
                @Override
                public boolean hasNext() {
                    return false;
                }

                @Override
                public Result next() {
                    return null;
                }

                @Override
                public boolean hasPrevious() {
                    return false;
                }

                @Override
                public Result previous() {
                    return null;
                }

                @Override
                public int nextIndex() {
                    return 0;
                }

                @Override
                public int previousIndex() {
                    return 0;
                }

                @Override
                public void remove() {

                }

                @Override
                public void set(Result result) {

                }

                @Override
                public void add(Result result) {

                }
            };
        }

    }

}
