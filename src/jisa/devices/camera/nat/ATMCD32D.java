package jisa.devices.camera.nat;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.PointerByReference;
import jisa.visa.Library;

import java.nio.*;
import java.util.Arrays;
import java.util.List;

public interface ATMCD32D extends Library {

    int DRV_ERROR_ACK                              = 20013;
    int AC_GETFUNCTION_GATEMODE                    = 0x80;
    int AT_DDG_TERMINATION_HIGHZ                   = 1;
    int AC_ACQMODE_FRAMETRANSFER                   = 16;
    int AC_SETFUNCTION_DDGTIMES                    = 0x020000;
    int AC_CAMERATYPE_ALTAF                        = 27;
    int AC_TRIGGERMODE_INTERNAL                    = 1;
    int AC_PIXELMODE_32BIT                         = 8;
    int DRV_IOCERROR                               = 20090;
    int DRV_OA_CAMERA_NOT_SUPPORTED                = 20194;
    int AC_SETFUNCTION_GATESTEP                    = 0x200000;
    int AC_CAMERATYPE_SIMCAM                       = 19;
    int AT_STEPMODE_CONSTANT                       = 0;
    int AC_SETFUNCTION_PREAMPGAIN                  = 0x0200;
    int AC_SETFUNCTION_DMAPARAMETERS               = 0x0800;
    int DRV_ERROR_BOARDTEST                        = 20012;
    int DRV_OA_PRESET_AND_USER_FILE_NOT_LOADED     = 20181;
    int AC_CAMERATYPE_IVAC_CCD                     = 23;
    int DRV_FLEXERROR                              = 20053;
    int AC_EMGAIN_12BIT                            = 2;
    int AC_ACQMODE_KINETIC                         = 8;
    int AC_CAMERATYPE_ASPEN                        = 24;
    int AC_SETFUNCTION_BASELINEOFFSET              = 0x0100;
    int AC_SETFUNCTION_INTELLIGATE                 = 0x080000;
    int DRV_ERROR_SCAN                             = 20004;
    int AC_PIXELMODE_14BIT                         = 2;
    int DRV_BINNING_ERROR                          = 20099;
    int DRV_TEMP_NOT_REACHED                       = 20037;
    int DRV_P7INVALID                              = 20083;
    int AC_ACQMODE_FASTKINETICS                    = 32;
    int AC_SETFUNCTION_VSAMPLITUDE                 = 0x40;
    int DRV_GPIBERROR                              = 20054;
    int DRV_UNKNOWN_FUNCTION                       = 20007;
    int DRV_NOT_INITIALIZED                        = 20075;
    int DRV_OA_MODE_DOES_NOT_EXIST                 = 20193;
    int DRV_DRIVER_ERRORS                          = 20065;
    int DRV_ERROR_UNMAP                            = 20116;
    int AC_SETFUNCTION_TEMPERATURE                 = 0x04;
    int DRV_ACQ_BUFFER                             = 20018;
    int DRV_P5INVALID                              = 20076;
    int DRV_P9INVALID                              = 20085;
    int AT_VERSION_INFO_LEN                        = 80;
    int DRV_P1INVALID                              = 20066;
    int DRV_P3INVALID                              = 20068;
    int AC_SETFUNCTION_IOC                         = 0x040000;
    int AT_DDGLite_ControlBit_ChannelEnable        = 0x01;
    int DRV_OWMEMORY_BAD_ADDR                      = 20151;
    int AC_ACQMODE_OVERLAP                         = 64;
    int DRV_NOT_SUPPORTED                          = 20991;
    int AC_CAMERATYPE_PDA                          = 0;
    int AT_DDGLite_ControlBit_DisableOnFrame       = 0x04;
    int AC_ACQMODE_SINGLE                          = 1;
    int AC_TRIGGERMODE_EXTERNALSTART               = 16;
    int AC_SETFUNCTION_EMADVANCED                  = 0x8000;
    int AC_CAMERATYPE_ICCD                         = 2;
    int DRV_TEMP_CODES                             = 20033;
    int AC_CAMERATYPE_IDUS                         = 7;
    int AT_GATEMODE_CW_OFF                         = 4;
    int AC_SETFUNCTION_ICCDGAIN                    = 8;
    int AC_EMGAIN_LINEAR12                         = 4;
    int DRV_INVALID_RINGEXPOSURES                  = 20098;
    int AT_DDG_TERMINATION_50OHMS                  = 0;
    int AC_GETFUNCTION_ICCDGAIN                    = 0x10;
    int DRV_INVALID_TRIGGER_MODE                   = 20095;
    int AC_FEATURES_SENSOR_COMPENSATION            = 0x40000000;
    int DRV_OA_MODE_ALREADY_EXISTS                 = 20189;
    int AC_GETFUNCTION_HVFLAG                      = 0x40;
    int AC_SETFUNCTION_GATEDELAYSTEP               = 0x200000;
    int DRV_OA_USER_FILE_NOT_LOADED                = 20180;
    int AC_GETFUNCTION_PHOSPHORSTATUS              = 0x2000;
    int AC_CAMERATYPE_ASCENT                       = 25;
    int DRV_INVALID_COUNTCONVERT_MODE              = 20101;
    int AC_CAMERATYPE_INGAAS                       = 14;
    int AC_SETFUNCTION_SUPERKINETICS               = 0x20000000;
    int DRV_ERROR_VXD_INIT                         = 20008;
    int DRV_TEMPERATURE_OUT_RANGE                  = 20038;
    int DRV_COF_NOTLOADED                          = 20051;
    int DRV_DIVIDE_BY_ZERO_ERROR                   = 20097;
    int AC_SETFUNCTION_MCPGAIN                     = 0x08;
    int AT_DDGLite_ControlBit_FreeRun              = 0x02;
    int DRV_I2CTIMEOUT                             = 20082;
    int DRV_NOT_AVAILABLE                          = 20992;
    int DRV_TEMPERATURE_DRIFT                      = 20040;
    int DRV_TEMP_NOT_SUPPORTED                     = 20039;
    int AC_FEATURES_OPTACQUIRE                     = 0x100000;
    int AC_GETFUNCTION_DETECTORSIZE                = 0x08;
    int AC_READMODE_SINGLETRACK                    = 4;
    int AC_CAMERATYPE_VOLMOS                       = 22;
    int AC_CAMERATYPE_IKONXL                       = 28;
    int DRV_TEMPERATURE_STABILIZED                 = 20036;
    int AC_SETFUNCTION_TRIGGERTERMINATION          = 0x400000;
    int DRV_USBERROR                               = 20089;
    int AC_SETFUNCTION_PRESCANS                    = 0x4000000;
    int AC_TRIGGERMODE_EXTERNAL_FVB_EM             = 4;
    int DRV_OW_CMD_FAIL                            = 20150;
    int DRV_TEMPERATURE_NOT_REACHED                = 20037;
    int AC_CAMERATYPE_RES1                         = 29;
    int AT_STEPMODE_LINEAR                         = 3;
    int AT_GATEMODE_CW_ON                          = 3;
    int DRV_OA_XML_INVALID_OR_NOT_FOUND_ERROR      = 20178;
    int AC_FEATURES_KINETICEXTERNALEXPOSURE        = 0x2000;
    int AC_CAMERATYPE_LUCA                         = 11;
    int DRV_RANDOM_TRACK_ERROR                     = 20094;
    int AC_CAMERATYPE_SURCAM                       = 9;
    int AC_FEATURES_POLLING                        = 1;
    int DRV_PROC_UNKONWN_INSTRUCTION               = 20020;
    int DRV_TEMP_OUT_RANGE                         = 20038;
    int AC_GETFUNCTION_TARGETTEMPERATURE           = 0x02;
    int AC_SETFUNCTION_INSERTION_DELAY             = 0x100000;
    int AC_CAMERATYPE_NEO                          = 20;
    int AC_GETFUNCTION_MCPGAINTABLE                = 0x4000;
    int DRV_ERROR_PATTERN                          = 20015;
    int DRV_SPOOLERROR                             = 20026;
    int DRV_PROCESSING_FAILED                      = 20211;
    int AT_STEPMODE_OFF                            = 100;
    int DRV_FILESIZELIMITERROR                     = 20028;
    int DRV_INVALID_AMPLIFIER                      = 20100;
    int AC_TRIGGERMODE_EXTERNAL_CHARGESHIFTING     = 0x80;
    int DRV_ACCUM_TIME_NOT_MET                     = 20023;
    int AC_CAMERATYPE_IXON                         = 1;
    int AC_FEATURES_ENDOFEXPOSURE_EVENT            = 0x4000000;
    int AC_GETFUNCTION_EMCCDGAIN                   = 0x20;
    int DRV_ERROR_NOPointer                        = 20121;
    int DRV_OW_NO_SLAVES                           = 20153;
    int DRV_P10INVALID                             = 20086;
    int DRV_TEMP_DRIFT                             = 20040;
    int DRV_OA_FAILED_TO_GET_MODE                  = 20195;
    int AC_GETFUNCTION_IOC                         = 0x0200;
    int DRV_ACQUIRING                              = 20072;
    int AC_FEATURES_PHOTONCOUNTING                 = 0x20000;
    int AT_DDGLite_ControlBit_Invert               = 0x10;
    int AC_FEATURES_TEMPERATUREDURINGACQUISITION   = 512;
    int AC_SETFUNCTION_CROPMODE                    = 0x0400;
    int DRV_INVALID_FILTER                         = 20079;
    int DRV_OA_FILE_HAS_BEEN_MODIFIED              = 20183;
    int AC_CAMERATYPE_VIDEO                        = 6;
    int DRV_TEMPERATURE_NOT_SUPPORTED              = 20039;
    int AC_CAMERATYPE_CLARA                        = 17;
    int AC_FEATURES_CAMERALINK                     = 0x8000000;
    int AC_READMODE_FVB                            = 8;
    int DRV_ERROR_FILELOAD                         = 20006;
    int AC_READMODE_FULLIMAGE                      = 1;
    int AC_TRIGGERMODE_EXTERNAL                    = 2;
    int DRV_OA_DTD_VALIDATE_ERROR                  = 20175;
    int DRV_OA_FILE_DOES_NOT_EXIST                 = 20177;
    int DRV_OA_BUFFER_FULL                         = 20184;
    int AC_PIXELMODE_16BIT                         = 4;
    int DRV_I2CDEVNOTFOUND                         = 20081;
    int DRV_P8INVALID                              = 20084;
    int AC_GETFUNCTION_INTELLIGATE                 = 0x0400;
    int AC_READMODE_RANDOMTRACK                    = 32;
    int DRV_TEMPCYCLE                              = 20074;
    int AT_DDGLite_ControlBit_GlobalEnable         = 0x01;
    int AC_SETFUNCTION_HREADOUT                    = 0x02;
    int AT_CONTROLLER_CARD_MODEL_LEN               = 80;
    int AC_TRIGGERMODE_BULB                        = 32;
    int AC_GETFUNCTION_BASELINECLAMP               = 0x8000;
    int AC_FEATURES_DUALPREAMPGAIN                 = 0x800000;
    int DRV_GATESTEPERROR                          = 20092;
    int AC_SETFUNCTION_EXTENDED_CROP_MODE          = 0x10000000;
    int DRV_EEPROMVERSIONERROR                     = 20055;
    int DRV_OA_INVALID_NAMING                      = 20187;
    int AC_EMGAIN_REAL12                           = 8;
    int AC_READMODE_MULTITRACKSCAN                 = 64;
    int AC_CAMERATYPE_IVAC                         = 15;
    int DRV_TEMPERATURE_CODES                      = 20033;
    int AC_SETFUNCTION_TIMESCAN                    = 0x40000000;
    int DRV_OA_INVALID_CHARS_IN_NAME               = 20186;
    int AT_NoOfVersionInfoIds                      = 2;
    int DRV_INVALID_MODE                           = 20078;
    int AC_SETFUNCTION_EXTENDEDNIR                 = 0x800000;
    int AC_READMODE_SUBIMAGE                       = 2;
    int DRV_ACQUISITION_ERRORS                     = 20017;
    int DRV_INIERROR                               = 20070;
    int AC_TRIGGERMODE_EXTERNALEXPOSURE            = 32;
    int DRV_OA_PRESET_FILE_NOT_LOADED              = 20179;
    int AC_FEATURES_SATURATIONEVENT                = 64;
    int AC_GETFUNCTION_GATESTEP                    = 0x1000;
    int AC_CAMERATYPE_IXONULTRA                    = 21;
    int AC_SETFUNCTION_BASELINECLAMP               = 0x20;
    int AC_CAMERATYPE_CCD                          = 4;
    int AC_CAMERATYPE_RESERVED                     = 12;
    int AC_FEATURES_METADATA                       = 0x8000;
    int DRV_TEMP_NOT_STABILIZED                    = 20035;
    int AC_GETFUNCTION_GAIN                        = 0x10;
    int AT_STEPMODE_EXPONENTIAL                    = 1;
    int DRV_LOAD_FIRMWARE_ERROR                    = 20096;
    int AC_FEATURES_COUNTCONVERT                   = 0x40000;
    int AC_FEATURES_POSTPROCESSSPURIOUSNOISEFILTER = 0x400000;
    int DRV_ERROR_CODES                            = 20001;
    int AC_FEATURES_STARTOFEXPOSURE_EVENT          = 0x2000000;
    int DRV_USB_INTERRUPT_ENDPOINT_ERROR           = 20093;
    int AC_FEATURES_FIFOFULL_EVENT                 = 0x10000000;
    int DRV_COFERROR                               = 20071;
    int AT_GATEMODE_DDG                            = 5;
    int DRV_OA_STRINGS_NOT_EQUAL                   = 20190;
    int DRV_ERROR_BUFFSIZE                         = 20119;
    int AC_CAMERATYPE_ALTA                         = 26;
    int AC_GETFUNCTION_TEMPERATURERANGE            = 0x04;
    int AC_FEATURES_DACCONTROL                     = 0x4000;
    int AC_EMGAIN_8BIT                             = 1;
    int DRV_TEMP_OFF                               = 20034;
    int DRV_GATING_NOT_AVAILABLE                   = 20130;
    int DRV_P11INVALID                             = 20087;
    int AC_CAMERATYPE_IKONLR                       = 31;
    int AT_STEPMODE_LOGARITHMIC                    = 2;
    int AT_GATEMODE_FIRE_ONLY                      = 1;
    int AC_PIXELMODE_MONO                          = 0x000000;
    int AC_ACQMODE_ACCUMULATE                      = 4;
    int DRV_ERROR_NOCAMERA                         = 20990;
    int DRV_ACQ_DOWNFIFO_FULL                      = 20019;
    int DRV_ERROR_MDL                              = 20117;
    int DRV_OA_INVALID_FILE                        = 20182;
    int AC_SETFUNCTION_SPOOLTHREADCOUNT            = 0x1000000;
    int DRV_DATATYPE                               = 20064;
    int DRV_OA_FILE_ACCESS_ERROR                   = 20176;
    int DRV_OWCMD_NOT_AVAILABLE                    = 20152;
    int AT_GATEMODE_FIRE_AND_GATE                  = 0;
    int DRV_KINETIC_TIME_NOT_MET                   = 20022;
    int AC_CAMERATYPE_NEWTON                       = 8;
    int DRV_P2INVALID                              = 20067;
    int AC_PIXELMODE_CMY                           = 0x020000;
    int DRV_P6INVALID                              = 20077;
    int AC_GETFUNCTION_DDGTIMES                    = 0x0100;
    int DRV_SUCCESS                                = 20002;
    int AC_SETFUNCTION_VREADOUT                    = 0x01;
    int AC_READMODE_MULTITRACK                     = 16;
    int AC_TRIGGERMODE_CONTINUOUS                  = 8;
    int DRV_IDLE                                   = 20073;
    int DRV_TEMPERATURE_OFF                        = 20034;
    int AC_GETFUNCTION_MCPGAIN                     = 0x10;
    int DRV_ERROR_UNMDL                            = 20118;
    int AT_DDGLite_ControlBit_RestartOnFire        = 0x08;
    int AT_DDG_POLARITY_POSITIVE                   = 0;
    int DRV_ERROR_CHECK_SUM                        = 20005;
    int DRV_ERROR_FILESAVE                         = 20029;
    int AC_SETFUNCTION_GAIN                        = 8;
    int AC_FEATURES_MIDFANCONTROL                  = 256;
    int DRV_OA_PARSE_DTD_ERROR                     = 20174;
    int AC_FEATURES_IOCONTROL                      = 0x10000;
    int DRV_OA_GET_CAMERA_ERROR                    = 20188;
    int AC_CAMERATYPE_USBICCD                      = 10;
    int DRV_VXDNOTINSTALLED                        = 20003;
    int AC_CAMERATYPE_IKON                         = 13;
    int AC_CAMERATYPE_ISTAR_SCMOS                  = 30;
    int AC_GETFUNCTION_INSERTION_DELAY             = 0x0800;
    int AT_DDG_POLARITY_NEGATIVE                   = 1;
    int DRV_GENERAL_ERRORS                         = 20049;
    int DRV_VRMVERSIONERROR                        = 20091;
    int AC_SETFUNCTION_HORIZONTALBIN               = 0x1000;
    int AC_FEATURES_DDGLITE                        = 0x0800;
    int AC_FEATURES_SHUTTER                        = 8;
    int AC_FEATURES2_ESD_EVENTS                    = 1;
    int AC_SETFUNCTION_GATEWIDTHSTEP               = 0x8000000;
    int AC_GETFUNCTION_GATEWIDTHSTEP               = 0x10000;
    int DRV_OW_ERROR_SLAVE_NUM                     = 20155;
    int DRV_MSTIMINGS_ERROR                        = 20156;
    int AC_FEATURES_DEFECT_CORRECTION              = 0x1000000;
    int AC_SETFUNCTION_MULTITRACKHRANGE            = 0x2000;
    int AC_FEATURES_FTEXTERNALEXPOSURE             = 0x1000;
    int DRV_FPGA_VOLTAGE_ERROR                     = 20131;
    int AC_SETFUNCTION_HIGHCAPACITY                = 0x80;
    int AC_CAMERATYPE_USBISTAR                     = 18;
    int AC_SETFUNCTION_EMCCDGAIN                   = 0x10;
    int DRV_I2CERRORS                              = 20080;
    int DRV_FPGAPROG                               = 20052;
    int AC_FEATURES_FANCONTROL                     = 128;
    int AC_FEATURES_EXTERNAL_I2C                   = 32;
    int AC_FEATURES_SPOOLING                       = 4;
    int AC_CAMERATYPE_ISTAR                        = 5;
    int AC_GETFUNCTION_GATEDELAYSTEP               = 0x1000;
    int DRV_OA_INVALID_STRING_LENGTH               = 20185;
    int AC_GETFUNCTION_TEMPERATURE                 = 0x01;
    int AC_FEATURES_EVENTS                         = 2;
    int AT_GATEMODE_GATE_ONLY                      = 2;
    int DRV_ILLEGAL_OP_CODE                        = 20021;
    int DRV_PCI_DMA_FAIL                           = 20025;
    int AC_FEATURES_DUALMODE                       = 0x80000;
    int AC_FEATURES_REALTIMESPURIOUSNOISEFILTER    = 0x200000;
    int AC_SETFUNCTION_REGISTERPACK                = 0x2000000;
    int DRV_OA_NULL_ERROR                          = 20173;
    int DRV_ERROR_ADDRESS                          = 20009;
    int AC_FEATURES_IRIG_SUPPORT                   = 0x80000000;
    int AT_DDGLite_ControlBit_EnableOnFire         = 0x20;
    int AC_PIXELMODE_8BIT                          = 1;
    int DRV_INVALID_AUX                            = 20050;
    int AC_SETFUNCTION_RANDOMTRACKNOGAPS           = 0x4000;
    int AC_ACQMODE_VIDEO                           = 2;
    int AC_CAMERATYPE_EMCCD                        = 3;
    int AC_CAMERATYPE_UNPROGRAMMED                 = 16;
    int DRV_ERROR_PAGEUNLOCK                       = 20011;
    int AC_SETFUNCTION_GATEMODE                    = 0x010000;
    int DRV_TEMP_STABILIZED                        = 20036;
    int DRV_ERROR_UP_FIFO                          = 20014;
    int AC_FEATURES_SHUTTEREX                      = 16;
    int DRV_NO_NEW_DATA                            = 20024;
    int DRV_TEMPERATURE_NOT_STABILIZED             = 20035;
    int DRV_OA_VALUE_NOT_SUPPORTED                 = 20192;
    int AC_FEATURES_KEEPCLEANCONTROL               = 1024;
    int AC_TRIGGERMODE_INVERTED                    = 0x40;
    int DRV_ERROR_PAGELOCK                         = 20010;
    int DRV_OA_NO_USER_DATA                        = 20191;
    int DRV_SPOOLSETUPERROR                        = 20027;
    int AC_FEATURES_SENSOR_PORT_CONFIGURATION      = 0x20000000;
    int DRV_ERROR_MAP                              = 20115;
    int DRV_P4INVALID                              = 20069;
    int AC_PIXELMODE_RGB                           = 0x010000;
    int DRV_OW_NOT_INITIALIZED                     = 20154;

