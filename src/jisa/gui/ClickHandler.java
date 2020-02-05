package jisa.gui;

public interface ClickHandler {

    void click() throws Exception;

    default void runRegardless() {
        try { click(); } catch (Exception e) {e.printStackTrace();}
    }

    default void start() {
        (new Thread(this::runRegardless)).start();
    }

}
