package com.example.chatapp;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class ChatServer {
    private static final int PORT = 12345;
    private final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();

    public static void main(String[] args) {
        new ChatServer().start();
    }

    public void start() {
        System.out.println("Server spuštěn na portu " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            ExecutorService pool = Executors.newCachedThreadPool();
            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket);
                clients.add(handler);
                pool.execute(handler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ClientHandler implements Runnable {
        private final Socket socket;
        private final BufferedReader in;
        private final PrintWriter out;
        private final String name;

        ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println("Zadej své jméno:");
            this.name = in.readLine();
            broadcast(name + " se připojil.");
        }

        @Override
        public void run() {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    broadcast(name + ": " + msg);
                }
            } catch (IOException e) {
                // klient odpojen
            } finally {
                broadcast(name + " odešel.");
                close();
            }
        }

        void close() {
            try {
                clients.remove(this);
                socket.close();
            } catch (IOException ignored) {}
        }

        void send(String message) {
            out.println(message);
        }
    }

    private void broadcast(String message) {
        System.out.println("Broadcast: " + message);
        for (ClientHandler c : clients) {
            c.send(message);
        }
    }
}