package JISA.GUI;

import JISA.Addresses.*;
import JISA.Control.ConfigStore;
import JISA.Devices.SMUCluster;
import JISA.Util;
import JISA.VISA.VISADevice;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.reflections.Reflections;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;

public class InstrumentConfig<T extends VISADevice> extends JFXWindow implements Element {

    public  ChoiceBox                   driverChoice;
    public  ChoiceBox                   protChoice;
    public  HBox                        GPIBRow;
    public  TextField                   GPIBBoard;
    public  TextField                   GPIBAddr;
    public  HBox                        TCPIPRow;
    public  TextField                   TCPIPHost;
    public  TextField                   TCPIPPort;
    public  Label                       TCPIPPortLabel;
    public  VBox                        USBRow;
    public  TextField                   USBBoard;
    public  TextField                   USBVendor;
    public  TextField                   USBProduct;
    public  TextField                   USBSN;
    public  HBox                        COMRow;
    public  TextField                   COMPort;
    public  Button                      browseButton;
    public  Button                      applyButton;
    public  String                      CHOICE_GPIB;
    public  String                      CHOICE_USB;
    public  String                      CHOICE_TCPIP;
    public  String                      CHOICE_TCPIP_RAW;
    public  String                      CHOICE_COM;
    public  Label                       title;
    public  ImageView                   image;
    public  StackPane                   topPanel;
    public  StackPane                   pane;
    private Class<T>                    deviceType;
    private ArrayList<Class>            possibleDrivers = new ArrayList<>();
    private Class<? extends VISADevice> driver;
    private Address                     address         = null;
    private T                           instrument      = null;
    private String                      realTitle;
    private ConfigStore                 config          = null;
    private String                      key;
    private LinkedList<Runnable>        onApply         = new LinkedList<>();

    public static class SMU extends InstrumentConfig<JISA.Devices.SMU> {

        public SMU(String title) throws IOException {
            super(title, null, JISA.Devices.SMU.class, null);
        }
    }

    public static class TC extends InstrumentConfig<JISA.Devices.TC> {

        public TC(String title) throws IOException {
            super(title, null, JISA.Devices.TC.class, null);
        }
    }

