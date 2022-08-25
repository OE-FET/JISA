package jisa;

import jisa.CMS_microscopy_experiment.CameraWrapper;

import java.io.IOException;

public class CameraWrapperTesting {
    public static void main(String[] args) throws IOException, InterruptedException {
        while (true){
            CameraWrapper camera = new CameraWrapper("localhost", 4920);
            camera.enableLogger(null, null);
            // wait for user input
            camera.setFileName("D:\\01_iSCAT_Working_Folders\\Yansheng\\test folder\\test2");
            camera.setNFrame(15);
            camera.isRecording();
            camera.startRecording();
            camera.setStatusDisplay("Hello world!");
            camera.setStatusDisplay("Bye world!");
            camera.closeConnection();
        }

    }
}
