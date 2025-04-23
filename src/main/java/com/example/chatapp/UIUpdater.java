package com.example.chatapp;

/**
 * A simple callback interface that ChatClient will call
 * whenever a new line arrives from the server.
 */
public interface UIUpdater {
    /**
     * @param user the username prefix (empty if it was a notification)
     * @param text the rest of the message (or full notification text)
     */
    void onUpdate(String user, String text);
}
