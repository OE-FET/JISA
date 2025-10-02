package jisa.devices.camera.nat;

import com.sun.jna.*;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.PointerByReference;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.List;

public interface LucamAPI extends Library {

    NativeLong LUCAM_PROP_KNEE1_EXPOSURE                         = new NativeLong(96, true);
    NativeLong LUCAM_PROP_STILL_EXPOSURE_DELAY                   = new NativeLong(100, true);
    NativeLong LUCAM_PROP_FLAG_RED                               = new NativeLong(0x00000001, true);
    NativeLong LUCAM_PROP_FLAG_GREEN1                            = new NativeLong(0x00000002, true);
    NativeLong LUCAM_PROP_THRESHOLD_LOW                          = new NativeLong(165, true);
    NativeLong LUCAM_PROP_FLAG_GREEN2                            = new NativeLong(0x00000004, true);
    NativeLong LUCAM_PROP_AUTO_IRIS_MAX                          = new NativeLong(123, true);
    NativeLong LUCAM_PROP_GAINHDR                                = new NativeLong(189, true);
    NativeLong LUCAM_SHUTTER_TYPE_ROLLING                        = new NativeLong(1, true);
    NativeLong LUCAM_PROP_TILT                                   = new NativeLong(17, true);
    NativeLong LUCAM_PROP_FLAG_AUTO                              = new NativeLong(0x40000000, true);
    NativeLong LUCAM_PROP_MAX_WIDTH                              = new NativeLong(81, true);
    NativeLong AUTO_ALGORITHM_SIMPLE_AVERAGING                   = new NativeLong(0, true);
    NativeLong LUCAM_PROP_CONTRAST                               = new NativeLong(1, true);
    NativeLong STOP_AVI                                          = new NativeLong(0, true);
    NativeLong LUCAM_CM_CUSTOM                                   = new NativeLong(15, true);
    NativeLong AVI_RAW_LUMENERA                                  = new NativeLong(0, true);
    NativeLong LUCAM_CF_MONO                                     = new NativeLong(0, true);
    NativeLong LUCAM_PROP_MAX_FRAME_RATE                         = new NativeLong(184, true);
    NativeLong LUCAM_PROP_FLAG_MEMORY_READBACK                   = new NativeLong(0x08000000, true);
    NativeLong LUCAM_PROP_KNEE2_LEVEL                            = new NativeLong(163, true);
    NativeLong LUCAM_DM_HIGHER_QUALITY                           = new NativeLong(3, true);
    NativeLong LUCAM_PROP_DIGITAL_HUE                            = new NativeLong(68, true);
    NativeLong START_STREAMING                                   = new NativeLong(1, true);
    NativeLong LUCAM_FRAME_FORMAT_FLAGS_BINNING                  = new NativeLong(0x0001, true);
    NativeLong LUCAM_PROP_DIGITAL_GAIN_RED                       = new NativeLong(72, true);
    NativeLong LUCAM_PROP_STILL_STROBE_DELAY                     = new NativeLong(56, true);
    NativeLong LUCAM_PROP_FLAG_HW_ENABLE                         = new NativeLong(0x40000000, true);
    NativeLong LUCAM_PROP_GAMMA                                  = new NativeLong(5, true);
    NativeLong START_RGBSTREAM                                   = new NativeLong(6, true);
    NativeLong LUCAM_PROP_AUTO_EXP_MAXIMUM                       = new NativeLong(107, true);
    NativeLong HDR_PIECEWISE_LINEAR_RESPONSE                     = new NativeLong(5, true);
    NativeLong LUCAM_PROP_TRIGGER                                = new NativeLong(110, true);
    NativeLong LUCAM_PF_YUV422                                   = new NativeLong(3, true);
    NativeLong LUCAM_SHUTTER_TYPE_GLOBAL                         = new NativeLong(0, true);
    NativeLong LUCAM_PROP_IRIS_STEPS_COUNT                       = new NativeLong(188, true);
    NativeLong LUCAM_EVENT_START_OF_READOUT                      = new NativeLong(2, true);
    NativeLong LUCAM_PROP_AUTO_GAIN_MAXIMUM                      = new NativeLong(170, true);
    NativeLong LUCAM_PROP_CORRECTION_MATRIX                      = new NativeLong(65, true);
    NativeLong LUCAM_PROP_STILL_STROBE_DURATION                  = new NativeLong(116, true);
    NativeLong LUCAM_PROP_THRESHOLD                              = new NativeLong(101, true);
    NativeLong LUCAM_CM_DAYLIGHT                                 = new NativeLong(2, true);
    NativeLong LUCAM_PF_FILTER                                   = new NativeLong(5, true);
    NativeLong LUCAM_EVENT_GPI3_CHANGED                          = new NativeLong(6, true);
    NativeLong LUCAM_PROP_FLAG_USE_FOR_SNAPSHOTS                 = new NativeLong(0x04000000, true);
    NativeLong LUCAM_PROP_GAIN_MAGENTA                           = new NativeLong(41, true);
    NativeLong LUCAM_PF_COUNT                                    = new NativeLong(4, true);
    NativeLong LUCAM_PROP_GAIN_BLUE                              = new NativeLong(42, true);
    NativeLong LUCAM_PROP_FLIPPING                               = new NativeLong(66, true);
    NativeLong HDR_ENABLED_PRIMARY_IMAGE                         = new NativeLong(1, true);
    NativeLong AVI_STANDARD_32                                   = new NativeLong(2, true);
    NativeLong LUCAM_PROP_SNAPSHOT_COUNT                         = new NativeLong(120, true);
    NativeLong HDR_ENABLED_SECONDARY_IMAGE                       = new NativeLong(2, true);
    NativeLong LUCAM_PROP_FLAG_STROBE_FROM_START_OF_EXPOSURE     = new NativeLong(0x20000000, true);
    NativeLong LUCAM_PROP_FLAG_POLARITY                          = new NativeLong(0x10000000, true);
    NativeLong LUCAM_PROP_EXPOSURE_INTERVAL                      = new NativeLong(113, true);
    NativeLong LUCAM_CM_IDENTITY                                 = new NativeLong(14, true);
    NativeLong LUCAM_PROP_STILL_GAIN_CYAN                        = new NativeLong(55, true);
    NativeLong LUCAM_PROP_STILL_GAIN_RED                         = new NativeLong(52, true);
    NativeLong START_DISPLAY                                     = new NativeLong(2, true);
    NativeLong LUCAM_PROP_LSC_X                                  = new NativeLong(121, true);
    NativeLong LUCAM_PROP_LSC_Y                                  = new NativeLong(122, true);
    NativeLong START_AVI                                         = new NativeLong(1, true);
    NativeLong LUCAM_API_RGB24_FORMAT                            = new NativeLong(1, true);
    NativeLong LUCAM_PROP_FLAG_BUSY                              = new NativeLong(0x00100000, true);
    NativeLong LUCAM_PROP_STILL_KNEE2_EXPOSURE                   = new NativeLong(97, true);
    NativeLong LUCAM_PROP_GAIN_YELLOW2                           = new NativeLong(44, true);
    NativeLong LUCAM_PROP_GAIN_YELLOW1                           = new NativeLong(43, true);
    NativeLong LUCAM_CF_BAYER_YMCY                               = new NativeLong(18, true);
    NativeLong LUCAM_PROP_STILL_GAIN_GREEN1                      = new NativeLong(53, true);
    NativeLong LUCAM_PROP_STILL_GAIN_GREEN2                      = new NativeLong(54, true);
    NativeLong LUCAM_PROP_VIDEO_CLOCK_SPEED                      = new NativeLong(126, true);
    NativeLong LUCAM_PROP_LUMINANCE                              = new NativeLong(169, true);
    NativeLong LUCAM_PROP_HOST_AUTO_WB_ALGORITHM                 = new NativeLong(258, true);
    NativeLong LUCAM_PROP_GEV_IPCONFIG_PERSISTENT_SUBNETMASK     = new NativeLong(516, true);
    NativeLong LUCAM_PROP_GAIN_GREEN1                            = new NativeLong(43, true);
    NativeLong LUCAM_PROP_GAIN_GREEN2                            = new NativeLong(44, true);
    NativeLong LUCAM_PROP_STILL_KNEE1_EXPOSURE                   = new NativeLong(96, true);
    NativeLong LUCAM_PF_12_PACKED                                = new NativeLong(12, true);
    NativeLong TAP_CONFIGURATION_SINGLE                          = new NativeLong(0, true);
    NativeLong LUCAM_CM_DAYLIGHT_H_AND_E                         = new NativeLong(7, true);
    NativeLong LUCAM_PROP_TRIGGER_MODE                           = new NativeLong(173, true);
    NativeLong LUCAM_CM_FLUORESCENT                              = new NativeLong(1, true);
    NativeLong LUCAM_PROP_STILL_GAINHDR                          = new NativeLong(190, true);
    NativeLong LUCAM_PROP_MAX_HEIGHT                             = new NativeLong(82, true);
    NativeLong LUCAM_CM_LED_H_AND_E                              = new NativeLong(8, true);
    NativeLong LUCAM_PROP_FLAG_ALTERNATE                         = new NativeLong(0x00080000, true);
    NativeLong LUCAM_PROP_GAIN_CYAN                              = new NativeLong(42, true);
    NativeLong LUCAM_PROP_ABS_FOCUS                              = new NativeLong(85, true);
    NativeLong AUTO_ALGORITHM_MACROBLOCKS                        = new NativeLong(2, true);
    NativeLong LUCAM_PROP_FLIPPING_X                             = new NativeLong(1, true);
    NativeLong TAP_CONFIGURATION_DUAL                            = new NativeLong(1, true);
    NativeLong LUCAM_PROP_FLIPPING_Y                             = new NativeLong(2, true);
    NativeLong TRIGGER_MODE_NORMAL                               = new NativeLong(0, true);
    NativeLong HDR_ENABLED_AVERAGED_IMAGE                        = new NativeLong(4, true);
    NativeLong LUCAM_PROP_TAP_CONFIGURATION                      = new NativeLong(176, true);
    NativeLong LUCAM_PROP_GEV_IPCONFIG_LLA                       = new NativeLong(512, true);
    NativeLong LUCAM_PROP_FLAG_BACKLASH_COMPENSATION             = new NativeLong(0x20000000L, true);
    NativeLong LUCAM_PROP_UNIT_WIDTH                             = new NativeLong(83, true);
    NativeLong LUCAM_PROP_FOCAL_LENGTH                           = new NativeLong(174, true);
    NativeLong LUCAM_PROP_FLAG_MASTER                            = new NativeLong(0x40000000L, true);
    NativeLong LUCAM_PROP_FLAG_READONLY                          = new NativeLong(0x00010000L, true);
    NativeLong LUCAM_PROP_AUTO_EXP_TARGET                        = new NativeLong(103, true);
    NativeLong LUCAM_PROP_ROLL                                   = new NativeLong(18, true);
    NativeLong TAP_CONFIGURATION_1X1                             = new NativeLong(0, true);
    NativeLong LUCAM_PROP_FLAG_UNKNOWN_MINIMUM                   = new NativeLong(0x00010000L, true);
    NativeLong TAP_CONFIGURATION_1X2                             = new NativeLong(2, true);
    NativeLong LUCAM_PROP_STILL_GAIN_MAGENTA                     = new NativeLong(52, true);
    NativeLong LUCAM_PROP_UNIT_HEIGHT                            = new NativeLong(84, true);
    NativeLong LUCAM_PROP_TEMPERATURE2                           = new NativeLong(167, true);
    NativeLong AUTO_ALGORITHM_HISTOGRAM                          = new NativeLong(1, true);
    NativeLong LUCAM_PROP_DIGITAL_SATURATION                     = new NativeLong(67, true);
    NativeLong LUCAM_PF_8                                        = new NativeLong(0, true);
    NativeLong LUCAM_PROP_GEV_IPCONFIG_PERSISTENT_DEFAULTGATEWAY = new NativeLong(517, true);
    NativeLong LUCAM_DM_HIGH_QUALITY                             = new NativeLong(2, true);
    NativeLong LUCAM_DM_SIMPLE                                   = new NativeLong(8, true);
    NativeLong LUCAM_DM_FAST                                     = new NativeLong(1, true);
    NativeLong LUCAM_PROP_EXPOSURE                               = new NativeLong(20, true);
    NativeLong LUCAM_DM_NONE                                     = new NativeLong(0, true);
    NativeLong LUCAM_PROP_PWM                                    = new NativeLong(114, true);
    NativeLong LUCAM_PROP_TRIGGER_PIN                            = new NativeLong(110, true);
    NativeLong LUCAM_PROP_MEMORY                                 = new NativeLong(115, true);
    NativeLong LUCAM_PROP_BRIGHTNESS                             = new NativeLong(0, true);
    NativeLong LUCAM_PF_32                                       = new NativeLong(6, true);
    NativeLong TAP_CONFIGURATION_2X1                             = new NativeLong(1, true);
    NativeLong TAP_CONFIGURATION_2X2                             = new NativeLong(4, true);
    NativeLong LUCAM_CF_BAYER_BGGR                               = new NativeLong(11, true);
    NativeLong LUCAM_PROP_AUTO_GAIN_MINIMUM                      = new NativeLong(186, true);
    NativeLong LUCAM_EVENT_GPI2_CHANGED                          = new NativeLong(5, true);
    NativeLong LUCAM_EVENT_DEVICE_SURPRISE_REMOVAL               = new NativeLong(32, true);
    NativeLong LUCAM_PROP_ZOOM                                   = new NativeLong(19, true);
    NativeLong LUCAM_PROP_FOCUS                                  = new NativeLong(22, true);
    NativeLong LUCAM_PROP_FLIPPING_NONE                          = new NativeLong(0, true);
    NativeLong LUCAM_PROP_IRIS_LATENCY                           = new NativeLong(175, true);
    NativeLong LUCAM_PROP_VIDEO_TRIGGER                          = new NativeLong(125, true);
    NativeLong LUCAM_METADATA_TIMESTAMP                          = new NativeLong(2, true);
    NativeLong LUCAM_CM_INCANDESCENT                             = new NativeLong(3, true);
    NativeLong LUCAM_PROP_JPEG_QUALITY                           = new NativeLong(256, true);
    NativeLong LUCAM_EXTERN_INTERFACE_USB3                       = new NativeLong(3, true);
    NativeLong LUCAM_PROP_STILL_GAIN                             = new NativeLong(51, true);
    NativeLong LUCAM_PROP_STILL_TAP_CONFIGURATION                = new NativeLong(177, true);
    NativeLong LUCAM_PF_48                                       = new NativeLong(7, true);
    NativeLong LUCAM_EXTERN_INTERFACE_USB2                       = new NativeLong(2, true);
    NativeLong LUCAM_CF_BAYER_YCMY                               = new NativeLong(17, true);
    NativeLong LUCAM_EXTERN_INTERFACE_USB1                       = new NativeLong(1, true);
    NativeLong LUCAM_PROP_STILL_GAIN_BLUE                        = new NativeLong(55, true);
    NativeLong LUCAM_CM_LED                                      = new NativeLong(6, true);
    NativeLong LUCAM_PROP_DIGITAL_WHITEBALANCE_U                 = new NativeLong(69, true);
    NativeLong LUCAM_API_RGB32_FORMAT                            = new NativeLong(1, true);
    NativeLong LUCAM_PROP_DIGITAL_WHITEBALANCE_V                 = new NativeLong(70, true);
    NativeLong LUCAM_CF_BAYER_MYYC                               = new NativeLong(19, true);
    NativeLong PAUSE_AVI                                         = new NativeLong(2, true);
    NativeLong LUCAM_PF_16                                       = new NativeLong(1, true);
    NativeLong LUCAM_PROP_FAN                                    = new NativeLong(118, true);
    NativeLong LUCAM_RGB_FORMAT_BMP                              = new NativeLong(1, true);
    NativeLong LUCAM_PROP_KNEE2_EXPOSURE                         = new NativeLong(97, true);
    NativeLong LUCAM_PROP_IRIS                                   = new NativeLong(21, true);
    NativeLong LUCAM_PROP_SHARPNESS                              = new NativeLong(4, true);
    NativeLong LUCAM_CM_XENON_FLASH                              = new NativeLong(4, true);
    NativeLong LUCAM_PROP_HUE                                    = new NativeLong(2, true);
    NativeLong LUCAM_PF_24                                       = new NativeLong(2, true);
    NativeLong TAP_CONFIGURATION_QUAD                            = new NativeLong(4, true);
    NativeLong LUCAM_PROP_DIGITAL_GAIN_GREEN                     = new NativeLong(73, true);
    NativeLong LUCAM_PROP_GEV_SCPD                               = new NativeLong(518, true);
    NativeLong LUCAM_RGB_FORMAT_RGB                              = new NativeLong(0, true);
    NativeLong LUCAM_PROP_THRESHOLD_HIGH                         = new NativeLong(166, true);
    NativeLong LUCAM_PROP_STILL_GAIN_YELLOW2                     = new NativeLong(54, true);
    NativeLong LUCAM_PROP_STILL_GAIN_YELLOW1                     = new NativeLong(53, true);
    NativeLong LUCAM_PROP_DIGITAL_GAIN                           = new NativeLong(71, true);
    NativeLong AVI_STANDARD_24                                   = new NativeLong(1, true);
    NativeLong LUCAM_PROP_STROBE_PIN                             = new NativeLong(172, true);
    NativeLong LUCAM_API_RGB48_FORMAT                            = new NativeLong(1, true);
    NativeLong LUCAM_PROP_GAIN_RED                               = new NativeLong(41, true);
    NativeLong AVI_STANDARD_8                                    = new NativeLong(4, true);
    NativeLong LUCAM_CF_BAYER_GRBG                               = new NativeLong(9, true);
    NativeLong LUCAM_PROP_SYNC_MODE                              = new NativeLong(119, true);
    NativeLong LUCAM_PF_10_PACKED                                = new NativeLong(10, true);
    NativeLong LUCAM_PROP_GEV_IPCONFIG_PERSISTENT                = new NativeLong(514, true);
    NativeLong LUCAM_EVENT_GPI4_CHANGED                          = new NativeLong(7, true);
    NativeLong LUCAM_PROP_TEMPERATURE                            = new NativeLong(108, true);
    NativeLong LUCAM_PROP_LIGHT_FREQUENCY                        = new NativeLong(168, true);
    NativeLong HDR_DISABLED                                      = new NativeLong(0, true);
    NativeLong TRIGGER_MODE_BULB                                 = new NativeLong(1, true);
    NativeLong LUCAM_CF_BAYER_GBRG                               = new NativeLong(10, true);
    NativeLong PAUSE_STREAM                                      = new NativeLong(3, true);
    NativeLong LUCAM_CM_NONE                                     = new NativeLong(0, true);
    NativeLong LUCAM_PROP_SNAPSHOT_CLOCK_SPEED                   = new NativeLong(106, true);
    NativeLong LUCAM_PROP_STILL_EXPOSURE                         = new NativeLong(50, true);
    NativeLong LUCAM_PROP_COLOR_FORMAT                           = new NativeLong(80, true);
    NativeLong LUCAM_PROP_KNEE1_LEVEL                            = new NativeLong(99, true);
    NativeLong LUCAM_PROP_GAIN                                   = new NativeLong(40, true);
    NativeLong LUCAM_PROP_STILL_KNEE3_EXPOSURE                   = new NativeLong(98, true);
    NativeLong LUCAM_PROP_FLIPPING_XY                            = new NativeLong(3, true);
    NativeLong LUCAM_PROP_FLAG_SW_TRIGGER                        = new NativeLong(0x00200000, true);
    NativeLong AVI_XVID_24                                       = new NativeLong(3, true);
    NativeLong LUCAM_EVENT_GPI1_CHANGED                          = new NativeLong(4, true);
    NativeLong LUCAM_CF_BAYER_RGGB                               = new NativeLong(8, true);
    NativeLong LUCAM_PROP_GEV_IPCONFIG_PERSISTENT_IPADDRESS      = new NativeLong(515, true);
    NativeLong HDR_ENABLED_MERGED_IMAGE                          = new NativeLong(3, true);
    NativeLong LUCAM_PROP_FLAG_LITTLE_ENDIAN                     = new NativeLong(0x80000000L, true);
    NativeLong LUCAM_EXTERN_INTERFACE_GIGEVISION                 = new NativeLong(4, true);
    NativeLong LUCAM_PROP_FRAME_GATE                             = new NativeLong(112, true);
    NativeLong LUCAM_PROP_FLAG_BLUE                              = new NativeLong(0x00000008, true);
    NativeLong LUCAM_CM_HALOGEN                                  = new NativeLong(5, true);
    NativeLong LUCAM_PROP_PAN                                    = new NativeLong(16, true);
    NativeLong LUCAM_PROP_DEMOSAICING_METHOD                     = new NativeLong(64, true);
    NativeLong LUCAM_METADATA_FRAME_COUNTER                      = new NativeLong(1, true);
    NativeLong LUCAM_PROP_BLACK_LEVEL                            = new NativeLong(86, true);
    NativeLong LUCAM_PROP_LENS_STABILIZATION                     = new NativeLong(124, true);
    NativeLong LUCAM_PROP_SATURATION                             = new NativeLong(3, true);
    NativeLong LUCAM_PROP_VIDEO_KNEE                             = new NativeLong(99, true);
    NativeLong LUCAM_PROP_TIMESTAMPS                             = new NativeLong(105, true);
    NativeLong LUCAM_PROP_FLAG_UNKNOWN_MAXIMUM                   = new NativeLong(0x00020000L, true);
    NativeLong LUCAM_PROP_FLAG_USE                               = new NativeLong(0x80000000L, true);
    NativeLong LUCAM_PROP_HOST_AUTO_EX_ALGORITHM                 = new NativeLong(259, true);
    NativeLong LUCAM_PROP_DIGITAL_GAIN_BLUE                      = new NativeLong(74, true);
    NativeLong LUCAM_PROP_GEV_IPCONFIG_DHCP                      = new NativeLong(513, true);
    NativeLong STOP_STREAMING                                    = new NativeLong(0, true);
    NativeLong LUCAM_PROP_FLAG_SEQUENCABLE                       = new NativeLong(0x08000000L, true);
    NativeLong LUCAM_CF_BAYER_CYYM                               = new NativeLong(16, true);

