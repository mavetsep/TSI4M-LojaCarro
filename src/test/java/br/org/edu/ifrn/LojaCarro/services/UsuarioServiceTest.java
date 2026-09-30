package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.dto.UsuarioAdminCreateRequest;
import br.org.edu.ifrn.LojaCarro.dto.UsuarioAdminUpdateRequest;
import br.org.edu.ifrn.LojaCarro.dto.UsuarioClienteUpdateRequest;
import br.org.edu.ifrn.LojaCarro.dto.UsuarioResponse;
import br.org.edu.ifrn.LojaCarro.exception.AcessoNegadoException;
import br.org.edu.ifrn.LojaCarro.exception.UsuarioDuplicadoException;
import br.org.edu.ifrn.LojaCarro.model.Perfil;
import br.org.edu.ifrn.LojaCarro.model.Usuario;
import br.org.edu.ifrn.LojaCarro.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private UsuarioService usuarioService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(usuarioRepository, passwordEncoder);
    }

    @Test
    void deveSalvarSenhaComBCryptEDevolverSomenteDadosSeguros() {
        when(usuarioRepository.existsByUsuarioIgnoreCase("admin")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(1L);
            return usuario;
        });

        UsuarioResponse response = usuarioService.criarAdministrativo(
                new UsuarioAdminCreateRequest("Administrador", "admin", "admin123", Perfil.ADMINISTRADOR));

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario saved = captor.getValue();
        assertTrue(saved.getSenha().startsWith("$2"));
        assertTrue(passwordEncoder.matches("admin123", saved.getSenha()));
        assertEquals(1L, response.id());
        assertEquals("admin", response.usuario());
        assertEquals(Perfil.ADMINISTRADOR, response.perfil());
    }

    @Test
    void deveRetornarConflitoParaUsuarioDuplicado() {
        when(usuarioRepository.existsByUsuarioIgnoreCase("admin")).thenReturn(true);

        assertThrows(UsuarioDuplicadoException.class, () -> usuarioService.criarAdministrativo(
                new UsuarioAdminCreateRequest("Outro", "admin", "admin123", Perfil.CLIENTE)));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void devePreservarSenhaQuandoEdicaoNaoInformaNovaSenha() {
        Usuario existente = new Usuario("Administrador", "admin", "$2a$10$senha-atual", Perfil.ADMINISTRADOR);
        existente.setId(1L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(usuarioRepository.existsByUsuarioIgnoreCaseAndIdNot("admin", 1L)).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        usuarioService.atualizarAdministrativo(1L,
                new UsuarioAdminUpdateRequest("Administrador", "admin", "", Perfil.ADMINISTRADOR));

        assertEquals("$2a$10$senha-atual", existente.getSenha());
    }

    @Test
    void endpointDeClientesNaoPodeAlterarUsuarioDeOutroPerfil() {
        Usuario vendedor = new Usuario("Vendedor", "vendedor", "hash", Perfil.VENDEDOR);
        vendedor.setId(2L);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(vendedor));

        assertThrows(AcessoNegadoException.class, () -> usuarioService.atualizarCliente(2L,
                new UsuarioClienteUpdateRequest("Novo nome", "vendedor", "")));
    }
}
