package jisa.gui;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import jisa.Util;
import jisa.addresses.Address;
import jisa.addresses.Address.AddressParams;
import jisa.addresses.StrAddress;
import jisa.control.ConfigBlock;
import jisa.control.IConf;
import jisa.devices.Instrument;
import jisa.devices.SMUCluster;
import jisa.visa.VISADevice;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.*;

@SuppressWarnings("rawtypes")
public class Connector<T extends Instrument> extends JFXElement implements Element, IConf<T> {

    // Properties
    private final Class<T>                    deviceType;
    private final List<Class>                 possibleDrivers   = new ArrayList<>();
    private final List<Class>                 possibleAddresses = new ArrayList<>();
    private final String                      realTitle;
    private final ConfigBlock                 config;
    private final List<Runnable>              onApply           = new LinkedList<>();
    private final List<Runnable>              titleActions      = new LinkedList<>();
    public        ChoiceBox<String>           driverChoice;
    public        ChoiceBox<String>           protChoice;
    public        Button                      browseButton;
    public        Button                      applyButton;
    public        TitledPane                  titled;
    public        ImageView                   image;
    public        StackPane                   pane;
    public        GridPane                    parameters;
    private       Pane                        title             = null;
    private       Class<? extends VISADevice> driver;
    private       Address                     address           = null;
    private       T                           instrument        = null;
    private       ConfigBlock.Value<String>   driverSave;
    private       ConfigBlock.Value<String>   addressSave;
    private       AddressParams<?>            currentParams     = null;

    public Connector(String title, Class<T> type, ConfigBlock c) {

        super(title, Connector.class.getResource("fxml/InstrumentConfig.fxml"));

        realTitle  = title;
        deviceType = type;

        this.titled.textProperty().bind(titleProperty());

        titled.widthProperty().addListener(o -> {
            this.title = (Pane) titled.lookup(".title");
            titleActions.forEach(Runnable::run);
            titleActions.clear();
        });

        titled.setTextFill(Colour.WHITE);

        updateDrivers();
        updateProtocols();
        makeRed();
        chooseProtocol();

        config = c;

        if (config != null) {
            driverSave  = config.stringValue("driver");
            addressSave = config.stringValue("address");

            try {
                String name = driverSave.getOrDefault(null);
                if (name != null) setDriver((Class<? extends VISADevice>) Class.forName(name));
            } catch (ClassNotFoundException ignored) {
            }

            String address = addressSave.getOrDefault(null);
            if (address != null) setAddress(new StrAddress(address));

        }

        protChoice.getSelectionModel()
                  .selectedItemProperty()
                  .addListener((observable, oldValue, newValue) -> chooseProtocol());

    }

    public void updateDrivers() {

        Reflections reflections = new Reflections("jisa");
        Set         drivers     = reflections.getSubTypesOf(deviceType);

        if (!Modifier.isAbstract(deviceType.getModifiers()) && !Modifier.isInterface(deviceType.getModifiers())) {
            drivers.add(deviceType);
        }

        ArrayList<Class> list = new ArrayList<Class>(drivers);

        list.sort(Comparator.comparing(Class::getSimpleName));

        driverChoice.getItems().clear();
        possibleDrivers.clear();

        for (Class driver : list) {

            if (Modifier.isAbstract(driver.getModifiers()) || Modifier.isInterface(driver.getModifiers()) || driver.getSimpleName().trim().equals(
                    "") || driver.equals(SMUCluster.class) || driver.getSimpleName().toLowerCase().contains("virtual") || driver.getSimpleName().toLowerCase().contains(
                    "dummy")) {
                continue;
            }

            driverChoice.getItems().add(driver.getSimpleName());
            possibleDrivers.add(driver);
        }

        protChoice.getSelectionModel().select(0);
        driverChoice.getSelectionModel().select(0);

    }

    public void updateProtocols() {

        Reflections      reflections = new Reflections("jisa");
        Set              drivers     = reflections.getSubTypesOf(AddressParams.class);
        ArrayList<Class> list        = new ArrayList<Class>(drivers);

        list.sort(Comparator.comparing(Class::getSimpleName));

        protChoice.getItems().clear();
        possibleAddresses.clear();

        for (Class address : list) {

            try {
                AddressParams instance = (AddressParams) address.newInstance();
                protChoice.getItems().add(instance.getName());
                possibleAddresses.add(address);
            } catch (Exception ignored) {
            }

        }

        protChoice.getSelectionModel().select(0);

    }

