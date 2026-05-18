package jisa.gui;

import com.sun.javafx.css.StyleManager;
import javafx.application.Application;
import javafx.application.Platform;

import javax.swing.*;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class JavaFX {

    public static void launch() {

        try {

            if (com.sun.jna.Platform.isLinux() && System.getProperty("glass.gtk.uiScale") == null) {

                int dpi;
                try {
                    // Short command execution syntax
                    Process p = Runtime.getRuntime().exec(new String[]{"bash", "-c", "xrdb -query | awk -F':[\t ]*' '/Xft.dpi/ {print $2}'"});

                    // Read all bytes directly and clean the string
                    String output = new String(p.getInputStream().readAllBytes()).trim();

                    dpi = output.isEmpty() ? 96 : Integer.parseInt(output);
                } catch (IOException | NumberFormatException e) {
                    dpi = 96; // Fallback default
                }

                double factor = (double) dpi / 96.0;

                System.setProperty("glass.gtk.uiScale", String.format("%.02f", factor));

            }

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