    NativeLong LucamNumCameras();

    NativeLong LucamEnumCameras(LucamAPI.LUCAM_VERSION pVersionsArray, NativeLong arrayCount);

    Pointer LucamCameraOpen(NativeLong index);

    boolean LucamCameraClose(Pointer hCamera);

    boolean LucamCameraReset(Pointer hCamera);

    NativeLong LucamGetLastError();

    NativeLong LucamGetLastErrorForCamera(Pointer hCamera);

    boolean LucamQueryVersion(Pointer hCamera, LucamAPI.LUCAM_VERSION pVersion);

    boolean LucamQueryExternInterface(Pointer hCamera, NativeLongByReference pExternInterface);

    boolean LucamGetCameraId(Pointer hCamera, NativeLongByReference pId);

    boolean LucamGetHardwareRevision(Pointer hCamera, NativeLongByReference pRevision);


    boolean LucamGetProperty(Pointer hCamera, NativeLong propertyId, FloatBuffer pValue, NativeLongByReference pFlags);

    boolean LucamSetProperty(Pointer hCamera, NativeLong propertyId, float value, NativeLong flags);


    boolean LucamPropertyRange(Pointer hCamera, NativeLong propertyId, FloatBuffer pMin, FloatBuffer pMax, FloatBuffer pDefault, NativeLongByReference pFlags);

