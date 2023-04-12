package jisa.gui;

import com.github.sarxos.webcam.Webcam;
import javafx.scene.image.Image;
import jisa.addresses.*;
import jisa.enums.Icon;
import jisa.visa.VISA;
import jisa.visa.VISADevice;

import java.net.URL;
import java.util.List;

public class VISABrowser extends ListDisplay<Address> {

    private final Button refresh = addToolbarButton("Refresh", this::updateList);

    public VISABrowser(String title) {
        super(title);
        setWindowSize(800, 600);
    }

    public void updateList() {

        refresh.setDisabled(true);
        refresh.setText("Scanning...");
        clear();

        try {

            List<Address> addresses = VISA.listInstruments();

            for (Address address : addresses) {

                URL icon;

                if (address instanceof GPIBAddress) {
                    icon = VISABrowser.class.getResource("images/gpib.png");
                } else if (address instanceof USBAddress){
                    icon = VISABrowser.class.getResource("images/usb.png");
                } else if (address instanceof LXIAddress || address instanceof TCPIPAddress) {
                    icon = VISABrowser.class.getResource("images/tcpip.png");
                } else if (address instanceof SerialAddress || address instanceof ModbusAddress) {
                    icon = VISABrowser.class.getResource("images/serial.png");
                } else {
                    icon = VISABrowser.class.getResource("images/smu.png");
                }

                String name;
                try {
                    VISADevice device = new VISADevice(address);
                    device.setTimeout(250);
                    device.setRetryCount(1);
                    device.setReadTerminator("\n");
                    device.setWriteTerminator("\r\n");
                    device.addAutoRemove("\r");
                    device.addAutoRemove("\n");
                    name = device.getIDN();
                    device.close();
                } catch (Exception e) {
                    name = "Unidentified Device";
                }

                add(address, name, address.getJISAString(), new Image(icon.toExternalForm()));

            }

            try {
                for (Webcam webcam : Webcam.getWebcams()) {
                    IDAddress address = new IDAddress(webcam.getName());
                    add(address, "Webcam: " + webcam.getName(), address.getJISAString(), Icon.DEVICE.getBlackImage());
                }
            } catch (Exception ignored) {}

        } catch (Exception e) {
            e.printStackTrace();
            GUI.errorAlert(e.getMessage());
        } finally {
            refresh.setDisabled(false);
            refresh.setText("Refresh");
        }

    }

    public Address getSelectedAddress() {
        return getSelected() != null ? getSelected().getObject() : null;
    }

    public Address selectAddress() {

        (new Thread(this::updateList)).start();

        if (showAsConfirmation() && getSelectedAddress() != null) {
            return getSelectedAddress();
        } else {
            return null;
        }

    }

}
