package jisa.devices.spectrometer.nat;

import jisa.Util;
import jisa.visa.Library;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public interface ATSpectrograph extends Library {

    int ERROR_CODE_COMMUNICATION_ERROR = 20201;
    int ERROR_CODE_SUCCESS             = 20202;
    int ERROR_CODE_ERROR               = 20249;
    int ERROR_CODE_P1INVALID           = 20266;
    int ERROR_CODE_P2INVALID           = 20267;
    int ERROR_CODE_P3INVALID           = 20268;
    int ERROR_CODE_P4INVALID           = 20269;
    int ERROR_CODE_P5INVALID           = 20270;
    int ERROR_CODE_NOT_INITIALIZED     = 20275;
    int ERROR_CODE_NOT_AVAILABLE       = 20292;

    int INPUT_FLIPPER  = 1;
    int OUTPUT_FLIPPER = 2;

    int PORT_DIRECT = 0;
    int PORT_SIDE   = 1;

    int SLIT_INPUT_SIDE    = 1;
    int SLIT_INPUT_DIRECT  = 2;
    int SLIT_OUTPUT_SIDE   = 3;
    int SLIT_OUTPUT_DIRECT = 4;

    int SHUTTER_CLOSED = 0;
    int SHUTTER_OPEN   = 1;
    int SHUTTER_BNC    = 2;


    default void initialise() throws Exception {

        ATSpectrographInitialize("");
        Util.addShutdownHook(this::ATSpectrographClose);

    }

    int ATSpectrographInitialize(String iniPath);

    int ATSpectrographClose();

    int ATSpectrographGetNumberDevices(IntBuffer noDevices);

    int ATSpectrographGetFunctionReturnDescription(int error, ByteBuffer description, int maxDescStrLen);

    int ATSpectrographGetSerialNumber(int device, ByteBuffer serial, int maxSerialStrLen);

    int ATSpectrographEepromSetOpticalParams(int device, float focalLength, float angularDeviation, float focalTilt);

    int ATSpectrographEepromGetOpticalParams(int device, FloatBuffer focalLength, FloatBuffer angularDeviation, FloatBuffer focalTilt);

    int ATSpectrographGetNumberGratings(int device, IntBuffer noGratings);

    int ATSpectrographSetGrating(int device, int grating);

    int ATSpectrographGetGrating(int device, IntBuffer grating);

    int ATSpectrographGetGratingInfo(int device, int grating, FloatBuffer lines, ByteBuffer blaze, int maxBlazeStrLen, IntBuffer home, IntBuffer offset);

    int ATSpectrographGratingIsPresent(int device, IntBuffer present);

    int ATSpectrographSetDetectorOffset(int device, int entrancePort, int exitPort, int offset);

    int ATSpectrographGetDetectorOffset(int device, int entrancePort, int exitPort, IntBuffer offset);

    int ATSpectrographSetGratingOffset(int device, int grating, int offset);

    int ATSpectrographGetGratingOffset(int device, int grating, IntBuffer offset);

    int ATSpectrographSetTurret(int device, int turret);

    int ATSpectrographGetTurret(int device, IntBuffer turret);

    int ATSpectrographWavelengthIsPresent(int device, IntBuffer present);

    int ATSpectrographWavelengthReset(int device);

    int ATSpectrographSetWavelength(int device, float wavelength);

    int ATSpectrographGetWavelength(int device, FloatBuffer wavelength);

    int ATSpectrographGotoZeroOrder(int device);

    int ATSpectrographAtZeroOrder(int device, IntBuffer atZeroOrder);

    int ATSpectrographGetWavelengthLimits(int device, int grating, FloatBuffer min, FloatBuffer max);

    int ATSpectrographSlitIsPresent(int device, int slit, IntBuffer present);

    int ATSpectrographSlitReset(int device, int slit);

    int ATSpectrographSetSlitWidth(int device, int slit, float width);

    int ATSpectrographGetSlitWidth(int device, int slit, FloatBuffer width);

    int ATSpectrographSetSlitZeroPosition(int device, int slit, int offset);

    int ATSpectrographGetSlitZeroPosition(int device, int slit, IntBuffer offset);

    int ATSpectrographSetSlitCoefficients(int device, int x1, int y1, int x2, int y2);

    int ATSpectrographGetSlitCoefficients(int device, IntBuffer x1, IntBuffer y1, IntBuffer x2, IntBuffer y2);

    int ATSpectrographShutterIsPresent(int device, IntBuffer present);

    int ATSpectrographIsShutterModePossible(int device, int mode, IntBuffer possible);

    int ATSpectrographSetShutter(int device, int mode);

    int ATSpectrographGetShutter(int device, IntBuffer mode);

    int ATSpectrographFilterIsPresent(int device, IntBuffer present);

    int ATSpectrographFilterReset(int device);

    int ATSpectrographSetFilter(int device, int filter);

    int ATSpectrographGetFilter(int device, IntBuffer filter);

    int ATSpectrographGetFilterInfo(int device, int Filter, ByteBuffer info, int maxInfoLen);

    int ATSpectrographSetFilterInfo(int device, int Filter, ByteBuffer info);

    int ATSpectrographFlipperMirrorIsPresent(int device, int flipper, IntBuffer present);

    int ATSpectrographFlipperMirrorReset(int device, int flipper);

    int ATSpectrographSetFlipperMirror(int device, int flipper, int port);

    int ATSpectrographGetFlipperMirror(int device, int flipper, IntBuffer port);

    int ATSpectrographSetFlipperMirrorPosition(int device, int flipper, int position);

    int ATSpectrographGetFlipperMirrorPosition(int device, int flipper, IntBuffer position);

    int ATSpectrographGetFlipperMirrorMaxPosition(int device, int flipper, IntBuffer maxPosition);

    int ATSpectrographGetCCDLimits(int device, int port, FloatBuffer low, FloatBuffer high);

    int ATSpectrographAccessoryIsPresent(int device, IntBuffer present);

    int ATSpectrographSetAccessoryState(int device, int accessory, int state);

    int ATSpectrographGetAccessoryState(int device, int accessory, IntBuffer state);

    int ATSpectrographFocusMirrorIsPresent(int device, IntBuffer present);

    int ATSpectrographFocusMirrorReset(int device);

    int ATSpectrographSetFocusMirror(int device, int focus);

    int ATSpectrographGetFocusMirror(int device, IntBuffer focus);

    int ATSpectrographGetFocusMirrorMaxSteps(int device, IntBuffer steps);

    int ATSpectrographSetPixelWidth(int device, float width);

    int ATSpectrographGetPixelWidth(int device, FloatBuffer width);

    int ATSpectrographSetNumberPixels(int device, int numberPixels);

    int ATSpectrographGetNumberPixels(int device, IntBuffer numberPixels);

    int ATSpectrographGetCalibration(int device, FloatBuffer calibrationValues, int numberPixels);

    int ATSpectrographGetPixelCalibrationCoefficients(int device, FloatBuffer A, FloatBuffer B, FloatBuffer C, FloatBuffer D);

    int ATSpectrographIrisIsPresent(int device, int iris, IntBuffer present);

    int ATSpectrographSetIris(int device, int iris, int value);

    int ATSpectrographGetIris(int device, int iris, IntBuffer value);

}
