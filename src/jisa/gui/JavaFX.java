package jisa.gui;

import jisa.Util;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class JavaFX {

    public static void launch() {

        // Start-up the JavaFx GUI thread
        try {
            Thread t = new Thread(() -> Application.launch(App.class));
            t.start();
            Platform.setImplicitExit(false);
        } catch (Exception ignored) {

        }

        Semaphore semaphore = new Semaphore(0);

        int RETRY_COUNT = 10;
        int count = 0;
        while(true)
        {
            try
            {
                Util.sleep((100L * count));
                Platform.runLater(semaphore::release);
                break;
            }
            catch (IllegalStateException e)
            {
                if(++count == RETRY_COUNT) throw e;
            }
        }

        try {
            semaphore.acquire();
        } catch (InterruptedException ignored) {}

    }


    public static class App extends Application {

        public final static Semaphore s = new Semaphore(0);

        @Override
        public void start(Stage primaryStage) throws Exception {
            s.release();
        }

    }

}
