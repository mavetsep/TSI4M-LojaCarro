package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.entity.Usuario;
import br.org.edu.ifrn.LojaCarro.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuario")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PreAuthorize("hasRole('GERENTE')")
    @PostMapping("/salvar")
    public ResponseEntity<Usuario> salvar(@RequestBody @Valid Usuario usuario) {
        Usuario saved = usuarioService.salvar(usuario);
        saved.setSenha(null);
        return ResponseEntity.ok(saved);
    }
}