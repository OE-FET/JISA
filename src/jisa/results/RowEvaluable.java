package jisa.results;

public interface RowEvaluable<T> {

    T evaluate(Row row);

}