    int AbortAcquisition();

    int CancelWait();

    int CoolerOFF();

    int CoolerON();

    int DemosaicImage(ShortBuffer grey, ShortBuffer red, ShortBuffer green, ShortBuffer blue, COLORDEMOSAICINFO info);

    int EnableKeepCleans(int iMode);

    int EnableSensorCompensation(int iMode);

    int SetIRIGModulation(byte mode);

    int FreeInternalMemory();

    int GetAcquiredData(NativeLongByReference arr, NativeLong size);

    int GetAcquiredData16(ShortBuffer arr, NativeLong size);

    int GetAcquiredFloatData(FloatBuffer arr, NativeLong size);

    int GetAcquisitionProgress(NativeLongByReference acc, NativeLongByReference series);

    int GetAcquisitionTimings(FloatBuffer exposure, FloatBuffer accumulate, FloatBuffer kinetic);

    int GetAdjustedRingExposureTimes(int inumTimes, FloatBuffer fptimes);

    int GetAllDMAData(NativeLongByReference arr, NativeLong size);

    int GetAmpDesc(int index, String name, int length);

    int GetAmpMaxSpeed(int index, FloatBuffer speed);

    int GetAvailableCameras(NativeLongByReference totalCameras);

    int GetBackground(NativeLongByReference arr, NativeLong size);

