package jisa.devices.smu;

import jisa.addresses.Address;
import jisa.devices.DeviceException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AgilentB1500A extends AgilentSPA {

    public final GNDU GNDU = new GNDU("GNDU", 0);

    private final List<ASMU> smuChannels;
    private final List<GNDU> switchChannels = List.of(GNDU);
    private final List<AVMU> vmuChannels;
    private final List<AVSU> vsuChannels;

    public static String getDescription() {
        return "Agilent B1500A Series SPA";
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

    public AgilentB1500A(Address address) throws IOException, DeviceException {

        super(address, false);

        List<ASMU> smuChannels    = new ArrayList<>();
        List<GNDU> switchChannels = new ArrayList<>();
        List<AVMU> vmuChannels    = new ArrayList<>();
        List<AVSU> vsuChannels    = new ArrayList<>();

        String[] slots = query("UNT?").toUpperCase().split(";");

        for (int i = 0; i < slots.length; i++) {

            String[] parts = slots[i].split(",");

            switch (parts[0]) {

                case "B1520A":
                    // Don't know what to do with a CMU ngl
                    break;

                case "B1511A":
                    smuChannels.add(new ASMU(String.format("MP-SMU %d", i + 1), i + 1, null, null));
                    break;

                case "B1517A":
                    smuChannels.add(new ASMU(String.format("HR-SMU %d", i + 1), i + 1, null, null));
                    break;

                case "B1510A":
                    smuChannels.add(new ASMU(String.format("HP-SMU %d", i + 1), i + 1, null, null));
                    break;

                case "B1525A":
                    // Not sure what to do with an SPGU either
                    break;


            }

        }

        this.smuChannels = List.copyOf(smuChannels);
        this.vmuChannels = List.copyOf(vmuChannels);
        this.vsuChannels = List.copyOf(vsuChannels);

    }

    @Override
    protected boolean confirmIdentity() {

        try {
            return getIDN().split(",")[1].contains("B1500A");
        } catch (Throwable e) {
            return false;
        }

    }

//    protected enum VoltRange implements AgilentRange {
//
//        // Ranges for normal SMU units
//        SMU_2_V(UnitType.SMU, 11, 2.0, 100e-3),
//        SMU_20_V(UnitType.SMU, 12, 20.0, 100e-3),
//        SMU_40_V(UnitType.SMU, 13, 40.0, 50e-3),
//        SMU_100_V(UnitType.SMU, 14, 100.0, 20e-3),
//
//        // Ranges for High-Power SMU units
//        HPSMU_2_V(UnitType.HPSMU, 11, 20.0, 1.0),
//        HPSMU_20_V(UnitType.HPSMU, 12, 20.0, 1.0),
//        HPSMU_40_V(UnitType.HPSMU, 13, 40.0, 500e-3),
//        HPSMU_100_V(UnitType.HPSMU, 14, 100.0, 125e-3),
//        HPSMU_200_V(UnitType.HPSMU, 15, 200.0, 50e-3);
//
//        private final UnitType type;
//        private final int      code;
//        private final double   range;
//        private final double   iComp;
//
//        VoltRange(UnitType type, int code, double range, double iComp) {
//            this.type  = type;
//            this.code  = code;
//            this.range = range;
//            this.iComp = iComp;
//        }
//
//        public static AgilentRange fromVoltage(UnitType type, double value) {
//
//            for (VoltRange range : values()) {
//
//                if (range.getRange() >= Math.abs(value) && range.type == type) {
//                    return range;
//                }
//
//            }
//
//            return AUTO_RANGING;
//
//        }
//
//        public int toInt() {
//            return code;
//        }
//
//        public double getRange() {
//            return range;
//        }
//
//        public double getCompliance() {return iComp;}
//
//    }
//
//    protected enum CurrRange implements AgilentRange {
//
//        // Ranges for normal SMU units
//        SMU_1_pA(UnitType.SMU, 9, 1e-12, 100),
//        SMU_10_pA(UnitType.SMU, 9, 10e-12, 100),
//        SMU_100_pA(UnitType.SMU, 10, 100e-12, 100),
//        SMU_1_nA(UnitType.SMU, 11, 1e-9, 100),
//        SMU_10_nA(UnitType.SMU, 12, 10e-9, 100),
//        SMU_100_nA(UnitType.SMU, 13, 100e-9, 100),
//        SMU_1_uA(UnitType.SMU, 14, 1e-6, 100),
//        SMU_10_uA(UnitType.SMU, 15, 10e-6, 100),
//        SMU_100_uA(UnitType.SMU, 16, 100e-6, 100),
//        SMU_1_mA(UnitType.SMU, 17, 1e-3, 100),
//        SMU_10_mA(UnitType.SMU, 18, 10e-3, 100),
//        SMU_100_mA(UnitType.SMU, 19, 100e-3, 20),
//
//        // Ranges for High-Power SMU units
//        HPSMU_1_nA(UnitType.HPSMU, 11, 1e-9, 200),
//        HPSMU_10_nA(UnitType.HPSMU, 12, 10e-9, 200),
//        HPSMU_100_nA(UnitType.HPSMU, 13, 100e-9, 200),
//        HPSMU_1_uA(UnitType.HPSMU, 14, 1e-6, 200),
//        HPSMU_10_uA(UnitType.HPSMU, 15, 10e-6, 200),
//        HPSMU_100_uA(UnitType.HPSMU, 16, 100e-6, 200),
//        HPSMU_1_mA(UnitType.HPSMU, 17, 1e-3, 200),
//        HPSMU_10_mA(UnitType.HPSMU, 18, 10e-3, 200),
//        HPSMU_100_mA(UnitType.HPSMU, 19, 100e-3, 100),
//        HPSMU_1_A(UnitType.HPSMU, 20, 1.0, 20);
//
//        private final UnitType type;
//        private final int      code;
//        private final double   range;
//        private final double   vComp;
//
//        CurrRange(UnitType type, int code, double range, double vComp) {
//            this.type  = type;
//            this.code  = code;
//            this.range = range;
//            this.vComp = vComp;
//        }
//
//        public static AgilentRange fromCurrent(UnitType type, double value) {
//
//            for (CurrRange range : values()) {
//
//                if (range.getRange() >= Math.abs(value) && range.type == type) {
//                    return range;
//                }
//
//            }
//
//            return AUTO_RANGING;
//
//        }
//
//        public int toInt() {
//            return code;
//        }
//
//        public double getRange() {
//            return range;
//        }
//
//        public double getCompliance() {return vComp;}
//
//    }


}
