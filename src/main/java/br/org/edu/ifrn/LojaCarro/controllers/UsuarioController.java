package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.dto.UsuarioAdminCreateRequest;
import br.org.edu.ifrn.LojaCarro.dto.UsuarioAdminUpdateRequest;
import br.org.edu.ifrn.LojaCarro.dto.UsuarioClienteCreateRequest;
import br.org.edu.ifrn.LojaCarro.dto.UsuarioClienteUpdateRequest;
import br.org.edu.ifrn.LojaCarro.dto.UsuarioResponse;
import br.org.edu.ifrn.LojaCarro.services.UsuarioService;
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

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarRespostaPorId(id));
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> criar(@Valid @RequestBody UsuarioAdminCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.criarAdministrativo(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioAdminUpdateRequest request) {
        return ResponseEntity.ok(usuarioService.atualizarAdministrativo(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        usuarioService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/clientes")
    public ResponseEntity<List<UsuarioResponse>> listarClientes() {
        return ResponseEntity.ok(usuarioService.listarClientes());
    }

    @PostMapping("/clientes")
    public ResponseEntity<UsuarioResponse> criarCliente(
            @Valid @RequestBody UsuarioClienteCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.criarCliente(request));
    }

    @PutMapping("/clientes/{id}")
    public ResponseEntity<UsuarioResponse> atualizarCliente(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioClienteUpdateRequest request) {
        return ResponseEntity.ok(usuarioService.atualizarCliente(id, request));
    }
}
