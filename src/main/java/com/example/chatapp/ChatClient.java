package com.example.chatapp;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private UIUpdater ui;

    public interface UIUpdater { void update(String user, String text); }
    public void setUIUpdater(UIUpdater ui) { this.ui = ui; }

    /** Automatically discover and then connect */
    public void connect() {
        try {
            InetSocketAddress serverAddr = discoverServer();
            socket = new Socket(serverAddr.getAddress(), serverAddr.getPort());
            in     = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out    = new PrintWriter(socket.getOutputStream(), true);
            new Thread(this::listen).start();
            System.out.println("Connected to " + serverAddr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Broadcasts discovery on every interface’s broadcast address */
    private InetSocketAddress discoverServer() throws IOException {
        byte[] req = DiscoveryConfig.DISCOVERY_REQUEST.getBytes();
        DatagramSocket ds = new DatagramSocket();
        ds.setSoTimeout(3000);

        // Enumerate all non-loopback interfaces :contentReference[oaicite:4]{index=4}
        Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
        List<InetAddress> broadcastAddrs = new ArrayList<>();

        while (ifaces.hasMoreElements()) {
            NetworkInterface nif = ifaces.nextElement();
            if (nif.isLoopback() || !nif.isUp()) continue;
            for (InterfaceAddress ia : nif.getInterfaceAddresses()) {
                InetAddress bc = ia.getBroadcast();
                if (bc != null) {
                    broadcastAddrs.add(bc);
                }
            }
        }

        // Send to each subnet broadcast :contentReference[oaicite:5]{index=5}
        for (InetAddress bcAddr : broadcastAddrs) {
            DatagramPacket pkt = new DatagramPacket(
                    req, req.length, bcAddr, DiscoveryConfig.UDP_DISCOVERY_PORT
            );
            ds.send(pkt);  // send on each interface :contentReference[oaicite:6]{index=6}
        }

        // Wait for the first valid response
        byte[] buf = new byte[256];
        DatagramPacket respPkt = new DatagramPacket(buf, buf.length);
        ds.receive(respPkt);  // blocks up to timeout :contentReference[oaicite:7]{index=7}

        String resp = new String(respPkt.getData(), 0, respPkt.getLength());
        // Expected format "CHAT_SERVER_HERE:<port>"
        int port = Integer.parseInt(resp.split(":")[1]);
        ds.close();
        return new InetSocketAddress(respPkt.getAddress(), port);
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