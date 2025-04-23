// app.js

window.addEventListener('DOMContentLoaded', () => {
    // Clear any leftover sessionStorage so each instance starts fresh
    sessionStorage.removeItem('username');

    const loginScreen   = document.getElementById('login-screen');
    const chatScreen    = document.getElementById('chat-screen');
    const usernameInput = document.getElementById('username');
    const rememberBox   = document.getElementById('remember');
    const messagesDiv   = document.getElementById('messages');
    const msgInput      = document.getElementById('msg-input');
    const btnLogin      = document.getElementById('btn-login');
    const btnSend       = document.getElementById('btn-send');

    // Expose API for inline handlers
    window.login              = login;
    window.logout             = logout;
    window.sendMessage        = sendMessage;
    window.appendMessage      = appendMessage;
    window.appendNotification = appendNotification;

    // Always show login first
    loginScreen.classList.remove('hidden');
    chatScreen.classList.add('hidden');

    // Bind events
    btnLogin.addEventListener('click', login);
    usernameInput.addEventListener('keydown', e => {
        if (e.key === 'Enter') login();
    });

    btnSend.addEventListener('click', sendMessage);
    msgInput.addEventListener('keydown', e => {
        if (e.key === 'Enter') sendMessage();
    });
});

// Show chat UI and notify Java
function login() {
    const user = document.getElementById('username').value.trim();
    if (!user) return alert('Please enter a username');
    // Remember in session if checkbox is checked
    if (document.getElementById('remember').checked) {
        sessionStorage.setItem('username', user);
    }
    document.getElementById('login-screen').classList.add('hidden');
    document.getElementById('chat-screen').classList.remove('hidden');
    document.getElementById('chat-user').textContent = user;

    if (window.javaClient) {
        try {
            javaClient.connect();
            javaClient.sendName(user);
        } catch (err) {
            console.error('Connection error:', err);
            alert('Cannot connect: ' + err.message);
        }
    } else {
        console.error('javaClient not available yet');
    }
}

function logout() {
    sessionStorage.removeItem('username');
    document.getElementById('chat-screen').classList.add('hidden');
    document.getElementById('login-screen').classList.remove('hidden');
    document.getElementById('messages').innerHTML = '';
}

function appendNotification(text) {
    const messagesDiv = document.getElementById('messages');
    const notice = document.createElement('div');
    notice.className = 'msg notification';
    notice.textContent = text;
    messagesDiv.appendChild(notice);
    messagesDiv.scrollTop = messagesDiv.scrollHeight;
}

function appendMessage(user, text) {
    const current = document.getElementById('chat-user').textContent;
    const isSelf  = (user === current);
    const wrapper = document.createElement('div');
    wrapper.className = 'msg ' + (isSelf ? 'self' : 'other');

    if (isSelf) {
        wrapper.innerHTML = `<div class="bubble self-bubble">${text}</div>`;
    } else {
        wrapper.innerHTML = `
      <div class="avatar">${user.charAt(0).toUpperCase()}</div>
      <div class="bubble">${text}</div>
    `;
    }

    const messagesDiv = document.getElementById('messages');
    messagesDiv.appendChild(wrapper);
    messagesDiv.scrollTop = messagesDiv.scrollHeight;
}

function sendMessage() {
    const txt = document.getElementById('msg-input').value.trim();
    if (!txt) return;
    if (window.javaClient) {
        javaClient.sendMessage(txt);
    } else {
        console.error('javaClient not available');
    }
    document.getElementById('msg-input').value = '';
    document.getElementById('msg-input').focus();
}