    int GetBaselineClamp(IntBuffer state);

    int GetBitDepth(int channel, IntBuffer depth);

    int GetCameraEventStatus(IntBuffer camStatus);

    int GetCameraHandle(NativeLong cameraIndex, NativeLongByReference cameraHandle);

    int GetCameraInformation(int index, NativeLongByReference information);

    int GetCameraSerialNumber(IntBuffer number);

    int GetCapabilities(ANDORCAPS caps);

    int GetControllerCardModel(String controllerCardModel);

    int GetCountConvertWavelengthRange(FloatBuffer minval, FloatBuffer maxval);

    int GetCurrentCamera(NativeLongByReference cameraHandle);

    int GetCYMGShift(IntBuffer iXshift, IntBuffer iYShift);

    int GetDDGExternalOutputEnabled(NativeLong uiIndex, NativeLongByReference puiEnabled);

    int GetDDGExternalOutputPolarity(NativeLong uiIndex, NativeLongByReference puiPolarity);

    int GetDDGExternalOutputStepEnabled(NativeLong uiIndex, NativeLongByReference puiEnabled);

    int GetDDGExternalOutputTime(NativeLong uiIndex, LongBuffer puiDelay, LongBuffer puiWidth);

    int GetDDGTTLGateWidth(long opticalWidth, LongBuffer ttlWidth);

