package jisa.gui;

import com.sun.javafx.scene.control.IntegerField;
import javafx.scene.control.CheckBox;
import jisa.addresses.Address;
import jisa.visa.VISADevice;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.IOException;

public class DeviceShell extends JFXElement {

    public        ListView     terminal;
    public        TextField    input;
    public        IntegerField timeOut;
    public        TextField    writeTerm;
    public        TextField    readTerm;
    public        CheckBox     eoi;
    public        IntegerField baud;
    public        IntegerField data;
    private final Address      address;
    private       VISADevice   device = null;

    public DeviceShell(Address address) {

        super(String.format("Device Shell: %s", address.toString()), DeviceShell.class.getResource("fxml/DeviceShell.fxml"));

        this.address = address;

        GUI.runNow(() -> {
            input.setDisable(true);
            this.getStage().setOnCloseRequest((we) -> {
                if (device != null) {
                    addStatusLine("Closing connection...");
                    try {
                        device.close();
                        addSuccessLine("Connection closed.");
                    } catch (IOException e) {
                        addErrorLine(e.getMessage());
                    }
                }
                close();
            });
        });

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

    public void showAndWait() {
        GUI.runNow(() -> getStage().showAndWait());
    }

    public void updateParameters() {

        if (device == null) {
            addErrorLine("Error: Not Connected.");
            return;
        }

        try {

            int     timeout   = this.timeOut.getValue();
            String  writeTerm = this.writeTerm.getText();
            String  readTerm  = this.readTerm.getText();
            boolean eoi       = this.eoi.isSelected();
            int     baud      = this.baud.getValue();
            int     data      = this.data.getValue();

            device.setTimeout(timeout);
            device.setWriteTerminator(writeTerm.replace("\\n", "\n").replace("\\r", "\r"));
            device.setReadTerminator(readTerm.replace("\\n", "\n").replace("\\r", "\r"));
            device.configGPIB(gpib -> gpib.setEOIEnabled(eoi));
            device.configSerial(serial -> serial.setSerialParameters(baud, data));

            addSuccessLine("Instrument settings applied");

        } catch (Exception e) {
            addErrorLine(String.format("Error when updating settings: [%s] %s", e.getClass().getSimpleName(), e.getMessage()));
            GUI.showException(e);
        }

    }

}
