package jisa.control;

public interface SRunnable {

    void run() throws Exception;

    default void runRegardless() {

        try {
            run();
        } catch (Exception ignored) {

        }

    }

}