    int GetDDGGateTime(LongBuffer puiDelay, LongBuffer puiWidth);

    int GetDDGInsertionDelay(IntBuffer piState);

    int GetDDGIntelligate(IntBuffer piState);

    int GetDDGIOC(IntBuffer state);

    int GetDDGIOCFrequency(DoubleBuffer frequency);

    int GetDDGIOCNumber(NativeLongByReference numberPulses);

    int GetDDGIOCNumberRequested(NativeLongByReference pulses);

    int GetDDGIOCPeriod(LongBuffer period);

    int GetDDGIOCPulses(IntBuffer pulses);

    int GetDDGIOCTrigger(NativeLongByReference trigger);

    int GetDDGOpticalWidthEnabled(NativeLongByReference puiEnabled);

    int GetDDGLiteGlobalControlByte(ByteBuffer control);

    int GetDDGLiteControlByte(int channel, ByteBuffer control);

    int GetDDGLiteInitialDelay(int channel, FloatBuffer fDelay);

    int GetDDGLitePulseWidth(int channel, FloatBuffer fWidth);

    int GetDDGLiteInterPulseDelay(int channel, FloatBuffer fDelay);

    int GetDDGLitePulsesPerExposure(int channel, NativeLongByReference ui32Pulses);

    int GetDDGPulse(double wid, double resolution, DoubleBuffer Delay, DoubleBuffer Width);

    int GetDDGStepCoefficients(NativeLong mode, DoubleBuffer p1, DoubleBuffer p2);

    int GetDDGWidthStepCoefficients(NativeLong mode, DoubleBuffer p1, DoubleBuffer p2);

    int GetDDGStepMode(NativeLongByReference mode);

    int GetDDGWidthStepMode(NativeLongByReference mode);

    int GetDetector(IntBuffer xpixels, IntBuffer ypixels);

