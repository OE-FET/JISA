package jisa.devices.camera.imagemodes;

import jisa.devices.ParameterList;

public interface FullVerticalBinning extends CameraImageMode {

    interface WithCropping extends FullVerticalBinning {

        static void addParameters(WithCropping inst, Class<?> target, ParameterList parameters) {

            parameters.addValue("Full Vertical Binning", "Offset X", inst::getFullVerticalBinningOffsetX, 0, inst::setFullVerticalBinningOffsetX);
            parameters.addValue("Full Vertical Binning", "Width", inst::getFullVerticalBinningWidth, 1, inst::setFullVerticalBinningWidth);

        }

        int getFullVerticalBinningOffsetX();

        void setFullVerticalBinningOffsetX(int value);

        int getFullVerticalBinningWidth();

        void setFullVerticalBinningWidth(int value);

    }

}
