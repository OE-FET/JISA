package JISA;

import JISA.Devices.DeviceException;
import JISA.VISA.VISAException;

import java.io.IOException;
import java.util.HashMap;

public class Util {

    public static boolean isBetween(double value, double min, double max) {
        return value >= min && value <= max;
    }

    public static boolean isBetween(int value, int min, int max) {
        return value >= min && value <= max;
    }

    public static void exceptionHandler(Exception e) {

        ExType exceptionType = ExType.fromClass(e.getClass());

        switch (exceptionType) {

            case IO_EXCEPTION:
                System.err.printf("Communication error: \"%s\"\n", e.getMessage());
                break;
            case DEVICE_EXCEPTION:
                System.err.printf("Device error: \"%s\"\n", e.getMessage());
                break;
            case INTERRUPTED_EXCEPTION:
                System.err.printf("Waiting error: \"%s\"\n", e.getMessage());
                break;
            case VISA_EXCEPTION:
                System.err.printf("VISA error: \"%s\"\n", e.getMessage());
                break;
            default:
                System.err.printf("Unknown error: \"%s\"\n", e.getMessage());
                break;
        }

        System.out.println("Stack Trace:");
        e.printStackTrace();

        System.exit(1);

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

        for (int i = 0; i < array.length; i++) {
            results[i] = array[i];
        }

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

        static ExType fromClass(Class c) {
            return lookup.getOrDefault(c, UNKNOWN_EXCEPTION);
        }

        private Class clazz;

        ExType(Class c) {
            clazz = c;
        }

        Class getClazz() {
            return clazz;
        }

    }

}
