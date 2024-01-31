package jisa.visa.connections;

import jisa.visa.exceptions.VISAException;

public interface GPIBConnection extends Connection {

    void setEOIEnabled(boolean use) throws VISAException;

    boolean isEOIEnabled() throws VISAException;

}
