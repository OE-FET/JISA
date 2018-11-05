package JISA.VISA;

import JISA.Addresses.InstrumentAddress;
import JISA.Addresses.StrAddress;

public interface Driver {

    long open(InstrumentAddress address) throws VISAException;

    void close(long instrument) throws VISAException;

    void write(long instrument, String toWrite) throws VISAException;

    String read(long instrument, int bufferSize) throws VISAException;

    void setEOI(long instrument, boolean set) throws VISAException;

    void setEOS(long instrument, long character) throws VISAException;

    void setTMO(long instrument, long duration) throws VISAException;

    void setSerial(long instrument, int baud, int data, int parity, int stop, int flow) throws VISAException;

    StrAddress[] search() throws VISAException;

}
