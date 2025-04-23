window.addEventListener('DOMContentLoaded', () => {
    const loginScreen   = document.getElementById('login-screen');
    const chatScreen    = document.getElementById('chat-screen');
    const usernameInput = document.getElementById('username');
    const messagesDiv   = document.getElementById('messages');
    const msgInput      = document.getElementById('msg-input');
    const btnLogin      = document.getElementById('btn-login');
    const btnSend       = document.getElementById('btn-send');

    btnLogin.addEventListener('click', login);
    usernameInput.addEventListener('keydown', e => e.key==='Enter' && login());
    btnSend.addEventListener('click', sendMessage);
    msgInput.addEventListener('keydown', e => e.key==='Enter' && sendMessage());

    loginScreen.classList.remove('hidden');
    chatScreen.classList.add('hidden');
});

function login() {
    const user = document.getElementById('username').value.trim();
    if (!user) return alert('Enter username');
    document.getElementById('login-screen').classList.add('hidden');
    document.getElementById('chat-screen').classList.remove('hidden');
    document.getElementById('chat-user').textContent = user;

    javaClient.connect();      // auto-discovers via UDP, then TCP
    javaClient.sendName(user);
}

function logout() {
    document.getElementById('chat-screen').classList.add('hidden');
    document.getElementById('login-screen').classList.remove('hidden');
    document.getElementById('messages').innerHTML = '';
}

function appendNotification(text) {
    const d = document.createElement('div');
    d.className = 'msg notification';
    d.textContent = text;
    document.getElementById('messages').appendChild(d);
    scrollBottom();
}

function appendMessage(user, text) {
    const cur = document.getElementById('chat-user').textContent;
    const self = user === cur;
    const w = document.createElement('div');
    w.className = 'msg ' + (self?'self':'other');
    if (self) {
        w.textContent = text;
    } else {
        w.innerHTML = `<div class="avatar">${user.charAt(0).toUpperCase()}</div>
                   <div class="bubble">${text}</div>`;
    }
    document.getElementById('messages').appendChild(w);
    scrollBottom();
}

function sendMessage() {
    const txt = document.getElementById('msg-input').value.trim();
    if (!txt) return;
    javaClient.sendMessage(txt);
    document.getElementById('msg-input').value = '';
}

function scrollBottom() {
    const m = document.getElementById('messages');
    m.scrollTop = m.scrollHeight;
}
