package JISA.Experiment;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Structure to contain the data of a single row of a ResultList.
 */
public class Result {

    private Double[] data;

    /**
     * Create a data row with the given data.
     *
     * @param data The data to add
     */
    public Result(Double... data) {
        this.data = data;
    }

    /**
     * Returns the data as a delimited string.
     *
     * @param delim Delimiter to use
     *
     * @return Delimited string
     */
    public String getOutput(String delim) {

        String[] formats = new String[data.length];
        Arrays.fill(formats, "%e");

        String format = String.join(delim, formats).concat("\n");

        return String.format(format, data);

    }

    /**
     * Outputs the data as a delimited string.
     *
     * @param stream Stream to output to
     * @param delim  Delimiter to use
     */
    public void output(PrintStream stream, String delim) {
        stream.print(getOutput(delim));
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

    /**
     * Returns the data in the object as an array.
     *
     * @return Array of data
     */
    public Double[] getData() {
        return data.clone();
    }

}
