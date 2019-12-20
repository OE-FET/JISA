package jisa.maths;

public class ColumnMatrix extends Matrix {

    public ColumnMatrix(double... values) {
        super(values.length, 1);
        setAllEntries(values);
    }

}
