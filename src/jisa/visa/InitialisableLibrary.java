package jisa.visa;

import com.sun.jna.Library;

public interface InitialisableLibrary extends Library {

    /**
     * This method is called by JISA whenever the library is loaded. Use this to call any initialisation methods
     * the library needs to be called before use.
     *
     * @throws Exception If something goes wrong...
     */
    void initialise() throws Exception;

}