    int GetDICameraInfo(Pointer info);

    int GetEMAdvanced(IntBuffer state);

    int GetEMCCDGain(IntBuffer gain);

    int GetEMGainRange(IntBuffer low, IntBuffer high);

    int GetExternalTriggerTermination(NativeLongByReference puiTermination);

    int GetFastestRecommendedVSSpeed(IntBuffer index, FloatBuffer speed);

    int GetFIFOUsage(IntBuffer FIFOusage);

    int GetFilterMode(IntBuffer mode);

    int GetFKExposureTime(FloatBuffer time);

    int GetFKVShiftSpeed(int index, IntBuffer speed);

    int GetFKVShiftSpeedF(int index, FloatBuffer speed);

    int GetFrontEndStatus(IntBuffer piFlag);

    int GetGateMode(IntBuffer piGatemode);

    int GetHardwareVersion(IntBuffer PCB, IntBuffer Decode, IntBuffer dummy1, IntBuffer dummy2, IntBuffer CameraFirmwareVersion, IntBuffer CameraFirmwareBuild);

    int GetHeadModel(String name);

    int GetHorizontalSpeed(int index, IntBuffer speed);

    int GetHSSpeed(int channel, int typ, int index, FloatBuffer speed);

    int GetHVflag(IntBuffer bFlag);

    int GetID(int devNum, IntBuffer id);

    int GetImageFlip(IntBuffer iHFlip, IntBuffer iVFlip);

    int GetImageRotate(IntBuffer iRotate);

    int GetImages(NativeLong first, NativeLong last, NativeLongByReference arr, NativeLong size, NativeLongByReference validfirst, NativeLongByReference validlast);

    int GetImages16(NativeLong first, NativeLong last, ShortBuffer arr, NativeLong size, NativeLongByReference validfirst, NativeLongByReference validlast);

    int GetImagesPerDMA(NativeLongByReference images);

    int GetIRQ(IntBuffer IRQ);

    int GetKeepCleanTime(FloatBuffer KeepCleanTime);

    int GetMaximumBinning(int ReadMode, int HorzVert, IntBuffer MaxBinning);

    int GetMaximumExposure(FloatBuffer MaxExp);

    int GetMaximumNumberRingExposureTimes(IntBuffer number);

    int GetMCPGain(IntBuffer piGain);

    int GetMCPGainRange(IntBuffer iLow, IntBuffer iHigh);

    int GetMCPGainTable(int iNum, IntBuffer piGain, FloatBuffer pfPhotoepc);

    int GetMCPVoltage(IntBuffer iVoltage);

    int GetMinimumImageLength(IntBuffer MinImageLength);

    int GetMinimumNumberInSeries(IntBuffer number);

    int GetMostRecentColorImage16(NativeLong size, int algorithm, ShortBuffer red, ShortBuffer green, ShortBuffer blue);

    int GetMostRecentImage(LongBuffer arr, NativeLong size);

    int GetMostRecentImage16(ShortBuffer arr, NativeLong size);

    int GetMSTimingsEnabled();

    int GetNewData(NativeLongByReference arr, NativeLong size);

    int GetNewData16(ShortBuffer arr, NativeLong size);

    int GetNewData8(ByteBuffer arr, NativeLong size);

    int GetNewFloatData(FloatBuffer arr, NativeLong size);

    int GetNumberADChannels(IntBuffer channels);

    int GetNumberAmp(IntBuffer amp);

    int GetNumberAvailableImages(NativeLongByReference first, NativeLongByReference last);

    int GetNumberDDGExternalOutputs(NativeLongByReference puiCount);

    int GetNumberDevices(IntBuffer numDevs);

    int GetNumberFKVShiftSpeeds(IntBuffer number);

    int GetNumberHorizontalSpeeds(IntBuffer number);

    int GetNumberHSSpeeds(int channel, int typ, IntBuffer speeds);

    int GetNumberMissedExternalTriggers(int first, int last, ShortBuffer arr, int size);

    int GetIRIGData(ByteBuffer _uc_irigData, int _ui_index);

    int GetNumberNewImages(NativeLongByReference first, NativeLongByReference last);

    int GetNumberPhotonCountingDivisions(NativeLongByReference noOfDivisions);

    int GetNumberPreAmpGains(IntBuffer noGains);

    int GetNumberRingExposureTimes(IntBuffer ipnumTimes);

    int GetNumberIO(IntBuffer iNumber);

    int GetNumberVerticalSpeeds(IntBuffer number);

    int GetNumberVSAmplitudes(IntBuffer number);

    int GetNumberVSSpeeds(IntBuffer speeds);

    int GetOldestImage(NativeLongByReference arr, NativeLong size);

    int GetOldestImage16(ShortBuffer arr, NativeLong size);

    int GetPhosphorStatus(IntBuffer piFlag);

    int GetPhysicalDMAAddress(NativeLongByReference Address1, NativeLongByReference Address2);

    int GetPixelSize(FloatBuffer xSize, FloatBuffer ySize);

    int GetPreAmpGain(int index, FloatBuffer gain);

    int GetPreAmpGainText(int index, String name, int length);

    int GetDualExposureTimes(FloatBuffer exposure1, FloatBuffer exposure2);

    int GetQE(String sensor, float wavelength, int mode, FloatBuffer QE);

    int GetReadOutTime(FloatBuffer ReadOutTime);

    int GetRegisterDump(IntBuffer mode);

    int GetRelativeImageTimes(int first, int last, LongBuffer arr, int size);

    int GetRingExposureRange(FloatBuffer fpMin, FloatBuffer fpMax);

    int GetSDK3Pointer(IntBuffer Pointer);

    int GetSensitivity(int channel, int horzShift, int amplifier, int pa, FloatBuffer sensitivity);

    int GetShutterMinTimes(IntBuffer minclosingtime, IntBuffer minopeningtime);

    int GetSizeOfCircularBuffer(NativeLongByReference index);

    int GetSlotBusDeviceFunction(IntBuffer dwslot, IntBuffer dwBus, IntBuffer dwDevice, IntBuffer dwFunction);

    int GetSoftwareVersion(IntBuffer eprom, IntBuffer coffile, IntBuffer vxdrev, IntBuffer vxdver, IntBuffer dllrev, IntBuffer dllver);

    int GetSpoolProgress(NativeLongByReference index);

    int GetStartUpTime(FloatBuffer time);

    int GetStatus(IntBuffer status);

    int GetTECStatus(IntBuffer piFlag);

    int GetTemperature(IntBuffer temperature);

    int GetTemperatureF(FloatBuffer temperature);

