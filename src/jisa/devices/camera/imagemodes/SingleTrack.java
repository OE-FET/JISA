package jisa.devices.camera.imagemodes;

import jisa.devices.DeviceException;
import jisa.devices.ParameterList;

import java.io.IOException;

public interface SingleTrack extends CameraImageMode {

    static void addParameters(SingleTrack inst, Class<?> target, ParameterList parameters) {

        parameters.addValue("Single-Track","Centre", inst::getSingleTrackCentre, 0, inst::setSingleTrackCentre);
        parameters.addValue("Single-Track","Height", inst::getSingleTrackHeight, 1, inst::setSingleTrackHeight);

    }

    void setSingleTrackCentre(int track) throws IOException, DeviceException;

    int getSingleTrackCentre() throws IOException, DeviceException;

    void setSingleTrackHeight(int tracks) throws IOException, DeviceException;

    int getSingleTrackHeight() throws IOException, DeviceException;

}
