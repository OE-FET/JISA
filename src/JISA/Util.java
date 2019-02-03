package JISA;

import JISA.Control.ERunnable;
import JISA.Devices.DeviceException;
import JISA.VISA.VISAException;

import java.io.IOException;
import java.util.HashMap;

public class Util {

    private static ERunnable exHandler = (e) -> {

        ExType exceptionType = ExType.fromClass(e.getClass());

        switch (exceptionType) {

            case IO_EXCEPTION:
                System.err.printf("\nCommunication error: \"%s\"\n", e.getMessage());
                break;
            case DEVICE_EXCEPTION:
                System.err.printf("\nDevice error: \"%s\"\n", e.getMessage());
                break;
            case INTERRUPTED_EXCEPTION:
                System.err.printf("\nWaiting error: \"%s\"\n", e.getMessage());
                break;
            case VISA_EXCEPTION:
                System.err.printf("\nVISA error: \"%s\"\n", e.getMessage());
                break;
            default:
                System.err.printf("\nUnknown error: \"%s\"\n", e.getMessage());
                break;
        }

        System.err.println("\nStack Trace:");
        e.printStackTrace();

        System.exit(1);

    };

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

    public static double roundSigFig(double value, int nSigDig, int dir) {

        double intermediate = value / Math.pow(10, Math.floor(Math.log10(Math.abs(value))) - (nSigDig - 1));

        if (dir > 0) intermediate = Math.ceil(intermediate);
        else if (dir < 0) intermediate = Math.floor(intermediate);
        else intermediate = Math.round(intermediate);

        return (intermediate * Math.pow(10, Math.floor(Math.log10(Math.abs(value))) - (nSigDig - 1)));

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
