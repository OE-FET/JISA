package jisa.gui;

import com.sun.javafx.css.StyleManager;
import javafx.application.Application;
import javafx.application.Platform;

import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class JavaFX {

    public static void launch() {

        try {

            // Start-up the JavaFx GUI thread
            Platform.startup(() -> { });
            Platform.setImplicitExit(false);

            Semaphore latch = new Semaphore(0);

            Platform.runLater(() -> {
                Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA);
                StyleManager.getInstance().addUserAgentStylesheet(Objects.requireNonNull(GUI.class.getResource("style/breeze.css")).toString());
                latch.release();
            });

            latch.tryAcquire(10, TimeUnit.SECONDS);

        } catch (Throwable e) {
            System.err.println("Error initialising JavaFX platform!");
            e.printStackTrace();
        }

    }

}
