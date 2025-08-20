package jisa.gui;

import com.sun.javafx.css.StyleManager;
import com.sun.javafx.util.Logging;
import javafx.application.Application;
import javafx.application.Platform;

import java.util.Objects;
import java.util.concurrent.Semaphore;

public class JavaFX {

    public static void launch() {

        // Start-up the JavaFx GUI thread
        Logging.getJavaFXLogger().disableLogging();
        Platform.startup(() -> {});
        Platform.setImplicitExit(false);
        Logging.getJavaFXLogger().disableLogging();

        Semaphore latch = new Semaphore(0);

        Platform.runLater(() -> {

            try {
                Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA);
                StyleManager.getInstance().addUserAgentStylesheet(Objects.requireNonNull(GUI.class.getResource("style/breeze.css")).toString());
            } finally {
                latch.release();
            }

        });

        latch.acquireUninterruptibly();

    }

}
