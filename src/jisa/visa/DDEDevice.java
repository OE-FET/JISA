package jisa.visa;

import com.pretty_tools.dde.client.DDEClientConversation;
import jisa.Main;
import jisa.addresses.Address;
import jisa.addresses.TCPIPAddress;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.Instrument;
import jisa.gui.GUI;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;


public class DDEDevice implements Instrument {

    private final String host;
    private final DDEClientConversation convo;

    static {
        String path = System.getProperty("java.library.path");

        try {

            // Create temporary directory to extract native libraries to
            File tempDir = Files.createTempDirectory("dde-extracted-").toFile();

            // Read the list of all jfx native libraries contained in this jar
            Scanner nat = new Scanner(Main.class.getResourceAsStream("/native/dde-libs.txt"));

            // Make sure we clean up when we exit.
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {

                File   directory = new File(tempDir.getAbsolutePath());
                File[] contents  = directory.listFiles();

                if (contents != null) {

                    for (File file : contents) {
                        file.delete();
                    }

                }

                directory.delete();

            }));

            // We only want to extract libraries for the current platform
            String osName = System.getProperty("os.name").toLowerCase();
            String extension;
            String libSep;

            if (osName.contains("win")) {
                extension = ".dll";
                libSep    = ";";
            } else if (osName.contains("mac")) {
                extension = ".dylib";
                libSep    = ":";
            } else {
                extension = ".so";
                libSep    = ":";
            }

            // Run through each library listed in the file, extracting it if it's for this platform
            while (nat.hasNextLine()) {

                String name = nat.nextLine();

                if (name.contains(extension)) {
                    InputStream resource = Main.class.getResourceAsStream("/native/" + name);
                    Files.copy(resource, Paths.get(tempDir.toString(), name));
                    resource.close();
                }

            }

            // Add the temporary directory to the library path list
            path = tempDir.toString() + libSep + path;
            System.setProperty("java.library.path", path);

        } catch (Exception ignored) {
            // If this goes wrong, then continue as planned hoping the there is a copy of JavaFx already installed
        }
    }

    public DDEDevice(TCPIPAddress address, String service, String topic, int timeout) throws Exception{
        host = address.getHost();
        convo = new DDEClientConversation();
        convo.setTimeout(timeout);
        convo.connect(service, topic);
    }

    public DDEDevice(Address address) throws Exception {
        TCPIPAddress tcp_addr = address.toTCPIPAddress();

        try {
            host = tcp_addr.getHost();
            InetAddress inet = InetAddress.getByName(host);
            if (inet.isReachable(5000)){
                convo = new DDEClientConversation();
                convo.setTimeout(50000);
                convo.connect("Opus", "System");
            }
            else{
                throw new Exception("Failed to connect to device IP address");
            }
        }
        catch (Exception e) {
            throw e;
        }
    }

    @Override
    public String getIDN() throws IOException, DeviceException {
        return "DDE Device";
    }

    @Override
    public void close() throws IOException, DeviceException {
        try{
            convo.disconnect();
        }
        catch (Exception e) {
            throw new IOException("Failed to disconnect DDE conversation");
        }
    }

    @Override
    public Address getAddress() {
        return new TCPIPAddress(host);
    }

    public String sendRequest(String request) throws Exception {
        return convo.request(request);
    }


}
