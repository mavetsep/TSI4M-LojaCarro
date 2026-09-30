package br.org.edu.ifrn.LojaCarro.config;

import br.org.edu.ifrn.LojaCarro.repository.UsuarioRepository;
import br.org.edu.ifrn.LojaCarro.services.UsuarioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final String nome;
    private final String usuario;
    private final String senha;

    public AdminInitializer(
            UsuarioRepository usuarioRepository,
            UsuarioService usuarioService,
            @Value("${APP_ADMIN_NAME:Administrador}") String nome,
            @Value("${APP_ADMIN_USERNAME:admin}") String usuario,
            @Value("${APP_ADMIN_PASSWORD:admin123}") String senha) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
        this.nome = nome;
        this.usuario = usuario;
        this.senha = senha;
    }

    @Override
    public void run(String... args) {
        if (!usuarioRepository.existsByPerfil(br.org.edu.ifrn.LojaCarro.model.Perfil.ADMINISTRADOR)) {
            usuarioService.criarAdministradorInicial(nome, usuario, senha);
        }
    }
}
