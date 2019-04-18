package JISA;

import JISA.Control.ERunnable;
import JISA.Devices.DeviceException;
import JISA.VISA.VISAException;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart.Data;
import javafx.util.Pair;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

public class Util {

    private static ERunnable exHandler = (e) -> {

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

    public static PrintStream errLog = System.err;

    public static void sleep(long msec) {
        try {
            Thread.sleep(msec);
        } catch (Exception e) {
        }
    }

    public static boolean isBetween(double value, double min, double max) {
        return value >= min && value <= max;
    }

    public static boolean isBetween(int value, int min, int max) {
        return value >= min && value <= max;
    }

    public static void setExceptionHandler(ERunnable handler) {
        exHandler = handler;
    }

    public static void exceptionHandler(Exception e) {
        exHandler.run(e);
    }

    public static double[] makeLinearArray(double min, double max, int numSteps) {

        double[] values = new double[numSteps];
        double   step   = (max - min) / (numSteps - 1D);

        values[0] = min;

        for (int i = 1; i < values.length; i++) {
            values[i] = values[i - 1] + step;
        }

        return values;

    }

    public static double[] makeSymLinearArray(double min, double max, int stepsEachWay) {
        return symArray(makeLinearArray(min, max, stepsEachWay));
    }

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

    public static double[] reverseArray(double[] arr) {

        double[] array = arr.clone();

        for (int i = 0; i < array.length / 2; i++) {
            double temp = array[i];
            array[i] = array[array.length - i - 1];
            array[array.length - i - 1] = temp;
        }

        return array;

    }

    public static double[] makeLogarithmicArray(double min, double max, int numSteps) {

        double[] values = new double[numSteps];
        double   step   = Math.pow(max / min, 1D / numSteps);

        values[0] = min;

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

    public static double oneSigFigFloor(double value) {

        if (value == 0) {
            return 0;
        }

        return Math.floor(value / Math.pow(10, Math.floor(Math.log10(Math.abs(value))))) * Math.pow(10, Math.floor(Math.log10(Math.abs(value))));
    }

    public static double oneSigFigCeil(double value) {
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

        ArrayList<Data<Double,Double>> list = new ArrayList<>();

        list.add(points.get(0));
        list.add(points.get(points.size()-1));

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
