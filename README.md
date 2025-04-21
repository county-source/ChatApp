## ChatApp

A simple LAN‑based chat application built with JavaFX & WebView (HTML/CSS/Tailwind) for the frontend and plain Java sockets for the backend. Users run a single server and one or more desktop clients that render a Tailwind‑styled chat UI inside an embedded browser component.

---

## Features

- **Multi‑user chat** over TCP sockets (server broadcasts to all clients)  
- **JavaFX WebView UI** loading an HTML/CSS frontend styled with Tailwind  
- **“Remember Me”** login option (per session)  
- **Notifications** when users join or leave  
- **Dark mode** styling with Tailwind utility classes  

---

## Project Structure

- **ChatServer.java** – simple multi‑threaded TCP server on port 12345  
- **ChatClient.java** – socket client with callback interface for UI updates  
- **MainApp.java** – JavaFX `Application` that loads `index.html` into a `WebView`, wires up `ChatClient` via JS bridge  
- **index.html** – login screen & chat UI, uses `output.css` (Tailwind)  
- **input.css** – Tailwind directives (`@tailwind base; @tailwind components; @tailwind utilities;`)  
- **output.css** – generated, minified Tailwind CSS  

---
