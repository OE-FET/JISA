package JISA;

import org.python.util.PythonInterpreter;

import java.io.InputStream;

public class PythonScript {

    private InputStream       stream;
    private PythonInterpreter interpreter;

    public PythonScript(String fileName) {
        interpreter = new PythonInterpreter();
        stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
    }

    public void run() {
        interpreter.execfile(stream);
    }

}
