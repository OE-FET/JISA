package jisa.devices.camera.nat;

import jisa.Util;

import java.util.Map;

public interface LucamError {

    long LucamNoError                       = 0;
    long LucamNoSuchIndex                   = 1;
    long LucamSnapshotNotSupported          = 2;
    long LucamInvalidPixelFormat            = 3;
    long LucamSubsamplingZero               = 4;
    long LucamBusy                          = 5;
    long LucamFailedToSetSubsampling        = 6;
    long LucamFailedToSetStartPosition      = 7;
    long LucamPixelFormatNotSupported       = 8;
    long LucamInvalidFrameFormat            = 9;
    long LucamPreparationFailed             = 10;
    long LucamCannotRun                     = 11;
    long LucamNoTriggerControl              = 12;
    long LucamNoPin                         = 13;
    long LucamNotRunning                    = 14;
    long LucamTriggerFailed                 = 15;
    long LucamCannotSetupFrameFormat        = 16;
    long LucamDirectShowInitError           = 17;
    long LucamCameraNotFound                = 18;
    long LucamTimeout                       = 19;
    long LucamPropertyUnknown               = 20;
    long LucamPropertyUnsupported           = 21;
    long LucamPropertyAccessFailed          = 22;
    long LucamLucustomNotFound              = 23;
    long LucamPreviewNotRunning             = 24;
    long LucamLutfNotLoaded                 = 25;
    long LucamDirectShowError               = 26;
    long LucamNoMoreCallbacks               = 27;
    long LucamUndeterminedFrameFormat       = 28;
    long LucamInvalidParameter              = 29;
    long LucamNotEnoughResources            = 30;
    long LucamNoSuchConversion              = 31;
    long LucamParameterNotWithinBoundaries  = 32;
    long LucamBadFileIo                     = 33;
    long LucamGdiplusNotFound               = 34;
    long LucamGdiplusError                  = 35;
    long LucamUnknownFormatType             = 36;
    long LucamFailedCreateDisplay           = 37;
    long LucamDpLibNotFound                 = 38;
    long LucamDpCmdNotSupported             = 39;
    long LucamDpCmdUnknown                  = 40;
    long LucamNotWhilePaused                = 41;
    long LucamCaptureFailed                 = 42;
    long LucamDpError                       = 43;
    long LucamNoSuchFrameRate               = 44;
    long LucamInvalidTarget                 = 45;
    long LucamFrameTooDark                  = 46;
    long LucamKsPropertySetNotFound         = 47;
    long LucamCancelled                     = 48;
    long LucamKsControlNotSupported         = 49;
    long LucamEventNotSupported             = 50;
    long LucamNoPreview                     = 51;
    long LucamSetPositionFailed             = 52;
    long LucamNoFrameRateList               = 53;
    long LucamFrameRateInconsistent         = 54;
    long LucamCameraNotConfiguredForCmd     = 55;
    long LucamGraphNotReady                 = 56;
    long LucamCallbackSetupError            = 57;
    long LucamInvalidTriggerMode            = 58;
    long LucamNotFound                      = 59;
    long LucamPermanentBufferNotSupported   = 60;
    long LucamEepromWriteFailed             = 61;
    long LucamUnknownFileType               = 62;
    long LucamEventIdNotSupported           = 63;
    long LucamEepromCorrupted               = 64;
    long LucamSectionTooBig                 = 65;
    long LucamFrameTooBright                = 66;
    long LucamNoCorrectionMatrix            = 67;
    long LucamUnknownCameraModel            = 68;
    long LucamApiTooOld                     = 69;
    long LucamSaturationZero                = 0;
    long LucamAlreadyInitialised            = 1;
    long LucamSameInputAndOutputFile        = 2;
    long LucamFileConversionFailed          = 3;
    long LucamFileAlreadyConverted          = 4;
    long LucamPropertyPageNotSupported      = 75;
    long LucamPropertyPageCreationFailed    = 76;
    long LucamDirectShowFilterNotInstalled  = 77;
    long LucamIndividualLutNotAvailable     = 78;
    long LucamUnexpectedError               = 79;
    long LucamStreamingStopped              = 80;
    long LucamMustBeInSwTriggerMode         = 81;
    long LucamTargetFlaky                   = 82;
    long LucamAutoLensUninitialized         = 83;
    long LucamLensNotInstalled              = 84;
    long LucamUnknownError                  = 85;
    long LucamFocusNoFeedbackError          = 86;
    long LucamLutfTooOld                    = 87;
    long LucamUnknownAviFormat              = 88;
    long LucamUnknownAviType                = 89;
    long LucamInvalidAviConversion          = 90;
    long LucamSeekFailed                    = 91;
    long LucamAviRunning                    = 92;
    long LucamCameraAlreadyOpened           = 93;
    long LucamNoSubsampledHighRes           = 94;
    long LucamOnlyOnMonochrome              = 95;
    long LucamNo8bppTo48bpp                 = 96;
    long LucamLut8Obsolete                  = 97;
    long LucamFunctionNotSupported          = 98;
    long LucamRetryLimitReached             = 99;
    long LucamLgDeviceError                 = 100;
    long LucamInvalidIpConfiguration        = 101;
    long LucamInvalidLicense                = 102;
    long LucamNoSystemEnumerator            = 103;
    long LucamBusEnumeratorNotInstalled     = 104;
    long LucamUnknownExternInterface        = 105;
    long LucamInterfaceDriverNotInstalled   = 106;
    long LucamCameraDriverNotInstalled      = 107;
    long LucamCameraDriverInstallInProgress = 108;
    long LucamLucamapiDotDllNotFound        = 109;
    long LucamLucamapiProcedureNotFound     = 110;
    long LucamPropertyNotEnumeratable       = 111;
    long LucamPropertyNotBufferable         = 112;
    long LucamSingleTapImage                = 113;
    long LucamUnknownTapConfiguration       = 114;
    long LucamBufferTooSmall                = 115;
    long LucamInCallbackOnly                = 116;
    long LucamPropertyUnavailable           = 117;
    long LucamTimestampNotEnabled           = 118;
    long LucamFramecounterNotEnabled        = 119;
    long LucamNoStatsWhenNotStreaming       = 120;
    long LucamFrameCapturePending           = 121;
    long LucamSequencingNotEnabled          = 122;
    long LucamFeatureNotSequencable         = 123;
    long LucamSequencingUnknownFeatureType  = 124;
    long LucamSequencingIndexOutOfSequence  = 125;
    long LucamSequencingBadFrameNumber      = 126;
    long LucamInformationNotAvailable       = 127;
    long LucamSequencingBadSetting          = 128;
    long LucamAutoFocusNeverStarted         = 129;
    long LucamAutoFocusNotRunning           = 130;
    long LucamCameraNotOpenable             = 1121;
    long LucamCameraNotSupported            = 1122;
    long LucamMmapFailed                    = 1123;
    long LucamNotWhileStreaming             = 1124;
    long LucamNoStreamingRights             = 1125;
    long LucamCameraInitializationError     = 1126;
    long LucamCannotVerifyPixelFormat       = 1127;
    long LucamCannotVerifyStartPosition     = 1128;
    long LucamOsError                       = 1129;
    long LucamBufferNotAvailable            = 1130;
    long LucamQueuingFailed                 = 1131;