    public void browse() {

        (new Thread(() -> {
            address = GUI.browseVISA();
            updateAddress();
        })).start();

    }

    public boolean isConnected() {
        return instrument != null;
    }

    public void apply() {

        address = currentParams.createAddress();

        int selectedDriver = driverChoice.getSelectionModel().getSelectedIndex();
        driver = possibleDrivers.get(selectedDriver);

        if (config != null) {
            driverSave.set(driver.getName());
            addressSave.set(address.toString());
        }

        (new Thread(this::connect)).start();

    }

    public void connect() {
        connect(true);
    }

    public void connect(boolean message) {

        if (isConnected()) {
            try {
                instrument.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        makeAmber();
        try {

            Constructor constructor = driver.getConstructor(Address.class);

            instrument = (T) constructor.newInstance(address);

            makeGreen();

        } catch (Throwable e) {

            instrument = null;

            if (message) {
                GUI.errorAlert(
                        "Connection Error",
                        "Connection Error",
                        e.getCause() == null ? e.getMessage() : e.getCause().getMessage(),
                        600
                );
                e.printStackTrace();
            } else {
                Util.errLog.println(e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
            }

            makeRed();

        }

        for (Runnable r : onApply) {
            r.run();
        }

    }

    public void setOnConnect(Runnable onConnect) {
        onApply.add(onConnect);
    }

    private void makeRed() {
        setTitleColour(Colour.BROWN);
    }

    private void makeAmber() {
        setTitleColour(Colour.string("#D9A200"));
    }

    private void makeGreen() {
        setTitleColour(Colour.TEAL);
    }

    private void setTitleColour(Color colour) {

        GUI.runNow(() -> {

            Runnable listener = () -> title.setBackground(
                    new Background(
                            new BackgroundFill(colour, new CornerRadii(5, 5, 0, 0, false), null)
                    )
            );

            if (title == null) {
                titleActions.add(listener);
            } else {
                listener.run();
            }

        });

    }

    private void updateAddress() {

        if (address == null) {
            return;
        }

        GUI.runNow(() -> {

            AddressParams<?> newParams = address.createParams();
            int              index     = possibleAddresses.indexOf(newParams.getClass());
            protChoice.getSelectionModel().select(index);
            currentParams = newParams;
            createAddressParams();

        });
    }

    private void createAddressParams() {

        parameters.getChildren().clear();

        currentParams.forEach((i, n, t) -> {

            int row = i / 2;
            int col = i % 2;

            Label name = new Label(n);
            name.setMinWidth(Region.USE_PREF_SIZE);
            name.setAlignment(Pos.CENTER_RIGHT);
            TextField field = t ? new TextField() : new IntegerField();

            if (t) {

                GUI.runNow(() -> field.setText(currentParams.getString(i)));

                field.textProperty().addListener((o, s, t1) -> currentParams.set(i, field.getText()));

            } else {

                GUI.runNow(() -> field.setText(String.valueOf(currentParams.getInt(i))));

                field.textProperty().addListener((o, s, t1) -> currentParams.set(
                        i,
                        ((IntegerField) field).getIntValue()
                ));

            }

            parameters.add(name, 2 * col, row);
            parameters.add(field, 2 * col + 1, row);

            GridPane.setHgrow(name, Priority.NEVER);
            GridPane.setHgrow(field, Priority.ALWAYS);
            GridPane.setVgrow(name, Priority.NEVER);
            GridPane.setVgrow(field, Priority.NEVER);

            GridPane.setHalignment(name, HPos.RIGHT);

        });

    }

    public void chooseProtocol() {

        int   selected = protChoice.getSelectionModel().getSelectedIndex();
        Class type     = possibleAddresses.get(selected);
        try {
            currentParams = (AddressParams) type.newInstance();
            createAddressParams();
        } catch (Exception e) {
            GUI.errorAlert(e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
            e.printStackTrace();
        }

    }

    public Node getBorderedNode() {

        BorderPane border = new BorderPane();
        border.setCenter(getNode());
        border.setPadding(new Insets(-GUI.SPACING));

        return border;

    }

    @Override
    public String getTitle() {
        return realTitle;
    }

    public T get() {
        return instrument;
    }

    public void set(T value) {
        address    = value.getAddress();
        instrument = value;

        for (Class d : possibleDrivers) {

            if (d.equals(instrument.getClass())) {
                driver = d;
                break;
            }

        }

        driverChoice.setValue(driver.getSimpleName());
        updateAddress();
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address a) {
        address = a;
        updateAddress();
    }

    public Class<? extends VISADevice> getDriver() {
        return driver;
    }

    public void setDriver(Class<? extends VISADevice> d) {
        driver = d;
        driverChoice.setValue(driver.getSimpleName());
    }

    public Class<T> getDeviceType() {
        return deviceType;
    }

    /**
     * Connector for SMU instruments
     */
    public static class SMU extends Connector<jisa.devices.SMU> {

        public SMU(String title, String key, ConfigBlock config) {
            super(title, jisa.devices.SMU.class, config);
        }

        public SMU(String title) {
            this(title, null, null);
        }

    }

    public static class TC extends Connector<jisa.devices.TC> {

        public TC(String title, String key, ConfigBlock config) {
            super(title, jisa.devices.TC.class, config);
        }

        public TC(String title) {
            this(title, null, null);
        }

    }

    public static class LockIn extends Connector<jisa.devices.LockIn> {

        public LockIn(String title, String key, ConfigBlock config) {
            super(title, jisa.devices.LockIn.class, config);
        }

        public LockIn(String title) {
            this(title, null, null);
        }

    }

    public static class DPLockIn extends Connector<jisa.devices.DPLockIn> {

        public DPLockIn(String title, String key, ConfigBlock config) {
            super(title, jisa.devices.DPLockIn.class, config);
        }

        public DPLockIn(String title) {
            this(title, null, null);
        }

    }

    public static class DCPower extends Connector<jisa.devices.DCPower> {

        public DCPower(String title, String key, ConfigBlock config) {
            super(title, jisa.devices.DCPower.class, config);
        }

        public DCPower(String title) {
            this(title, null, null);
        }

    }

    public static class VPreAmp extends Connector<jisa.devices.VPreAmp> {

        public VPreAmp(String title, String key, ConfigBlock config) {
            super(title, jisa.devices.VPreAmp.class, config);
        }

        public VPreAmp(String title) {
            this(title, null, null);
        }

    }

    public static class VMeter extends Connector<jisa.devices.VMeter> {

        public VMeter(String title, String key, ConfigBlock config) {
            super(title, jisa.devices.VMeter.class, config);
        }

        public VMeter(String title) {
            this(title, null, null);
        }

    }

    public static class IMeter extends Connector<jisa.devices.IMeter> {

        public IMeter(String title, String key, ConfigBlock config) {
            super(title, jisa.devices.IMeter.class, config);
        }

        public IMeter(String title) {
            this(title, null, null);
        }

    }

    public static class VSource extends Connector<jisa.devices.VSource> {

        public VSource(String title, String key, ConfigBlock config) {
            super(title, jisa.devices.VSource.class, config);
        }

        public VSource(String title) {
            this(title, null, null);
        }

    }

    public static class ISource extends Connector<jisa.devices.ISource> {

        public ISource(String title, String key, ConfigBlock config) {
            super(title, jisa.devices.ISource.class, config);
        }

        public ISource(String title) {
            this(title, null, null);
        }

    }

    public static class IVMeter extends Connector<jisa.devices.IVMeter> {

        public IVMeter(String title, String key, ConfigBlock config) {
            super(title, jisa.devices.IVMeter.class, config);
        }

        public IVMeter(String title) {
            this(title, null, null);
        }

    }

    public static class IVSource extends Connector<jisa.devices.IVSource> {

        public IVSource(String title, String key, ConfigBlock config) {
            super(title, jisa.devices.IVSource.class, config);
        }

        public IVSource(String title) {
            this(title, null, null);
        }

    }

    public static class TMeter extends Connector<jisa.devices.TMeter> {

        public TMeter(String title, String key, ConfigBlock config) {
            super(title, jisa.devices.TMeter.class, config);
        }

        public TMeter(String title) {
            this(title, null, null);
        }

    }

}
