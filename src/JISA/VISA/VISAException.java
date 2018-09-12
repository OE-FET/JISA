package JISA.VISA;

public class VISAException extends Exception {

    public VISAException(String message, Object... params) {
        super(String.format(message, params));
    }

}
