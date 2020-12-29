package jisa.gui;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.TextAlignment;
import javafx.util.StringConverter;
import jisa.Util;
import jisa.addresses.Address;
import jisa.control.ConfigBlock;
import jisa.control.Connection;
import jisa.control.SRunnable;
import jisa.devices.Instrument;
import jisa.devices.SMUCluster;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.stream.Collectors;

public class Connector<T extends Instrument> extends JFXElement {


    @FXML
    protected Label                                             errorText;
    @FXML
    protected ChoiceBox<Class<? extends T>>                     driverChoice;
    @FXML
    protected ChoiceBox<Class<? extends Address.AddressParams>> protocolChoice;
    @FXML
    protected GridPane                                          parameters;
    @FXML
    protected ImageView icon;
    @FXML
    protected Button    removeButton;

    protected Connection<T>            connection;
    protected Address.AddressParams<?> addressParams = null;
    protected boolean                  connecting    = false;


    public Connector(Connection<T> connection) {

        super(connection.getName(), Connector.class.getResource("fxml/Connector.fxml"));

        Reflections reflections = new Reflections("jisa");

        driverChoice.setConverter(new StringConverter<>() {

            @Override
            public String toString(Class<? extends T> aClass) {

                try {
                    return String.format("%s (%s)", aClass.getMethod("getDescription").invoke(null), aClass.getSimpleName());
                } catch (Exception e) {
                    return aClass.getSimpleName();
                }

            }

            @Override
            public Class<? extends T> fromString(String s) {
                return null;
            }

        });

        driverChoice.getItems().setAll((reflections.getSubTypesOf(connection.getType()).stream().filter(driver ->
            !(
                Modifier.isAbstract(driver.getModifiers())
                    || Modifier.isInterface(driver.getModifiers())
                    || driver.getSimpleName().trim().equals("")
                    || driver.equals(SMUCluster.class)
                    || driver.getSimpleName().toLowerCase().contains("virtual")
                    || driver.getSimpleName().toLowerCase().contains("dummy")
            )
        ).sorted(Comparator.comparing(Class::getSimpleName)).collect(Collectors.toList())));

        protocolChoice.getItems().setAll(reflections.getSubTypesOf(Address.AddressParams.class)
                                                    .stream()
                                                    .sorted(Comparator.comparing(Class::getSimpleName))
                                                    .collect(Collectors.toList()));

        protocolChoice.setConverter(new StringConverter<>() {

            @Override
            public String toString(Class<? extends Address.AddressParams> aClass) {
                try {
                    return aClass.getConstructor().newInstance().getName();
                } catch (Exception e) {
                    return "Unknown Protocol";
                }
            }

            @Override
            public Class<? extends Address.AddressParams> fromString(String s) {
                return null;
            }

        });

        icon.setImage(connection.getStatus().getImage());

        if (connection.getDriver() != null) {
            driverChoice.getSelectionModel().select(connection.getDriver());
        }

        if (connection.getAddress() != null) {
            addressParams = connection.getAddress().createParams();
            protocolChoice.getSelectionModel().select(addressParams.getClass());
            updateAddressParameters();
        }

        protocolChoice.valueProperty().addListener(o -> changeProtocol());

        errorText.setOnMouseClicked(event -> GUI.runNow(() -> {
            errorText.setVisible(false);
            errorText.setManaged(false);
        }));

        connection.addChangeListener(() -> GUI.runNow(this::update));

        this.connection = connection;

    }

    public void setRemoveButton(SRunnable onClick) {

        GUI.runNow(() -> {

            if (onClick == null) {
                removeButton.setVisible(false);
            } else {
                removeButton.setOnAction(event -> onClick.start());
                removeButton.setVisible(true);
            }

        });

    }

    public void removeRemoveButton() {
        setRemoveButton(null);
    }

    public Connector(String name, Class<T> target) {
        this(new Connection<>(name, target));
    }

    public void loadFromConfig(ConfigBlock block) {
        connection.loadFromConfig(block);
    }

    public void writeToConfig(ConfigBlock block) {
        connection.writeToConfig(block);
    }

    public void linkToConfig(ConfigBlock block) {
        loadFromConfig(block);
        Util.addShutdownHook(() -> writeToConfig(block));
    }

    private synchronized void changeProtocol() {

        if (addressParams == null || addressParams.getClass() != protocolChoice.getValue()) {

            try {
                addressParams = protocolChoice.getValue().getConstructor().newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        updateAddressParameters();

    }

    private synchronized void update() {

        GUI.runNow(() -> {

            if (!connecting) {

                driverChoice.setValue(connection.getDriver());

                if (connection.getAddress() != null) {
                    addressParams = connection.getAddress().createParams();
                    protocolChoice.setValue(addressParams.getClass());
                }

            }

            icon.setImage(connection.getStatus().getImage());

        });

    }

    private synchronized void updateAddressParameters() {

        GUI.runNow(() -> {

            parameters.getChildren().clear();

            if (addressParams == null) {
                return;
            }

            addressParams.forEach((index, name, isText) -> {

                Label label = new Label(name);
                label.setAlignment(Pos.TOP_RIGHT);
                label.setTextAlignment(TextAlignment.RIGHT);
                label.setMaxWidth(Double.MAX_VALUE);
                label.setMinWidth(Region.USE_PREF_SIZE);

                TextField field;

                if (isText) {
                    field = new TextField();
                } else {
                    field = new IntegerField();
                }

                field.setText(addressParams.getString(index));
                field.setMaxWidth(Double.MAX_VALUE);

                field.textProperty().addListener(o -> addressParams.set(index, field.getText()));

                parameters.add(label, 2 * (index % 2), index / 2);
                parameters.add(field, 2 * (index % 2) + 1, index / 2);

                GridPane.setHgrow(label, Priority.NEVER);
                GridPane.setHgrow(field, Priority.ALWAYS);

            });

        });

    }

    public void connect() {

        if (driverChoice.getValue() == null || protocolChoice.getValue() == null || addressParams == null) {
            return;
        }

        connection.disconnect();

        GUI.runNow(() -> {
            errorText.setVisible(false);
            errorText.setManaged(false);
        });

        Address address = addressParams.createAddress();

        try {
            connecting = true;
            connection.setDriver(driverChoice.getValue());
            connection.setAddress(address);
            connection.connect();
        } catch (Exception e) {

            e.printStackTrace();

            GUI.runNow(() -> {
                errorText.setText("Error: " + e.getMessage());
                errorText.setVisible(true);
                errorText.setManaged(true);
            });

        } finally {
            connecting = false;
        }

    }

    public void apply() {


        GUI.runNow(() -> {
            errorText.setVisible(false);
            errorText.setManaged(false);
        });

        Address address = addressParams.createAddress();

        Util.runAsync(() -> {

            try {
                connecting = true;
                connection.setDriver(driverChoice.getValue());
                connection.setAddress(address);
                connection.connect();
            } catch (Exception e) {

                e.printStackTrace();

                GUI.runNow(() -> {
                    errorText.setText("Error: " + e.getMessage());
                    errorText.setVisible(true);
                    errorText.setManaged(true);
                });

            } finally {
                connecting = false;
            }

        });


    }

    public void browse() {

        Util.runAsync(() -> {
            Address selected = GUI.browseVISA();
            if (selected != null) {
                connection.setAddress(selected);
            }
        });

    }

    public Connection<T> getConnection() {
        return connection;
    }


}
