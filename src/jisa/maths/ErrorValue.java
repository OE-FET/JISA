package jisa.maths;

public class ErrorValue extends Number {

    private final double value;
    private final double error;

    public ErrorValue(double value, double error) {
        this.value = value;
        this.error = error;
    }

    public double getValue() {
        return value;
    }

    public double getError() {
        return error;
    }

    @Override
    public int intValue() {
        return (int) value;
    }

    @Override
    public long longValue() {
        return (long) value;
    }

    @Override
    public float floatValue() {
        return (float) value;
    }

    @Override
    public double doubleValue() {
        return value;
    }

    public ErrorValue add(ErrorValue other) {

        double value = getValue() + other.getValue();
        double error = Math.sqrt(Math.pow(getError(), 2) + Math.pow(other.getError(), 2));

        return new ErrorValue(value, error);

    }
    
    public ErrorValue add(double other) {
        return add(new ErrorValue(other, 0.0));
    }

    public ErrorValue plus(ErrorValue other) {
        return add(other);
    }

    public ErrorValue plus(double other) {
        return add(other);
    }

    public ErrorValue subtract(ErrorValue other) {

        double value = getValue() - other.getValue();
        double error = Math.sqrt(Math.pow(getError(), 2) + Math.pow(other.getError(), 2));

        return new ErrorValue(value, error);

    }

    public ErrorValue subtract(double other) {
        return subtract(new ErrorValue(other, 0.0));
    }

    public ErrorValue minus(ErrorValue other) {
        return subtract(other);
    }

    public ErrorValue minus(double other) {
        return subtract(other);
    }

    public ErrorValue multiply(ErrorValue other) {

        double value = getValue() * other.getValue();
        double error = Math.sqrt(Math.pow(getError() * other.getValue(), 2) + Math.pow(other.getError() * getValue(), 2));

        return new ErrorValue(value, error);

    }

    public ErrorValue multiply(double other) {
        return multiply(new ErrorValue(other, 0.0));
    }

    public ErrorValue times(ErrorValue other) {
        return multiply(other);
    }

    public ErrorValue times(double other) {
        return multiply(other);
    }

    public ErrorValue divide(ErrorValue other) {

        double value = getValue() / other.getValue();
        double error = Math.sqrt(Math.pow(getError() / other.getValue(), 2) + Math.pow(other.getError() * (getValue() / Math.pow(other.getValue(), 2)), 2));

        return new ErrorValue(value, error);

    }

    public ErrorValue divide(double other) {
        return divide(new ErrorValue(other, 0.0));
    }

    public ErrorValue div(ErrorValue other) {
        return divide(other);
    }

    public ErrorValue div(double other) {
        return divide(other);
    }

    public ErrorValue pow(ErrorValue power) {

        double A  = getValue();
        double B  = power.getValue();
        double EA = getError();
        double EB = power.getError();

        double value = Math.pow(A, B);
        double error = Math.sqrt(Math.pow(B * Math.pow(A, B - 1) * EA, 2) + Math.pow(Math.pow(A,B) * Math.log(A) * EB, 2));

        return new ErrorValue(value, error);

    }

    public ErrorValue pow(double other) {
        return pow(new ErrorValue(other, 0.0));
    }

}
