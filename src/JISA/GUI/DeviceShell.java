package JISA.GUI;

import JISA.Addresses.Address;
import JISA.VISA.VISADevice;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;

public class DeviceShell {

    public  ListView   terminal;
    public  TextField  input;
    private Stage      stage;
    private Address    address;
    private VISADevice device = null;

    public DeviceShell(Address address) {

        this.address = address;

        try {

            FXMLLoader loader = new FXMLLoader(DeviceShell.class.getResource("FXML/DeviceShell.fxml"));
            loader.setController(this);
            Parent root  = loader.load();
            Scene  scene = new Scene(root);
            GUI.runNow(() -> {
                Stage stage = new Stage();
                stage.setTitle(String.format("Device Shell: %s", address.toString()));
                stage.setScene(scene);
                this.stage = stage;
                input.setDisable(true);
                this.stage.setOnCloseRequest((we) -> {
                    if (device != null) {
                        addStatusLine("Closing connection...");
                        try {
                            device.close();
                            addSuccessLine("Connection closed.");
                        } catch (IOException e) {
                            addErrorLine(e.getMessage());
                        }
                    }
                    stage.close();
                });
            });

        } catch (IOException ignored) {

        }

    }

    public void connect() {

        input.setDisable(true);
        addStatusLine("Connecting to device...");

        try {
            device = new VISADevice(address);
            device.setTimeout(500);
        } catch (IOException e) {
            addErrorLine(e.getMessage());
            return;
        }

        addSuccessLine("Successfully Connected.");
        input.setDisable(false);

    }

    public void textEnter() {

        if (input.getText().contains("?")) {
            queryLine();
        } else {
            writeLine();
        }

    }

    @FXML
    public void writeLine() {

        String line = input.getText();
        input.setText("");
        try {
            device.write(line);
            addInputLine(line);
        } catch (Exception e) {
            addErrorLine(e.getMessage());
        }

    }

    public void readLine() {

        try {
            addReturnLine(device.read(1));
        } catch (Exception e) {
            addErrorLine(e.getMessage());
        }

    }

    public void queryLine() {
        writeLine();
        readLine();
    }

    private void addStatusLine(String text) {


        Label l = new Label(text);
        l.setFont(new Font("Monospaced Bold", 12));
        l.setTextFill(Color.BLUE);

        terminal.getItems().add(l);

    }

    private void addSuccessLine(String text) {

        Label l = new Label(text);
        l.setFont(new Font("Monospaced Bold", 12));
        l.setTextFill(Color.GREEN);

        terminal.getItems().add(l);
    }

    private void addErrorLine(String text) {

        Label l = new Label(text);
        l.setFont(new Font("Monospaced Bold", 12));
        l.setTextFill(Color.RED);

        terminal.getItems().add(l);

    }

    private void addInputLine(String text) {

        Label l = new Label(text);
        l.setFont(new Font("Monospaced Italic", 12));
        l.setTextFill(Color.TEAL);

        terminal.getItems().add(l);

    }

    private void addReturnLine(String text) {

        Label l = new Label(text);
        l.setFont(new Font("Monospaced", 12));
        l.setTextFill(Color.BLACK);

        terminal.getItems().add(l);

    }

    public void show() {
        GUI.runNow(() -> {
            stage.show();
        });
    }

    public void showAndWait() {
        GUI.runNow(() -> {
            stage.showAndWait();
        });
    }

    public void hide() {
        GUI.runNow(() -> {
            stage.hide();
        });
    }

    public void close() {
        GUI.runNow(() -> {
            stage.close();
        });
    }

}
