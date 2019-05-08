package JISA;

import JISA.Control.ERunnable;
import JISA.Devices.DeviceException;
import JISA.VISA.VISAException;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart.Data;
import javafx.util.Pair;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Util {

    public static  PrintStream errLog    = System.err;
    private static ERunnable   exHandler = (e) -> {

        ExType exceptionType = ExType.fromClass(e.getClass());

        switch (exceptionType) {

            case IO_EXCEPTION:
                Util.errLog.printf("\nCommunication error: \"%s\"\n", e.getMessage());
                break;
            case DEVICE_EXCEPTION:
                Util.errLog.printf("\nDevice error: \"%s\"\n", e.getMessage());
                break;
            case INTERRUPTED_EXCEPTION:
                Util.errLog.printf("\nWaiting error: \"%s\"\n", e.getMessage());
                break;
            case VISA_EXCEPTION:
                Util.errLog.printf("\nVISA error: \"%s\"\n", e.getMessage());
                break;
            default:
                Util.errLog.printf("\nUnknown error: \"%s\"\n", e.getMessage());
                break;
        }

        Util.errLog.println("\nStack Trace:");
        e.printStackTrace();

        System.exit(1);

    };

    /**
     * Sleep function that doesn't throw interrupted exceptions. Upon an interrupt it will simply stop sleeping.
     *
     * @param msec Number of milliseconds to sleep for
     */
    public static void sleep(long msec) {
        try {
            Thread.sleep(msec);
        } catch (Exception e) {
        }
    }

    /**
     * Checks whether a number is within a range.
     *
     * @param value Value to check
     * @param min   Minimum of range
     * @param max   Maximum of range
     *
     * @return Is it bound by the range?
     */
    public static boolean isBetween(Number value, Number min, Number max) {
        return value.doubleValue() >= min.doubleValue() && value.doubleValue() <= max.doubleValue();
    }

    /**
     * Set what the standard exception handler should be.
     *
     * @param handler Lambda
     */
    public static void setExceptionHandler(ERunnable handler) {
        exHandler = handler;
    }

    /**
     * Pass an exception to the standard exception handler.
     *
     * @param e Exception to handle
     */
    public static void exceptionHandler(Exception e) {
        exHandler.run(e);
    }

    /**
     * Creates an equally spaced array of numbers, starting at min, ending at max in numSteps steps.
     *
     * @param min      Number to start at
     * @param max      Number to end at
     * @param numSteps Number of steps to do it in
     *
     * @return Array of numbers
     */
    public static double[] makeLinearArray(Number min, Number max, int numSteps) {

        if (numSteps < 1) {
            throw new IllegalArgumentException("You cannot have fewer than 1 step.");
        }

        double[] values = new double[numSteps];
        double   step   = (max.doubleValue() - min.doubleValue()) / (numSteps - 1D);

        values[0] = min.doubleValue();

        for (int i = 1; i < values.length; i++) {
            values[i] = values[i - 1] + step;
        }

        return values;

    }

    /**
     * Creates an equally spaced symmetric array of numbers, starting at min, ending at max in numSteps steps, and then back again.
     *
     * @param min          Number to start at
     * @param max          Number to end at
     * @param stepsEachWay Number of steps each way
     *
     * @return Array of numbers
     */
    public static double[] makeSymLinearArray(Number min, Number max, int stepsEachWay) {
        return symArray(makeLinearArray(min, max, stepsEachWay));
    }

    /**
     * Takes an array of doubles, reverses it and appends it onto the end of the original whilst avoiding repeating the last element.
     *
     * @param array Array to symmetrise
     *
     * @return Symmetrised array
     */
    public static double[] symArray(double[] array) {

        double[] results = new double[2 * array.length - 1];

        System.arraycopy(array, 0, results, 0, array.length);

        for (int i = 0; i < array.length - 1; i++) {

            int j = array.length + i;
            int k = array.length - 2 - i;
            results[j] = array[k];

        }

        return results;

    }

    /**
     * Reverses an array of doubles.
     *
     * @param arr Array to reverse
     *
     * @return Reversed array
     */
    public static double[] reverseArray(double[] arr) {

        double[] array = arr.clone();

        for (int i = 0; i < array.length / 2; i++) {
            double temp = array[i];
            array[i] = array[array.length - i - 1];
            array[array.length - i - 1] = temp;
        }

        return array;

    }

    /**
     * Creates an array of logarithmically spaced numbers.
     *
     * @param min      Value to start at
     * @param max      Value to end at
     * @param numSteps Number of steps
     *
     * @return Logarithmic array
     */
    public static double[] makeLogarithmicArray(Number min, Number max, int numSteps) {

        if (numSteps < 1) {
            throw new IllegalArgumentException("You cannot have fewer than 1 step.");
        }

        double[] values = new double[numSteps];
        double   step   = Math.pow(max.doubleValue() / min.doubleValue(), 1D / numSteps);

        values[0] = min.doubleValue();

        for (int i = 1; i < values.length; i++) {
            values[i] = values[i - 1] * step;
        }

        return values;

    }

    public static String[] makeCountingString(int start, int length, String pattern) {

        String[] result = new String[length];
        for (int i = 0; i < length; i++) {
            result[i] = String.format(pattern, i + start);
        }
        return result;

    }

    public static double oneSigFigFloor(Number value) {

        if (value.doubleValue() == 0) {
            return 0;
        }

        return Math.floor(value.doubleValue() / Math.pow(10, Math.floor(Math.log10(Math.abs(value.doubleValue()))))) * Math.pow(10, Math.floor(Math.log10(Math.abs(value.doubleValue()))));
    }

    public static double oneSigFigCeil(double value) {

        if (value == 0) {
            return 0;
        }

        return Math.ceil(value / Math.pow(10, Math.floor(Math.log10(Math.abs(value))))) * Math.pow(10, Math.floor(Math.log10(Math.abs(value))));
    }

    public static double roundSigFig(double value, int nSigDig, int dir) {

        double intermediate = value / Math.pow(10, Math.floor(Math.log10(Math.abs(value))) - (nSigDig - 1));

        if (dir > 0) intermediate = Math.ceil(intermediate);
        else if (dir < 0) intermediate = Math.floor(intermediate);
        else intermediate = Math.round(intermediate);

        return (intermediate * Math.pow(10, Math.floor(Math.log10(Math.abs(value))) - (nSigDig - 1)));

    }

    public static void reduceData(ObservableList<Data<Double, Double>> points, int n) {

        ArrayList<Data<Double, Double>> list = new ArrayList<>();

        list.add(points.get(0));
        list.add(points.get(points.size() - 1));

        while (list.size() < n) {

            Pair<Integer, Integer> dev = mostDeviant(list);


        }

    }

    public static byte[] trimArray(byte[] toTrim) {

        int pos = toTrim.length - 1;

        for (int i = toTrim.length - 1; i >= 0; i--) {

            if (toTrim[i] > 0) {
                pos = i;
                break;
            }

        }

        byte[] trimmed = new byte[pos + 1];

        System.arraycopy(toTrim, 0, trimmed, 0, trimmed.length);

        return trimmed;

    }

    public static byte[] padArray(byte[] toTrim) {

        int pos = toTrim.length - 1;

        for (int i = toTrim.length - 1; i >= 0; i--) {

            if (toTrim[i] > 0) {
                pos = i;
                break;
            }

        }

        byte[] trimmed = new byte[pos + 1];

        System.arraycopy(toTrim, 0, trimmed, 0, trimmed.length);

        return trimmed;

    }

    private static Pair<Integer, Integer> mostDeviant(List<Data<Double, Double>> line) {

        double maxD = 0;
        int    maxI = 0;
        int    maxJ = line.size() - 1;

        for (int i = 0; i < line.size() - 1; i++) {

            for (int j = i + 1; j < line.size(); j++) {

                Data<Double, Double> point1 = line.get(i);
                Data<Double, Double> point2 = line.get(j);

                double distance = Math.sqrt(Math.pow(point1.getXValue() - point2.getXValue(), 2) + Math.pow(point1.getYValue() - point2.getYValue(), 2));

                if (distance > maxD) {
                    maxD = distance;
                    maxI = i;
                    maxJ = j;
                }

            }

        }

        return new Pair<>(maxI, maxJ);

    }

    enum ExType {

        IO_EXCEPTION(IOException.class),
        DEVICE_EXCEPTION(DeviceException.class),
        INTERRUPTED_EXCEPTION(InterruptedException.class),
        VISA_EXCEPTION(VISAException.class),
        UNKNOWN_EXCEPTION(Exception.class);

        private static HashMap<Class, ExType> lookup = new HashMap<>();

        static {
            for (ExType e : ExType.values()) {
                lookup.put(e.getClazz(), e);
            }
        }

        private Class clazz;

        ExType(Class c) {
            clazz = c;
        }

        static ExType fromClass(Class c) {
            return lookup.getOrDefault(c, UNKNOWN_EXCEPTION);
        }

        Class getClazz() {
            return clazz;
        }

    }

}
