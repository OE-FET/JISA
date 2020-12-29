package jisa.devices;

public interface IVSource extends ISource, VSource {

    public static String getDescription() {
        return "Current and Voltage Source";
    }

}
