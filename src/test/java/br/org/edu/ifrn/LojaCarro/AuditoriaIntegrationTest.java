package br.org.edu.ifrn.LojaCarro;

import br.org.edu.ifrn.LojaCarro.model.AcaoAuditoria;
import br.org.edu.ifrn.LojaCarro.model.Auditoria;
import br.org.edu.ifrn.LojaCarro.model.Carro;
import br.org.edu.ifrn.LojaCarro.model.ResultadoAuditoria;
import br.org.edu.ifrn.LojaCarro.repository.AuditoriaRepository;
import br.org.edu.ifrn.LojaCarro.repository.CarroRepository;
import br.org.edu.ifrn.LojaCarro.repository.UsuarioRepository;
import br.org.edu.ifrn.LojaCarro.services.AuditoriaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(AuditoriaIntegrationTest.RollbackConfiguration.class)
class AuditoriaIntegrationTest {

    @Resource
    private MockMvc mockMvc;

    @Resource
    private AuditoriaRepository auditoriaRepository;

    @Resource
    private CarroRepository carroRepository;

    @Resource
    private UsuarioRepository usuarioRepository;

    @Resource
    private AuditoriaService auditoriaService;

    @BeforeEach
    void limparDadosDeTeste() {
        auditoriaRepository.deleteAll();
        carroRepository.deleteAll();
    }