    boolean LucamSetFormat(Pointer hCamera, LucamAPI.LUCAM_FRAME_FORMAT pFormat, float frameRate);


    boolean LucamGetFormat(Pointer hCamera, LucamAPI.LUCAM_FRAME_FORMAT pFormat, FloatBuffer pFrameRate);


    NativeLong LucamEnumAvailableFrameRates(Pointer hCamera, NativeLong entryCount, FloatBuffer pAvailableFrameRates);

    boolean LucamStreamVideoControl(Pointer hCamera, NativeLong controlType, Pointer hWnd);


    boolean LucamTakeVideo(Pointer hCamera, long numFrames, ByteBuffer pData);


    boolean LucamTakeVideoEx(Pointer hCamera, ByteBuffer pData, NativeLongByReference pLength, NativeLong timeout);

    boolean LucamCancelTakeVideo(Pointer hCamera);


    boolean LucamTakeSnapshot(Pointer hCamera, LucamAPI.LUCAM_SNAPSHOT pSettings, ByteBuffer pData);

    NativeLong LucamAddStreamingCallback(Pointer hCamera, LucamAPI.LucamAddStreamingCallback_VideoFilter_callback VideoFilter, Pointer pCBContext);

    boolean LucamRemoveStreamingCallback(Pointer hCamera, NativeLong callbackId);

    NativeLong LucamAddRgbPreviewCallback(Pointer hCamera, LucamAPI.LucamAddRgbPreviewCallback_RgbVideoFilter_callback RgbVideoFilter, Pointer pContext, NativeLong rgbPixelFormat);

    boolean LucamRemoveRgbPreviewCallback(Pointer hCamera, NativeLong callbackId);

    boolean LucamQueryRgbPreviewPixelFormat(Pointer hCamera, NativeLongByReference pRgbPixelFormat);

    NativeLong LucamAddSnapshotCallback(Pointer hCamera, LucamAPI.LucamAddSnapshotCallback_SnapshotCallback_callback SnapshotCallback, Pointer pCBContext);

    boolean LucamRemoveSnapshotCallback(Pointer hCamera, NativeLong callbackId);

    boolean LucamConvertFrameToGreyscale8Ex(Pointer hCamera, ByteBuffer pDest, byte[] pSrc, LucamAPI.LUCAM_IMAGE_FORMAT pImageFormat, LucamAPI.LUCAM_CONVERSION_PARAMS pParams);

    boolean LucamConvertFrameToGreyscale16Ex(Pointer hCamera, ShortBuffer pDest, short[] pSrc, LucamAPI.LUCAM_IMAGE_FORMAT pImageFormat, LucamAPI.LUCAM_CONVERSION_PARAMS pParams);

