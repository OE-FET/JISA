package jisa.visa.connections;

import jisa.visa.VISAException;

public interface TCPIPConnection extends Connection {

    void setKeepAlive(boolean on) throws VISAException;

    boolean isKeepAlive() throws VISAException;

}
