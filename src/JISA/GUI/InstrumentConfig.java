package JISA.GUI;

import JISA.Addresses.Address;
import JISA.Addresses.Address.AddressParams;
import JISA.Control.ConfigStore;
import JISA.Control.IConf;
import JISA.Devices.Instrument;
import JISA.Devices.SMUCluster;
import JISA.Util;
import JISA.VISA.VISADevice;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.reflections.Reflections;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.*;

public class InstrumentConfig<T extends Instrument> extends JFXWindow implements Element, IConf<T> {

    // Elements
    public ChoiceBox  driverChoice;
    public ChoiceBox  protChoice;
    public Button     browseButton;
    public Button     applyButton;
    public TitledPane titled;
    public ImageView  image;
    public StackPane  pane;
    public GridPane   parameters;

    // Properties
    private Class<T>                    deviceType;
    private List<Class>                 possibleDrivers   = new ArrayList<>();
    private List<Class>                 possibleAddresses = new ArrayList<>();
    private Class<? extends VISADevice> driver;
    private Address                     address           = null;
    private T                           instrument        = null;
    private String                      realTitle;
    private ConfigStore                 config            = null;
    private String                      key;
    private List<Runnable>              onApply           = new LinkedList<>();
    private AddressParams               currentParams     = null;
    
    public static class SMU extends InstrumentConfig<JISA.Devices.SMU> {

        public SMU(String title, String key, ConfigStore config) throws IOException {
            super(title, key, JISA.Devices.SMU.class, config);
        }

        public SMU(String title) throws IOException {
            this(title, null, null);
        }

    }

    public static class TC extends InstrumentConfig<JISA.Devices.TC> {

        public TC(String title, String key, ConfigStore config) throws IOException {
            super(title, key, JISA.Devices.TC.class, config);
        }

        public TC(String title) throws IOException {
            this(title, null, null);
        }

    }

    public static class LockIn extends InstrumentConfig<JISA.Devices.LockIn> {

        public LockIn(String title, String key, ConfigStore config) throws IOException {
            super(title, key, JISA.Devices.LockIn.class, config);
        }

        public LockIn(String title) throws IOException {
            this(title, null, null);
        }

    }

    public static class DPLockIn extends InstrumentConfig<JISA.Devices.DPLockIn> {

        public DPLockIn(String title, String key, ConfigStore config) throws IOException {
            super(title, key, JISA.Devices.DPLockIn.class, config);
        }

        public DPLockIn(String title) throws IOException {
            this(title, null, null);
        }

    }

    public static class DCPower extends InstrumentConfig<JISA.Devices.DCPower> {

        public DCPower(String title, String key, ConfigStore config) throws IOException {
            super(title, key, JISA.Devices.DCPower.class, config);
        }

        public DCPower(String title) throws IOException {
            this(title, null, null);
        }

    }

    public static class VPreAmp extends InstrumentConfig<JISA.Devices.VPreAmp> {

        public VPreAmp(String title, String key, ConfigStore config) throws IOException {
            super(title, key, JISA.Devices.VPreAmp.class, config);
        }

        public VPreAmp(String title) throws IOException {
            this(title, null, null);
        }

    }

    public static class VMeter extends InstrumentConfig<JISA.Devices.VMeter> {

        public VMeter(String title, String key, ConfigStore config) throws IOException {
            super(title, key, JISA.Devices.VMeter.class, config);
        }

        public VMeter(String title) throws IOException {
            this(title, null, null);
        }

    }

    public static class IMeter extends InstrumentConfig<JISA.Devices.IMeter> {

        public IMeter(String title, String key, ConfigStore config) throws IOException {
            super(title, key, JISA.Devices.IMeter.class, config);
        }

        public IMeter(String title) throws IOException {
            this(title, null, null);
        }

    }

    public static class VSource extends InstrumentConfig<JISA.Devices.VSource> {

        public VSource(String title, String key, ConfigStore config) throws IOException {
            super(title, key, JISA.Devices.VSource.class, config);
        }

        public VSource(String title) throws IOException {
            this(title, null, null);
        }

    }

    public static class ISource extends InstrumentConfig<JISA.Devices.ISource> {

        public ISource(String title, String key, ConfigStore config) throws IOException {
            super(title, key, JISA.Devices.ISource.class, config);
        }

        public ISource(String title) throws IOException {
            this(title, null, null);
        }

    }

    public static class IVMeter extends InstrumentConfig<JISA.Devices.IVMeter> {

        public IVMeter(String title, String key, ConfigStore config) throws IOException {
            super(title, key, JISA.Devices.IVMeter.class, config);
        }

        public IVMeter(String title) throws IOException {
            this(title, null, null);
        }

    }

    public static class IVSource extends InstrumentConfig<JISA.Devices.IVSource> {

