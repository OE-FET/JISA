package jisa.control;

public interface SRunnable {

    void run() throws Exception;

    default void runRegardless() {

        try {
            run();
        } catch (Exception ignored) {

        }

    }

    default void start() {
        (new Thread(this::runRegardless)).start();
    }

    static SRunnable fromJProxy(Runnable runnable) {
        return runnable::run;
    }

}
