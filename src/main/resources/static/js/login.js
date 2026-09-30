(() => {
    const message = document.getElementById('login-message');
    const params = new URLSearchParams(window.location.search);
    if (params.has('erro')) {
        message.textContent = 'Usuário ou senha inválidos.';
        message.className = 'message error';
    } else if (params.has('logout')) {
        message.textContent = 'Logout realizado com sucesso.';
        message.className = 'message success';
    }
})();
