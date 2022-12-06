package jisa.control;

import javafx.scene.image.Image;
import jisa.Util;
import jisa.addresses.Address;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.Instrument;
import jisa.devices.interfaces.MultiInstrument;
import jisa.experiment.queue.Action;

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

    public static Connection<?> findConnectionFor(Instrument instrument) {
        return getAllConnections().stream().filter(con -> con.isConnected() && con.getInstrument() == instrument).findAny().orElse(null);
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
                                  (c.isConnected() && target.isAssignableFrom(c.getDriver()))
                                      || target.isAssignableFrom(c.getType())
                                      || (c.getInstrument() instanceof MultiInstrument && ((MultiInstrument) c.getInstrument()).getSubInstrumentTypes().stream().anyMatch(target::isAssignableFrom))
                              ).collect(Collectors.toList());

    }

    public static <T> List<Connection<?>> getConnectionsOf(Class<T> type) {

        return ALL_CONNECTIONS.stream()
                              .filter(con -> con.isConnected() && type.isAssignableFrom(con.getInstrument().getClass()))
                              .collect(Collectors.toList());

    }

    public void writeToConfig(ConfigBlock block) {

        block.stringValue("Driver").set(driver == null ? null : driver.getName());
        block.stringValue("Address").set(address == null ? null : address.getJISAString());
        block.save();

    }

    public void loadFromConfig(ConfigBlock block) {

        try {
            driver  = (Class<? extends T>) Class.forName(block.stringValue("Driver").get());
            address = Address.parse(block.stringValue("Address").get());
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

        DISCONNECTED(Action.Status.NOT_STARTED.getImage()),
        CONNECTING(Action.Status.RUNNING.getImage()),
        CONNECTED(Action.Status.COMPLETED.getImage()),
        ERROR(Action.Status.ERROR.getImage());

        private final Image image;

        Status(Image image) {
            this.image = image;
        }

        public Image getImage() {
            return image;
        }

    }

}
