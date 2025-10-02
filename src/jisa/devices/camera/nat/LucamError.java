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

    Map<Long, String> NAMES = Util.map(0L, "LucamNoError")
                                  .map(1L, "LucamNoSuchIndex")
                                  .map(2L, "LucamSnapshotNotSupported")
                                  .map(3L, "LucamInvalidPixelFormat")
                                  .map(4L, "LucamSubsamplingZero")
                                  .map(5L, "LucamBusy")
                                  .map(6L, "LucamFailedToSetSubsampling")
                                  .map(7L, "LucamFailedToSetStartPosition")
                                  .map(8L, "LucamPixelFormatNotSupported")
                                  .map(9L, "LucamInvalidFrameFormat")
                                  .map(10L, "LucamPreparationFailed")
                                  .map(11L, "LucamCannotRun")
                                  .map(12L, "LucamNoTriggerControl")
                                  .map(13L, "LucamNoPin")
                                  .map(14L, "LucamNotRunning")
                                  .map(15L, "LucamTriggerFailed")
                                  .map(16L, "LucamCannotSetupFrameFormat")
                                  .map(17L, "LucamDirectShowInitError")
                                  .map(18L, "LucamCameraNotFound")
                                  .map(19L, "LucamTimeout")
                                  .map(20L, "LucamPropertyUnknown")
                                  .map(21L, "LucamPropertyUnsupported")
                                  .map(22L, "LucamPropertyAccessFailed")
                                  .map(23L, "LucamLucustomNotFound")
                                  .map(24L, "LucamPreviewNotRunning")
                                  .map(25L, "LucamLutfNotLoaded")
                                  .map(26L, "LucamDirectShowError")
                                  .map(27L, "LucamNoMoreCallbacks")
                                  .map(28L, "LucamUndeterminedFrameFormat")
                                  .map(29L, "LucamInvalidParameter")
                                  .map(30L, "LucamNotEnoughResources")
                                  .map(31L, "LucamNoSuchConversion")
                                  .map(32L, "LucamParameterNotWithinBoundaries")
                                  .map(33L, "LucamBadFileIo")
                                  .map(34L, "LucamGdiplusNotFound")
                                  .map(35L, "LucamGdiplusError")
                                  .map(36L, "LucamUnknownFormatType")
                                  .map(37L, "LucamFailedCreateDisplay")
                                  .map(38L, "LucamDpLibNotFound")
                                  .map(39L, "LucamDpCmdNotSupported")
                                  .map(40L, "LucamDpCmdUnknown")
                                  .map(41L, "LucamNotWhilePaused")
                                  .map(42L, "LucamCaptureFailed")
                                  .map(43L, "LucamDpError")
                                  .map(44L, "LucamNoSuchFrameRate")
                                  .map(45L, "LucamInvalidTarget")
                                  .map(46L, "LucamFrameTooDark")
                                  .map(47L, "LucamKsPropertySetNotFound")
                                  .map(48L, "LucamCancelled")
                                  .map(49L, "LucamKsControlNotSupported")
                                  .map(50L, "LucamEventNotSupported")
                                  .map(51L, "LucamNoPreview")
                                  .map(52L, "LucamSetPositionFailed")
                                  .map(53L, "LucamNoFrameRateList")
                                  .map(54L, "LucamFrameRateInconsistent")
                                  .map(55L, "LucamCameraNotConfiguredForCmd")
                                  .map(56L, "LucamGraphNotReady")
                                  .map(57L, "LucamCallbackSetupError")
                                  .map(58L, "LucamInvalidTriggerMode")
                                  .map(59L, "LucamNotFound")
                                  .map(60L, "LucamPermanentBufferNotSupported")
                                  .map(61L, "LucamEepromWriteFailed")
                                  .map(62L, "LucamUnknownFileType")
                                  .map(63L, "LucamEventIdNotSupported")
                                  .map(64L, "LucamEepromCorrupted")
                                  .map(65L, "LucamSectionTooBig")
                                  .map(66L, "LucamFrameTooBright")
                                  .map(67L, "LucamNoCorrectionMatrix")
                                  .map(68L, "LucamUnknownCameraModel")
                                  .map(69L, "LucamApiTooOld")
                                  .map(75L, "LucamPropertyPageNotSupported")
                                  .map(76L, "LucamPropertyPageCreationFailed")
                                  .map(77L, "LucamDirectShowFilterNotInstalled")
                                  .map(78L, "LucamIndividualLutNotAvailable")
                                  .map(79L, "LucamUnexpectedError")
                                  .map(80L, "LucamStreamingStopped")
                                  .map(81L, "LucamMustBeInSwTriggerMode")
                                  .map(82L, "LucamTargetFlaky")
                                  .map(83L, "LucamAutoLensUninitialized")
                                  .map(84L, "LucamLensNotInstalled")
                                  .map(85L, "LucamUnknownError")
                                  .map(86L, "LucamFocusNoFeedbackError")
                                  .map(87L, "LucamLutfTooOld")
                                  .map(88L, "LucamUnknownAviFormat")
                                  .map(89L, "LucamUnknownAviType")
                                  .map(90L, "LucamInvalidAviConversion")
                                  .map(91L, "LucamSeekFailed")
                                  .map(92L, "LucamAviRunning")
                                  .map(93L, "LucamCameraAlreadyOpened")
                                  .map(94L, "LucamNoSubsampledHighRes")
                                  .map(95L, "LucamOnlyOnMonochrome")
                                  .map(96L, "LucamNo8bppTo48bpp")
                                  .map(97L, "LucamLut8Obsolete")
                                  .map(98L, "LucamFunctionNotSupported")
                                  .map(99L, "LucamRetryLimitReached")
                                  .map(100L, "LucamLgDeviceError")
                                  .map(101L, "LucamInvalidIpConfiguration")
                                  .map(102L, "LucamInvalidLicense")
                                  .map(103L, "LucamNoSystemEnumerator")
                                  .map(104L, "LucamBusEnumeratorNotInstalled")
                                  .map(105L, "LucamUnknownExternInterface")
                                  .map(106L, "LucamInterfaceDriverNotInstalled")
                                  .map(107L, "LucamCameraDriverNotInstalled")
                                  .map(108L, "LucamCameraDriverInstallInProgress")
                                  .map(109L, "LucamLucamapiDotDllNotFound")
                                  .map(110L, "LucamLucamapiProcedureNotFound")
                                  .map(111L, "LucamPropertyNotEnumeratable")
                                  .map(112L, "LucamPropertyNotBufferable")
                                  .map(113L, "LucamSingleTapImage")
                                  .map(114L, "LucamUnknownTapConfiguration")
                                  .map(115L, "LucamBufferTooSmall")
                                  .map(116L, "LucamInCallbackOnly")
                                  .map(117L, "LucamPropertyUnavailable")
                                  .map(118L, "LucamTimestampNotEnabled")
                                  .map(119L, "LucamFramecounterNotEnabled")
                                  .map(120L, "LucamNoStatsWhenNotStreaming")
                                  .map(121L, "LucamFrameCapturePending")
                                  .map(122L, "LucamSequencingNotEnabled")
                                  .map(123L, "LucamFeatureNotSequencable")
                                  .map(124L, "LucamSequencingUnknownFeatureType")
                                  .map(125L, "LucamSequencingIndexOutOfSequence")
                                  .map(126L, "LucamSequencingBadFrameNumber")
                                  .map(127L, "LucamInformationNotAvailable")
                                  .map(128L, "LucamSequencingBadSetting")
                                  .map(129L, "LucamAutoFocusNeverStarted")
                                  .map(130L, "LucamAutoFocusNotRunning")
                                  .map(1121L, "LucamCameraNotOpenable")
                                  .map(1122L, "LucamCameraNotSupported")
                                  .map(1123L, "LucamMmapFailed")
                                  .map(1124L, "LucamNotWhileStreaming")
                                  .map(1125L, "LucamNoStreamingRights")
                                  .map(1126L, "LucamCameraInitializationError")
                                  .map(1127L, "LucamCannotVerifyPixelFormat")
                                  .map(1128L, "LucamCannotVerifyStartPosition")
                                  .map(1129L, "LucamOsError")
                                  .map(1130L, "LucamBufferNotAvailable")
                                  .map(1131L, "LucamQueuingFailed");


}
