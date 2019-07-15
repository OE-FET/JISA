package jisa.gui;

import jisa.addresses.*;
import jisa.Util;
import jisa.visa.VISA;
import jisa.visa.VISADevice;
import jisa.visa.VISAException;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
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
    public  ProgressBar           progBar;
    private ArrayList<StrAddress> found = new ArrayList<>();
    private AddrHandler           onOkay;
    private Stage                 stage;

    public BrowseVISA(String title) {

        try {

            FXMLLoader loader = new FXMLLoader(BrowseVISA.class.getResource("fxml/browseVISA.fxml"));
            loader.setController(this);
            Parent    root  = loader.load();
            Scene     scene = new Scene(root);
            Semaphore s     = new Semaphore(0);
            Platform.runLater(() -> {
                Stage stage = new Stage();
                stage.setTitle(title);
                stage.setScene(scene);
                this.stage = stage;
                s.release();
                this.stage.setOnCloseRequest((we) -> {
                    cancel();
                });
            });
            s.acquire();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public interface AddrHandler {

        void onOkay(StrAddress address);

    }

    public void search(AddrHandler onOkay) {

        stage.show();
        progBar.setVisible(true);
        progBar.setManaged(true);

        list.getChildren().clear();
        searching.setText("Searching...");

        found.clear();

        this.onOkay = onOkay;

        Thread t = new Thread(() -> {
            int count = 0;
            try {
                for (StrAddress a : VISA.getInstruments()) {
                    String[] addr   = a.toString().trim().split("::");
                    String   ending = addr[addr.length - 1].trim();
                    if (ending.equals("INTFC")) {
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
                progBar.setVisible(false);
                progBar.setManaged(false);
            });
        });

        t.start();

    }

    public void addInstrument(StrAddress address) throws IOException {

        String i = "Unknown Instrument";
        VISADevice device = null;
        try {
            device = new VISADevice(address);
            device.setTimeout(100);
            device.setRetryCount(1);
            Util.sleep(1500);
            i = device.query("*IDN?\n").trim().replace("\n", "").replace("\r", "");
        } catch (Exception e) {
            i = "Unknown Instrument";
        } finally {
            if (device != null) {
                device.close();
            }
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
            String uri  = address.toString();

            switch (address.getType()) {

                case GPIB:

                    prot = "GPIB";
                    GPIBAddress g = address.toGPIBAddress();
                    if (g == null) {
                        g = new GPIBAddress(0, 0);
                    }
                    uri = String.format("Board %d, Address %d", g.getBus(), g.getAddress());
                    image.setImage(new Image(getClass().getResource("Images/gpib.png").toString()));
                    break;

                case TCPIP:

                    prot = "TCP-IP";
                    TCPIPAddress t = address.toTCPIPAddress();
                    if (t == null) {
                        t = new TCPIPAddress("Unknown");
                    }
                    uri = t.getHost();
                    image.setImage(new Image(getClass().getResource("Images/tcpip.png").toString()));
                    break;

                case TCPIP_SOCKET:

                    prot = "TCP-IP Socket";
                    TCPIPSocketAddress ts = address.toTCPIPSocketAddress();
                    if (ts == null) {
                        ts = new TCPIPSocketAddress("Unknown", 0);
                    }
                    uri = ts.getHost() + ":" + ts.getPort();
                    image.setImage(new Image(getClass().getResource("Images/tcpip.png").toString()));
                    break;

                case USB:

                    prot = "USB-TMC";
                    USBAddress u = address.toUSBAddress();
                    if (u == null) {
                        u = new USBAddress(0, 0, "0");
                    }
                    uri = String.format("VendorID: %s, ProductID: %s", u.getManufacturer(), u.getModel());
                    image.setImage(new Image(getClass().getResource("Images/usb.png").toString()));
                    break;

                case SERIAL:

                    prot = "Serial (VISA)";
                    SerialAddress s = address.toSerialAddress();
                    if (s == null) {
                        s = new SerialAddress(0);
                    }
                    uri = String.format("COM %d", s.getBoard());
                    image.setImage(new Image(getClass().getResource("Images/serial.png").toString()));
                    break;

                case COM:

                    prot = "Serial (Native)";
                    COMAddress ca = address.toCOMAddress();
                    if (ca == null) {
                        ca = new COMAddress("null");
                    }

                    uri = ca.getDevice();
                    image.setImage(new Image(getClass().getResource("Images/serial.png").toString()));
                    break;

                default:
                    prot = "Unknown";
                    uri = address.toString();
                    image.setImage(new Image(getClass().getResource("Images/serial.png").toString()));
                    break;


            }

            vBox.getChildren().addAll(name, location);
            HBox.setHgrow(image, Priority.NEVER);
            HBox.setHgrow(vBox, Priority.ALWAYS);
            title.setText(address.toString());

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
