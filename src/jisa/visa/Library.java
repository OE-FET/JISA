package jisa.visa;

public interface Library extends com.sun.jna.Library {

    /**
     * This method is called by JISA whenever the library is loaded. Use this to call any initialisation methods
     * the library needs to be called before use.
     *
     * @throws Exception If something goes wrong...
     */
    void initialise() throws Exception;

}
