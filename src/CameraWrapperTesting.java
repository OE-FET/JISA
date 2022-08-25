import jisa.CMS_microscopy_experiment.CameraWrapper;

import java.io.IOException;

public class CameraWrapperTesting {
    public static void main(String[] args) throws IOException, InterruptedException {
        CameraWrapper camera = new CameraWrapper("localhost", 4920);
        camera.enableLogger(null, null);
        while (true)
        {
            // wait for user input
            camera.setFileName("my file name");
            camera.setNFrame(1000);
            camera.isRecording();
            camera.startRecording();
            camera.setStatusDisplay("Hello world!");
            camera.setStatusDisplay("Hello world!");
        }
    }
}
