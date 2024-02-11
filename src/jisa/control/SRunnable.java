package jisa.control;

import jisa.Util;

public interface SRunnable {

    void run() throws Exception;

    static void start(SRunnable runnable) {
        Util.runAsync(() -> Util.runRegardless(runnable));
    }

}