    public InstrumentConfig(String title, String key, Class<T> type, ConfigStore c) throws IOException {
        super(title, "FXML/InstrumentConfig.fxml");
        realTitle = title;
        this.title.setText(title);
        deviceType = type;
        updateDrivers();
        protChoice.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            chooseProtocol();
        });
        makeRed();
        chooseProtocol();
        config = c;
        this.key = key;

        if (config != null) {
            config.loadInstrument(key, this);
        }

    }

    public void updateDrivers() {

        Reflections reflections = new Reflections("JISA");
        Set         drivers     = reflections.getSubTypesOf(deviceType);

        if (!Modifier.isAbstract(deviceType.getModifiers())) {
            drivers.add(deviceType);
        }

        ArrayList<Class> list = new ArrayList<Class>(drivers);

        driverChoice.getItems().clear();
        possibleDrivers.clear();

        for (Class driver : list) {

            if (Modifier.isAbstract(driver.getModifiers()) || driver.equals(SMUCluster.class) || driver.getSimpleName().toLowerCase().contains("virtual") || driver.getSimpleName().toLowerCase().contains("dummy")) {
                continue;
            }

            driverChoice.getItems().add(driver.getSimpleName());
            possibleDrivers.add(driver);
        }

        protChoice.getSelectionModel().select(0);
        driverChoice.getSelectionModel().select(0);

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

        String selected = (String) protChoice.getValue();

        if (selected.equals(CHOICE_GPIB)) {
            int board = Integer.valueOf(GPIBBoard.getText().trim());
            int addr  = Integer.valueOf(GPIBAddr.getText().trim());
            address = new GPIBAddress(board, addr);
        } else if (selected.equals(CHOICE_USB)) {
            int    board   = Integer.valueOf(USBBoard.getText().trim());
            int    vendor  = Integer.decode(USBVendor.getText().trim());
            int    product = Integer.decode(USBProduct.getText().trim());
            String SN      = USBSN.getText().trim();
            address = new USBAddress(board, vendor, product, SN);
        } else if (selected.equals(CHOICE_TCPIP)) {
            String host = TCPIPHost.getText().trim();
            address = new TCPIPAddress(host);
        } else if (selected.equals(CHOICE_TCPIP_RAW)) {
            String host = TCPIPHost.getText().trim();
            int    port = Integer.valueOf(TCPIPPort.getText().trim());
            address = new TCPIPSocketAddress(host, port);
        } else if (selected.equals(CHOICE_COM)) {
            int board = Integer.valueOf(COMPort.getText().trim());
            address = new SerialAddress(board);
        } else {
            return;
        }

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

        makeAmber();
        try {
            Constructor constructor = driver.getConstructor(Address.class);
            instrument = (T) constructor.newInstance(address);
            makeGreen();
        } catch (Throwable e) {
            instrument = null;
            if (message) {
                GUI.errorAlert("Connection Error", "Connection Error", e.getCause() == null ? e.getMessage() : e.getCause().getMessage(), 600);
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
            topPanel.setStyle("-fx-background-color: brown; -fx-background-radius: 5px 5px 0 0;");
            title.setText(realTitle);
        });
    }

    private void makeAmber() {
        GUI.runNow(() -> {
            topPanel.setStyle("-fx-background-color: #D9A200; -fx-background-radius: 5px 5px 0 0;");
            title.setText(realTitle + " (Connecting...)");
        });
    }

    private void makeGreen() {
        GUI.runNow(() -> {
            topPanel.setStyle("-fx-background-color: teal; -fx-background-radius: 5px 5px 0 0;");
            title.setText(realTitle);
        });
    }

    private void updateAddress() {

        if (address == null) {
            return;
        }

        GUI.runNow(() -> {
            switch (address.getType()) {

                case GPIB:
                    protChoice.setValue(CHOICE_GPIB);
                    GPIBAddress gpib = address.toGPIBAddress();
                    GPIBBoard.setText(String.valueOf(gpib.getBus()));
                    GPIBAddr.setText(String.valueOf(gpib.getAddress()));
                    break;

                case USB:
                    protChoice.setValue(CHOICE_USB);
                    USBAddress usb = address.toUSBAddress();
                    USBBoard.setText(Integer.toString(usb.getBoard()));
                    USBVendor.setText(String.format("0x%04X", usb.getManufacturer()));
                    USBProduct.setText(String.format("0x%04X", usb.getModel()));
                    USBSN.setText(usb.getSerialNumber());
                    break;

                case SERIAL:
                    protChoice.setValue(CHOICE_COM);
                    COMPort.setText(String.valueOf(address.toSerialAddress().getBoard()));
                    break;

                case TCPIP:
                    protChoice.setValue(CHOICE_TCPIP);
                    TCPIPHost.setText(address.toTCPIPAddress().getHost());
                    break;

                case TCPIP_SOCKET:
                    protChoice.setValue(CHOICE_TCPIP_RAW);
                    TCPIPSocketAddress socket = address.toTCPIPSocketAddress();
                    TCPIPHost.setText(socket.getHost());
                    TCPIPPort.setText(String.valueOf(socket.getPort()));
                    break;

            }

        });
    }

    public void chooseProtocol() {

        GPIBRow.setVisible(false);
        GPIBRow.setManaged(false);
        TCPIPRow.setVisible(false);
        TCPIPRow.setManaged(false);
        TCPIPPort.setVisible(false);
        TCPIPPort.setManaged(false);
        TCPIPPortLabel.setVisible(false);
        TCPIPPortLabel.setManaged(false);
        USBRow.setVisible(false);
        USBRow.setManaged(false);
        COMRow.setVisible(false);
        COMRow.setManaged(false);

        String selected = (String) protChoice.getValue();

        if (selected.equals(CHOICE_GPIB)) {
            GPIBRow.setVisible(true);
            GPIBRow.setManaged(true);
        } else if (selected.equals(CHOICE_USB)) {
            USBRow.setVisible(true);
            USBRow.setManaged(true);
        } else if (selected.equals(CHOICE_TCPIP)) {
            TCPIPRow.setVisible(true);
            TCPIPRow.setManaged(true);
        } else if (selected.equals(CHOICE_TCPIP_RAW)) {
            TCPIPRow.setVisible(true);
            TCPIPRow.setManaged(true);
            TCPIPPort.setVisible(true);
            TCPIPPort.setManaged(true);
            TCPIPPortLabel.setVisible(true);
            TCPIPPortLabel.setManaged(true);
        } else if (selected.equals(CHOICE_COM)) {
            COMRow.setVisible(true);
            COMRow.setManaged(true);
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
