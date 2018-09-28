package JISA.Experiment;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;

public class Result {

    private Double[] data;

    public Result(Double... data) {
        this.data = data;
    }

    public String getOutput(String delim) {

        String[] formats = new String[data.length];
        Arrays.fill(formats, "%e");

        String format = String.join(delim, formats).concat("\n");

        return String.format(format, data);

    }

    public void output(PrintStream stream, String delim) {
        stream.print(getOutput(delim));
    }

    public Double get(int i) {
        return data[i];
    }

    public Double[] getData() {
        return data.clone();
    }

}
