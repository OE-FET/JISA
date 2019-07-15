package jisa.control;

import jisa.devices.DeviceException;

import java.io.IOException;

public class PIDController {

    private final int    INTERVAL;
    private final double INT_SEC;

    public interface Input<T> {
        T read() throws IOException, DeviceException;
    }

    public interface Output<T> {
        void write(T value) throws IOException, DeviceException;
    }

    private Input<Double>  input;
    private Output<Double> output;
    private double         P = 10;
    private double         I = 2;
    private double         D = 0;
    private double         setPoint;
    private double         sum;
    private double         last;
    private RTask          control;

    public PIDController(int interval, Input<Double> input, Output<Double> output) {
        INTERVAL = interval;
        INT_SEC = INTERVAL / 1000D;
        this.input = input;
        this.output = output;

        control = new RTask(INTERVAL, () -> {

            double error = setPoint - input.read();
            double diff  = (error - last) / INT_SEC;
            last = error;
            sum += error * INT_SEC;

            output.write(P * error + I * sum + D * diff);

        });

    }

    public void start() {
        sum = 0;
        last = 0;
        control.start();
    }

    public void stop() {
        control.stop();
    }

    public void setTarget(double value) {
        setPoint = value;
    }

    public double getTarget() {
        return setPoint;
    }

    public void setPID(double P, double I, double D) {
        this.P = P;
        this.I = I;
        this.D = D;
    }

    public double getP() {
        return P;
    }

    public double getI() {
        return I;
    }

    public double getD() {
        return D;
    }

}
