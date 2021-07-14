package jisa.devices.interfaces;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import jisa.devices.DeviceException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Instrument interface for implementing cameras.
 */
public interface Camera extends Instrument {

    /**
     * Turns the camera on (starts capturing).
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device incompatibility
     */
    void turnOn() throws IOException, DeviceException;

    /**
     * Turns the camera off (stops capturing).
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device incompatibility
     */
    void turnOff() throws IOException, DeviceException;

    /**
     * Sets whether the camera is on (capturing) or off (not capturing).
     *
     * @param on Should it be on?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device incompatibility
     */
    default void setOn(boolean on) throws IOException, DeviceException {

        if (on) {
            turnOn();
        } else {
            turnOff();
        }

    }

    /**
     * Returns whether the camera is switched on or off.
     *
     * @return Is it on?
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device incompatibility
     */
    boolean isOn() throws IOException, DeviceException;

    /**
     * Returns the resolution mode the camera is currently using.
     *
     * @return Resolution mode
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device incompatibility
     */
    Mode getMode() throws IOException, DeviceException;

    /**
     * Sets the resolution mode the camera is to use.
     *
     * @param mode Mode to use
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device incompatibility
     */
    void setMode(Mode mode) throws IOException, DeviceException;

    /**
     * Returns how many frames the camera is/can capture each second.
     *
     * @return Current framerate
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device incompatibility
     */
    double getFrameRate() throws IOException, DeviceException;

    /**
     * Captures a frame from the camera, returning it as a BufferedImage object.
     *
     * @return Captured frame
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device incompatibility
     */
    BufferedImage getBufferedImage() throws IOException, DeviceException;

    /**
     * Captures a frame from the camera, returning it as a JavaFX Image object.
     *
     * @return Captured frame
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device incompatibility
     */
    default Image getImage() throws IOException, DeviceException {
        return SwingFXUtils.toFXImage(getBufferedImage(), null);
    }

    /**
     * Captures a frame from the camera, returning it as a JISA Camera.Frame object.
     *
     * @return Captured frame
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device incompatibility
     */
    default Frame captureFrame() throws IOException, DeviceException {
        return new Frame(getBufferedImage());
    }

    /**
     * Returns a list of all available resolution modes for the camera.
     *
     * @return List of modes
     *
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device incompatibility
     */
    List<Mode> getModes() throws IOException, DeviceException;

    class Frame {

        private final BufferedImage image;

        public Frame(BufferedImage image) {
            this.image = image;
        }

        /**
         * Saves the captured camera frame as a PNG file.
         *
         * @param path Path to write image to
         *
         * @throws IOException Upon error writing to file
         */
        public void savePNG(String path) throws IOException {
            ImageIO.write(image, "png", new File(path));
        }

        /**
         * Saves the captured camera frame as a JPEG file.
         *
         * @param path Path to write image to
         *
         * @throws IOException Upon error writing to file
         */
        public void saveJPG(String path) throws IOException {
            ImageIO.write(image, "jpg", new File(path));
        }

        /**
         * Returns the camera frame as a JavaFX Image object.
         *
         * @return JavaFX Image object
         */
        public Image getFXImage() {
            return SwingFXUtils.toFXImage(image, null);
        }

        /**
         * Returns the camera frame as a BufferedImage object.
         *
         * @return BufferedImage object
         */
        public BufferedImage getBufferedImage() {
            return image;
        }

    }

    class Mode {

        private final int xResolution;
        private final int yResolution;

        public Mode(int xResolution, int yResolution) {
            this.xResolution = xResolution;
            this.yResolution = yResolution;
        }

        public int getXResolution() {
            return xResolution;
        }

        public int getYResolution() {
            return yResolution;
        }

        public int getPixelCount() {
            return getXResolution() * getYResolution();
        }

    }

}
