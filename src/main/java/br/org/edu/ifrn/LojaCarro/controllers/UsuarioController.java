package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.dto.UsuarioAdminCreateRequest;
import br.org.edu.ifrn.LojaCarro.dto.UsuarioAdminUpdateRequest;
import br.org.edu.ifrn.LojaCarro.dto.UsuarioClienteCreateRequest;
import br.org.edu.ifrn.LojaCarro.dto.UsuarioClienteUpdateRequest;
import br.org.edu.ifrn.LojaCarro.dto.UsuarioResponse;
import br.org.edu.ifrn.LojaCarro.model.AcaoAuditoria;
import br.org.edu.ifrn.LojaCarro.model.ResultadoAuditoria;
import br.org.edu.ifrn.LojaCarro.services.AuditoriaService;
import br.org.edu.ifrn.LojaCarro.services.UsuarioService;
import br.org.edu.ifrn.LojaCarro.util.RequestUtils;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final Optional<AuditoriaService> auditoriaService;

    public UsuarioController(UsuarioService usuarioService, Optional<AuditoriaService> auditoriaService) {
        this.usuarioService = usuarioService;
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar() {
        List<UsuarioResponse> usuarios = usuarioService.listarTodos();
        auditar(AcaoAuditoria.LISTAR, null, ResultadoAuditoria.SUCESSO, "Lista de usuários consultada");
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> buscar(@PathVariable Long id) {
        UsuarioResponse usuario = usuarioService.buscarRespostaPorId(id);
        auditar(AcaoAuditoria.CONSULTAR, id, ResultadoAuditoria.SUCESSO,
                "Usuário consultado: id=" + id);
        return ResponseEntity.ok(usuario);
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody UsuarioAdminCreateRequest request) {
        UsuarioResponse usuario = usuarioService.criarAdministrativo(request);
        auditar(AcaoAuditoria.CRIAR, usuario.id(), ResultadoAuditoria.SUCESSO,
                "Usuário criado: id=" + usuario.id() + ", usuario="
                        + RequestUtils.safe(usuario.usuario(), 80) + ", perfil=" + usuario.perfil());
        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioAdminUpdateRequest request) {
        UsuarioResponse anterior = usuarioService.buscarRespostaPorId(id);
        UsuarioResponse usuario = usuarioService.atualizarAdministrativo(id, request);
        String detalhes = anterior.perfil() != usuario.perfil()
                ? "Perfil alterado: id=" + usuario.id() + ", de " + anterior.perfil()
                + " para " + usuario.perfil()
                : "Usuário alterado: id=" + usuario.id() + ", usuario="
                + RequestUtils.safe(usuario.usuario(), 80);
        auditar(AcaoAuditoria.EDITAR, usuario.id(), ResultadoAuditoria.SUCESSO, detalhes);
        return ResponseEntity.ok(usuario);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        UsuarioResponse usuario = usuarioService.buscarRespostaPorId(id);
        usuarioService.excluir(id);
        auditar(AcaoAuditoria.EXCLUIR, id, ResultadoAuditoria.SUCESSO,
                "Usuário excluído: id=" + id + ", usuario=" + RequestUtils.safe(usuario.usuario(), 80));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/clientes")
    public ResponseEntity<List<UsuarioResponse>> listarClientes() {
        List<UsuarioResponse> usuarios = usuarioService.listarClientes();
        auditar(AcaoAuditoria.LISTAR, null, ResultadoAuditoria.SUCESSO, "Lista de clientes consultada");
        return ResponseEntity.ok(usuarios);
    }

    @PostMapping("/clientes")
    public ResponseEntity<UsuarioResponse> criarCliente(
            @Valid @RequestBody UsuarioClienteCreateRequest request) {
        UsuarioResponse usuario = usuarioService.criarCliente(request);
        auditar(AcaoAuditoria.CRIAR, usuario.id(), ResultadoAuditoria.SUCESSO,
                "Usuário criado: id=" + usuario.id() + ", usuario="
                        + RequestUtils.safe(usuario.usuario(), 80) + ", perfil=" + usuario.perfil());
        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }

    @PutMapping("/clientes/{id}")
    public ResponseEntity<UsuarioResponse> atualizarCliente(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioClienteUpdateRequest request) {
        UsuarioResponse usuario = usuarioService.atualizarCliente(id, request);
        auditar(AcaoAuditoria.EDITAR, usuario.id(), ResultadoAuditoria.SUCESSO,
                "Usuário alterado: id=" + usuario.id() + ", usuario="
                        + RequestUtils.safe(usuario.usuario(), 80));
        return ResponseEntity.ok(usuario);
    }

    private void auditar(AcaoAuditoria acao, Long recursoId, ResultadoAuditoria resultado, String detalhes) {
        auditoriaService.ifPresent(service -> service.registrar(
                acao, "USUARIO", recursoId, resultado, detalhes));
    }
}