    boolean LucamConvertFrameToRgb24(Pointer hCamera, ByteBuffer pDest, ByteBuffer pSrc, NativeLong width, NativeLong height, NativeLong pixelFormat, LucamAPI.LUCAM_CONVERSION pParams);

    boolean LucamConvertFrameToRgb32(Pointer hCamera, ByteBuffer pDest, ByteBuffer pSrc, NativeLong width, NativeLong height, NativeLong pixelFormat, LucamAPI.LUCAM_CONVERSION pParams);

    boolean LucamConvertFrameToRgb48(Pointer hCamera, ShortBuffer pDest, ShortBuffer pSrc, NativeLong width, NativeLong height, NativeLong pixelFormat, LucamAPI.LUCAM_CONVERSION pParams);

    boolean LucamConvertFrameToRgb48(Pointer hCamera, ShortBuffer pDest, ShortBuffer pSrc, long width, long height, long pixelFormat, LucamAPI.LUCAM_CONVERSION pParams);

    boolean LucamConvertFrameToRgb24Ex(Pointer hCamera, ByteBuffer pDest, byte[] pSrc, LucamAPI.LUCAM_IMAGE_FORMAT pImageFormat, LucamAPI.LUCAM_CONVERSION_PARAMS pParams);

    boolean LucamConvertFrameToRgb32Ex(Pointer hCamera, ByteBuffer pDest, byte[] pSrc, LucamAPI.LUCAM_IMAGE_FORMAT pImageFormat, LucamAPI.LUCAM_CONVERSION_PARAMS pParams);

    boolean LucamConvertFrameToRgb48Ex(Pointer hCamera, ShortBuffer pDest, short[] pSrc, LucamAPI.LUCAM_IMAGE_FORMAT pImageFormat, LucamAPI.LUCAM_CONVERSION_PARAMS pParams);

    boolean LucamSetupCustomMatrix(Pointer hCamera, FloatBuffer pMatrix);

    boolean LucamGetCurrentMatrix(Pointer hCamera, FloatBuffer pMatrix);

    boolean LucamEnableFastFrames(Pointer hCamera, LucamAPI.LUCAM_SNAPSHOT pSettings);

    boolean LucamTakeFastFrame(Pointer hCamera, ByteBuffer pData);

    boolean LucamForceTakeFastFrame(Pointer hCamera, ByteBuffer pData);

    boolean LucamTakeFastFrameNoTrigger(Pointer hCamera, ByteBuffer pData);

    boolean LucamDisableFastFrames(Pointer hCamera);

    boolean LucamTriggerFastFrame(Pointer hCamera);

    boolean LucamSetTriggerMode(Pointer hCamera);

    boolean LucamCancelTakeFastFrame(Pointer hCamera);

    boolean LucamGetTruePixelDepth(Pointer hCamera, NativeLongByReference pCount);

    boolean LucamGpioRead(Pointer hCamera, ByteBuffer pGpoValues, ByteBuffer pGpiValues);

    boolean LucamGpioWrite(Pointer hCamera, byte gpoValues);

    boolean LucamGpoSelect(Pointer hCamera, byte gpoEnable);

    boolean LucamGpioConfigure(Pointer hCamera, byte enableOutput);

    boolean LucamOneShotAutoExposure(Pointer hCamera, byte target, NativeLong startX, NativeLong startY, NativeLong width, NativeLong height);

    boolean LucamOneShotAutoGain(Pointer hCamera, byte target, NativeLong startX, NativeLong startY, NativeLong width, NativeLong height);

    boolean LucamOneShotAutoWhiteBalance(Pointer hCamera, NativeLong startX, NativeLong startY, NativeLong width, NativeLong height);

    boolean LucamDigitalWhiteBalance(Pointer hCamera, NativeLong startX, NativeLong startY, NativeLong width, NativeLong height);

    boolean LucamGetVideoImageFormat(Pointer hCamera, LucamAPI.LUCAM_IMAGE_FORMAT pImageFormat);

    boolean LucamGetStillImageFormat(Pointer hCamera, LucamAPI.LUCAM_IMAGE_FORMAT pImageFormat);

    boolean LucamPerformDualTapCorrection(Pointer hCamera, ByteBuffer pFrame, LucamAPI.LUCAM_IMAGE_FORMAT pImageFormat);

    boolean LucamPerformMultiTapCorrection(Pointer hCamera, ByteBuffer pFrame, LucamAPI.LUCAM_IMAGE_FORMAT pImageFormat);

    boolean LucamSaveImageEx(Pointer hCamera, NativeLong width, NativeLong height, NativeLong pixelFormat, ByteBuffer pData, byte[] pFilename);

    boolean LucamSaveImageWEx(Pointer hCamera, NativeLong width, NativeLong height, NativeLong pixelFormat, ByteBuffer pData, short[] pFilename);

    boolean LucamGetSubsampleBinDescription(Pointer hCamera, LucamAPI.LUCAM_SS_BIN_DESC pDesc);

    boolean LucamSaveImage(NativeLong width, NativeLong height, NativeLong pixelFormat, ByteBuffer pData, byte[] pFilename);

    boolean LucamSaveImageW(NativeLong width, NativeLong height, NativeLong pixelFormat, ByteBuffer pData, short[] pFilename);

    NativeLong LucamQueryStats(Pointer hCamera, LucamAPI.LUCAM_STREAM_STATS pStats, NativeLong sizeofStats);

    boolean LucamDisplayPropertyPage(Pointer hCamera, Pointer hParentWnd);

    boolean LucamDisplayVideoFormatPage(Pointer hCamera, Pointer hParentWnd);

    boolean LucamQueryDisplayFrameRate(Pointer hCamera, FloatBuffer pValue);

    boolean LucamCreateDisplayWindow(Pointer hCamera, String lpTitle, int dwStyle, int x, int y, int width, int height, Pointer hParent, Pointer childId);

    boolean LucamAdjustDisplayWindow(Pointer hCamera, String lpTitle, int x, int y, int width, int height);

    boolean LucamDestroyDisplayWindow(Pointer hCamera);

    boolean LucamReadRegister(Pointer hCamera, NativeLong address, NativeLong numReg, NativeLongByReference pValue);

    boolean LucamWriteRegister(Pointer hCamera, NativeLong address, NativeLong numReg, NativeLongByReference pValue);

    boolean LucamConvertFrameToGreyscale8(Pointer hCamera, ByteBuffer pDest, ByteBuffer pSrc, NativeLong width, NativeLong height, NativeLong pixelFormat, LucamAPI.LUCAM_CONVERSION pParams);

    boolean LucamConvertFrameToGreyscale16(Pointer hCamera, ShortBuffer pDest, ShortBuffer pSrc, NativeLong width, NativeLong height, NativeLong pixelFormat, LucamAPI.LUCAM_CONVERSION pParams);

    void LucamConvertBmp24ToRgb24(ByteBuffer pFrame, NativeLong width, NativeLong height);

    boolean LucamStreamVideoControlAVI(Pointer hCamera, NativeLong controlType, WString pFileName, Pointer hWnd);

    boolean LucamConvertRawAVIToStdVideo(Pointer hCamera, short[] pOutputFileName, short[] pInputFileName, NativeLong outputType);

    Pointer LucamPreviewAVIOpen(short[] pFileName);

    boolean LucamPreviewAVIClose(Pointer hAVI);

    boolean LucamPreviewAVIControl(Pointer hAVI, NativeLong previewControlType, Pointer previewWindow);

    boolean LucamPreviewAVIGetDuration(Pointer hAVI, LongBuffer pDurationMinutes, LongBuffer pDurationSeconds, LongBuffer pDurationMilliseconds, LongBuffer pDurationMicroSeconds);

    boolean LucamPreviewAVIGetFrameCount(Pointer hAVI, LongBuffer pFrameCount);

    boolean LucamPreviewAVIGetFrameRate(Pointer hAVI, FloatBuffer pFrameRate);

    boolean LucamPreviewAVISetPositionFrame(Pointer hAVI, long pPositionFrame);

    boolean LucamPreviewAVIGetPositionFrame(Pointer hAVI, LongBuffer pPositionFrame);

    boolean LucamPreviewAVISetPositionTime(Pointer hAVI, long positionMinutes, long positionSeconds, long positionMilliSeconds, long positionMicroSeconds);

    boolean LucamPreviewAVIGetPositionTime(Pointer hAVI, LongBuffer pPositionMinutes, LongBuffer pPositionSeconds, LongBuffer pPositionMilliSeconds, LongBuffer pPositionMicroSeconds);

    boolean LucamPreviewAVIGetFormat(Pointer hAVI, NativeLongByReference width, NativeLongByReference height, NativeLongByReference fileType, NativeLongByReference bitDepth);

