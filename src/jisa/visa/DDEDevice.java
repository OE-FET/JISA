package jisa.visa;

import com.pretty_tools.dde.client.DDEClientConversation;
import jisa.addresses.Address;
import jisa.addresses.LXIAddress;
import jisa.devices.DeviceException;
import jisa.devices.interfaces.Instrument;

import java.io.IOException;
import java.net.InetAddress;


public class DDEDevice implements Instrument {

    private final String host;
    private final DDEClientConversation convo;

    public DDEDevice(LXIAddress address, String service, String topic, int timeout) throws Exception{
        host = address.getHost();
        convo = new DDEClientConversation();
        convo.setTimeout(timeout);
        convo.connect(service, topic);
    }

    public DDEDevice(Address address) throws Exception {
        LXIAddress tcp_addr = address.toTCPIPAddress();

        try {
            host = tcp_addr.getHost();
            InetAddress inet = InetAddress.getByName(host);
            if (inet.isReachable(5000)){
                convo = new DDEClientConversation();
                //Setting timeout to 20 minutes
                convo.setTimeout(1200000);
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
        return new LXIAddress(host);
    }

    public String sendRequest(String request) throws Exception {
        return convo.request(request);
    }


}