    @Test
    void loginBemSucedidoGeraAuditoria() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "admin")
                        .param("password", "admin123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/index.html"));

        Auditoria auditoria = ultimaAuditoria(AcaoAuditoria.LOGIN_SUCESSO);
        assertNotNull(auditoria);
        assertTrue(auditoria.getDetalhes().contains("sucesso"));
        assertFalse(auditoria.getDetalhes().contains("admin123"));
    }

    @Test
    void loginInvalidoGeraAuditoriaSemSenha() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "usuario-inexistente")
                        .param("password", "senha-super-secreta"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login.html?erro"));

        Auditoria auditoria = ultimaAuditoria(AcaoAuditoria.LOGIN_FALHA);
        assertTrue(auditoria.getUsuario().equals("usuario-inexistente"));
        assertFalse(auditoria.getDetalhes().contains("senha-super-secreta"));
        assertFalse(auditoria.getDetalhes().matches(".*\\$2[aby]\\$.*"));
    }

    @Test
    void logoutGeraAuditoria() throws Exception {
        mockMvc.perform(post("/logout").with(user("admin").roles("ADMINISTRADOR")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login.html?logout"));

        Auditoria auditoria = ultimaAuditoria(AcaoAuditoria.LOGOUT);
        assertTrue(auditoria.getUsuario().equals("admin"));
    }

    @Test
    void crudDeCarroGeraAuditorias() throws Exception {
        mockMvc.perform(post("/carro/salvar")
                        .with(user("vendedor").roles("VENDEDOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"modelo\":\"Gol\",\"ano\":2024,\"preco\":50000}"))
                .andExpect(status().isOk());
        Carro carro = carroRepository.findFirstByModelo("Gol").orElseThrow();

        mockMvc.perform(put("/carro/{id}", carro.getId())
                        .with(user("vendedor").roles("VENDEDOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"modelo\":\"Polo\",\"ano\":2024,\"preco\":60000}"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/carro/{id}", carro.getId())
                        .with(user("admin").roles("ADMINISTRADOR")))
                .andExpect(status().isNoContent());

        assertNotNull(ultimaAuditoria(AcaoAuditoria.CRIAR));
        assertNotNull(ultimaAuditoria(AcaoAuditoria.EDITAR));
        assertNotNull(ultimaAuditoria(AcaoAuditoria.EXCLUIR));
    }

    @Test
    void crudDeUsuarioGeraAuditoriasSemSenha() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .with(user("admin").roles("ADMINISTRADOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"João\",\"usuario\":\"joao-audit\",\"senha\":\"senha-secreta\",\"perfil\":\"CLIENTE\"}"))
                .andExpect(status().isCreated());

        Auditoria criacao = ultimaAuditoria(AcaoAuditoria.CRIAR);
        assertFalse(criacao.getDetalhes().contains("senha-secreta"));
        assertFalse(criacao.getDetalhes().matches(".*\\$2[aby]\\$.*"));

        mockMvc.perform(put("/usuarios/{id}", usuarioId("joao-audit"))
                        .with(user("admin").roles("ADMINISTRADOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"João Atualizado\",\"usuario\":\"joao-audit\",\"senha\":\"\",\"perfil\":\"VENDEDOR\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/usuarios/{id}", usuarioId("joao-audit"))
                        .with(user("admin").roles("ADMINISTRADOR")))
                .andExpect(status().isNoContent());

        assertNotNull(ultimaAuditoria(AcaoAuditoria.EDITAR));
        assertNotNull(ultimaAuditoria(AcaoAuditoria.EXCLUIR));
        assertTrue(auditoriaRepository.findAll().stream()
                .noneMatch(auditoria -> auditoria.getDetalhes() != null
                        && auditoria.getDetalhes().contains("senha-secreta")));
    }

    @Test
    void clienteEVendedorRecebem403AoConsultarAuditorias() throws Exception {
        mockMvc.perform(get("/auditorias").with(user("cliente").roles("CLIENTE")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/auditorias").with(user("vendedor").roles("VENDEDOR")))
                .andExpect(status().isForbidden());

        assertTrue(auditoriaRepository.findAll().stream()
                .filter(auditoria -> auditoria.getAcao().equals(AcaoAuditoria.ACESSO_NEGADO.name()))
                .count() >= 2);
    }

    @Test
    void administradorConsultaAuditoriasComPaginacao() throws Exception {
        for (int i = 0; i < 3; i++) {
            auditoriaService.registrar(AcaoAuditoria.CONSULTAR, "CARRO", (long) i,
                    ResultadoAuditoria.SUCESSO, "Consulta segura");
        }

        mockMvc.perform(get("/auditorias?page=0&size=2")
                        .with(user("admin").roles("ADMINISTRADOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

    @Test
    void erroDeValidacaoGeraAuditoria() throws Exception {
        mockMvc.perform(post("/carro/salvar")
                        .with(user("vendedor").roles("VENDEDOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"modelo\":\"\",\"ano\":2024,\"preco\":-1}"))
                .andExpect(status().isBadRequest());

        Auditoria erro = ultimaAuditoria(AcaoAuditoria.ERRO);
        assertEquals(ResultadoAuditoria.ERRO, erro.getResultado());
        assertTrue(erro.getDetalhes().contains("inválidos"));
    }

    @Test
    void usuarioDuplicadoGeraAuditoriaSemSenha() throws Exception {
        String payload = "{\"nome\":\"Duplicado\",\"usuario\":\"duplicado-audit\",\"senha\":\"senha-secreta\",\"perfil\":\"CLIENTE\"}";
        mockMvc.perform(post("/usuarios").with(user("admin").roles("ADMINISTRADOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/usuarios").with(user("admin").roles("ADMINISTRADOR"))
                        .contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isConflict());

        Auditoria erro = ultimaAuditoria(AcaoAuditoria.ERRO);
        assertTrue(erro.getDetalhes().contains("duplicado"));
        assertFalse(erro.getDetalhes().contains("senha-secreta"));
    }

    @Test
    void auditoriaDeErroSobreviveAoRollbackDaOperacaoPrincipal() {
        assertThrows(RuntimeException.class, () -> rollbackProbe().executar());

        assertTrue(auditoriaRepository.findAll().stream()
                .anyMatch(auditoria -> auditoria.getDetalhes().equals("Operação que sofreu rollback")));
    }

    private RollbackProbe rollbackProbe() {
        return rollbackProbe;
    }

    @Resource
    private RollbackProbe rollbackProbe;

    private Long usuarioId(String usuario) {
        return usuarioRepository.findByUsuario(usuario).orElseThrow().getId();
    }

    private Auditoria ultimaAuditoria(AcaoAuditoria acao) {
        List<Auditoria> auditorias = auditoriaRepository.findAll().stream()
                .filter(item -> acao.name().equals(item.getAcao()))
                .toList();
        return auditorias.isEmpty() ? null : auditorias.get(auditorias.size() - 1);
    }

    @TestConfiguration
    static class RollbackConfiguration {
        @Bean
        RollbackProbe rollbackProbe(AuditoriaService auditoriaService) {
            return new RollbackProbe(auditoriaService);
        }
    }

    static class RollbackProbe {
        private final AuditoriaService auditoriaService;

        RollbackProbe(AuditoriaService auditoriaService) {
            this.auditoriaService = auditoriaService;
        }

        @Transactional
        public void executar() {
            auditoriaService.registrar(AcaoAuditoria.ERRO, "CARRO", 999L,
                    ResultadoAuditoria.ERRO, "Operação que sofreu rollback");
            throw new RuntimeException("rollback de teste");
        }
    }
}
