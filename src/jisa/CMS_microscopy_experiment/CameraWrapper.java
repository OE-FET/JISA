package jisa.CMS_microscopy_experiment;

import jisa.visa.VISADevice;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.logging.*;

import static java.lang.Thread.sleep;

public class CameraWrapper {
    Socket socket;
    OutputStream output;
    InputStream input;
    InputStreamReader reader;

    final static byte[] SET_FILE_NAME = {1};
    final static byte[] SET_N_FRAME = {2};
    final static byte[] IS_RECORDING = {3};
    final static byte[] START_RECORDING = {4};
    final static byte[] SET_STATUS_DISPLAY = {5};
    final static byte[] CLOSE_CONNECTION = {6};

    // logging utilities
    private Logger logger = null;
    private StreamHandler loggerHandler = null;
    private boolean loggerEnabled = false;


    public CameraWrapper(String host, int port) throws IOException, InterruptedException {
        socket = new Socket(host, port);

        output = socket.getOutputStream();
        input = socket.getInputStream();
        while ((input == null) || (output == null)) {
            addLog(Level.WARNING, "input or output null");
            sleep(1000);
            output = socket.getOutputStream();
            input = socket.getInputStream();
        }
        reader = new InputStreamReader(input);
    }

    private void clearInputStream() throws IOException {
        byte[] dummy_array = input.readAllBytes();
    }

    public void setFileName(String fileName) throws IOException {
        addLog(Level.INFO, "setFileName " + fileName);
        byte[] filename_byte = string2ByteArray(fileName);
        byte[] length = int2ByteArray(filename_byte.length);
        output.write(SET_FILE_NAME);
        output.write(length);
        output.write(filename_byte);
        output.flush();
    }

    public void setNFrame(int nFrame) throws IOException {
        addLog(Level.INFO, "setNFrame " + nFrame);
        byte[] nFrame_byte = int2ByteArray(nFrame);
        output.write(SET_N_FRAME);
        output.write(nFrame_byte);
        output.flush();
    }

    // checks if the camera is currently busy
    public boolean isRecording() throws IOException, InterruptedException {
        addLog(Level.INFO, "check is recording");
        //clearInputStream();
        output.write(IS_RECORDING);
        int data = input.read();
        addLog(Level.INFO, "received " + data);
        while (data == -1) {
            data = input.read();
            addLog(Level.INFO, "received " + data);
            sleep(10);
        }

        return data != 0;
    }

    public void startRecording() throws IOException {
        addLog(Level.INFO, "start recording");
        output.write(START_RECORDING);
        output.flush();
    }

    public void setStatusDisplay(String displayMessage) throws IOException {
        addLog(Level.INFO, "setStatusDisplay " + displayMessage);
        byte[] displayMessage_byte = string2ByteArray(displayMessage);
        byte[] length = int2ByteArray(displayMessage_byte.length);
        output.write(SET_STATUS_DISPLAY);
        output.write(length);
        output.write(displayMessage_byte);
        output.flush();
    }

    public void closeConnection() throws IOException
    {
        addLog(Level.INFO, "closing connection...");
        output.write(CLOSE_CONNECTION);
        output.flush();
    }

    private byte[] string2ByteArray(String string)
    {
        return string.getBytes(StandardCharsets.US_ASCII);
    }

    private byte[] int2ByteArray(int val)
    {
        return ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(val).array();
    }



    // logging functions
    /**
     * Enable the logging functions for the device. The driver will log all the commands sent out and all the
     * readings received. Once enabled, the logging cannot be disabled (for now).
     *
     * @param loggerName Name of the logger. It should be distinct from other loggers and ideally shorter than
     *                   15 characters. If the name given is null, the class name will be used as a default.
     * @param logFileName Name of the file where the log will be written. If the name is null, the log will be printed
     *                    to the terminal.
     */
    public void enableLogger(String loggerName, String logFileName) {
        if (loggerEnabled)
            return;
        loggerEnabled = true;
        if (loggerName == null)
        {
            loggerName = this.getClass().getName();
        }
        logger = Logger.getLogger(loggerName);
        logger.setUseParentHandlers(false);
        if (logFileName == null)
        {
            loggerHandler = new ConsoleHandler();
            loggerHandler.setLevel(Level.INFO);
        }
        else
        {
            try {
                loggerHandler = new FileHandler(logFileName);
                loggerHandler.setLevel(Level.INFO);
            } catch (IOException e){
                System.out.print("Logger file handler initialization failed!");
                loggerEnabled = false;
                return;
            }
        }
        logger.addHandler(loggerHandler);
        loggerHandler.setFormatter(new CustomLoggerFormatter());
        logger.log(Level.INFO, "Logger enabled");
    }

    /**
     * Check if the logger is enabled.
     * @return true if the logger is enabled.
     */
    public boolean isLoggerEnabled()
    {
        return loggerEnabled;
    }

    /**
     * Add a new log entry. Safe to call even if the logger is not enabled. In this case, the function will do nothing.
     * @param level level of the log entry
     * @param message message of the log entry
     */
    public void addLog(Level level, String message)
    {
        if (loggerEnabled)
            logger.log(level, message);
    }

    private static class CustomLoggerFormatter extends SimpleFormatter
    {
        // formatter for the optional logging functionality.
        private static final String format = "[%1$tF %1$tT] [%2$-7s] [%3$-15.15s] %4$s %n";

        @Override
        public synchronized String format(LogRecord lr) {
            return String.format(format,
                    new Date(lr.getMillis()),
                    lr.getLevel().getLocalizedName(),
                    lr.getLoggerName(),
                    lr.getMessage()
            );
        }
    }

}
