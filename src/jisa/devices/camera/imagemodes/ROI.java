package jisa.devices.camera.imagemodes;

import jisa.devices.DeviceException;
import jisa.devices.ParameterList;

import java.io.IOException;

public interface ROI extends CameraImageMode {

    public static void addParameters(ROI inst, Class<?> target, ParameterList parameters) {

        parameters.addValue("Region of Interest", "Width", inst::getImageWidth, 1024, inst::setImageWidth);
        parameters.addValue("Region of Interest", "Height", inst::getImageHeight, 1024, inst::setImageHeight);

        parameters.addAuto("Region of Interest", "X Offset", inst::isImageCentredX, false, inst::getImageOffsetX, 1, o -> inst.setImageCentredX(true), o -> {
            inst.setImageCentredX(false);
            inst.setImageOffsetX(o);
        });

        parameters.addAuto("Region of Interest", "Y Offset", inst::isImageCentredY, false, inst::getImageOffsetY, 1, o -> inst.setImageCentredY(true), o -> {
            inst.setImageCentredY(false);
            inst.setImageOffsetY(o);
        });

    }

    /**
     * Sets the width (in pixels) to use when ImageMode is set to IMAGE.
     *
     * @param width Width, in pixels.
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    void setImageWidth(int width) throws IOException, DeviceException;

    /**
     * Returns the width (in pixels) the camera is configured to use when ImageMode is set to IMAGE
     *
     * @return width, in pixels.
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    int getImageWidth() throws IOException, DeviceException;

    /**
     * Sets the height (in pixels) to use when when ImageMode is set to IMAGE.
     *
     * @param height Height, in pixels.
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    void setImageHeight(int height) throws IOException, DeviceException;

    /**
     * Returns the height (in pixels) the camera is configured to use when ImageMode is set to IMAGE
     *
     * @return height, in pixels.
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    int getImageHeight() throws IOException, DeviceException;


    /**
     * Returns which physical pixel column on the sensor is the left-most column used for acquiring frames.
     *
     * @return X-Offset of image, in physical pixels.
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    int getImageOffsetX() throws IOException, DeviceException;

    /**
     * Sets which physical pixel column on the sensor is the left-most column used for acquiring frames.
     *
     * @param offsetX X-Offset of image, in physical pixels.
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    void setImageOffsetX(int offsetX) throws IOException, DeviceException;

    /**
     * Sets whether the offset set using setFrameOffsetX() is ignored and the frame instead automatically
     * centred in x on the sensor.
     *
     * @param centredX Centre in the x direction?
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    void setImageCentredX(boolean centredX) throws IOException, DeviceException;

    /**
     * Returns whether the offset set using setFrameOffsetX() is ignored and the frame instead automatically
     * centred in x on the sensor.
     *
     * @return Centre in the x direction?
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    boolean isImageCentredX() throws IOException, DeviceException;

    /**
     * Returns the y co-ordinate of the top-most pixel used for capturing images.
     *
     * @return Y-Offset of image, in pixels.
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    int getImageOffsetY() throws IOException, DeviceException;

    /**
     * Sets the y co-ordinate of the top-most pixel used for capturing images.
     *
     * @param offsetY Y-Offset for images, in pixels.
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    void setImageOffsetY(int offsetY) throws IOException, DeviceException;

    /**
     * Sets whether the offset set using setFrameOffsetY() is ignored and the frame instead automatically
     * centred in y on the sensor.
     *
     * @param centredY Centre in the x direction?
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    void setImageCentredY(boolean centredY) throws IOException, DeviceException;

    /**
     * Returns whether the offset set using setFrameOffsetY() is ignored and the frame instead automatically
     * centred in y on the sensor.
     *
     * @return Centre in the y direction?
     * @throws IOException     Upon communications error
     * @throws DeviceException Upon device compatibility error
     */
    boolean isImageCentredY() throws IOException, DeviceException;

}
