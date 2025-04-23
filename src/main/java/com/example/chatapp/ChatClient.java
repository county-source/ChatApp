package com.example.chatapp;

import java.io.*;
import java.net.*;

/**
 * ChatClient discovers the server via UDP, then connects via TCP.
 */
public class ChatClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private UIUpdater ui;

    public interface UIUpdater { void update(String user, String text); }

    public void setUIUpdater(UIUpdater ui) {
        this.ui = ui;
    }

    /** Automatically discover server and connect */
    public void connect() {
        try {
            // 1) Discover server address
            InetSocketAddress addr = discoverServer();                              // :contentReference[oaicite:4]{index=4}

            // 2) Open TCP socket
            socket = new Socket(addr.getAddress(), addr.getPort());
            in     = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out    = new PrintWriter(socket.getOutputStream(), true);
            new Thread(this::listen).start();
            System.out.println("ChatClient connected to " + addr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Broadcasts a UDP packet and waits for the server’s reply */
    private InetSocketAddress discoverServer() throws IOException {
        try (DatagramSocket ds = new DatagramSocket()) {
            ds.setBroadcast(true);
            byte[] req = DiscoveryConfig.DISCOVERY_REQUEST.getBytes();
            DatagramPacket sendPkt = new DatagramPacket(
                    req, req.length,
                    InetAddress.getByName("255.255.255.255"),
                    DiscoveryConfig.UDP_DISCOVERY_PORT
            );
            ds.send(sendPkt);                                                       //

            // wait for response
            byte[] buf = new byte[256];
            DatagramPacket recvPkt = new DatagramPacket(buf, buf.length);
            ds.setSoTimeout(3000);
            ds.receive(recvPkt);
            String resp = new String(recvPkt.getData(), 0, recvPkt.getLength());
            // format: "CHAT_SERVER_HERE:12345"
            String portStr = resp.substring(resp.indexOf(':') + 1);
            return new InetSocketAddress(
                    recvPkt.getAddress(),
                    Integer.parseInt(portStr)
            );
        }
    }

    public void sendName(String name) {
        if (out == null) connect();
        out.println(name);
        if (ui != null) ui.update("", name + " se připojil.");
    }

    public void sendMessage(String text) {
        if (out == null) connect();
        out.println(text);
    }

    private void listen() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                String user = "", msg = line;
                int idx = line.indexOf(": ");
                if (idx != -1) {
                    user = line.substring(0, idx);
                    msg  = line.substring(idx + 2);
                }
                if (ui != null) ui.update(user, msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
