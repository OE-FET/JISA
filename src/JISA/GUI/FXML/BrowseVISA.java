package JISA.GUI.FXML;

import JISA.Addresses.*;
import JISA.VISA.VISA;
import JISA.VISA.VISADevice;
import JISA.VISA.VISAException;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class BrowseVISA {

    public  VBox                  list;
    public  Label                 searching;
    private ArrayList<StrAddress> found = new ArrayList<>();
    private AddrHandler           onOkay;
    private Stage                 stage;

    public static BrowseVISA create(String title) {

        try {
            FXMLLoader loader     = new FXMLLoader(TableWindow.class.getResource("browseVISA.fxml"));
            Parent     root       = loader.load();
            Scene      scene      = new Scene(root);
            BrowseVISA controller = (BrowseVISA) loader.getController();
            Semaphore  s          = new Semaphore(0);
            Platform.runLater(() -> {
                Stage stage = new Stage();
                stage.setOnCloseRequest((ae) -> {
                    controller.cancel();
                });
                controller.stage = stage;
                stage.setTitle(title);
                stage.setScene(scene);
                s.release();
            });
            s.acquire();
            return controller;
        } catch (IOException | InterruptedException ignored) {
        }

        return null;

    }

    public interface AddrHandler {

        public void onOkay(StrAddress address);

    }

    public void search(AddrHandler onOkay) {

        stage.show();

        list.getChildren().clear();
        searching.setText("Searching...");

        found.clear();

        this.onOkay = onOkay;

        Thread t = new Thread(() -> {
            int count = 0;
            try {
                for (StrAddress a : VISA.getInstruments()) {
                    String[] addr = a.getVISAAddress().trim().split("::");
                    if (!addr[addr.length - 1].trim().equals("INSTR")) {
                        continue;
                    }
                    try {
                        addInstrument(a);
                        count++;
                    } catch (IOException e) {

                    }
                }
            } catch (VISAException e) {
                e.printStackTrace();
            }
            final int c = count;
            Platform.runLater(() -> {
                searching.setText(String.format("Done, found %d instruments.", c));
            });
        });

        t.start();

    }

    public void addInstrument(StrAddress address) throws IOException {

        String i = "Unknown Instrument";
        try {
            VISADevice device = new VISADevice(address);
            device.setTimeout(100);
            i = device.getIDN();
        } catch (Exception e) {
            i = "Unknown Instrument";
        }

        final String idn = i;

        Platform.runLater(() -> {

            // Create the outer box
            BorderPane outer = new BorderPane();
            outer.setStyle("-fx-background-color: white; -fx-background-radius: 5px");
            outer.setEffect(new DropShadow(10, new Color(0, 0, 0, 0.25)));

            // Create the title bar
            HBox top = new HBox();
            top.setAlignment(Pos.CENTER_LEFT);
            top.setPadding(new Insets(10, 10, 10, 10));
            top.setStyle("-fx-background-color: #4c4c4c; -fx-background-radius: 5px 5px 0 0;");

            // Title label needs to go inside a panel since labels do not grow to fill space
            Pane  tPane = new Pane();
            Label title = new Label();
            title.setTextFill(Color.WHITE);
            title.setFont(new Font("System Bold", 14));
            tPane.getChildren().add(title);

            // Add title to title bar
            top.getChildren().add(tPane);
            HBox.setHgrow(tPane, Priority.ALWAYS);
            ImageView image = new ImageView();
            image.setFitHeight(32);
            image.setFitWidth(32);

            HBox hBox = new HBox();
            hBox.setAlignment(Pos.CENTER_LEFT);
            hBox.setSpacing(15);
            hBox.setPadding(new Insets(15, 15, 15, 15));
            hBox.getChildren().add(image);
            outer.setTop(top);
            outer.setCenter(hBox);

            VBox vBox = new VBox();
            vBox.setSpacing(5);

            hBox.getChildren().add(vBox);

            Label name     = new Label();
            Label location = new Label();

            String prot = "Unknown";
            String uri  = address.getVISAAddress();

            switch (address.getType()) {

                case GPIB:
                    prot = "GPIB";
                    GPIBAddress g = address.toGPIBAddress();
                    if (g == null) {
                        g = new GPIBAddress(0, 0);
                    }
                    uri = String.format("Board %d, Address %d", g.getBus(), g.getAddress());
                    image.setImage(new Image("/JISA/GUI/Images/gpib.png"));
                    break;

                case TCPIP:
                    prot = "TCP-IP";
                    TCPIPAddress t = address.toTCPIPAddress();
                    if (t == null) {
                        t = new TCPIPAddress("Unknown");
                    }
                    uri = t.getHost();
                    image.setImage(new Image("/JISA/GUI/Images/tcpip.png"));
                    break;
                case USB:
                    prot = "USB-TMC";
                    USBAddress u = address.toUSBAddress();
                    if (u == null) {
                        u = new USBAddress("0", "0", "0");
                    }
                    uri = String.format("VendorID: %s, ProductID: %s", u.getManufacturer(), u.getModel());
                    image.setImage(new Image("/JISA/GUI/Images/usb.png"));
                    break;
                case SERIAL:
                    prot = "Serial";
                    SerialAddress s = address.toSerialAddress();
                    if (s == null) {
                        s = new SerialAddress(0);
                    }
                    uri = String.format("Port %d", s.getBoard());
                    image.setImage(new Image("/JISA/GUI/Images/serial.png"));
                    break;
                default:
                    prot = "Unknown";
                    uri = address.getVISAAddress();
                    image.setImage(new Image("/JISA/GUI/Images/serial.png"));
                    break;


            }

            vBox.getChildren().addAll(name, location);
            HBox.setHgrow(image, Priority.NEVER);
            HBox.setHgrow(vBox, Priority.ALWAYS);
            title.setText(address.getVISAAddress());

            name.setText(idn);
            location.setText(String.format("%s: %s", prot, uri));

            Button select = new Button("Select >");
            select.setOnAction((ae) -> {
                onOkay.onOkay(address);
                close();
            });

            hBox.getChildren().add(select);

            list.getChildren().add(outer);
            found.add(address);
        });

    }

    public void okay() {

        int selected = 0;
        onOkay.onOkay(found.get(selected));
        close();

    }

    public void cancel() {
        onOkay.onOkay(null);
        close();
    }

    public void close() {
        ((Stage) list.getScene().getWindow()).close();
    }

}
