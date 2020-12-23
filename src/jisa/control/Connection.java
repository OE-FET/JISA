package jisa.control;

import javafx.scene.image.Image;
import jisa.Util;
import jisa.addresses.Address;
import jisa.addresses.StrAddress;
import jisa.devices.*;
import jisa.experiment.ActionQueue;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Connection<T extends Instrument> {

    private static final List<Connection<?>> ALL_CONNECTIONS = new LinkedList<>();
    private static final List<SRunnable>     LISTENERS       = new LinkedList<>();

    private final String             name;
    private final Class<T>           driverClass;
    private final List<Runnable>     onChange   = new LinkedList<>();
    private       Class<? extends T> driver     = null;
    private       Address            address    = null;
    private       T                  instrument = null;
    private       Status             status     = Status.DISCONNECTED;

    public Connection(String name, Class<T> type) {
        this.driverClass = type;
        this.name        = name;
        registerConnection(this);
    }

    public static List<Connection<?>> getAllConnections() {
        return List.copyOf(ALL_CONNECTIONS);
    }

    public static SRunnable addListener(SRunnable listener) {
        LISTENERS.add(listener);
        return listener;
    }

    public static void removeListener(SRunnable listener) {
        LISTENERS.remove(listener);
    }

    protected static void registerConnection(Connection<?> connection) {
        ALL_CONNECTIONS.add(connection);
        LISTENERS.forEach(SRunnable::runRegardless);
    }

    protected static void unregisterConnection(Connection<?> connection) {
        ALL_CONNECTIONS.remove(connection);
        LISTENERS.forEach(SRunnable::runRegardless);
    }

    protected static void triggerListeners() {
        LISTENERS.forEach(SRunnable::runRegardless);
    }

    public static <T> List<Connection<?>> getConnectionsByTarget(Class<T> target) {

        return ALL_CONNECTIONS.stream()
                              .filter(c ->
                                  target.isAssignableFrom(c.getType())
                                      || (c instanceof MultiChannel && target.isAssignableFrom(((MultiChannel<?>) c).getChannelType()))
                                      || (c instanceof MultiOutput && target.isAssignableFrom(((MultiOutput<?>) c).getOutputType()))
                                      || (c instanceof MultiSensor && target.isAssignableFrom(((MultiSensor<?>) c).getSensorType()))
                              )
                              .collect(Collectors.toList());

    }

    public void writeToConfig(ConfigBlock block) {

        block.stringValue("Driver").set(driver == null ? null : driver.getName());
        block.stringValue("Address").set(address == null ? null : address.toString());
        block.save();

    }

    public void loadFromConfig(ConfigBlock block) {

        try {
            driver  = (Class<? extends T>) Class.forName(block.stringValue("Driver").get());
            address = new StrAddress(block.stringValue("Address").get());
            triggerChange();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String getName() {
        return name;
    }

    public Class<T> getType() {
        return driverClass;
    }

    public Status getStatus() {
        return status;
    }

    public Class<? extends T> getDriver() {
        return driver;
    }

    public void setDriver(Class<? extends T> driver) {
        this.driver = driver;
        triggerChange();
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
        triggerChange();
    }

    public boolean isConnected() {
        return instrument != null;
    }

    public T getInstrument() {
        return instrument;
    }

    public Runnable addChangeListener(Runnable listener) {
        onChange.add(listener);
        return listener;
    }

    public void connect() throws DriverException, DeviceException, IOException {

        status = Status.CONNECTING;
        triggerChange();

        try {

            Constructor<? extends T> constructor = driver.getConstructor(Address.class);
            instrument = constructor.newInstance(address);
            status     = Status.CONNECTED;

        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException e) {

            status = Status.ERROR;
            throw new DriverException("Could not construct driver", e);

        } catch (InvocationTargetException e) {

            status = Status.ERROR;

            Throwable thrown = e.getTargetException();

            if (thrown instanceof IOException) {
                throw (IOException) thrown;
            }

            if (thrown instanceof DeviceException) {
                throw (DeviceException) thrown;
            }

            throw new DriverException("Could not construct driver", e);

        } finally {
            triggerChange();
            triggerListeners();
        }

    }

    public void disconnect() {

        if (instrument != null) {
            Util.runRegardless(instrument::close);
        }

        instrument = null;
        status     = Status.DISCONNECTED;
        triggerChange();
        triggerListeners();
    }

    private void triggerChange() {
        onChange.forEach(Runnable::run);
    }

    public void delete() {
        disconnect();
        unregisterConnection(this);
    }

    public enum Status {

        DISCONNECTED(ActionQueue.Status.NOT_STARTED.getImage()),
        CONNECTING(ActionQueue.Status.RUNNING.getImage()),
        CONNECTED(ActionQueue.Status.COMPLETED.getImage()),
        ERROR(ActionQueue.Status.ERROR.getImage());

        private final Image image;

        Status(Image image) {
            this.image = image;
        }

        public Image getImage() {
            return image;
        }

    }

}
