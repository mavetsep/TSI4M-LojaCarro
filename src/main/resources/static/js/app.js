(() => {
    const state = { user: null, editingCarId: null, editingClientId: null, editingUserId: null, auditPage: 0 };
    const byId = (id) => document.getElementById(id);

    function showMessage(text, type = 'error') {
        const element = byId('app-message');
        element.textContent = text || '';
        element.className = text ? `message ${type}` : 'message';
    }

    async function request(url, options = {}) {
        const config = { ...options, headers: { ...(options.body ? { 'Content-Type': 'application/json' } : {}), ...(options.headers || {}) } };
        const response = await fetch(url, config);
        const contentType = response.headers.get('content-type') || '';
        const body = contentType.includes('application/json') ? await response.json() : await response.text();
        if (!response.ok) {
            const fields = body && body.campos ? ` ${Object.values(body.campos).join(' ')}` : '';
            throw new Error((body && body.erro ? body.erro : `Erro HTTP ${response.status}`) + fields);
        }
        return body;
    }

    const isVendor = () => state.user && state.user.perfil === 'VENDEDOR';
    const isAdmin = () => state.user && state.user.perfil === 'ADMINISTRADOR';

    async function loadCars() {
        const cars = await request('carro/listarCarros');
        const body = byId('cars-body');
        body.replaceChildren();
        cars.forEach((car) => {
            const row = document.createElement('tr');
            row.append(cell(car.modelo), cell(car.ano), cell(Number(car.preco).toFixed(2)), actionsForCar(car));
            body.append(row);
        });
    }

    function cell(value) {
        const element = document.createElement('td');
        element.textContent = value ?? '';
        return element;
    }

    function actionsForCar(car) {
        const element = document.createElement('td');
        if (isVendor() || isAdmin()) {
            const edit = document.createElement('button');
            edit.textContent = 'Editar';
            edit.addEventListener('click', () => editCar(car));
            element.append(edit);
        }
        if (isAdmin()) {
            const remove = document.createElement('button');
            remove.textContent = 'Excluir';
            remove.className = 'danger';
            remove.addEventListener('click', async () => {
                if (!window.confirm(`Excluir o carro ${car.modelo}?`)) return;
                try { await request(`carro/${car.id}`, { method: 'DELETE' }); await loadCars(); showMessage('Carro excluído.', 'success'); }
                catch (error) { showMessage(error.message); }
            });
            element.append(remove);
        }
        return element;
    }

    function editCar(car) {
        state.editingCarId = car.id;
        byId('car-modelo').value = car.modelo;
        byId('car-ano').value = car.ano;
        byId('car-preco').value = car.preco;
        byId('car-form-title').textContent = 'Editar carro';
        byId('car-form-section').classList.remove('hidden');
        window.scrollTo({ top: byId('car-form-section').offsetTop, behavior: 'smooth' });
    }

    function resetCarForm() {
        state.editingCarId = null;
        byId('car-form').reset();
        byId('car-form-title').textContent = 'Cadastrar carro';
    }

    async function saveCar(event) {
        event.preventDefault();
        const payload = { modelo: byId('car-modelo').value, ano: Number(byId('car-ano').value), preco: Number(byId('car-preco').value) };
        try {
            const url = state.editingCarId ? `carro/${state.editingCarId}` : 'carro/salvar';
            await request(url, { method: state.editingCarId ? 'PUT' : 'POST', body: JSON.stringify(payload) });
            resetCarForm(); await loadCars(); showMessage('Carro salvo com sucesso.', 'success');
        } catch (error) { showMessage(error.message); }
    }

    async function loadClients() {
        const clients = await request('usuarios/clientes');
        const body = byId('clients-body');
        body.replaceChildren();
        clients.forEach((client) => {
            const row = document.createElement('tr');
            row.append(cell(client.nome), cell(client.usuario), cell(client.perfil), clientActions(client));
            body.append(row);
        });
    }

    function clientActions(client) {
        const element = document.createElement('td');
        const edit = document.createElement('button');
        edit.textContent = 'Editar';
        edit.addEventListener('click', () => editClient(client));
        element.append(edit);
        return element;
    }

    function editClient(client) {
        state.editingClientId = client.id;
        byId('client-name').value = client.nome;
        byId('client-username').value = client.usuario;
        byId('client-password').value = '';
        byId('client-form-title').textContent = 'Editar cliente';
    }

    function resetClientForm() {
        state.editingClientId = null;
        byId('client-form').reset();
        byId('client-form-title').textContent = 'Cadastrar cliente';
    }

    async function saveClient(event) {
        event.preventDefault();
        const password = byId('client-password').value;
        const payload = { nome: byId('client-name').value, usuario: byId('client-username').value };
        if (password) payload.senha = password;
        try {
            const url = state.editingClientId ? `usuarios/clientes/${state.editingClientId}` : 'usuarios/clientes';
            await request(url, { method: state.editingClientId ? 'PUT' : 'POST', body: JSON.stringify(payload) });
            resetClientForm(); await loadClients(); showMessage('Cliente salvo com sucesso.', 'success');
        } catch (error) { showMessage(error.message); }
    }

    async function loadUsers() {
        const users = await request('usuarios');
        const body = byId('users-body');
        body.replaceChildren();
        users.forEach((user) => {
            const row = document.createElement('tr');
            row.append(cell(user.nome), cell(user.usuario), cell(user.perfil), userActions(user));
            body.append(row);
        });
    }

    function auditFilters() {
        return {
            usuario: byId('audit-user').value.trim(),
            acao: byId('audit-action').value,
            recurso: byId('audit-resource').value,
            resultado: byId('audit-result').value,
            dataInicial: byId('audit-start').value,
            dataFinal: byId('audit-end').value
        };
    }

    async function loadAudits(resetPage = false) {
        if (resetPage) state.auditPage = 0;
        const filters = auditFilters();
        const params = new URLSearchParams({ page: String(state.auditPage), size: '10', sort: 'dataHora,desc' });
        Object.entries(filters).forEach(([key, value]) => { if (value) params.set(key, value); });
        const page = await request(`auditorias?${params.toString()}`);
        const body = byId('audits-body');
        body.replaceChildren();
        page.content.forEach((audit) => {
            const row = document.createElement('tr');
            row.append(
                cell(formatAuditDate(audit.dataHora)),
                cell(audit.usuario),
                cell(audit.perfil),
                cell(audit.acao),
                cell(audit.recurso),
                cell(audit.resultado),
                cell(audit.enderecoIp),
                cell(audit.detalhes)
            );
            body.append(row);
        });
        byId('audit-page-info').textContent = page.totalPages === 0
            ? 'Nenhum registro'
            : `Página ${page.number + 1} de ${page.totalPages}`;
        byId('audit-previous').disabled = page.first;
        byId('audit-next').disabled = page.last || page.totalPages === 0;
    }

    function formatAuditDate(value) {
        return value ? value.replace('T', ' ') : '';
    }

    function userActions(user) {
        const element = document.createElement('td');
        const edit = document.createElement('button');
        edit.textContent = 'Editar';
        edit.addEventListener('click', () => editUser(user));
        element.append(edit);
        const remove = document.createElement('button');
        remove.textContent = 'Excluir';
        remove.className = 'danger';
        remove.addEventListener('click', async () => {
            if (!window.confirm(`Excluir o usuário ${user.usuario}?`)) return;
            try { await request(`usuarios/${user.id}`, { method: 'DELETE' }); await loadUsers(); showMessage('Usuário excluído.', 'success'); }
            catch (error) { showMessage(error.message); }
        });
        element.append(remove);
        return element;
    }

    function editUser(user) {
        state.editingUserId = user.id;
        byId('user-name').value = user.nome;
        byId('user-username').value = user.usuario;
        byId('user-password').value = '';
        byId('user-profile').value = user.perfil;
        byId('user-form-title').textContent = 'Editar usuário';
    }

    function resetUserForm() {
        state.editingUserId = null;
        byId('user-form').reset();
        byId('user-form-title').textContent = 'Cadastrar usuário';
    }

    async function saveUser(event) {
        event.preventDefault();
        const password = byId('user-password').value;
        const payload = { nome: byId('user-name').value, usuario: byId('user-username').value, perfil: byId('user-profile').value };
        if (password) payload.senha = password;
        try {
            const url = state.editingUserId ? `usuarios/${state.editingUserId}` : 'usuarios';
            await request(url, { method: state.editingUserId ? 'PUT' : 'POST', body: JSON.stringify(payload) });
            resetUserForm(); await loadUsers(); showMessage('Usuário salvo com sucesso.', 'success');
        } catch (error) { showMessage(error.message); }
    }

    async function logout() {
        try { await request('logout', { method: 'POST' }); }
        finally { window.location.href = 'login.html?logout'; }
    }

    async function start() {
        try {
            state.user = await request('api/auth/me');
            byId('user-summary').textContent = `${state.user.nome} (${state.user.perfil})`;
            const canEditCars = isVendor() || isAdmin();
            byId('car-form-section').classList.toggle('hidden', !canEditCars);
            byId('client-section').classList.toggle('hidden', !isVendor());
            byId('user-section').classList.toggle('hidden', !isAdmin());
            byId('audit-section').classList.toggle('hidden', !isAdmin());
            await loadCars();
            if (isVendor()) await loadClients();
            if (isAdmin()) { await loadUsers(); await loadAudits(); }
        } catch (error) {
            window.location.href = 'login.html';
        }
    }

    byId('logout-button').addEventListener('click', logout);
    byId('refresh-cars').addEventListener('click', () => loadCars().catch((error) => showMessage(error.message)));
    byId('car-form').addEventListener('submit', saveCar);
    byId('cancel-car').addEventListener('click', resetCarForm);
    byId('refresh-clients').addEventListener('click', () => loadClients().catch((error) => showMessage(error.message)));
    byId('client-form').addEventListener('submit', saveClient);
    byId('cancel-client').addEventListener('click', resetClientForm);
    byId('refresh-users').addEventListener('click', () => loadUsers().catch((error) => showMessage(error.message)));
    byId('user-form').addEventListener('submit', saveUser);
    byId('cancel-user').addEventListener('click', resetUserForm);
    byId('refresh-audits').addEventListener('click', () => loadAudits().catch((error) => showMessage(error.message)));
    byId('audit-filter-form').addEventListener('submit', (event) => {
        event.preventDefault();
        loadAudits(true).catch((error) => showMessage(error.message));
    });
    byId('audit-previous').addEventListener('click', () => {
        if (state.auditPage > 0) { state.auditPage -= 1; loadAudits().catch((error) => showMessage(error.message)); }
    });
    byId('audit-next').addEventListener('click', () => {
        state.auditPage += 1;
        loadAudits().catch((error) => showMessage(error.message));
    });
    start();
})();