    int GetTemperatureRange(IntBuffer mintemp, IntBuffer maxtemp);

    int GetTemperaturePrecision(IntBuffer precision);

    int GetTemperatureStatus(FloatBuffer SensorTemp, FloatBuffer TargetTemp, FloatBuffer AmbientTemp, FloatBuffer CoolerVolts);

    int GetTotalNumberImagesAcquired(NativeLongByReference index);

    int GetIODirection(int index, IntBuffer iDirection);

    int GetIOLevel(int index, IntBuffer iLevel);

    int GetUSBDeviceDetails(ShortBuffer VendorID, ShortBuffer ProductID, ShortBuffer FirmwareVersion, ShortBuffer SpecificationNumber);

    int GetVersionInfo(int arr, String szVersionInfo, NativeLong ui32BufferLen);

    int GetVerticalSpeed(int index, IntBuffer speed);

    int GetVirtualDMAAddress(PointerByReference Address1, PointerByReference Address2);

    int GetVSAmplitudeString(int index, String text);

    int GetVSAmplitudeFromString(String text, IntBuffer index);

    int GetVSAmplitudeValue(int index, IntBuffer value);

    int GetVSSpeed(int index, FloatBuffer speed);

    int GPIBReceive(int id, short address, String text, int size);

    int GPIBSend(int id, short address, String text);

    int I2CBurstRead(byte i2cAddress, NativeLong nBytes, ByteBuffer data);

    int I2CBurstWrite(byte i2cAddress, NativeLong nBytes, ByteBuffer data);

    int I2CRead(byte deviceID, byte intAddress, ByteBuffer pdata);

    int I2CReset();

    int I2CWrite(byte deviceID, byte intAddress, byte data);

    int IdAndorDll();

    int InAuxPort(int port, IntBuffer state);

    int Initialize(String dir);

    int InitializeDevice(String dir);

    int IsAmplifierAvailable(int iamp);

    int IsCoolerOn(IntBuffer iCoolerStatus);

    int IsCountConvertModeAvailable(int mode);

    int IsInternalMechanicalShutter(IntBuffer InternalShutter);

    int IsPreAmpGainAvailable(int channel, int amplifier, int index, int pa, IntBuffer status);

    int IsReadoutFlippedByAmplifier(int iAmplifier, IntBuffer iFlipped);

    int IsTriggerModeAvailable(int iTriggerMode);

    int Merge(NativeLong[] arr, NativeLong nOrder, NativeLong nPoint, NativeLong nPixel, FloatBuffer coeff, NativeLong fit, NativeLong hbin, NativeLongByReference output, FloatBuffer start, FloatBuffer step_Renamed);

    int OutAuxPort(int port, int state);

    int PrepareAcquisition();

    int SaveAsBmp(String path, String palette, NativeLong ymin, NativeLong ymax);

    int SaveAsCalibratedSif(String path, int x_data_type, int x_unit, FloatBuffer x_cal, float rayleighWavelength);

    int SaveAsCommentedSif(String path, String comment);

    int SaveAsEDF(String szPath, int iMode);

    int SaveAsFITS(String szFileTitle, int typ);

    int SaveAsRaw(String szFileTitle, int typ);

    int SaveAsSif(String path);

    int SaveAsSPC(String path);

    int SaveAsTiff(String path, String palette, int position, int typ);

    int SaveAsTiffEx(String path, String palette, int position, int typ, int mode);

    int SaveEEPROMToFile(String cFileName);

    int SaveToClipBoard(String palette);

    int SelectDevice(int devNum);

    int SendSoftwareTrigger();

    int SetAccumulationCycleTime(float time);

    int SetAcqStatusEvent(Pointer statusEvent);

    int SetAcquisitionMode(int mode);

    int SetSensorPortMode(int mode);

    int SelectSensorPort(int port);

    int SetAcquisitionType(int typ);

    int SetADChannel(int channel);

    int SetAdvancedTriggerModeState(int iState);

    int SetBackground(NativeLongByReference arr, NativeLong size);

    int SetBaselineClamp(int state);

    int SetBaselineOffset(int offset);

    int SetCameraLinkMode(int mode);

    int SetCameraStatusEnable(int Enable);

    int SetChargeShifting(int NumberRows, int NumberRepeats);

    int SetComplexImage(int numAreas, IntBuffer areas);

    int SetCoolerMode(int mode);

    int SetCountConvertMode(int Mode);

    int SetCountConvertWavelength(float wavelength);

    int SetCropMode(int active, int cropHeight, int reserved);

    int SetCurrentCamera(NativeLong cameraHandle);

    int SetCustomTrackHBin(int bin);

    int SetDataType(int typ);

    int SetDACOutput(int iOption, int iResolution, int iValue);

    int SetDACOutputScale(int iScale);

    int SetDDGAddress(byte t0, byte t1, byte t2, byte t3, byte address);

    int SetDDGExternalOutputEnabled(NativeLong uiIndex, NativeLong uiEnabled);

    int SetDDGExternalOutputPolarity(NativeLong uiIndex, NativeLong uiPolarity);

    int SetDDGExternalOutputStepEnabled(NativeLong uiIndex, NativeLong uiEnabled);

    int SetDDGExternalOutputTime(NativeLong uiIndex, long uiDelay, long uiWidth);

    int SetDDGGain(int gain);

    int SetDDGGateStep(double step_Renamed);

    int SetDDGGateTime(long uiDelay, long uiWidth);

    int SetDDGInsertionDelay(int state);

    int SetDDGIntelligate(int state);

    int SetDDGIOC(int state);

    int SetDDGIOCFrequency(double frequency);

    int SetDDGIOCNumber(NativeLong numberPulses);

    int SetDDGIOCPeriod(long period);

    int SetDDGIOCTrigger(NativeLong trigger);

    int SetDDGOpticalWidthEnabled(NativeLong uiEnabled);

    int SetDDGLiteGlobalControlByte(byte control);

    int SetDDGLiteControlByte(int channel, byte control);

    int SetDDGLiteInitialDelay(int channel, float fDelay);

    int SetDDGLitePulseWidth(int channel, float fWidth);

    int SetDDGLiteInterPulseDelay(int channel, float fDelay);

    int SetDDGLitePulsesPerExposure(int channel, NativeLong ui32Pulses);

    int SetDDGStepCoefficients(NativeLong mode, double p1, double p2);

    int SetDDGWidthStepCoefficients(NativeLong mode, double p1, double p2);

    int SetDDGStepMode(NativeLong mode);