    Map<Long, String> NAMES = Util.map(0L, "No Error")
                                  .map(1L, "No Such Index")
                                  .map(2L, "Snapshot not Supported")
                                  .map(3L, "Invalid Pixel Format")
                                  .map(4L, "Subsampling Zero")
                                  .map(5L, "Busy")
                                  .map(6L, "Failed to Set Subsampling")
                                  .map(7L, "Failed to Set Start Position")
                                  .map(8L, "Pixel Format not Supported")
                                  .map(9L, "Invalid Frame Format")
                                  .map(10L, "Preparation Failed")
                                  .map(11L, "Cannot Run")
                                  .map(12L, "No Trigger Control")
                                  .map(13L, "No Pin")
                                  .map(14L, "Not Running")
                                  .map(15L, "Trigger Failed")
                                  .map(16L, "Cannot Setup Frame Format")
                                  .map(17L, "Direct Show Init Error")
                                  .map(18L, "Camera Not Found")
                                  .map(19L, "Timeout")
                                  .map(20L, "Property Unknown")
                                  .map(21L, "Property Unsupported")
                                  .map(22L, "Property Access Failed")
                                  .map(23L, "Lucustom Not Found")
                                  .map(24L, "Preview Not Running")
                                  .map(25L, "Lutf Not Loaded")
                                  .map(26L, "Direct Show Error")
                                  .map(27L, "No More Callbacks")
                                  .map(28L, "Undetermined Frame Format")
                                  .map(29L, "Invalid Parameter")
                                  .map(30L, "Not Enough Resources")
                                  .map(31L, "No Such Conversion")
                                  .map(32L, "Parameter Not Within Boundaries")
                                  .map(33L, "Bad File Io")
                                  .map(34L, "Gdiplus Not Found")
                                  .map(35L, "Gdiplus Error")
                                  .map(36L, "Unknown Format Type")
                                  .map(37L, "Failed Create Display")
                                  .map(38L, "Dp Lib Not Found")
                                  .map(39L, "Dp Cmd Not Supported")
                                  .map(40L, "Dp Cmd Unknown")
                                  .map(41L, "Not While Paused")
                                  .map(42L, "Capture Failed")
                                  .map(43L, "Dp Error")
                                  .map(44L, "No Such Frame Rate")
                                  .map(45L, "Invalid Target")
                                  .map(46L, "Frame Too Dark")
                                  .map(47L, "Ks Property Set Not Found")
                                  .map(48L, "Cancelled")
                                  .map(49L, "Ks Control Not Supported")
                                  .map(50L, "Event Not Supported")
                                  .map(51L, "No Preview")
                                  .map(52L, "Set Position Failed")
                                  .map(53L, "No Frame Rate List")
                                  .map(54L, "Frame Rate Inconsistent")
                                  .map(55L, "Camera Not Configured For Cmd")
                                  .map(56L, "Graph Not Ready")
                                  .map(57L, "Callback Setup Error")
                                  .map(58L, "Invalid Trigger Mode")
                                  .map(59L, "Not Found")
                                  .map(60L, "Permanent Buffer Not Supported")
                                  .map(61L, "Eeprom Write Failed")
                                  .map(62L, "Unknown File Type")
                                  .map(63L, "Event Id Not Supported")
                                  .map(64L, "Eeprom Corrupted")
                                  .map(65L, "Section Too Big")
                                  .map(66L, "Frame Too Bright")
                                  .map(67L, "No Correction Matrix")
                                  .map(68L, "Unknown Camera Model")
                                  .map(69L, "Api Too Old")
                                  .map(75L, "Property Page Not Supported")
                                  .map(76L, "Property Page Creation Failed")
                                  .map(77L, "Direct Show Filter Not Installed")
                                  .map(78L, "Individual Lut Not Available")
                                  .map(79L, "Unexpected Error")
                                  .map(80L, "Streaming Stopped")
                                  .map(81L, "Must Be In Sw Trigger Mode")
                                  .map(82L, "Target Flaky")
                                  .map(83L, "Auto Lens Uninitialized")
                                  .map(84L, "Lens Not Installed")
                                  .map(85L, "Unknown Error")
                                  .map(86L, "Focus No Feedback Error")
                                  .map(87L, "Lutf Too Old")
                                  .map(88L, "Unknown Avi Format")
                                  .map(89L, "Unknown Avi Type")
                                  .map(90L, "Invalid Avi Conversion")
                                  .map(91L, "Seek Failed")
                                  .map(92L, "Avi Running")
                                  .map(93L, "Camera Already Opened")
                                  .map(94L, "No Subsampled High Res")
                                  .map(95L, "Only On Monochrome")
                                  .map(96L, "No 8bpp To 48bpp")
                                  .map(97L, "Lut8 Obsolete")
                                  .map(98L, "Function Not Supported")
                                  .map(99L, "Retry Limit Reached")
                                  .map(100L, "Lg Device Error")
                                  .map(101L, "Invalid Ip Configuration")
                                  .map(102L, "Invalid License")
                                  .map(103L, "No System Enumerator")
                                  .map(104L, "Bus Enumerator Not Installed")
                                  .map(105L, "Unknown Extern Interface")
                                  .map(106L, "Interface Driver Not Installed")
                                  .map(107L, "Camera Driver Not Installed")
                                  .map(108L, "Camera Driver Install In Progress")
                                  .map(109L, "api Dot Dll Not Found")
                                  .map(110L, "api Procedure Not Found")
                                  .map(111L, "Property Not Enumeratable")
                                  .map(112L, "Property Not Bufferable")
                                  .map(113L, "Single Tap Image")
                                  .map(114L, "Unknown Tap Configuration")
                                  .map(115L, "Buffer Too Small")
                                  .map(116L, "In Callback Only")
                                  .map(117L, "Property Unavailable")
                                  .map(118L, "Timestamp Not Enabled")
                                  .map(119L, "Framecounter Not Enabled")
                                  .map(120L, "No Stats When Not Streaming")
                                  .map(121L, "Frame Capture Pending")
                                  .map(122L, "Sequencing Not Enabled")
                                  .map(123L, "Feature Not Sequencable")
                                  .map(124L, "Sequencing Unknown Feature Type")
                                  .map(125L, "Sequencing Index Out Of Sequence")
                                  .map(126L, "Sequencing Bad Frame Number")
                                  .map(127L, "Information Not Available")
                                  .map(128L, "Sequencing Bad Setting")
                                  .map(129L, "Auto Focus Never Started")
                                  .map(130L, "Auto Focus Not Running")
                                  .map(1121L, "Camera Not Openable")
                                  .map(1122L, "Camera Not Supported")
                                  .map(1123L, "Mmap Failed")
                                  .map(1124L, "Not While Streaming")
                                  .map(1125L, "No Streaming Rights")
                                  .map(1126L, "Camera Initialization Error")
                                  .map(1127L, "Cannot Verify Pixel Format")
                                  .map(1128L, "Cannot Verify Start Position")
                                  .map(1129L, "Os Error")
                                  .map(1130L, "Buffer Not Available")
                                  .map(1131L, "Queuing Failed");


}