    Pointer LucamEnableSynchronousSnapshots(NativeLong numberOfCameras, PointerByReference phCameras, LucamAPI.LUCAM_SNAPSHOT.ByReference[] ppSettings);

    boolean LucamTakeSynchronousSnapshots(Pointer syncSnapsHandle, PointerByReference ppBuffers);

    boolean LucamDisableSynchronousSnapshots(Pointer syncSnapsHandle);

    boolean LucamLedSet(Pointer hCamera, NativeLong led);

    boolean LucamOneShotAutoExposureEx(Pointer hCamera, byte target, NativeLong startX, NativeLong startY, NativeLong width, NativeLong height, float lightingPeriod);

    boolean LucamOneShotAutoWhiteBalanceEx(Pointer hCamera, float redOverGreen, float blueOverGreen, NativeLong startX, NativeLong startY, NativeLong width, NativeLong height);

    boolean LucamDigitalWhiteBalanceEx(Pointer hCamera, float redOverGreen, float blueOverGreen, NativeLong startX, NativeLong startY, NativeLong width, NativeLong height);

    boolean LucamAdjustWhiteBalanceFromSnapshot(Pointer hCamera, LucamAPI.LUCAM_SNAPSHOT pSettings, ByteBuffer pData, float redOverGreen, float blueOverGreen, NativeLong startX, NativeLong startY, NativeLong width, NativeLong height);

    boolean LucamOneShotAutoIris(Pointer hCamera, byte target, NativeLong startX, NativeLong startY, NativeLong width, NativeLong height);

    boolean LucamContinuousAutoExposureEnable(Pointer hCamera, byte target, NativeLong startX, NativeLong startY, NativeLong width, NativeLong height, float lightingPeriod);

    boolean LucamContinuousAutoExposureDisable(Pointer hCamera);

    boolean LucamAutoFocusStart(Pointer hCamera, NativeLong startX, NativeLong startY, NativeLong width, NativeLong height, float putZeroThere1, float putZeroThere2, float putZeroThere3, LucamAPI.LucamAutoFocusStart_ProgressCallback_callback ProgressCallback, Pointer contextForCallback);

    boolean LucamAutoFocusWait(Pointer hCamera, int timeout);

    boolean LucamAutoFocusStop(Pointer hCamera);

    boolean LucamAutoFocusQueryProgress(Pointer hCamera, FloatBuffer pPercentageCompleted);

    boolean LucamInitAutoLens(Pointer hCamera);

    boolean LucamSetup8bitsLUT(Pointer hCamera, ByteBuffer pLut, NativeLong length);

    boolean LucamSetup8bitsColorLUT(Pointer hCamera, ByteBuffer pLut, NativeLong length);

    int LucamRs232Transmit(Pointer hCamera, ByteBuffer pData, int length);

    int LucamRs232Receive(Pointer hCamera, ByteBuffer pData, int maxLength);

    boolean LucamAddRs232Callback(Pointer hCamera, LucamAPI.LucamAddRs232Callback_callback_callback callback, Pointer context);

    void LucamRemoveRs232Callback(Pointer hCamera);

    boolean LucamPermanentBufferRead(Pointer hCamera, ByteBuffer pBuf, NativeLong offset, NativeLong length);

    boolean LucamPermanentBufferWrite(Pointer hCamera, ByteBuffer pBuf, NativeLong offset, NativeLong length);

    boolean LucamSetTimeout(Pointer hCamera, float timeout);

    boolean LucamGetTimestampFrequency(Pointer hCamera, LongBuffer pTimestampTickFrequency);

    boolean LucamGetTimestamp(Pointer hCamera, LongBuffer pTimestamp);

    boolean LucamSetTimestamp(Pointer hCamera, long timestamp);

    boolean LucamEnableTimestamp(Pointer hCamera, byte enable);

    boolean LucamIsTimestampEnabled(Pointer hCamera, boolean pIsEnabled);

    boolean LucamGetMetadata(Pointer hCamera, ByteBuffer pImageBuffer, LucamAPI.LUCAM_IMAGE_FORMAT pFormat, NativeLong metaDataIndex, LongBuffer pMetaData);

    boolean LucamGetDualGainFactor(Pointer hCamera, ByteBuffer pValue);

    boolean LucamSetDualGainFactor(Pointer hCamera, byte value);

    boolean LucamGetPiecewiseLinearResponseParameters(Pointer hCamera, ByteBuffer pKneepoint, NativeLongByReference pGainDivider);

    boolean LucamSetPiecewiseLinearResponseParameters(Pointer hCamera, byte kneepoint, NativeLong gainDivider);

    boolean LucamGetHdrMode(Pointer hCamera, ByteBuffer pValue);

    boolean LucamSetHdrMode(Pointer hCamera, byte value);

    Pointer LucamRegisterEventNotification(Pointer hCamera, int eventId, Pointer hEvent);

    boolean LucamUnregisterEventNotification(Pointer hCamera, Pointer pEventInformation);

    boolean LucamPerformMonoGridCorrection(Pointer hCamera, ByteBuffer pFrame, LucamAPI.LUCAM_IMAGE_FORMAT pImageFormat);

    boolean LucamGetImageIntensity(Pointer hCamera, ByteBuffer pFrame, LucamAPI.LUCAM_IMAGE_FORMAT pImageFormat, NativeLong startX, NativeLong startY, NativeLong width, NativeLong height, FloatBuffer pIntensity, FloatBuffer pRedIntensity, FloatBuffer pGreen1Intensity, FloatBuffer pGreen2Intensity, FloatBuffer pBlueIntensity);

    boolean LucamAutoRoiGet(Pointer hCamera, NativeLongByReference pStartX, NativeLongByReference pStartY, NativeLongByReference pWidth, NativeLongByReference pHeight);

    boolean LucamAutoRoiSet(Pointer hCamera, NativeLong startX, NativeLong startY, NativeLong width, NativeLong height);

    boolean LucamDataLsbAlign(Pointer hCamera, LucamAPI.LUCAM_IMAGE_FORMAT pLif, ByteBuffer pData);

    boolean LucamEnableInterfacePowerSpecViolation(Pointer hCamera, byte enable);

    boolean LucamIsInterfacePowerSpecViolationEnabled(Pointer hCamera, boolean pIsEnabled);

    boolean LucamSelectExternInterface(NativeLong externInterface);

    byte LgcamGetIPConfiguration(NativeLong index, ByteBuffer cameraMac, LucamAPI.LGCAM_IP_CONFIGURATION pCameraConfiguration, ByteBuffer hostMac, LucamAPI.LGCAM_IP_CONFIGURATION pHostConfiguration);

    byte LgcamSetIPConfiguration(NativeLong index, LucamAPI.LGCAM_IP_CONFIGURATION pCameraConfiguration);

    interface LucamAddStreamingCallback_VideoFilter_callback extends Callback {
        void apply(Pointer pContext, Pointer pData, NativeLong dataLength);
    }

    interface LucamAddRgbPreviewCallback_RgbVideoFilter_callback extends Callback {
        void apply(Pointer pContext, Pointer pData, NativeLong dataLength, NativeLong unused);
    }

    interface LucamAddSnapshotCallback_SnapshotCallback_callback extends Callback {
        void apply(Pointer pContext, Pointer pData, NativeLong dataLength);
    }

    interface LucamAutoFocusStart_ProgressCallback_callback extends Callback {
        byte apply(Pointer context, float percentageCompleted);
    }

    interface LucamAddRs232Callback_callback_callback extends Callback {
        void apply(Pointer VOIDPtr1);
    }

    class LUCAM_VERSION extends Structure {
        /**
         * Camera firmware version.      Not available with LucamEnumCameras
         */
        public NativeLong firmware;
        /**
         * Camera FPGA version.          Not available with LucamEnumCameras
         */
        public NativeLong fpga;
        /**
         * API version (lucamapi.dll, lucamapi.so.*)
         */
        public NativeLong api;
        /**
         * Device driver version.        Not available with LucamEnumCameras
         */
        public NativeLong driver;
        /**
         * Unique serial number of a camera.
         */
        public NativeLong serialnumber;
        /**
         * Also known as camera model id.
         */
        public NativeLong cameraid;

        public LUCAM_VERSION() {
            super();
        }

        public LUCAM_VERSION(NativeLong firmware, NativeLong fpga, NativeLong api, NativeLong driver, NativeLong serialnumber, NativeLong cameraid) {
            super();
            this.firmware     = firmware;
            this.fpga         = fpga;
            this.api          = api;
            this.driver       = driver;
            this.serialnumber = serialnumber;
            this.cameraid     = cameraid;
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("firmware", "fpga", "api", "driver", "serialnumber", "cameraid");
        }

        public static class ByReference extends LUCAM_VERSION implements Structure.ByReference {

        }

        public static class ByValue extends LUCAM_VERSION implements Structure.ByValue {

        }

    }

