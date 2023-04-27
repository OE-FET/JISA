package jisa.gui;

import com.sun.javafx.util.Logging;
import javafx.application.Platform;

import java.util.concurrent.Semaphore;

public class JavaFX {

    public static void launch() {

        // Start-up the JavaFx GUI thread
        Logging.getJavaFXLogger().disableLogging();
        Platform.startup(() -> {});
        Platform.setImplicitExit(false);
        Logging.getJavaFXLogger().enableLogging();

        Semaphore latch = new Semaphore(0);

        Platform.runLater(latch::release);

        latch.acquireUninterruptibly();

    }

}
