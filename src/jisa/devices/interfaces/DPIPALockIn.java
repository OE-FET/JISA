package jisa.devices.interfaces;

public interface DPIPALockIn extends IPALockIn, DPLockIn {

    static String getDescription() {
        return "Dual-Phase Lock-In Amplifier with Current Pre-Amp";
    }

}
