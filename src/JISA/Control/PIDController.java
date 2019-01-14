package JISA.Control;

import JISA.Devices.DeviceException;

import java.io.IOException;

public class PIDController {

    private final static int    INTERVAL = 100;
    private final static double INT_SEC  = INTERVAL / 1000D;

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

    public PIDController(Input<Double> input, Output<Double> output) {
        this.input = input;
        this.output = output;

        control = new RTask(INTERVAL, () -> {

            double error = input.read() - setPoint;
            double diff  = (error - last) / INT_SEC;
            sum += error * INT_SEC;

            output.write(P * error + I * sum + D * diff);

        });

    }

    public void start() {
        sum  = 0;
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
