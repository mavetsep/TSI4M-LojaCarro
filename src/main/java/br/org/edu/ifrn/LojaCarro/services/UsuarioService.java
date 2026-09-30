package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.dto.UsuarioAdminCreateRequest;
import br.org.edu.ifrn.LojaCarro.dto.UsuarioAdminUpdateRequest;
import br.org.edu.ifrn.LojaCarro.dto.UsuarioClienteCreateRequest;
import br.org.edu.ifrn.LojaCarro.dto.UsuarioClienteUpdateRequest;
import br.org.edu.ifrn.LojaCarro.dto.UsuarioResponse;
import br.org.edu.ifrn.LojaCarro.exception.AcessoNegadoException;
import br.org.edu.ifrn.LojaCarro.exception.DadosInvalidosException;
import br.org.edu.ifrn.LojaCarro.exception.RecursoNaoEncontradoException;
import br.org.edu.ifrn.LojaCarro.exception.UsuarioDuplicadoException;
import br.org.edu.ifrn.LojaCarro.model.Perfil;
import br.org.edu.ifrn.LojaCarro.model.Usuario;
import br.org.edu.ifrn.LojaCarro.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarTodos() {
        return usuarioRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarClientes() {
        return usuarioRepository.findByPerfilOrderByNomeAsc(Perfil.CLIENTE)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarRespostaPorId(Long id) {
        return toResponse(buscarPorId(id));
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> findByUsuario(String usuario) {
        return usuarioRepository.findByUsuario(usuario);
    }

    @Transactional
    public UsuarioResponse criarAdministrativo(UsuarioAdminCreateRequest request) {
        Usuario usuario = criar(request.nome(), request.usuario(), request.senha(), request.perfil());
        return toResponse(usuario);
    }

    @Transactional
    public UsuarioResponse criarCliente(UsuarioClienteCreateRequest request) {
        Usuario usuario = criar(request.nome(), request.usuario(), request.senha(), Perfil.CLIENTE);
        return toResponse(usuario);
    }

    @Transactional
    public UsuarioResponse atualizarAdministrativo(Long id, UsuarioAdminUpdateRequest request) {
        Usuario usuario = prepararAtualizacao(id, request.nome(), request.usuario());
        usuario.setPerfil(request.perfil());
        atualizarSenhaSeInformada(usuario, request.senha());
        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse atualizarCliente(Long id, UsuarioClienteUpdateRequest request) {
        Usuario existente = buscarPorId(id);
        if (existente.getPerfil() != Perfil.CLIENTE) {
            throw new AcessoNegadoException("Somente usuários com perfil CLIENTE podem ser editados neste endpoint");
        }
        Usuario usuario = prepararAtualizacao(id, request.nome(), request.usuario());
        usuario.setPerfil(Perfil.CLIENTE);
        atualizarSenhaSeInformada(usuario, request.senha());
        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public void excluir(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    @Transactional
    public Usuario criarAdministradorInicial(String nome, String usuario, String senha) {
        String nomeNormalizado = normalizarObrigatorio(nome, "Nome");
        String usuarioNormalizado = normalizarUsuario(usuario);
        validarSenha(senha, true);
        verificarUsuarioDisponivel(usuarioNormalizado, null);

        Usuario novoUsuario = new Usuario(nomeNormalizado, usuarioNormalizado,
                passwordEncoder.encode(senha), Perfil.ADMINISTRADOR);
        return usuarioRepository.save(novoUsuario);
    }

    private Usuario criar(String nome, String usuario, String senha, Perfil perfil) {
        String nomeNormalizado = normalizarObrigatorio(nome, "Nome");
        String usuarioNormalizado = normalizarUsuario(usuario);
        if (perfil == null) {
            throw new DadosInvalidosException("Perfil é obrigatório");
        }
        validarSenha(senha, true);
        verificarUsuarioDisponivel(usuarioNormalizado, null);

        Usuario novoUsuario = new Usuario(nomeNormalizado, usuarioNormalizado,
                passwordEncoder.encode(senha), perfil);
        return usuarioRepository.save(novoUsuario);
    }

    private Usuario prepararAtualizacao(Long id, String nome, String usuario) {
        Usuario existente = buscarPorId(id);
        String nomeNormalizado = normalizarObrigatorio(nome, "Nome");
        String usuarioNormalizado = normalizarUsuario(usuario);
        verificarUsuarioDisponivel(usuarioNormalizado, id);

        existente.setNome(nomeNormalizado);
        existente.setUsuario(usuarioNormalizado);
        return existente;
    }

    private void atualizarSenhaSeInformada(Usuario usuario, String senha) {
        if (senha != null && !senha.isBlank()) {
            validarSenha(senha, false);
            usuario.setSenha(passwordEncoder.encode(senha));
        }
    }

    private void verificarUsuarioDisponivel(String usuario, Long idAtual) {
        boolean duplicado = idAtual == null
                ? usuarioRepository.existsByUsuarioIgnoreCase(usuario)
                : usuarioRepository.existsByUsuarioIgnoreCaseAndIdNot(usuario, idAtual);
        if (duplicado) {
            throw new UsuarioDuplicadoException(usuario);
        }
    }

    private Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado: " + id));
    }

    private String normalizarObrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new DadosInvalidosException(campo + " é obrigatório");
        }
        return valor.trim();
    }

    private String normalizarUsuario(String usuario) {
        String normalizado = normalizarObrigatorio(usuario, "Nome de usuário");
        if (normalizado.length() > 80) {
            throw new DadosInvalidosException("Nome de usuário deve ter no máximo 80 caracteres");
        }
        return normalizado;
    }

    private void validarSenha(String senha, boolean obrigatoria) {
        if (senha == null || senha.isBlank()) {
            if (obrigatoria) {
                throw new DadosInvalidosException("Senha é obrigatória no cadastro");
            }
            return;
        }
        if (senha.length() < 6) {
            throw new DadosInvalidosException("A senha deve ter pelo menos 6 caracteres");
        }
    }

    public UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getUsuario(), usuario.getPerfil());
    }
}
