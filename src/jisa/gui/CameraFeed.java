package jisa.gui;

import com.github.sarxos.webcam.Webcam;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import jisa.control.RTask;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.Camera;

import java.io.IOException;
import java.util.List;

public class CameraFeed extends JFXElement {

    @FXML
    protected ImageView  imageView;
    @FXML
    protected ScrollPane scroll;

    private final Camera camera;
    private final RTask  frameGrabber = new RTask((long) (1e3 / 30), task -> updateFrame());

    public CameraFeed(String title, Camera camera) {

        super(title, CameraFeed.class.getResource("fxml/Webcam.fxml"));

        this.camera = camera;

        imageView.fitWidthProperty().bind(scroll.widthProperty().subtract(2));
        imageView.fitHeightProperty().bind(scroll.heightProperty().subtract(2));

    }

    public Camera getCamera() {
        return camera;
    }

    public void stop() throws IOException, DeviceException {

        if (camera != null) {
            frameGrabber.stop();
            camera.turnOff();
        }

    }

    public void start() throws IOException, DeviceException {

        if (camera != null) {
            camera.turnOn();
            frameGrabber.setInterval((long) (1e3 / 30));
            frameGrabber.start();
        }

    }

    private void updateFrame() {
        GUI.runNow(() -> {
            try {
                imageView.setImage(camera.getImage());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }

}
