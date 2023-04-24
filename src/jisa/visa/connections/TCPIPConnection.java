package jisa.visa.connections;

import jisa.visa.VISAException;

public interface TCPIPConnection extends Connection {

    void setKeepAliveEnabled(boolean on) throws VISAException;

    boolean isKeepAliveEnabled() throws VISAException;

}
