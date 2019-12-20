package jisa.maths;

public class RowMatrix extends Matrix {

    public RowMatrix(double... values) {
        super(1, values.length);
        setAllEntries(values);
    }

}
