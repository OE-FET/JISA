package jisa.devices.translator;

import jisa.devices.DeviceException;
import jisa.devices.Instrument;
import jisa.devices.MultiInstrument;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Stage<T extends Translator> extends Instrument, MultiInstrument {

    /**
     * Returns a list of all axes controlled by this stage, represented as Translator objects.
     *
     * @return List of Translators
     */
    List<T> getAllAxes();

    @Override
    default List<T> getSubInstruments() {
        return getAllAxes();
    }

    default void stop() throws IOException, DeviceException {

        List<DeviceException> deviceExceptions = new LinkedList<>();
        List<IOException>     ioExceptions     = new LinkedList<>();

        for (Translator translator : getAllAxes()) {

            try {
                translator.stop();
            } catch (DeviceException e) {
                deviceExceptions.add(e);
            } catch (IOException e) {
                ioExceptions.add(e);
            } catch (Throwable ignored) { }

        }

        if (!deviceExceptions.isEmpty() && !ioExceptions.isEmpty()) {

            throw new IOException(
                ioExceptions.stream().map(IOException::getMessage).collect(Collectors.joining("\n\n"))
                    + "\n\n"
                    + deviceExceptions.stream().map(DeviceException::getMessage).collect(Collectors.joining("\n\n"))
            );

        }

        if (!deviceExceptions.isEmpty()) {
            throw new DeviceException(deviceExceptions.stream().map(DeviceException::getMessage).collect(Collectors.joining("\n\n")));
        }

        if (!ioExceptions.isEmpty()) {
            throw new IOException(ioExceptions.stream().map(IOException::getMessage).collect(Collectors.joining("\n\n")));
        }

    }

    default void moveToHome() throws IOException, DeviceException {

        List<DeviceException> deviceExceptions = new LinkedList<>();
        List<IOException>     ioExceptions     = new LinkedList<>();

        for (Translator translator : getAllAxes()) {

            try {
                translator.moveToHome();
            } catch (DeviceException e) {
                deviceExceptions.add(e);
            } catch (IOException e) {
                ioExceptions.add(e);
            } catch (Throwable ignored) { }

        }

        if (!deviceExceptions.isEmpty() && !ioExceptions.isEmpty()) {

            throw new IOException(
                ioExceptions.stream().map(IOException::getMessage).collect(Collectors.joining("\n\n"))
                    + "\n\n"
                    + deviceExceptions.stream().map(DeviceException::getMessage).collect(Collectors.joining("\n\n"))
            );

        }

        if (!deviceExceptions.isEmpty()) {
            throw new DeviceException(deviceExceptions.stream().map(DeviceException::getMessage).collect(Collectors.joining("\n\n")));
        }

        if (!ioExceptions.isEmpty()) {
            throw new IOException(ioExceptions.stream().map(IOException::getMessage).collect(Collectors.joining("\n\n")));
        }

    }

    default boolean isMoving() throws IOException, DeviceException {

        for (Translator translator : getAllAxes()) {

            if (translator.isMoving()) {
                return true;
            }

        }

        return false;

    }

    interface Linear<L extends Translator.Linear, T extends Translator> extends Stage<T> {

        /**
         * Returns a list of all axes that are linear.
         *
         * @return List of all linear axes
         */
        List<L> getLinearAxes();

        @Override
        default List<T> getAllAxes() {
            return (List<T>) getLinearAxes();
        }

    }

    interface Rotational<R extends Translator.Rotational, T extends Translator> extends Stage<T> {

        /**
         * Returns a list of all axes that are rotational.
         *
         * @return List of all rotational axes
         */
        List<R> getRotationalAxes();

        @Override
        default List<T> getAllAxes() {
            return (List<T>) getRotationalAxes();
        }

    }

    interface Mixed<L extends Translator.Linear, R extends Translator.Rotational, T extends Translator> extends Linear<L,T>, Rotational<R,T> {

        @Override
        default List<T> getAllAxes() {
            return (List<T>) Stream.concat(getLinearAxes().stream(), getRotationalAxes().stream()).collect(Collectors.toList());
        }

    }

}
