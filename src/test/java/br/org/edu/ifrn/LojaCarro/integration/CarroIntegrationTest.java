package br.org.edu.ifrn.LojaCarro.integration;

import br.org.edu.ifrn.LojaCarro.model.Carro;
import br.org.edu.ifrn.LojaCarro.repository.CarroRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
public class CarroIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CarroRepository carroRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void limpar() {
        carroRepository.deleteAll();
    }

    // ===================== HAPPY PATH =====================

    // 1. Salvar
    @Test
    void testSalvarCarro() throws Exception {
        Carro carro = new Carro();
        carro.setModelo("Civic");
        carro.setAno(2022);
        carro.setPreco(120000.00);

        mockMvc.perform(post("/carro/salvar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(carro)))
                .andDo(print())
                .andExpect(status().isOk());

        assertEquals(1, carroRepository.findAll().size());
    }

    // 2. Deletar
    @Test
    void testDeletarCarro() throws Exception {
        Carro carro = new Carro();
        carro.setModelo("Palio");
        carro.setAno(2010);
        carro.setPreco(30000.00);
        Carro salvo = carroRepository.save(carro);

        mockMvc.perform(delete("/carro/" + salvo.getId()))
                .andDo(print())
                .andExpect(status().isNoContent());

        assertEquals(0, carroRepository.findAll().size());
    }

    // 3. Atualizar
    @Test
    void testAtualizarCarro() throws Exception {
        Carro carro = new Carro();
        carro.setModelo("Gol");
        carro.setAno(2015);
        carro.setPreco(40000.00);
        Carro salvo = carroRepository.save(carro);

        salvo.setModelo("Gol G6");
        salvo.setPreco(45000.00);

        mockMvc.perform(put("/carro/" + salvo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(salvo)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelo").value("Gol G6"));
    }

    // 4. Procurar pelo ID
    @Test
    void testProcurarPeloId() throws Exception {
        Carro carro = new Carro();
        carro.setModelo("Corolla");
        carro.setAno(2021);
        carro.setPreco(115000.00);
        Carro salvo = carroRepository.save(carro);

        mockMvc.perform(get("/carro/" + salvo.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelo").value("Corolla"));
    }

    // 5. Listar todos
    @Test
    void testListarTodosOsCarros() throws Exception {
        Carro c1 = new Carro(); c1.setModelo("Fusca"); c1.setAno(1980); c1.setPreco(25000.00);
        Carro c2 = new Carro(); c2.setModelo("HB20");  c2.setAno(2023); c2.setPreco(85000.00);
        Carro c3 = new Carro(); c3.setModelo("Onix");  c3.setAno(2022); c3.setPreco(75000.00);
        carroRepository.save(c1);
        carroRepository.save(c2);
        carroRepository.save(c3);

        mockMvc.perform(get("/carro"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    // ===================== BAD PATH =====================

    // 1. Salvar com preço negativo → 400
    @Test
    void testSalvarComPrecoNegativoRetorna400() throws Exception {
        Carro carro = new Carro();
        carro.setModelo("Civic");
        carro.setAno(2022);
        carro.setPreco(-5000.00);

        mockMvc.perform(post("/carro/salvar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(carro)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    // 2. Deletar ID inexistente → 404
    @Test
    void testDeletarIdInexistenteRetorna404() throws Exception {
        mockMvc.perform(delete("/carro/9999"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // 3. Atualizar ID inexistente → 404
    @Test
    void testAtualizarIdInexistenteRetorna404() throws Exception {
        Carro carro = new Carro();
        carro.setId(9999L);
        carro.setModelo("Civic");
        carro.setAno(2022);
        carro.setPreco(50000.00);

        mockMvc.perform(put("/carro/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(carro)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // 4. Procurar ID inexistente → 404
    @Test
    void testProcurarIdInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/carro/9999"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // 5. Listar com banco vazio → lista vazia []
    @Test
    void testListarComBancoVazioRetornaListaVazia() throws Exception {
        mockMvc.perform(get("/carro"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}