package jisa.gui;

import javafx.scene.image.Image;
import jisa.addresses.Address;
import jisa.visa.VISA;
import jisa.visa.VISADevice;

import java.net.URL;

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

            Address[] addresses = VISA.getInstruments();

            for (Address address : addresses) {

                URL icon;

                switch (address.getType()) {

                    case GPIB:
                        icon = VISABrowser.class.getResource("images/gpib.png");
                        break;

                    case USB:
                        icon = VISABrowser.class.getResource("images/usb.png");
                        break;

                    case TCPIP:
                    case TCPIP_SOCKET:
                        icon = VISABrowser.class.getResource("images/tcpip.png");
                        break;

                    case SERIAL:
                    case MODBUS:
                    case COM:
                        icon = VISABrowser.class.getResource("images/serial.png");
                        break;

                    default:
                        icon = VISABrowser.class.getResource("images/smu.png");
                        break;

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
                } catch (Exception e) {
                    name = "Unidentified Device";
                }

                add(address, name, address.toString(), new Image(icon.toExternalForm()));

            }

        } catch (Exception e) {
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
