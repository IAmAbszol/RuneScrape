package net.runelite.client.plugins.runescrape.output;

import net.runelite.client.plugins.runescrape.RuneScrapeConfig;
import org.json.JSONObject;

import java.io.IOException;
import java.net.*;
import java.util.Queue;

public class SocketWriter extends BaseWriter {

    private DatagramSocket senderSocket = null;
    private InetAddress senderAddress = null;

    public SocketWriter(RuneScrapeConfig config, String writerName) {
        super(config, writerName);

        try {
            senderAddress = InetAddress.getByName(config.address());
            senderSocket = new DatagramSocket();
        } catch (UnknownHostException uhe) {
            uhe.printStackTrace();
        } catch (SecurityException se) {
            se.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Boolean call() throws Exception {
        boolean return_status = true;
        try {
            byte[] buffer = this.message.toString().getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, senderAddress, config.port());
            senderSocket.send(packet);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return_status = false;
        }
        return return_status;
    }

    @Override
    public void shutDown() {
        senderSocket.close();
    }
}