    class LUCAM_FRAME_FORMAT extends Structure {
        /**
         * X coordinate on imager of top left corner of subwindow, in pixels
         */
        public NativeLong   xOffset;
        /**
         * Y coordinate on imager of top left corner of subwindow, in pixels
         */
        public NativeLong   yOffset;
        /**
         * Width  of subwindow, in pixels
         */
        public NativeLong   width;
        /**
         * Height of subwindow, in pixls
         */
        public NativeLong   height;
        /**
         * Pixel format LUCAM_PF
         */
        public NativeLong pixelFormat;
        public binX_union binX;
        /**
         * LUCAM_FRAME_FORMAT_FLAGS_*
         */
        public short      flagsX;
        public binY_union binY;
        /**
         * LUCAM_FRAME_FORMAT_FLAGS_*
         */
        public short      flagsY;

        public LUCAM_FRAME_FORMAT() {
            super();
        }

        public LUCAM_FRAME_FORMAT(NativeLong xOffset, NativeLong yOffset, NativeLong width, NativeLong height, NativeLong pixelFormat, binX_union field1, short flagsX, binY_union field2, short flagsY) {
            super();
            this.xOffset     = xOffset;
            this.yOffset     = yOffset;
            this.width       = width;
            this.height      = height;
            this.pixelFormat = pixelFormat;
            this.binX        = field1;
            this.flagsX = flagsX;
            this.binY   = field2;
            this.flagsY = flagsY;
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("xOffset", "yOffset", "width", "height", "pixelFormat", "binX", "flagsX", "binY", "flagsY");
        }

        public static class binX_union extends Union {
            /**
             * Sub-sample ratio in x direction, in pixels (x:1)
             */
            public short subSampleX;
            /**
             * Binning ratio in x direction, in pixels (x:1
             */
            public short binningX;

            public binX_union() {
                super();
            }

            public binX_union(short subSampleX_or_binningX) {
                super();
                this.binningX = this.subSampleX = subSampleX_or_binningX;
                setType(Short.TYPE);
            }

            public static class ByReference extends binX_union implements Structure.ByReference {

            }

            public static class ByValue extends binX_union implements Structure.ByValue {

            }

        }

        public static class binY_union extends Union {
            /**
             * Sub-sample ratio in y direction, in pixels (y:1)
             */
            public short subSampleY;
            /**
             * Binning ratio in y direction, in pixels (y:1)
             */
            public short binningY;

            public binY_union() {
                super();
            }

            public binY_union(short subSampleY_or_binningY) {
                super();
                this.binningY = this.subSampleY = subSampleY_or_binningY;
                setType(Short.TYPE);
            }

            public static class ByReference extends binY_union implements Structure.ByReference {

            }

            public static class ByValue extends binY_union implements Structure.ByValue {

            }

        }

        public static class ByReference extends LUCAM_FRAME_FORMAT implements Structure.ByReference {

        }

        public static class ByValue extends LUCAM_FRAME_FORMAT implements Structure.ByValue {

        }

    }

    class LUCAM_SNAPSHOT extends Structure {
        /**
         * Exposure in milliseconds
         */
        public float                       exposure;
        /**
         * Overall gain as a multiplicative factor
         */
        public float                       gain;
        public field1_union                field1;
        public field2_union                field2;
        /**
         * Time interval from when exposure starts to time the flash is fired in milliseconds
         */
        public float                       strobeDelay;
        /**
         * Wait for hardware trigger
         */
        public byte                        useHwTrigger;
        /**
         * Maximum time to wait for hardware trigger prior to returning from function in milliseconds
         */
        public float                       timeout;
        /**
         * Frame format for data
         */
        public LucamAPI.LUCAM_FRAME_FORMAT format;
        public NativeLong                  shutterType;
        public float                       exposureDelay;
        public field3_union                field3;
        /**
         * Must be set to 0
         */
        public NativeLong                  ulReserved2;
        /**
         * Must be set to 0
         */
        public float                       flReserved1;
        /**
         * Must be set to 0
         */
        public float                       flReserved2;

        public LUCAM_SNAPSHOT() {
            super();
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("exposure", "gain", "field1", "field2", "strobeDelay", "useHwTrigger", "timeout", "format", "shutterType", "exposureDelay", "field3", "ulReserved2", "flReserved1", "flReserved2");
        }

        public static class field1_union extends Union {
            public field1_struct field1;
            public field2_struct field2;

            public field1_union() {
                super();
            }

            public field1_union(field1_struct field1) {
                super();
                this.field1 = field1;
                setType(field1_struct.class);
            }

            public field1_union(field2_struct field2) {
                super();
                this.field2 = field2;
                setType(field2_struct.class);
            }

            public static class field1_struct extends Structure {
                /**
                 * Gain for Red pixels as multiplicative factor
                 */
                public float gainRed;
                /**
                 * Gain for Blue pixels as multiplicative factor
                 */
                public float gainBlue;
                /**
                 * Gain for Green pixels on Red rows as multiplicative factor
                 */
                public float gainGrn1;
                /**
                 * Gain for Green pixels on Blue rows as multiplicative factor
                 */
                public float gainGrn2;

                public field1_struct() {
                    super();
                }

                public field1_struct(float gainRed, float gainBlue, float gainGrn1, float gainGrn2) {
                    super();
                    this.gainRed  = gainRed;
                    this.gainBlue = gainBlue;
                    this.gainGrn1 = gainGrn1;
                    this.gainGrn2 = gainGrn2;
                }

                protected List<String> getFieldOrder() {
                    return Arrays.asList("gainRed", "gainBlue", "gainGrn1", "gainGrn2");
                }

                public static class ByReference extends field1_struct implements Structure.ByReference {

                }

                public static class ByValue extends field1_struct implements Structure.ByValue {

                }

            }

            public static class field2_struct extends Structure {
                /**
                 * Gain for Magenta pixels as multiplicative factor
                 */
                public float gainMag;
                /**
                 * Gain for Cyan pixels as multiplicative factor
                 */
                public float gainCyan;
                /**
                 * Gain for Yellow pixels on Magenta rows as multiplicative factor
                 */
                public float gainYel1;
                /**
                 * Gain for Yellow pixels on Cyan rows as multiplicative factor
                 */
                public float gainYel2;

                public field2_struct() {
                    super();
                }

                public field2_struct(float gainMag, float gainCyan, float gainYel1, float gainYel2) {
                    super();
                    this.gainMag  = gainMag;
                    this.gainCyan = gainCyan;
                    this.gainYel1 = gainYel1;
                    this.gainYel2 = gainYel2;
                }

                protected List<String> getFieldOrder() {
                    return Arrays.asList("gainMag", "gainCyan", "gainYel1", "gainYel2");
                }

                public static class ByReference extends field2_struct implements Structure.ByReference {

                }

                public static class ByValue extends field2_struct implements Structure.ByValue {

                }

            }

            public static class ByReference extends field1_union implements Structure.ByReference {

            }

            public static class ByValue extends field1_union implements Structure.ByValue {

            }

        }

        public static class field2_union extends Union {
            /**
             * For backward compatibility
             */
            public byte       useStrobe;
            /**
             * Use LUCAM_PROP_FLAG_USE and/or LUCAM_PROP_FLAG_STROBE_FROM_START_OF_EXPOSURE
             */
            public NativeLong strobeFlags;

            public field2_union() {
                super();
            }

            public field2_union(NativeLong strobeFlags) {
                super();
                this.strobeFlags = strobeFlags;
                setType(NativeLong.class);
            }

            public field2_union(byte useStrobe) {
                super();
                this.useStrobe = useStrobe;
                setType(Byte.TYPE);
            }

            public static class ByReference extends field2_union implements Structure.ByReference {

            }

            public static class ByValue extends field2_union implements Structure.ByValue {

            }

        }

        public static class field3_union extends Union {
            /**
             * Set to TRUE if you want TakeFastFrame to return an already received frame.
             */
            public byte       bufferlastframe;
            public NativeLong ulReserved1;

            public field3_union() {
                super();
            }

            public field3_union(NativeLong ulReserved1) {
                super();
                this.ulReserved1 = ulReserved1;
                setType(NativeLong.class);
            }

            public field3_union(byte bufferlastframe) {
                super();
                this.bufferlastframe = bufferlastframe;
                setType(Byte.TYPE);
            }

            public static class ByReference extends field3_union implements Structure.ByReference {

            }

            public static class ByValue extends field3_union implements Structure.ByValue {

            }

        }

        public static class ByReference extends LUCAM_SNAPSHOT implements Structure.ByReference {

        }

        public static class ByValue extends LUCAM_SNAPSHOT implements Structure.ByValue {

        }

    }

    class LUCAM_CONVERSION extends Structure {
        /**
         * LUCAM_DM_*
         */
        public NativeLong DemosaicMethod;
        /**
         * LUCAM_CM_*
         */
        public NativeLong CorrectionMatrix;

