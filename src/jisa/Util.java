package jisa;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import jisa.control.ERunnable;
import jisa.control.SRunnable;
import jisa.devices.DeviceException;
import jisa.visa.exceptions.VISAException;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class Util {

    public static        PrintStream     errLog     = System.err;
    private static final List<SRunnable> onShutdown = new LinkedList<>();
    private static       ERunnable       exHandler  = (e) -> {

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

    static {

        Runtime.getRuntime().addShutdownHook(new Thread(() -> onShutdown.forEach(SRunnable::runRegardless)));

    }

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

    public static String pluralise(String word, Number value) {
        return pluralise(word, word + "s", value);
    }

    public static String pluralise(String word, String plural, Number value) {
        return (value.doubleValue() == 1.0) ? word : plural;
    }

    public static void runRegardless(SRunnable toRun) {
        toRun.runRegardless();
    }

    public static void runRegardless(SRunnable... toRun) {

        for (SRunnable run : toRun) {
            run.runRegardless();
        }

    }

    public static String msToString(long millis) {

        long ms = (millis) % 1000;
        long s  = (millis / (1000)) % 60;
        long m  = (millis / (1000 * 60)) % 60;
        long h  = (millis / (1000 * 60 * 60)) % 24;
        long d  = (millis / (1000 * 60 * 60 * 24));

        List<String> parts = new LinkedList<>();

        if (d > 0) {parts.add(String.format("%dd", d));}
        if (h > 0) {parts.add(String.format("%dh", h));}
        if (m > 0) {parts.add(String.format("%dm", m));}
        if (s > 0) {parts.add(String.format("%ds", s));}
        if (ms > 0) {parts.add(String.format("%dms", ms));}

        return String.join(" ", parts);

    }

    public static String msToPaddedString(long millis) {

        long ms = (millis) % 1000;
        long s  = (millis / (1000)) % 60;
        long m  = (millis / (1000 * 60)) % 60;
        long h  = (millis / (1000 * 60 * 60)) % 24;
        long d  = (millis / (1000 * 60 * 60 * 24));

        List<String> parts = new LinkedList<>();

        if (d > 0) {parts.add(String.format("%dd", d));}
        if (h > 0) {parts.add(String.format("%02dh", h));}
        if (m > 0) {parts.add(String.format("%02dm", m));}
        if (s > 0) {parts.add(String.format("%02ds", s));}
        parts.add(String.format("%03dms", ms));

        return String.join(" ", parts);

    }

    public static String joinPath(String first, String... more) {

        return Paths.get(first, more).toString();

    }

    public static String colourToCSS(Color colour) {

        return String.format(
            "rgba(%s,%s,%s,%s)",
            colour.getRed() * 255,
            colour.getGreen() * 255,
            colour.getBlue() * 255,
            colour.getOpacity()
        );

    }

    public static double getNiceValue(double range, boolean round) {

        double exponent = Math.floor(Math.log10(range));
        double fraction = range / Math.pow(10, exponent);
        double niceFraction;

        if (round) {
            if (fraction < 1.5) {niceFraction = 1;} else if (fraction < 3) {
                niceFraction = 2;
            } else if (fraction < 7) {niceFraction = 5;} else {
                niceFraction = 10;
            }
        } else {
            if (fraction <= 1) {niceFraction = 1;} else if (fraction <= 2) {
                niceFraction = 2;
            } else if (fraction <= 5) {niceFraction = 5;} else {
                niceFraction = 10;
            }
        }

        return niceFraction * Math.pow(10, exponent);
    }

    public static double truncate(Number value, Number min, Number max) {
        return Math.min(max.doubleValue(), Math.max(min.doubleValue(), value.doubleValue()));
    }

    public static int truncate(int value, int min, int max) {
        return Math.min(max, Math.max(min, value));
    }

    public static void runAsync(Runnable toRun) {
        (new Thread(toRun)).start();
    }

    public static String pathToSVG(Path path) {

        List<String> list = new LinkedList<>();

        for (PathElement element : path.getElements()) {

            if (element instanceof MoveTo) {
                list.add(String.format("M%s %s", ((MoveTo) element).getX(), ((MoveTo) element).getY()));
            } else if (element instanceof LineTo) {
                list.add(String.format("L%s %s", ((LineTo) element).getX(), ((LineTo) element).getY()));
            } else if (element instanceof ClosePath) {
                list.add("Z");
            }

        }

        return String.join(" ", list);

    }

    public static String polygonToSVG(Polygon path) {

        List<String> list = new LinkedList<>();

        for (int i = 0; i < path.getPoints().size(); i += 2) {

            list.add(String.format("%s,%s", path.getPoints().get(i), path.getPoints().get(i + 1)));

        }

        return String.join(" ", list);

    }

    public static void addShutdownHook(SRunnable toRun) {
        onShutdown.add(toRun);
    }

    public static Image invertImage(Image toInvert) {

        int           width    = (int) toInvert.getWidth();
        int           height   = (int) toInvert.getHeight();
        WritableImage inverted = new WritableImage(width, height);
        PixelReader   reader   = toInvert.getPixelReader();
        PixelWriter   writer   = inverted.getPixelWriter();

        for (int x = 0; x < width; x++) {

            for (int y = 0; y < height; y++) {

                writer.setColor(x, y, reader.getColor(x, y).invert());

            }

        }

        return inverted;

    }

    /**
     * Runs multiple Runnables in parallel, returning only when all have completed.
     *
     * @param runnables Runnables to run
     *
     * @throws InterruptedException Upon thread being interrupted
     */
    public static void runInParallel(SRunnable... runnables) throws InterruptedException {

        List<Thread> threads = new LinkedList<>();

        for (SRunnable runnable : runnables) {
            threads.add(new Thread(runnable::runRegardless));
        }

        threads.forEach(Thread::start);

        for (Thread thread : threads) {
            thread.join();
        }

    }

    public static Image colourImage(Image toColour, Color colour) {

        int           width      = (int) toColour.getWidth();
        int           height     = (int) toColour.getHeight();
        WritableImage inverted   = new WritableImage(width, height);
        PixelReader   reader     = toColour.getPixelReader();
        PixelWriter   writer     = inverted.getPixelWriter();
        double        hue        = colour.getHue();
        double        saturation = colour.getSaturation();
        double        brightness = colour.getBrightness();

        for (int x = 0; x < width; x++) {

            for (int y = 0; y < height; y++) {

                Color  pixel   = reader.getColor(x, y);
                double opacity = pixel.getOpacity();

                writer.setColor(x, y, Color.hsb(hue, saturation, brightness, opacity));

            }

        }

        return inverted;

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

    public static <K,V> Map<K,V> mapOf(Object... values) {

        if (values.length % 2 != 0) {
            throw new IllegalArgumentException("Every key needs a value.");
        }

        Map<K,V> map = new LinkedHashMap<>();

        for (int i = 0; i < values.length; i += 2) {

            K key = (K) values[i];
            V val = (V) values[i + 1];

            map.put(key, val);

        }

        return map;

    }

    public static <K,V> MapBuilder<K,V> buildMap(Class<K> keyType, Class<V> valueType) {
        return new MapBuilder<>(keyType, valueType);
    }

    public static <K,V> Map<K,V> buildMap(MapBuild<K,V> build) {

        Map<K,V> map = new LinkedHashMap<>();
        build.map(map);
        return map;

    }

    public static <T> T castOrDefault(Object toCast, T orElse) {

        try {
            return (T) toCast;
        } catch (ClassCastException e) {
            return orElse;
        }

    }

    public static <T> T build(T toBuild, Build<T> builder) {
        builder.build(toBuild);
        return toBuild;
    }

    public static boolean isValidIndex(int index, Object[] array) {
        return Util.isBetween(index, 0, array.length - 1);
    }

    public static boolean isValidIndex(int index, double[] array) {
        return Util.isBetween(index, 0, array.length - 1);
    }

    public static boolean isValidIndex(int index, float[] array) {
        return Util.isBetween(index, 0, array.length - 1);
    }

    public static boolean isValidIndex(int index, int[] array) {
        return Util.isBetween(index, 0, array.length - 1);
    }

    public static boolean isValidIndex(int index, long[] array) {
        return Util.isBetween(index, 0, array.length - 1);
    }

    public static boolean isValidIndex(int index, short[] array) {
        return Util.isBetween(index, 0, array.length - 1);
    }

    public static boolean isValidIndex(int index, boolean[] array) {
        return Util.isBetween(index, 0, array.length - 1);
    }

    public static boolean isValidIndex(int index, char[] array) {
        return Util.isBetween(index, 0, array.length - 1);
    }

    public static boolean isValidIndex(int index, byte[] array) {
        return Util.isBetween(index, 0, array.length - 1);
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

        values[0]            = min.doubleValue();
        values[numSteps - 1] = max.doubleValue();

        for (int i = 1; i < numSteps - 1; i++) {
            values[i] = values[i - 1] + step;
        }

        return values;

    }

    public static int[] makeCountingArray(int start, int stop) {

        int[] result = new int[stop - start + 1];

        for (int i = start; i <= stop; i++) {
            result[i - start] = i;
        }

        return result;

    }

    public static String joinInts(String delim, int... ints) {

        String[] parts = new String[ints.length];

        for (int i = 0; i < ints.length; i++) {
            parts[i] = String.format("%d", ints[i]);
        }

        return String.join(delim, parts);

    }

    public static String joinDoubles(String delim, Collection<Double> doubles) {

        String[] parts = new String[doubles.size()];

        int i = 0;
        for (double d : doubles) {
            parts[i++] = String.format("%s", d);
        }

        return String.join(delim, parts);

    }

    /**
     * Creates an equally spaced symmetric array of numbers, starting at min, ending at max in numSteps steps, and then back again to min in numSteps.
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
        System.arraycopy(reverseArray(array), 1, results, array.length, array.length - 1);

        return results;

    }

    /**
     * Reverses an array of doubles.
     *
     * @param toReverse Array to reverse
     *
     * @return Reversed array
     */
    public static double[] reverseArray(double[] toReverse) {

        double[] array = new double[toReverse.length];

        for (int i = 0; i < array.length; i++) {
            array[i] = toReverse[array.length - i - 1];
        }

        return array;

    }

    public static double[] primitiveArray(Double... values) {

        double[] primitive = new double[values.length];

        for (int i = 0; i < values.length; i++) {
            primitive[i] = values[i];
        }

        return primitive;

    }

    public static <T> T[] reverseArray(T[] toReverse) {

        T[] array = Arrays.copyOf(toReverse, toReverse.length);

        for (int i = 0; i < array.length; i++) {
            array[i] = toReverse[array.length - i - 1];
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

        values[0]            = min.doubleValue();
        values[numSteps - 1] = max.doubleValue();

        for (int i = 1; i < numSteps - 1; i++) {
            values[i] = values[i - 1] * step;
        }

        return values;

    }

    public static boolean areAnyNull(Object... objects) {

        for (Object o : objects) {

            if (o == null) {
                return true;
            }

        }

        return false;

    }

    public static String getCurrentTimeString() {

        LocalDateTime time = LocalDateTime.now();

        return String.format(
            "%02d-%02d-%04d %02d:%02d:%02d",
            time.getYear(),
            time.getMonthValue(),
            time.getDayOfMonth(),
            time.getHour(),
            time.getMinute(),
            time.getSecond()
        );

    }

    public static void openInBrowser(String url) {

        String  os = System.getProperty("os.name").toLowerCase();
        Runtime rt = Runtime.getRuntime();

        try {

            if (os.contains("win")) {

                // this doesn't support showing urls in the form of "page.html#nameLink"
                rt.exec("rundll32 url.dll,FileProtocolHandler " + url);

            } else if (os.contains("mac")) {

                rt.exec("open " + url);

            } else if (os.contains("nix") || os.contains("nux")) {

                // Try xdg-open before trying a list of others
                String[] browsers = {"xdg-open", "chromium-browser", "firefox"};

                // Build a command string which looks like "browser1 "url" || browser2 "url" ||..."
                StringBuilder cmd = new StringBuilder();
                for (int i = 0; i < browsers.length; i++) {
                    cmd.append(i == 0 ? "" : " || ").append(browsers[i]).append(" \"").append(url).append("\" ");
                }

                rt.exec(new String[]{"sh", "-c", cmd.toString()});

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Returns an array of Strings specified by the pattern and a counting integer.
     *
     * @param start   Integer to start at
     * @param length  How many integers to go through
     * @param pattern The pattern to use
     *
     * @return Array of Strings
     */
    public static String[] makeCountingString(int start, int length, String pattern) {

        String[] result = new String[length];

        for (int i = 0; i < length; i++) {
            result[i] = String.format(pattern, i + start);
        }

        return result;

    }

    /**
     * Floors the number to 1 significant figure.
     *
     * @param value Number to floor.
     *
     * @return Floored number
     */
    public static double oneSigFigFloor(Number value) {

        if (value.doubleValue() == 0) {
            return 0;
        }

        return Math.floor(value.doubleValue() / Math.pow(
            10,
            Math.floor(Math.log10(Math.abs(value.doubleValue())))
        )) * Math.pow(10, Math.floor(Math.log10(Math.abs(value.doubleValue()))));
    }

    /**
     * Ceilings the number to 1 significant figure.
     *
     * @param value Number to floor.
     *
     * @return Ceilinged number
     */
    public static double oneSigFigCeil(double value) {

        if (value == 0) {
            return 0;
        }

        return Math.ceil(value / Math.pow(10, Math.floor(Math.log10(Math.abs(value))))) * Math.pow(
            10,
            Math.floor(Math.log10(Math.abs(value)))
        );
    }

    public static double roundSigFig(double value, int nSigDig, int dir) {

        double intermediate = value / Math.pow(10, Math.floor(Math.log10(Math.abs(value))) - (nSigDig - 1));

        if (dir > 0) {
            intermediate = Math.ceil(intermediate);
        } else if (dir < 0) {
            intermediate = Math.floor(intermediate);
        } else {
            intermediate = Math.round(intermediate);
        }

        return (intermediate * Math.pow(10, Math.floor(Math.log10(Math.abs(value))) - (nSigDig - 1)));

    }

    public static byte[] trimBytes(byte[] toTrim) {

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

    public static byte[] trimBytes(ByteBuffer buffer, int start, int length) {
        return trimBytes(buffer.array(), start, length);
    }

    public static byte[] trimBytes(byte[] toTrim, int start, int length) {

        byte[] trimmed = new byte[length];
        System.arraycopy(toTrim, start, trimmed, 0, length);
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

    public static <U, V> void iterateCombined(Iterable<U> x, Iterable<V> y, BiConsumer<U, V> forEach) {

        Iterator<U> ittrX = x.iterator();
        Iterator<V> ittrY = y.iterator();

        while (ittrX.hasNext() && ittrY.hasNext()) {
            forEach.accept(ittrX.next(), ittrY.next());
        }

    }

    public static <T> void iterateCombined(Consumer<T[]> forEach, Iterable<T>... iterables) {

        List<Iterator<T>> iterators = Arrays.stream(iterables).map(Iterable::iterator).collect(Collectors.toList());

        while (iterators.stream().allMatch(Iterator::hasNext)) {
            forEach.accept((T[]) iterators.stream().map(Iterator::next).toArray());
        }

    }

    public static double[] iterableToArray(Iterable<Double> iterable) {

        int count = 0;

        for (double v : iterable) {
            count++;
        }

        double[] array = new double[count];

        int i = 0;
        for (double v : iterable) {
            array[i++] = v;
        }

        return array;

    }

    public static <T> List<T> iterableToList(Iterable<T> iterable) {

        List<T> list = new LinkedList<>();

        for (T v : iterable) {
            list.add(v);
        }

        return list;

    }

    enum ExType {

        IO_EXCEPTION(IOException.class),
        DEVICE_EXCEPTION(DeviceException.class),
        INTERRUPTED_EXCEPTION(InterruptedException.class),
        VISA_EXCEPTION(VISAException.class),
        UNKNOWN_EXCEPTION(Exception.class);

        private static final HashMap<Class, ExType> lookup = new HashMap<>();

        static {
            for (ExType e : ExType.values()) {
                lookup.put(e.getClazz(), e);
            }
        }

        private final Class clazz;

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

    public interface MapBuild<K,V> {

        void map(Map<K,V> map);

    }

    public interface Build<T> {
        void build(T toBuild);
    }

    public static class MapBuilder<K,V> {

        private final Map<K,V> map = new LinkedHashMap<>();

        public MapBuilder(Class<K> keyType, Class<V> valueType) {}

        public MapBuilder<K, V> put(K key, V value) {
            map.put(key, value);
            return this;
        }

        public Map<K, V> map() {
            return map;
        }

    }

}
