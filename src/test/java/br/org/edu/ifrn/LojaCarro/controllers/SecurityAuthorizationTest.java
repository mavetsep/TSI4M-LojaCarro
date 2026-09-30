package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.model.Carro;
import br.org.edu.ifrn.LojaCarro.services.CarroService;
import br.org.edu.ifrn.LojaCarro.services.UsuarioDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CarroController.class)
@Import(br.org.edu.ifrn.LojaCarro.config.SecurityConfig.class)
class SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CarroService carroService;

    @MockBean
    private UsuarioDetailsService usuarioDetailsService;

    @Test
    void clientePodeListarCarros() throws Exception {
        when(carroService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/carro/listarCarros").with(user("cliente").roles("CLIENTE")))
                .andExpect(status().isOk());
    }

    @Test
    void clienteNaoPodeCadastrarCarro() throws Exception {
        mockMvc.perform(post("/carro/salvar")
                        .with(user("cliente").roles("CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"modelo\":\"Gol\",\"ano\":2024,\"preco\":50000}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void vendedorPodeCadastrarCarro() throws Exception {
        when(carroService.save(any(Carro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/carro/salvar")
                        .with(user("vendedor").roles("VENDEDOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"modelo\":\"Gol\",\"ano\":2024,\"preco\":50000}"))
                .andExpect(status().isOk());
    }

    @Test
    void vendedorNaoPodeExcluirCarro() throws Exception {
        mockMvc.perform(delete("/carro/1").with(user("vendedor").roles("VENDEDOR")))
                .andExpect(status().isForbidden());
    }
}
