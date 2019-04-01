package JISA;

import org.python.util.PythonInterpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class PythonScript {

    private InputStream       stream;
    private PythonInterpreter interpreter;

    public PythonScript(URL file) throws IOException {

        interpreter = new PythonInterpreter();
        stream = new FileInputStream(file.getPath());

    }

    public void run() {
        interpreter.execfile(stream);
    }

}
