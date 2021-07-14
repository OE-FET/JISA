package jisa.results;

public interface RowEvaluable<T> {

    T evaluate(ResultTable.Row row);

}
