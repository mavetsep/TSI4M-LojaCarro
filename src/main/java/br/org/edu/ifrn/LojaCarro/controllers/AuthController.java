package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.entity.Usuario;
import br.org.edu.ifrn.LojaCarro.repository.UsuarioRepository;
import br.org.edu.ifrn.LojaCarro.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager,
                          UsuarioRepository usuarioRepository,
                          JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha())
        );

        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow();

        String token = jwtService.generateToken(usuario);

        return ResponseEntity.ok(new LoginResponse(token));
    }

    public record LoginRequest(String email, String senha) {}
    public record LoginResponse(String token) {}
}