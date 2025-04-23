window.addEventListener('DOMContentLoaded', () => {
    const loginScreen   = document.getElementById('login-screen');
    const chatScreen    = document.getElementById('chat-screen');
    const usernameInput = document.getElementById('username');
    const messagesDiv   = document.getElementById('messages');
    const msgInput      = document.getElementById('msg-input');
    const btnLogin      = document.getElementById('btn-login');
    const btnSend       = document.getElementById('btn-send');

    // Bind events
    btnLogin.addEventListener('click', login);
    usernameInput.addEventListener('keydown', e => e.key==='Enter' && login());
    btnSend.addEventListener('click', sendMessage);
    msgInput.addEventListener('keydown', e => e.key==='Enter' && sendMessage());

    // Show login first
    loginScreen.classList.remove('hidden');
    chatScreen.classList.add('hidden');
});

// Login and notify server
function login() {
    const user = document.getElementById('username').value.trim();
    if (!user) return alert('Enter username');
    document.getElementById('login-screen').classList.add('hidden');
    document.getElementById('chat-screen').classList.remove('hidden');
    document.getElementById('chat-user').textContent = user;

    javaClient.connect();           // connects to localhost:12345
    javaClient.sendName(user);      // notifies join
}

// Logout
function logout() {
    document.getElementById('chat-screen').classList.add('hidden');
    document.getElementById('login-screen').classList.remove('hidden');
    document.getElementById('messages').innerHTML = '';
}

// Append a join/leave notification
function appendNotification(text) {
    const div = document.createElement('div');
    div.className = 'msg notification';
    div.textContent = text;
    document.getElementById('messages').appendChild(div);
    scrollToBottom();
}

// Append a chat bubble
function appendMessage(user, text) {
    const current = document.getElementById('chat-user').textContent;
    const isSelf = (user===current);
    const wrapper = document.createElement('div');
    wrapper.className = 'msg ' + (isSelf?'self':'other');
    if (isSelf) {
        wrapper.innerHTML = `<div class="bubble self-bubble">${text}</div>`;
    } else {
        wrapper.innerHTML = `
      <div class="avatar">${user.charAt(0).toUpperCase()}</div>
      <div class="bubble">${text}</div>
    `;
    }
    document.getElementById('messages').appendChild(wrapper);
    scrollToBottom();
}

// Send a chat message
function sendMessage() {
    const txt = document.getElementById('msg-input').value.trim();
    if (!txt) return;
    javaClient.sendMessage(txt);
    document.getElementById('msg-input').value = '';
}

// Scroll messages to bottom
function scrollToBottom() {
    const m = document.getElementById('messages');
    m.scrollTop = m.scrollHeight;
}
