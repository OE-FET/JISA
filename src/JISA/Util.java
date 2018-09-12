package JISA;

import JISA.Devices.DeviceException;

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
            default:
                System.err.printf("Unknown error: \"%s\"\n", e.getMessage());
                break;
        }

        System.out.println("Stack Trace:");
        e.printStackTrace();

        System.exit(1);

    }

    enum ExType {

        IO_EXCEPTION(IOException.class),
        DEVICE_EXCEPTION(DeviceException.class),
        INTERRUPTED_EXCEPTION(InterruptedException.class),
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