        public LUCAM_CONVERSION() {
            super();
        }

        public LUCAM_CONVERSION(NativeLong DemosaicMethod, NativeLong CorrectionMatrix) {
            super();
            this.DemosaicMethod   = DemosaicMethod;
            this.CorrectionMatrix = CorrectionMatrix;
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("DemosaicMethod", "CorrectionMatrix");
        }

        public static class ByReference extends LUCAM_CONVERSION implements Structure.ByReference {

        }

        public static class ByValue extends LUCAM_CONVERSION implements Structure.ByValue {

        }

    }

    class LUCAM_CONVERSION_PARAMS extends Structure {
        /**
         * Must be set to sizeof this struct
         */
        public NativeLong   Size;
        /**
         * LUCAM_DM_*
         */
        public NativeLong   DemosaicMethod;
        /**
         * LUCAM_CM_*
         */
        public NativeLong   CorrectionMatrix;
        public byte         FlipX;
        public byte         FlipY;
        public float        Hue;
        public float        Saturation;
        public byte         UseColorGainsOverWb;
        public field1_union field1;

        public LUCAM_CONVERSION_PARAMS() {
            super();
        }

        public LUCAM_CONVERSION_PARAMS(NativeLong Size, NativeLong DemosaicMethod, NativeLong CorrectionMatrix, byte FlipX, byte FlipY, float Hue, float Saturation, byte UseColorGainsOverWb, field1_union field1) {
            super();
            this.Size                = Size;
            this.DemosaicMethod      = DemosaicMethod;
            this.CorrectionMatrix    = CorrectionMatrix;
            this.FlipX               = FlipX;
            this.FlipY               = FlipY;
            this.Hue                 = Hue;
            this.Saturation          = Saturation;
            this.UseColorGainsOverWb = UseColorGainsOverWb;
            this.field1              = field1;
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("Size", "DemosaicMethod", "CorrectionMatrix", "FlipX", "FlipY", "Hue", "Saturation", "UseColorGainsOverWb", "field1");
        }

        public static class field1_union extends Union {
            public field1_struct field1;
            public field2_struct field2;

            public field1_union() {
                super();
            }

            public field1_union(field1_struct field1) {
                super();
                this.field1 = field1;
                setType(field1_struct.class);
            }

            public field1_union(field2_struct field2) {
                super();
                this.field2 = field2;
                setType(field2_struct.class);
            }

            public static class field1_struct extends Structure {
                public float DigitalGain;
                public float DigitalWhiteBalanceU;
                public float DigitalWhiteBalanceV;

                public field1_struct() {
                    super();
                }

                public field1_struct(float DigitalGain, float DigitalWhiteBalanceU, float DigitalWhiteBalanceV) {
                    super();
                    this.DigitalGain          = DigitalGain;
                    this.DigitalWhiteBalanceU = DigitalWhiteBalanceU;
                    this.DigitalWhiteBalanceV = DigitalWhiteBalanceV;
                }

                protected List<String> getFieldOrder() {
                    return Arrays.asList("DigitalGain", "DigitalWhiteBalanceU", "DigitalWhiteBalanceV");
                }

                public static class ByReference extends field1_struct implements Structure.ByReference {

                }

                public static class ByValue extends field1_struct implements Structure.ByValue {

                }

            }

            public static class field2_struct extends Structure {
                public float DigitalGainRed;
                public float DigitalGainGreen;
                public float DigitalGainBlue;

                public field2_struct() {
                    super();
                }

                public field2_struct(float DigitalGainRed, float DigitalGainGreen, float DigitalGainBlue) {
                    super();
                    this.DigitalGainRed   = DigitalGainRed;
                    this.DigitalGainGreen = DigitalGainGreen;
                    this.DigitalGainBlue  = DigitalGainBlue;
                }

                protected List<String> getFieldOrder() {
                    return Arrays.asList("DigitalGainRed", "DigitalGainGreen", "DigitalGainBlue");
                }

                public static class ByReference extends field2_struct implements Structure.ByReference {

                }

                public static class ByValue extends field2_struct implements Structure.ByValue {

                }

            }

            public static class ByReference extends field1_union implements Structure.ByReference {

            }

            public static class ByValue extends field1_union implements Structure.ByValue {

            }

        }

        public static class ByReference extends LUCAM_CONVERSION_PARAMS implements Structure.ByReference {

        }

        public static class ByValue extends LUCAM_CONVERSION_PARAMS implements Structure.ByValue {

        }

    }

    class LUCAM_IMAGE_FORMAT extends Structure {
        /**
         * Must be set to sizeof this struct
         */
        public NativeLong   Size;
        public NativeLong   Width;
        public NativeLong   Height;
        /**
         * LUCAM_PF_*
         */
        public NativeLong   PixelFormat;
        public NativeLong   ImageSize;
        public NativeLong[] LucamReserved = new NativeLong[8];

        public LUCAM_IMAGE_FORMAT() {
            super();
        }

        public LUCAM_IMAGE_FORMAT(NativeLong Size, NativeLong Width, NativeLong Height, NativeLong PixelFormat, NativeLong ImageSize, NativeLong[] LucamReserved) {
            super();
            this.Size        = Size;
            this.Width       = Width;
            this.Height      = Height;
            this.PixelFormat = PixelFormat;
            this.ImageSize   = ImageSize;
            if ((LucamReserved.length != this.LucamReserved.length)) {
                throw new IllegalArgumentException("Wrong array size !");
            }
            this.LucamReserved = LucamReserved;
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("Size", "Width", "Height", "PixelFormat", "ImageSize", "LucamReserved");
        }

        public static class ByReference extends LUCAM_IMAGE_FORMAT implements Structure.ByReference {

        }

        public static class ByValue extends LUCAM_IMAGE_FORMAT implements Structure.ByValue {

        }

    }

    class LUCAM_STREAM_STATS extends Structure {
        public NativeLong   FramesCompleted;
        public NativeLong   FramesDropped;
        public NativeLong   ActualFramesDropped;
        public field1_union field1;

        public LUCAM_STREAM_STATS() {
            super();
        }

        public LUCAM_STREAM_STATS(NativeLong FramesCompleted, NativeLong FramesDropped, NativeLong ActualFramesDropped, field1_union field1) {
            super();
            this.FramesCompleted     = FramesCompleted;
            this.FramesDropped       = FramesDropped;
            this.ActualFramesDropped = ActualFramesDropped;
            this.field1              = field1;
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("FramesCompleted", "FramesDropped", "ActualFramesDropped", "field1");
        }

        public static class field1_union extends Union {
            public USB_struct   USB;
            public USB2_struct  USB2;
            public GEV_struct   GEV;
            public LSGEV_struct LSGEV;

            public field1_union() {
                super();
            }

            public field1_union(USB_struct USB) {
                super();
                this.USB = USB;
                setType(USB_struct.class);
            }

            public field1_union(USB2_struct USB2) {
                super();
                this.USB2 = USB2;
                setType(USB2_struct.class);
            }

            public field1_union(GEV_struct GEV) {
                super();
                this.GEV = GEV;
                setType(GEV_struct.class);
            }

            public field1_union(LSGEV_struct LSGEV) {
                super();
                this.LSGEV = LSGEV;
                setType(LSGEV_struct.class);
            }

            public static class USB_struct extends Structure {
                public NativeLong ShortErrors;
                public NativeLong XactErrors;
                public NativeLong BabbleErrors;
                public NativeLong OtherErrors;

                public USB_struct() {
                    super();
                }

                public USB_struct(NativeLong ShortErrors, NativeLong XactErrors, NativeLong BabbleErrors, NativeLong OtherErrors) {
                    super();
                    this.ShortErrors  = ShortErrors;
                    this.XactErrors   = XactErrors;
                    this.BabbleErrors = BabbleErrors;
                    this.OtherErrors  = OtherErrors;
                }

                protected List<String> getFieldOrder() {
                    return Arrays.asList("ShortErrors", "XactErrors", "BabbleErrors", "OtherErrors");
                }

                public static class ByReference extends USB_struct implements Structure.ByReference {

                }

                public static class ByValue extends USB_struct implements Structure.ByValue {

                }

            }

            public static class USB2_struct extends Structure {
                public NativeLong ShortErrors;
                public NativeLong XactErrors;
                public NativeLong BabbleErrors;
                public NativeLong OtherErrors;
                public NativeLong TransfersOutOfOrderErrors;
                public NativeLong PendingFrames;
                public NativeLong PendingUsbTransfers;

                public USB2_struct() {
                    super();
                }

