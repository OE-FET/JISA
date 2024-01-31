package jisa.visa.exceptions;

import java.io.IOException;

public class VISAException extends IOException {

    public VISAException(String message, Object... params) {
        super(String.format(message, params));
    }

}
