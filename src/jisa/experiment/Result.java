package jisa.experiment;

import java.io.PrintStream;

/**
 * Structure to contain the data of a single row of a ResultList.
 */
public class Result {

    private final ResultTable table;
    private final double[]    data;

    /**
     * Create a data row with the given data.
     *
     * @param data The data to add
     */
    public Result(ResultTable table, double... data) {
        this.table = table;
        this.data  = data;
    }

    /**
     * Returns the data as a delimited string.
     *
     * @param delimiter Delimiter to use
     *
     * @return Delimited string
     */
    public String getOutput(String delimiter) {

        String[] chunks = new String[data.length];

        for (int i = 0; i < chunks.length; i++) {
            chunks[i] = String.format("%s", data[i]);
        }

        return String.join(delimiter, chunks).concat("\n");

    }

    /**
     * Outputs the data as a delimited string.
     *
     * @param stream    Stream to output to
     * @param delimiter Delimiter to use
     */
    public void output(PrintStream stream, String delimiter) {
        stream.print(getOutput(delimiter));
    }

    /**
     * Returns the value in the specified column.
     *
     * @param i Column index
     *
     * @return Data value
     */
    public Double get(int i) {
        return data[i];
    }

    public Double get(String name) {
        return get(table.getColumnFromString(name));
    }

    public Double get(Col column) {
        return get(table.getColumnFromCol(column));
    }

    /**
     * Returns the data in the object as an array.
     *
     * @return Array of data
     */
    public double[] getData() {
        return data.clone();
    }

}
