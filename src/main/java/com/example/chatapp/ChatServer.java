package com.example.chatapp;

import java.io.*;
import java.net.*;
import java.util.Set;
import java.util.concurrent.*;

/**
 * ChatServer with UDP discovery responder and TCP broadcast chat.
 * All IOExceptions are caught internally to avoid unreported exception errors.
 */
public class ChatServer {
    private static final int TCP_PORT = 12345;
    private static final int UDP_DISCOVERY_PORT = 54321;

    private final Set<PrintWriter> clients = ConcurrentHashMap.newKeySet();
    private final ExecutorService pool = Executors.newCachedThreadPool();  // efficient thread pooling :contentReference[oaicite:4]{index=4}

    public static void main(String[] args) {
        new ChatServer().start();
    }

    /** Starts both UDP discovery responder and TCP chat server */
    public void start() {
        // Start discovery responder
        startDiscoveryResponder();                                               // binds to 0.0.0.0 for UDP :contentReference[oaicite:5]{index=5}

        // Start TCP server in try-catch to handle IOExceptions
        try (ServerSocket server = new ServerSocket(TCP_PORT)) {
            System.out.println("ChatServer TCP listening on port " + TCP_PORT);
            while (true) {
                Socket sock = server.accept();                                   // may throw IOException :contentReference[oaicite:6]{index=6}
                pool.execute(() -> handleClient(sock));                          // cannot propagate checked exceptions :contentReference[oaicite:7]{index=7}
            }
        } catch (IOException e) {
            System.err.println("Error in TCP server: " + e.getMessage());
            e.printStackTrace();
        } finally {
            pool.shutdown();
        }
    }

    /** Responds to UDP discovery requests */
    private void startDiscoveryResponder() {
        pool.execute(() -> {
            try (DatagramSocket ds = new DatagramSocket(UDP_DISCOVERY_PORT,
                    InetAddress.getByName("0.0.0.0"))) { // :contentReference[oaicite:8]{index=8}
                System.out.println("Discovery responder on UDP port " + UDP_DISCOVERY_PORT);
                byte[] buf = new byte[256];
                while (true) {
                    DatagramPacket pkt = new DatagramPacket(buf, buf.length);
                    ds.receive(pkt);                                              // may throw IOException :contentReference[oaicite:9]{index=9}
                    String req = new String(pkt.getData(), 0, pkt.getLength());
                    if ("CHAT_SERVER_DISCOVERY".equals(req)) {
                        byte[] resp = ("CHAT_SERVER_HERE:" + TCP_PORT).getBytes();
                        DatagramPacket reply = new DatagramPacket(
                                resp, resp.length, pkt.getAddress(), pkt.getPort()
                        );
                        ds.send(reply);                                         // send response
                    }
                }
            } catch (IOException e) {
                System.err.println("Error in discovery responder: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /** Handles a single TCP client: broadcasts join, all messages, and leave */
    private void handleClient(Socket sock) {
        System.out.println("Client connected: " + sock.getRemoteSocketAddress());
        try (BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
             PrintWriter out = new PrintWriter(sock.getOutputStream(), true)) {     // auto-flush on println :contentReference[oaicite:10]{index=10}

            String name = in.readLine();                                           // may throw IOException
            broadcast(name + " se připojil.");
            clients.add(out);

            String line;
            while ((line = in.readLine()) != null) {
                broadcast(name + ": " + line);
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
            e.printStackTrace();
        } finally {
            broadcast("Uživatel odešel.");
        }
    }

    /** Sends a message to all connected clients */
    private void broadcast(String msg) {
        System.out.println("Broadcast: " + msg);
        for (PrintWriter client : clients) {
            client.println(msg);                                                  // safe even if socket closed :contentReference[oaicite:11]{index=11}
        }
    }
}
