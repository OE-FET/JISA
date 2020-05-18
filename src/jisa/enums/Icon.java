package jisa.enums;

import javafx.scene.image.Image;
import jisa.Util;
import jisa.gui.GUI;

import java.net.URL;

public enum Icon {

    FLASK("images/experiment.png"),
    DEVICE("images/devices.png"),
    DASHBOARD("images/dashboard.png"),
    PLOT("images/results.png"),
    DATA("images/table.png"),
    VOLTMETER("images/meter.png"),
    THERMOMETER("images/thermometer.png"),
    SNOWFLAKE("images/frost.png"),
    LIGHTBULB("images/light.png"),
    WIFI("images/wifi.png"),
    CONNECTION("images/connection.png"),
    MAGNET("images/magnet.png"),
    WAVE("images/wave.png"),
    COGS("images/cogs.png"),
    DIODE("images/diode.png"),
    TRANSISTOR("images/transistor.png"),
    RHEOSTAT("images/rheostat.png"),
    LED("images/led.png"),
    RESISTOR("images/resistor.png");

    private String path;
    private Image white = null;
    private Image black = null;

    Icon(String path) {
        this.path = path;
    }

    public URL getURL() {
        return GUI.class.getResource(path);
    }

    public Image getWhiteImage() {
        if (white == null) white = new Image(GUI.class.getResource(path).toExternalForm());
        return white;
    }

    public Image getBlackImage() {
        if (black == null) black = Util.invertImage(getWhiteImage());
        return black;
    }

}
