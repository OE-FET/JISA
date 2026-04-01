package jisa.devices.camera.imagemodes;

import jisa.devices.DeviceException;
import jisa.devices.ParameterList;

import java.io.IOException;

public interface TrackSequence extends CameraImageMode {

    void setTrackSequenceCount(int count) throws IOException, DeviceException;

    int getTrackSequenceCount() throws IOException, DeviceException;

    void setTrackSequenceHeight(int height) throws IOException, DeviceException;

    int getTrackSequenceHeight() throws IOException, DeviceException;

    void setTrackSequenceOffset(int offset) throws IOException, DeviceException;

    int getTrackSequenceOffset() throws IOException, DeviceException;

    static void addParameters(TrackSequence inst, Class<?> target, ParameterList parameters) {

        parameters.addValue("Track Sequence Count", inst::getTrackSequenceCount, 1, inst::setTrackSequenceCount);
        parameters.addValue("Track Sequence Height", inst::getTrackSequenceHeight, 1, inst::setTrackSequenceHeight);
        parameters.addValue("Track Sequence Offset", inst::getTrackSequenceOffset, 1, inst::setTrackSequenceOffset);

    }



}