                public USB2_struct(NativeLong ShortErrors, NativeLong XactErrors, NativeLong BabbleErrors, NativeLong OtherErrors, NativeLong TransfersOutOfOrderErrors, NativeLong PendingFrames, NativeLong PendingUsbTransfers) {
                    super();
                    this.ShortErrors               = ShortErrors;
                    this.XactErrors                = XactErrors;
                    this.BabbleErrors              = BabbleErrors;
                    this.OtherErrors               = OtherErrors;
                    this.TransfersOutOfOrderErrors = TransfersOutOfOrderErrors;
                    this.PendingFrames             = PendingFrames;
                    this.PendingUsbTransfers       = PendingUsbTransfers;
                }

                protected List<String> getFieldOrder() {
                    return Arrays.asList("ShortErrors", "XactErrors", "BabbleErrors", "OtherErrors", "TransfersOutOfOrderErrors", "PendingFrames", "PendingUsbTransfers");
                }

                public static class ByReference extends USB2_struct implements Structure.ByReference {

                }

                public static class ByValue extends USB2_struct implements Structure.ByValue {

                }

            }

            public static class GEV_struct extends Structure {
                public NativeLong    ExpectedResend;
                public NativeLong    LostPacket;
                public NativeLong    DataOverrun;
                public NativeLong    PartialLineMissing;
                public NativeLong    FullLineMissing;
                public NativeLong    OtherErrors;
                public NativeLong    ExpectedSingleResend;
                public NativeLong    UnexpectedResend;
                public NativeLong    ResendGroupRequested;
                public NativeLong    ResendPacketRequested;
                public NativeLong    IgnoredPacket;
                public NativeLong    RedundantPacket;
                public NativeLong    PacketOutOfOrder;
                public NativeLong    BlocksDropped;
                public NativeLong    BlockIDsMissing;
                public Result_struct Result;

                public GEV_struct() {
                    super();
                }

                protected List<String> getFieldOrder() {
                    return Arrays.asList("ExpectedResend", "LostPacket", "DataOverrun", "PartialLineMissing", "FullLineMissing", "OtherErrors", "ExpectedSingleResend", "UnexpectedResend", "ResendGroupRequested", "ResendPacketRequested", "IgnoredPacket", "RedundantPacket", "PacketOutOfOrder", "BlocksDropped", "BlockIDsMissing", "Result");
                }

                public static class Result_struct extends Structure {
                    public NativeLong ImageError;
                    public NativeLong MissingPackets;
                    public NativeLong StateError;
                    public NativeLong TooManyResends;
                    public NativeLong TooManyConsecutiveResends;
                    public NativeLong ResendsFailure;

                    public Result_struct() {
                        super();
                    }

                    public Result_struct(NativeLong ImageError, NativeLong MissingPackets, NativeLong StateError, NativeLong TooManyResends, NativeLong TooManyConsecutiveResends, NativeLong ResendsFailure) {
                        super();
                        this.ImageError                = ImageError;
                        this.MissingPackets            = MissingPackets;
                        this.StateError                = StateError;
                        this.TooManyResends            = TooManyResends;
                        this.TooManyConsecutiveResends = TooManyConsecutiveResends;
                        this.ResendsFailure            = ResendsFailure;
                    }

                    protected List<String> getFieldOrder() {
                        return Arrays.asList("ImageError", "MissingPackets", "StateError", "TooManyResends", "TooManyConsecutiveResends", "ResendsFailure");
                    }

                    public static class ByReference extends Result_struct implements Structure.ByReference {

                    }

                    public static class ByValue extends Result_struct implements Structure.ByValue {

                    }

                }

                public static class ByReference extends GEV_struct implements Structure.ByReference {

                }

                public static class ByValue extends GEV_struct implements Structure.ByValue {

                }

            }

            public static class LSGEV_struct extends Structure {
                public NativeLong InputBufferAcqSuccess;
                /**
                 * Same as FramesDropped and ActualFramesDropped
                 */
                public NativeLong InputBufferAcqFailures;
                /**
                 * Same as FramesCompleted
                 */
                public NativeLong FramesCompletedSuccess;
                public NativeLong FramesCompletedError;
                public NativeLong PktReceived;
                public NativeLong PktInLastBlockError;
                public NativeLong PktInNextBlockError;
                public NativeLong BlockIdWayAheadError;
                public NativeLong NonSeqJumpAhead;
                public NativeLong NonSeqJumpBack;
                public NativeLong SegmentOverflowError;
                public NativeLong SegmentCreatedOnDesynch;
                public NativeLong PktOnlyPrecedingSegment;
                public NativeLong ResendOnSkip;
                public NativeLong ResendOnCountdown;
                public NativeLong PktAlreadyReceived;
                public NativeLong DesynchFixed;
                public NativeLong PktDroppedForAcqFailureCur;
                public NativeLong PktDroppedForAcqFailureNext;
                public NativeLong PktDiscardedForPreviousFailure;
                public NativeLong InvalidGvspHeader;
                public NativeLong InvalidPayloadSize;
                public NativeLong GvspStatusError;
                public NativeLong GvspStatusWarning;
                public NativeLong GvspLeaderReceived;
                public NativeLong GvspTrailerReceived;
                public NativeLong GvspPayloadReceived;

                public LSGEV_struct() {
                    super();
                }

                protected List<String> getFieldOrder() {
                    return Arrays.asList("InputBufferAcqSuccess", "InputBufferAcqFailures", "FramesCompletedSuccess", "FramesCompletedError", "PktReceived", "PktInLastBlockError", "PktInNextBlockError", "BlockIdWayAheadError", "NonSeqJumpAhead", "NonSeqJumpBack", "SegmentOverflowError", "SegmentCreatedOnDesynch", "PktOnlyPrecedingSegment", "ResendOnSkip", "ResendOnCountdown", "PktAlreadyReceived", "DesynchFixed", "PktDroppedForAcqFailureCur", "PktDroppedForAcqFailureNext", "PktDiscardedForPreviousFailure", "InvalidGvspHeader", "InvalidPayloadSize", "GvspStatusError", "GvspStatusWarning", "GvspLeaderReceived", "GvspTrailerReceived", "GvspPayloadReceived");
                }

                public static class ByReference extends LSGEV_struct implements Structure.ByReference {

                }

                public static class ByValue extends LSGEV_struct implements Structure.ByValue {

                }

            }

            public static class ByReference extends field1_union implements Structure.ByReference {

            }

            public static class ByValue extends field1_union implements Structure.ByValue {

            }

        }

        public static class ByReference extends LUCAM_STREAM_STATS implements Structure.ByReference {

        }

        public static class ByValue extends LUCAM_STREAM_STATS implements Structure.ByValue {

        }

    }

    class LUCAM_SS_BIN_DESC extends Structure {
        /**
         * 0x80: X and Y settings must be the same
         */
        public byte   flags;
        public byte   reserved;
        public byte   ssNot1Count;
        public byte   binNot1Count;
        public byte[] ssFormatsNot1  = new byte[8];
        public byte[] binFormatsNot1 = new byte[8];

        public LUCAM_SS_BIN_DESC() {
            super();
        }

        public LUCAM_SS_BIN_DESC(byte flags, byte reserved, byte ssNot1Count, byte binNot1Count, byte[] ssFormatsNot1, byte[] binFormatsNot1) {
            super();
            this.flags        = flags;
            this.reserved     = reserved;
            this.ssNot1Count  = ssNot1Count;
            this.binNot1Count = binNot1Count;
            if ((ssFormatsNot1.length != this.ssFormatsNot1.length)) {
                throw new IllegalArgumentException("Wrong array size !");
            }
            this.ssFormatsNot1 = ssFormatsNot1;
            if ((binFormatsNot1.length != this.binFormatsNot1.length)) {
                throw new IllegalArgumentException("Wrong array size !");
            }
            this.binFormatsNot1 = binFormatsNot1;
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("flags", "reserved", "ssNot1Count", "binNot1Count", "ssFormatsNot1", "binFormatsNot1");
        }

        public static class ByReference extends LUCAM_SS_BIN_DESC implements Structure.ByReference {

        }

        public static class ByValue extends LUCAM_SS_BIN_DESC implements Structure.ByValue {

        }

    }

    class LGCAM_IP_CONFIGURATION extends Structure {
        public NativeLong IPAddress;
        public NativeLong SubnetMask;
        public NativeLong DefaultGateway;

        public LGCAM_IP_CONFIGURATION() {
            super();
        }

        public LGCAM_IP_CONFIGURATION(NativeLong IPAddress, NativeLong SubnetMask, NativeLong DefaultGateway) {
            super();
            this.IPAddress      = IPAddress;
            this.SubnetMask     = SubnetMask;
            this.DefaultGateway = DefaultGateway;
        }

        protected List<String> getFieldOrder() {
            return Arrays.asList("IPAddress", "SubnetMask", "DefaultGateway");
        }

        public static class ByReference extends LGCAM_IP_CONFIGURATION implements Structure.ByReference {

        }

        public static class ByValue extends LGCAM_IP_CONFIGURATION implements Structure.ByValue {

        }

    }
}
