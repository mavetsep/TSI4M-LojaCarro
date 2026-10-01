package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.dto.UsuarioResponse;
import br.org.edu.ifrn.LojaCarro.exception.RecursoNaoEncontradoException;
import br.org.edu.ifrn.LojaCarro.model.AcaoAuditoria;
import br.org.edu.ifrn.LojaCarro.model.ResultadoAuditoria;
import br.org.edu.ifrn.LojaCarro.services.AuditoriaService;
import br.org.edu.ifrn.LojaCarro.services.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;
    private final Optional<AuditoriaService> auditoriaService;

    public AuthController(UsuarioService usuarioService, Optional<AuditoriaService> auditoriaService) {
        this.usuarioService = usuarioService;
        this.auditoriaService = auditoriaService;
    }

    @GetMapping("/me")
    public UsuarioResponse me(Authentication authentication) {
        UsuarioResponse response = usuarioService.findByUsuario(authentication.getName())
                .map(usuarioService::toResponse)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário autenticado não foi encontrado"));
        auditoriaService.ifPresent(service -> service.registrar(
                AcaoAuditoria.CONSULTAR, "USUARIO", response.id(), ResultadoAuditoria.SUCESSO,
                "Consulta do usuário autenticado"));
        return response;
    }
}
