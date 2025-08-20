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

            System.out.println("Launching JavaFX...");

            // Start-up the JavaFx GUI thread
            Platform.startup(() -> { });

            System.out.println("Setting implicit exit...");
            Platform.setImplicitExit(false);

            System.out.println("Starting platform...");
            Semaphore latch = new Semaphore(0);

            Platform.runLater(() -> {
                Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA);
                StyleManager.getInstance().addUserAgentStylesheet(Objects.requireNonNull(GUI.class.getResource("style/breeze.css")).toString());
                latch.release();
            });

            System.out.println("Waiting for JavaFX thread...");

            try {
                latch.tryAcquire(10, TimeUnit.SECONDS);
                System.out.println("Java FX Loaded!");
            } catch (InterruptedException e) {
                System.err.println("Error initialising JavaFX platform!");
                e.printStackTrace();
                throw new RuntimeException(e);
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

}
