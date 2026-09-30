package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.model.Perfil;
import br.org.edu.ifrn.LojaCarro.model.Usuario;
import br.org.edu.ifrn.LojaCarro.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UsuarioDetailsServiceTest {

    @Test
    void deveLocalizarUsuarioPeloCampoUsuario() {
        UsuarioRepository repository = mock(UsuarioRepository.class);
        Usuario usuario = new Usuario("Administrador", "admin", "$2a$10$hash", Perfil.ADMINISTRADOR);
        when(repository.findByUsuario("admin")).thenReturn(Optional.of(usuario));

        var details = new UsuarioDetailsService(repository).loadUserByUsername("admin");

        assertEquals("admin", details.getUsername());
        assertEquals("ROLE_ADMINISTRADOR", details.getAuthorities().iterator().next().getAuthority());
    }
}
