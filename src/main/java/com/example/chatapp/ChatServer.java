package com.example.chatapp;

import java.io.*;
import java.net.*;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    private static final int PORT = 12345;
    private final Set<PrintWriter> clients = ConcurrentHashMap.newKeySet();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        new ChatServer().start();
    }

    public void start() {
        System.out.println("Server starting on port " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                executor.execute(() -> handleClient(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }

    private void handleClient(Socket socket) {
        System.out.println("New connection from " + socket.getRemoteSocketAddress());
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // Read and broadcast the username
            String name = in.readLine();
            broadcast(name + " se připojil.");
            clients.add(out);

            // Relay messages
            String line;
            while ((line = in.readLine()) != null) {
                broadcast(name + ": " + line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            broadcast("Uživatel odešel.");
        }
    }

    private void broadcast(String message) {
        System.out.println("Broadcast: " + message);
        for (PrintWriter writer : clients) {
            writer.println(message);
        }
    }
}
