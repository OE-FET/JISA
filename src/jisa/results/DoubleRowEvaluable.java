package jisa.results;

public interface DoubleRowEvaluable extends RowEvaluable<Double> {

    default DoubleRowEvaluable pow(double power) {
        return r -> Math.pow(evaluate(r), power);
    }

    default DoubleRowEvaluable abs() {
        return r -> Math.abs(evaluate(r));
    }

    default DoubleRowEvaluable negate() {
        return r -> -evaluate(r);
    }

    default DoubleRowEvaluable add(RowEvaluable<Double> rhs) {
        return r -> evaluate(r) + rhs.evaluate(r);
    }

    default DoubleRowEvaluable add(double rhs) {
        return r -> evaluate(r) + rhs;
    }

    default DoubleRowEvaluable plus(RowEvaluable<Double> rhs) {
        return add(rhs);
    }

    default DoubleRowEvaluable plus(double rhs) {
        return add(rhs);
    }

    default DoubleRowEvaluable subtract(RowEvaluable<Double> rhs) {
        return r -> evaluate(r) - rhs.evaluate(r);
    }

    default DoubleRowEvaluable subtract(double rhs) {
        return r -> evaluate(r) - rhs;
    }

    default DoubleRowEvaluable minus(RowEvaluable<Double> rhs) {
        return subtract(rhs);
    }

    default DoubleRowEvaluable minus(double rhs) {
        return subtract(rhs);
    }

    default DoubleRowEvaluable multiply(RowEvaluable<Double> rhs) {
        return r -> evaluate(r) * rhs.evaluate(r);
    }

    default DoubleRowEvaluable multiply(double rhs) {
        return r -> evaluate(r) * rhs;
    }

    default DoubleRowEvaluable times(RowEvaluable<Double> rhs) {
        return multiply(rhs);
    }

    default DoubleRowEvaluable times(double rhs) {
        return multiply(rhs);
    }

    default DoubleRowEvaluable divide(RowEvaluable<Double> rhs) {
        return r -> evaluate(r) / rhs.evaluate(r);
    }

    default DoubleRowEvaluable divide(double rhs) {
        return r -> evaluate(r) / rhs;
    }

    default DoubleRowEvaluable div(RowEvaluable<Double> rhs) {
        return divide(rhs);
    }

    default DoubleRowEvaluable div(double rhs) {
        return divide(rhs);
    }
    
}