    int SetDDGWidthStepMode(NativeLong mode);

    int SetDDGTimes(double t0, double t1, double t2);

    int SetDDGTriggerMode(int mode);

    int SetDDGVariableGateStep(int mode, double p1, double p2);

    int SetDelayGenerator(int board, short address, int typ);

    int SetDMAParameters(int MaxImagesPerDMA, float SecondsPerDMA);

    int SetDriverEvent(Pointer driverEvent);

    int SetESDEvent(Pointer esdEvent);

    int SetEMAdvanced(int state);

    int SetEMCCDGain(int gain);

    int SetEMClockCompensation(int EMClockCompensationFlag);

    int SetEMGainMode(int mode);

    int SetExposureTime(float time);

    int SetExternalTriggerTermination(NativeLong uiTermination);

    int SetFanMode(int mode);

    int SetFastExtTrigger(int mode);

    int SetFastKinetics(int exposedRows, int seriesLength, float time, int mode, int hbin, int vbin);

    int SetFastKineticsEx(int exposedRows, int seriesLength, float time, int mode, int hbin, int vbin, int offset);

    int SetFastKineticsStorageMode(int mode);

    int SetFastKineticsTimeScanMode(int rows, int tracks, int mode);

    int SetFilterMode(int mode);

    int SetFilterParameters(int width, float sensitivity, int range, float accept, int smooth, int noise);

    int SetFKVShiftSpeed(int index);

    int SetFPDP(int state);

    int SetFrameTransferMode(int mode);

    int SetFrontEndEvent(Pointer driverEvent);

    int SetFullImage(int hbin, int vbin);

    int SetFVBHBin(int bin);

    int SetGain(int gain);

    int SetGate(float delay, float width, float stepRenamed);

    int SetGateMode(int gatemode);

    int SetHighCapacity(int state);

    int SetHorizontalSpeed(int index);

    int SetHSSpeed(int typ, int index);

    int SetImage(int hbin, int vbin, int hstart, int hend, int vstart, int vend);

    int SetImageFlip(int iHFlip, int iVFlip);

    int SetImageRotate(int iRotate);

    int SetIsolatedCropMode(int active, int cropheight, int cropwidth, int vbin, int hbin);

    int SetIsolatedCropModeEx(int active, int cropheight, int cropwidth, int vbin, int hbin, int cropleft, int cropbottom);

    int SetKineticCycleTime(float time);

    int SetMCPGain(int gain);

    int SetMCPGating(int gating);

    int SetMessageWindow(Pointer wnd);

    int SetMetaData(int state);

    int SetMultiTrack(int number, int height, int offset, IntBuffer bottom, IntBuffer gap);

    int SetMultiTrackHBin(int bin);

    int SetMultiTrackHRange(int iStart, int iEnd);

    int SetMultiTrackScan(int trackHeight, int numberTracks, int iSIHStart, int iSIHEnd, int trackHBinning, int trackVBinning, int trackGap, int trackOffset, int trackSkip, int numberSubFrames);

    int SetNextAddress(NativeLongByReference data, NativeLong lowAdd, NativeLong highAdd, NativeLong length, NativeLong physical);

    int SetNextAddress16(NativeLongByReference data, NativeLong lowAdd, NativeLong highAdd, NativeLong length, NativeLong physical);

    int SetNumberAccumulations(int number);

    int SetNumberKinetics(int number);

    int SetNumberPrescans(int iNumber);

    int SetOutputAmplifier(int typ);

    int SetOverlapMode(int mode);

    int SetPCIMode(int mode, int value);

    int SetPhotonCounting(int state);

    int SetPhotonCountingThreshold(NativeLong min, NativeLong max);

    int SetPhosphorEvent(Pointer driverEvent);

    int SetPhotonCountingDivisions(NativeLong noOfDivisions, NativeLongByReference divisions);

    int SetPixelMode(int bitdepth, int colormode);

    int SetPreAmpGain(int index);

    int SetDualExposureTimes(float expTime1, float expTime2);

    int SetDualExposureMode(int mode);

    int SetRandomTracks(int numTracks, IntBuffer areas);

    int SetReadMode(int mode);

    int SetReadoutRegisterPacking(int mode);

    int SetRegisterDump(int mode);

    int SetRingExposureTimes(int numTimes, FloatBuffer times);

    int SetSaturationEvent(Pointer saturationEvent);

    int SetShutter(int typ, int mode, int closingtime, int openingtime);

    int SetShutterEx(int typ, int mode, int closingtime, int openingtime, int extmode);

    int SetShutters(int typ, int mode, int closingtime, int openingtime, int exttype, int extmode, int dummy1, int dummy2);

    int SetSifComment(String comment);

    int SetSingleTrack(int centre, int height);

    int SetSingleTrackHBin(int bin);

    int SetSpool(int active, int method, String path, int framebuffersize);

    int SetSpoolThreadCount(int count);

    int SetStorageMode(NativeLong mode);

    int SetTECEvent(Pointer driverEvent);

    int SetTemperature(int temperature);

    int SetTemperatureEvent(Pointer temperatureEvent);

    int SetTriggerMode(int mode);

    int SetTriggerInvert(int mode);

    int GetTriggerLevelRange(FloatBuffer minimum, FloatBuffer maximum);

    int SetTriggerLevel(float f_level);

    int SetIODirection(int index, int iDirection);

    int SetIOLevel(int index, int iLevel);

    int SetUserEvent(Pointer userEvent);

    int SetUSGenomics(NativeLong width, NativeLong height);

    int SetVerticalRowBuffer(int rows);

    int SetVerticalSpeed(int index);

    int SetVirtualChip(int state);

    int SetVSAmplitude(int index);

    int SetVSSpeed(int index);

    int ShutDown();

    int StartAcquisition();

    int UnMapPhysicalAddress();

    int UpdateDDGTimings();

    int WaitForAcquisition();

    int WaitForAcquisitionByHandle(NativeLong cameraHandle);

    int WaitForAcquisitionByHandleTimeOut(NativeLong cameraHandle, int iTimeOutMs);

    int WaitForAcquisitionTimeOut(int iTimeOutMs);

    int WhiteBalance(ShortBuffer wRed, ShortBuffer wGreen, ShortBuffer wBlue, FloatBuffer fRelR, FloatBuffer fRelB, WHITEBALANCEINFO info);

    int OA_Initialize(byte[] pcFilename, int uiFileNameLen);

    int OA_EnableMode(byte[] pcModeName);

    int OA_GetModeAcqParams(byte[] pcModeName, String pcListOfParams);

