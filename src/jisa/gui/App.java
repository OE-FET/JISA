package jisa.gui;

import javafx.application.Application;
import javafx.stage.Stage;

import java.util.concurrent.Semaphore;

public class App extends Application {

    static Semaphore s = new Semaphore(0);

    @Override
    public void start(Stage primaryStage) throws Exception {
        s.release();
    }

}
