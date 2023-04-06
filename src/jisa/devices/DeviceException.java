package jisa.devices;

public class DeviceException extends Exception {

    private final String message;

    public DeviceException(String message, Object... args) {

        this.message = String.format(message, args);

    }

    public String getMessage() {
        return message;
    }

}