    int OA_GetUserModeNames(String pcListOfModes);

    int OA_GetPreSetModeNames(String pcListOfModes);

    int OA_GetNumberOfUserModes(int[] puiNumberOfModes);

    int OA_GetNumberOfPreSetModes(int[] puiNumberOfModes);

    int OA_GetNumberOfAcqParams(byte[] pcModeName, int[] puiNumberOfParams);

    int OA_AddMode(String pcModeName, int uiModeNameLen, String pcModeDescription, int uiModeDescriptionLen);

    int OA_WriteToFile(byte[] pcFileName, int uiFileNameLen);

    int OA_DeleteMode(byte[] pcModeName, int uiModeNameLen);

    int OA_SetInt(byte[] pcModeName, String pcModeParam, int iIntValue);

    int OA_SetFloat(byte[] pcModeName, String pcModeParam, float fFloatValue);

    int OA_SetString(byte[] pcModeName, String pcModeParam, String pcStringValue, int uiStringLen);

    int OA_GetInt(byte[] pcModeName, byte[] pcModeParam, IntBuffer iIntValue);

    int OA_GetFloat(byte[] pcModeName, byte[] pcModeParam, FloatBuffer fFloatValue);

    int OA_GetString(byte[] pcModeName, byte[] pcModeParam, String pcStringValue, int uiStringLen);

    int Filter_SetMode(int mode);

    int Filter_GetMode(IntBuffer mode);

    int Filter_SetThreshold(float threshold);

    int Filter_GetThreshold(FloatBuffer threshold);

    int Filter_SetDataAveragingMode(int mode);

    int Filter_GetDataAveragingMode(IntBuffer mode);

    int Filter_SetAveragingFrameCount(int frames);

    int Filter_GetAveragingFrameCount(IntBuffer frames);

    int Filter_SetAveragingFactor(int averagingFactor);

    int Filter_GetAveragingFactor(IntBuffer averagingFactor);

    int PostProcessNoiseFilter(NativeLongByReference pInputImage, NativeLongByReference pOutputImage, int iOutputBufferSize, int iBaseline, int iMode, float fThreshold, int iHeight, int iWidth);

    int PostProcessCountConvert(NativeLongByReference pInputImage, NativeLongByReference pOutputImage, int iOutputBufferSize, int iNumImages, int iBaseline, int iMode, int iEmGain, float fQE, float fSensitivity, int iHeight, int iWidth);

    int PostProcessPhotonCounting(NativeLongByReference pInputImage, NativeLongByReference pOutputImage, int iOutputBufferSize, int iNumImages, int iNumframes, int iNumberOfThresholds, FloatBuffer pfThreshold, int iHeight, int iWidth);

    int PostProcessDataAveraging(NativeLongByReference pInputImage, NativeLongByReference pOutputImage, int iOutputBufferSize, int iNumImages, int iAveragingFilterMode, int iHeight, int iWidth, int iFrameCount, int iAveragingFactor);

    interface AT_VersionInfoId {
        int AT_SDKVersion          = 0x40000000;
        int AT_DeviceDriverVersion = 0x40000001;
    }

    interface AT_DDGLiteChannelId {
        int AT_DDGLite_ChannelA = 0x40000000;
        int AT_DDGLite_ChannelB = 0x40000001;
        int AT_DDGLite_ChannelC = 0x40000002;
    }

    class ANDORCAPS extends Structure {
        public NativeLong ulSize;
        public NativeLong ulAcqModes;
        public NativeLong ulReadModes;
        public NativeLong ulTriggerModes;
        public NativeLong ulCameraType;
        public NativeLong ulPixelMode;
        public NativeLong ulSetFunctions;
        public NativeLong ulGetFunctions;
        public NativeLong ulFeatures;
        public NativeLong ulPCICard;
        public NativeLong ulEMGainCapability;
        public NativeLong ulFTReadModes;
        public NativeLong ulFeatures2;

        public ANDORCAPS() {
            super();
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("ulSize", "ulAcqModes", "ulReadModes", "ulTriggerModes", "ulCameraType", "ulPixelMode", "ulSetFunctions", "ulGetFunctions", "ulFeatures", "ulPCICard", "ulEMGainCapability", "ulFTReadModes", "ulFeatures2");
        }

        public static class ByReference extends ANDORCAPS implements Structure.ByReference {

        }

        public static class ByValue extends ANDORCAPS implements Structure.ByValue {

        }
    }

    class COLORDEMOSAICINFO extends Structure {
        public int iX;
        public int iY;
        public int iAlgorithm;
        public int iXPhase;
        public int iYPhase;
        public int iBackground;

        public COLORDEMOSAICINFO() {
            super();
        }

        public COLORDEMOSAICINFO(int iX, int iY, int iAlgorithm, int iXPhase, int iYPhase, int iBackground) {
            super();
            this.iX          = iX;
            this.iY          = iY;
            this.iAlgorithm  = iAlgorithm;
            this.iXPhase     = iXPhase;
            this.iYPhase     = iYPhase;
            this.iBackground = iBackground;
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("iX", "iY", "iAlgorithm", "iXPhase", "iYPhase", "iBackground");
        }

        public static class ByReference extends COLORDEMOSAICINFO implements Structure.ByReference {

        }

        public static class ByValue extends COLORDEMOSAICINFO implements Structure.ByValue {

        }

    }

    class WHITEBALANCEINFO extends Structure {
        public int iSize;
        public int iX;
        public int iY;
        public int iAlgorithm;
        public int iROI_left;
        public int iROI_right;
        public int iROI_top;
        public int iROI_bottom;
        public int iOperation;

        public WHITEBALANCEINFO() {
            super();
        }

        public WHITEBALANCEINFO(int iSize, int iX, int iY, int iAlgorithm, int iROI_left, int iROI_right, int iROI_top, int iROI_bottom, int iOperation) {
            super();
            this.iSize       = iSize;
            this.iX          = iX;
            this.iY          = iY;
            this.iAlgorithm  = iAlgorithm;
            this.iROI_left   = iROI_left;
            this.iROI_right  = iROI_right;
            this.iROI_top    = iROI_top;
            this.iROI_bottom = iROI_bottom;
            this.iOperation  = iOperation;
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("iSize", "iX", "iY", "iAlgorithm", "iROI_left", "iROI_right", "iROI_top", "iROI_bottom", "iOperation");
        }

        public static class ByReference extends WHITEBALANCEINFO implements Structure.ByReference {

        }

        public static class ByValue extends WHITEBALANCEINFO implements Structure.ByValue {

        }

    }

}