        public IVSource(String title, String key, ConfigStore config) throws IOException {
            super(title, key, JISA.Devices.IVSource.class, config);
        }

        public IVSource(String title) throws IOException {
            this(title, null, null);
        }

    }
    
    public static class TMeter extends InstrumentConfig<JISA.Devices.TMeter> {

        public TMeter(String title, String key, ConfigStore config) throws IOException {
            super(title, key, JISA.Devices.TMeter.class, config);
        }

        public TMeter(String title) throws IOException {
            this(title, null, null);
        }

    }

    public InstrumentConfig(String title, String key, Class<T> type, ConfigStore c) throws IOException {
        super(title, "FXML/InstrumentConfig.fxml");
        realTitle = title;
        this.titled.setText(title);
        deviceType = type;
        updateDrivers();
        updateProtocols();
        makeRed();
        chooseProtocol();
        config = c;
        this.key = key;

        if (config != null) {
            config.loadInstrument(key, this);
        }

        protChoice.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            chooseProtocol();
        });

    }

    public void updateDrivers() {

        Reflections reflections = new Reflections("JISA");
        Set         drivers     = reflections.getSubTypesOf(deviceType);

        if (!Modifier.isAbstract(deviceType.getModifiers()) && !Modifier.isInterface(deviceType.getModifiers())) {
            drivers.add(deviceType);
        }

        ArrayList<Class> list = new ArrayList<Class>(drivers);

        list.sort(Comparator.comparing(Class::getSimpleName));

        driverChoice.getItems().clear();
        possibleDrivers.clear();

        for (Class driver : list) {

            if (Modifier.isAbstract(driver.getModifiers()) || Modifier.isInterface(driver.getModifiers()) || driver.getSimpleName().trim().equals("") || driver.equals(SMUCluster.class) || driver.getSimpleName().toLowerCase().contains("virtual") || driver.getSimpleName().toLowerCase().contains("dummy")) {
                continue;
            }

            driverChoice.getItems().add(driver.getSimpleName());
            possibleDrivers.add(driver);
        }

        protChoice.getSelectionModel().select(0);
        driverChoice.getSelectionModel().select(0);

    }

    public void updateProtocols() {

        Reflections      reflections = new Reflections("JISA");
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
            config.saveInstrument(key, this);
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
                GUI.errorAlert("Connection Error", "Connection Error", e.getCause() == null ? e.getMessage() : e.getCause().getMessage(), 600);
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
        GUI.runNow(() -> {
            titled.applyCss();
            titled.layout();
            titled.lookup(".title").setStyle("-fx-background-color: brown; -fx-background-radius: 5px 5px 0 0; -fx-text-fill: white;");
            titled.setTextFill(Color.WHITE);
            titled.setText(realTitle);
        });
    }

    private void makeAmber() {
        GUI.runNow(() -> {
            titled.applyCss();
            titled.layout();
            titled.lookup(".title").setStyle("-fx-background-color: #D9A200; -fx-background-radius: 5px 5px 0 0; -fx-text-fill: white;");
            titled.setTextFill(Color.WHITE);
            titled.setText(realTitle + " (Connecting...)");
        });
    }

    private void makeGreen() {
        GUI.runNow(() -> {
            titled.applyCss();
            titled.layout();
            titled.lookup(".title").setStyle("-fx-background-color: teal; -fx-background-radius: 5px 5px 0 0;");
            titled.setTextFill(Color.WHITE);
            titled.setText(realTitle);
        });
    }

    private void updateAddress() {

        if (address == null) {
            return;
        }

        GUI.runNow(() -> {

            currentParams = address.createParams();
            int index = possibleAddresses.indexOf(currentParams.getClass());
            protChoice.getSelectionModel().select(index);
            createAddressParams();

        });
    }

    private void createAddressParams() {

        parameters.getChildren().clear();

        currentParams.forEach((i, n, t) -> {

            int row = (int) i / 2;
            int col = (int) i % 2;

            Label name = new Label((String) n);
            name.setMinWidth(Region.USE_PREF_SIZE);
            name.setAlignment(Pos.CENTER_RIGHT);
            TextField field = (boolean) t ? new TextField() : new IntegerField();

            if ((boolean) t) {

                field.setText(currentParams.getString((int) i));

                field.textProperty().addListener((observableValue, s, t1) -> {
                    currentParams.set((int) i, field.getText());
                });

            } else {

                field.setText(String.valueOf(currentParams.getInt((int) i)));

                field.textProperty().addListener((observableValue, s, t1) -> {
                    currentParams.set((int) i, ((IntegerField) field).getIntValue());
                });

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

    public Pane getPane() {
        return pane;
    }

    @Override
    public String getTitle() {
        return realTitle;
    }

    public T get() {
        return instrument;
    }

    public void set(T value) {
        address = value.getAddress();
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

}
