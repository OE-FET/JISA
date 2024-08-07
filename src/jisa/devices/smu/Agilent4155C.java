package jisa.devices.smu;

import jisa.addresses.Address;
import jisa.devices.DeviceException;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class Agilent4155C extends AgilentSPA {

    public static String getDescription() {
        return "Agilent 4155C SPA";
    }

    /* All possible SMUs, VMUs, and VSUs */
    public final ASMU SMU1  = new ASMU("SMU 1", 1, SMUVoltageRanges.values(), SMUCurrentRanges.values());
    public final ASMU SMU2  = new ASMU("SMU 2", 2, SMUVoltageRanges.values(), SMUCurrentRanges.values());
    public final ASMU SMU3  = new ASMU("SMU 3", 3, SMUVoltageRanges.values(), SMUCurrentRanges.values());
    public final ASMU SMU4  = new ASMU("SMU 4", 4, SMUVoltageRanges.values(), SMUCurrentRanges.values());
    public final ASMU SMU5  = new ASMU("SMU 5", 5, SMUVoltageRanges.values(), SMUCurrentRanges.values());
    public final ASMU SMU6  = new ASMU("SMU 6", 6, SMUVoltageRanges.values(), SMUCurrentRanges.values());
    public final ASMU HPSMU = new ASMU("HPSMU", 5, HPSMUVoltageRanges.values(), HPSMUCurrentRanges.values());
    public final AVSU VSU1  = new AVSU("VSU 1", 21);
    public final AVSU VSU2  = new AVSU("VSU 2", 22);
    public final AVMU VMU1  = new AVMU("VMU 1", 23, SMUVoltageRanges.values());
    public final AVMU VMU2  = new AVMU("VMU 2", 24, SMUVoltageRanges.values());
    public final GNDU GNDU  = new GNDU("GNDU", 26);

    private final List<ASMU> smuChannels;
    private final List<GNDU> switchChannels;
    private final List<AVMU> vmuChannels = List.of(VMU1, VMU2);
    private final List<AVSU> vsuChannels = List.of(VSU1, VSU2);

    public Agilent4155C(Address address) throws IOException, DeviceException {

        super(address, true);

        String response = query("UNT?");

        smuChannels    = parseUNTResponse(response, HPSMU, SMU1, SMU2, SMU3, SMU4, SMU5, SMU6);
        switchChannels = response.contains("GNDU") ? List.of(GNDU) : Collections.emptyList();

    }

    @Override
    public List<ASMU> getSMUChannels() {
        return smuChannels;
    }

    @Override
    public List<AVMU> getVMeterChannels() {
        return vmuChannels;
    }

    @Override
    public List<AVSU> getVSourceChannels() {
        return vsuChannels;
    }

    @Override
    public List<GNDU> getSwitchChannels() {
        return switchChannels;
    }

    @Override
    protected boolean confirmIdentity() {

        try {
            String[] idn = getIDN().toUpperCase().split(",");
            return idn[1].contains("4155C");
        } catch (Throwable e) {
            return false;
        }

    }

    protected enum SMUVoltageRanges implements Range {

        // Ranges for normal SMU units
        SMU_2_V(11, 2.0, 100e-3),
        SMU_20_V(12, 20.0, 100e-3),
        SMU_40_V(13, 40.0, 50e-3),
        SMU_100_V(14, 100.0, 20e-3);

        private final int    code;
        private final double range;
        private final double iComp;

        SMUVoltageRanges(int code, double range, double iComp) {
            this.code  = code;
            this.range = range;
            this.iComp = iComp;
        }

        public int toInt() {
            return code;
        }

        public double getRange() {
            return range;
        }

        public double getCompliance() {
            return iComp;
        }

    }

    protected enum HPSMUVoltageRanges implements Range {

        HPSMU_20_V(12, 20.0, 1.0),
        HPSMU_40_V(13, 40.0, 500e-3),
        HPSMU_100_V(14, 100.0, 125e-3),
        HPSMU_200_V(15, 200.0, 50e-3);

        private final int    code;
        private final double range;
        private final double iComp;

        HPSMUVoltageRanges(int code, double range, double iComp) {
            this.code  = code;
            this.range = range;
            this.iComp = iComp;
        }

        public int toInt() {
            return code;
        }

        public double getRange() {
            return range;
        }

        public double getCompliance() {
            return iComp;
        }

    }

    protected enum SMUCurrentRanges implements Range {

        // Ranges for normal SMU units
        SMU_1_nA(11, 1e-9, 100),
        SMU_10_nA(12, 10e-9, 100),
        SMU_100_nA(13, 100e-9, 100),
        SMU_1_uA(14, 1e-6, 100),
        SMU_10_uA(15, 10e-6, 100),
        SMU_100_uA(16, 100e-6, 100),
        SMU_1_mA(17, 1e-3, 100),
        SMU_10_mA(18, 10e-3, 100),
        SMU_100_mA(19, 100e-3, 20);

        private final int    code;
        private final double range;
        private final double vComp;

        SMUCurrentRanges(int code, double range, double vComp) {
            this.code  = code;
            this.range = range;
            this.vComp = vComp;
        }

        public int toInt() {
            return code;
        }

        public double getRange() {
            return range;
        }

        public double getCompliance() {
            return vComp;
        }

    }

    protected enum HPSMUCurrentRanges implements Range {

        // Ranges for normal SMU units
        HPSMU_1_nA(11, 1e-9, 200),
        HPSMU_10_nA(12, 10e-9, 200),
        HPSMU_100_nA(13, 100e-9, 200),
        HPSMU_1_uA(14, 1e-6, 200),
        HPSMU_10_uA(15, 10e-6, 200),
        HPSMU_100_uA(16, 100e-6, 200),
        HPSMU_1_mA(17, 1e-3, 200),
        HPSMU_10_mA(18, 10e-3, 200),
        HPSMU_100_mA(19, 100e-3, 100),
        HPSMU_1_A(20, 1.0, 20);

        private final int    code;
        private final double range;
        private final double vComp;

        HPSMUCurrentRanges(int code, double range, double vComp) {
            this.code  = code;
            this.range = range;
            this.vComp = vComp;
        }

        public int toInt() {
            return code;
        }

        public double getRange() {
            return range;
        }

        public double getCompliance() {
            return vComp;
        }

    }

}
