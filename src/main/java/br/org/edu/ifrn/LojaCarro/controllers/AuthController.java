package br.org.edu.ifrn.LojaCarro.controller;

import br.org.edu.ifrn.LojaCarro.entity.Usuario;
import br.org.edu.ifrn.LojaCarro.repository.UsuarioRepository;
import br.org.edu.ifrn.LojaCarro.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          UsuarioRepository usuarioRepository,
                          JwtService jwtService,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        Usuario usuario = usuarioRepository.findByUsername(request.username())
                .orElseThrow();

        String token = jwtService.generateToken(usuario);

        return ResponseEntity.ok(new LoginResponse(token));
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Usuario usuario) {
        Optional<Usuario> existente = usuarioRepository.findByUsername(usuario.getUsername());
        if (existente.isPresent()) {
            return ResponseEntity.badRequest().body("Usuário já existe");
        }

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuarioRepository.save(usuario);

        return ResponseEntity.ok("Usuário cadastrado com sucesso");
    }

    public record LoginRequest(String username, String password) {}
    public record LoginResponse(String token) {}
}