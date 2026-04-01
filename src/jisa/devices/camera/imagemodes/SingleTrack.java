package jisa.devices.camera.imagemodes;

import jisa.devices.DeviceException;
import jisa.devices.ParameterList;

import java.io.IOException;

public interface SingleTrack extends CameraImageMode {

    void setSingleTrackStart(int track) throws IOException, DeviceException;

    int getSingleTrackStart() throws IOException, DeviceException;

    void setSingleTrackHeight(int tracks) throws IOException, DeviceException;

    int getSingleTrackHeight() throws IOException, DeviceException;

    static void addParameters(SingleTrack inst, Class<?> target, ParameterList parameters) {

        parameters.addValue("Single-Track","Single Track Start", inst::getSingleTrackStart, 0, inst::setSingleTrackStart);
        parameters.addValue("Single-Track","Single Track Height", inst::getSingleTrackHeight, 1, inst::setSingleTrackHeight);

    }

}
