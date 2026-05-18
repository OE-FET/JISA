package jisa.devices.spectrometer.nat;

import jisa.Util;
import jisa.visa.Library;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public interface ShamrockSDK extends Library {

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

        ShamrockInitialize("");
        Util.addShutdownHook(this::ShamrockClose);

    }

    int ShamrockInitialize(String iniPath);

    int ShamrockClose();

    int ShamrockGetNumberDevices(IntBuffer noDevices);

    int ShamrockGetFunctionReturnDescription(int error, ByteBuffer description, int maxDescStrLen);

    int ShamrockGetSerialNumber(int device, ByteBuffer serial, int maxSerialStrLen);

    int ShamrockEepromSetOpticalParams(int device, float focalLength, float angularDeviation, float focalTilt);

    int ShamrockEepromGetOpticalParams(int device, FloatBuffer focalLength, FloatBuffer angularDeviation, FloatBuffer focalTilt);

    int ShamrockGetNumberGratings(int device, IntBuffer noGratings);

    int ShamrockSetGrating(int device, int grating);

    int ShamrockGetGrating(int device, IntBuffer grating);

    int ShamrockGetGratingInfo(int device, int grating, FloatBuffer lines, ByteBuffer blaze, int maxBlazeStrLen, IntBuffer home, IntBuffer offset);

    int ShamrockGratingIsPresent(int device, IntBuffer present);

    int ShamrockSetDetectorOffset(int device, int entrancePort, int exitPort, int offset);

    int ShamrockGetDetectorOffset(int device, int entrancePort, int exitPort, IntBuffer offset);

    int ShamrockSetGratingOffset(int device, int grating, int offset);

    int ShamrockGetGratingOffset(int device, int grating, IntBuffer offset);

    int ShamrockSetTurret(int device, int turret);

    int ShamrockGetTurret(int device, IntBuffer turret);

    int ShamrockWavelengthIsPresent(int device, IntBuffer present);

    int ShamrockWavelengthReset(int device);

    int ShamrockSetWavelength(int device, float wavelength);

    int ShamrockGetWavelength(int device, FloatBuffer wavelength);

    int ShamrockGotoZeroOrder(int device);

    int ShamrockAtZeroOrder(int device, IntBuffer atZeroOrder);

    int ShamrockGetWavelengthLimits(int device, int grating, FloatBuffer min, FloatBuffer max);

    int ShamrockAutoSlitIsPresent(int device, int slit, IntBuffer present);

    int ShamrockAutoSlitReset(int device, int slit);

    int ShamrockSetAutoSlitWidth(int device, int slit, float width);

    int ShamrockGetAutoSlitWidth(int device, int slit, FloatBuffer width);

    int ShamrockSetSlitZeroPosition(int device, int slit, int offset);

    int ShamrockGetSlitZeroPosition(int device, int slit, IntBuffer offset);

    int ShamrockSetAutoSlitCoefficients(int device, int x1, int y1, int x2, int y2);

    int ShamrockGetAutoSlitCoefficients(int device, IntBuffer x1, IntBuffer y1, IntBuffer x2, IntBuffer y2);

    int ShamrockShutterIsPresent(int device, IntBuffer present);

    int ShamrockIsShutterModePossible(int device, int mode, IntBuffer possible);

    int ShamrockSetShutter(int device, int mode);

    int ShamrockGetShutter(int device, IntBuffer mode);

    int ShamrockFilterIsPresent(int device, IntBuffer present);

    int ShamrockFilterReset(int device);

    int ShamrockSetFilter(int device, int filter);

    int ShamrockGetFilter(int device, IntBuffer filter);

    int ShamrockGetFilterInfo(int device, int Filter, ByteBuffer info, int maxInfoLen);

    int ShamrockSetFilterInfo(int device, int Filter, ByteBuffer info);

    int ShamrockFlipperMirrorIsPresent(int device, int flipper, IntBuffer present);

    int ShamrockFlipperMirrorReset(int device, int flipper);

    int ShamrockSetFlipperMirror(int device, int flipper, int port);

    int ShamrockGetFlipperMirror(int device, int flipper, IntBuffer port);

    int ShamrockSetFlipperMirrorPosition(int device, int flipper, int position);

    int ShamrockGetFlipperMirrorPosition(int device, int flipper, IntBuffer position);

    int ShamrockGetFlipperMirrorMaxPosition(int device, int flipper, IntBuffer maxPosition);

    int ShamrockGetCCDLimits(int device, int port, FloatBuffer low, FloatBuffer high);

    int ShamrockAccessoryIsPresent(int device, IntBuffer present);

    int ShamrockSetAccessoryState(int device, int accessory, int state);

    int ShamrockGetAccessoryState(int device, int accessory, IntBuffer state);

    int ShamrockFocusMirrorIsPresent(int device, IntBuffer present);

    int ShamrockFocusMirrorReset(int device);

    int ShamrockSetFocusMirror(int device, int focus);

    int ShamrockGetFocusMirror(int device, IntBuffer focus);

    int ShamrockGetFocusMirrorMaxSteps(int device, IntBuffer steps);

    int ShamrockSetPixelWidth(int device, float width);

    int ShamrockGetPixelWidth(int device, FloatBuffer width);

    int ShamrockSetNumberPixels(int device, int numberPixels);

    int ShamrockGetNumberPixels(int device, IntBuffer numberPixels);

    int ShamrockGetCalibration(int device, FloatBuffer calibrationValues, int numberPixels);

    int ShamrockGetPixelCalibrationCoefficients(int device, FloatBuffer A, FloatBuffer B, FloatBuffer C, FloatBuffer D);

    int ShamrockIrisIsPresent(int device, int iris, IntBuffer present);

    int ShamrockSetIris(int device, int iris, int value);

    int ShamrockGetIris(int device, int iris, IntBuffer value);

}
