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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CarroIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CarroRepository carroRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void limparBanco() {
        carroRepository.deleteAll();
    }

    private Carro carroValido() {
        return new Carro("Toyota", "Corolla", 2023, 95000.0);
    }

    @Test
    void deveSalvarCarro() throws Exception {
        Carro carro = carroValido();

        mockMvc.perform(post("/carro/salvar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(carro)))
                .andExpect(status().isOk());

        assertEquals(1, carroRepository.count());
    }

    @Test
    void deveListarCarros() throws Exception {
        carroRepository.save(new Carro("Fiat", "Uno", 2020, 45000.0));
        carroRepository.save(new Carro("Honda", "Civic", 2022, 88000.0));

        mockMvc.perform(get("/carro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void deveBuscarCarroPorId() throws Exception {
        Carro salvo = carroRepository.save(carroValido());

        mockMvc.perform(get("/carro/" + salvo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelo").value("Corolla"));
    }

    @Test
    void deveAtualizarCarro() throws Exception {
        Carro salvo = carroRepository.save(carroValido());
        salvo.setModelo("Corolla Altis");
        salvo.setPreco(99000.0);

        mockMvc.perform(put("/carro/" + salvo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(salvo)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelo").value("Corolla Altis"));
    }

    @Test
    void deveDeletarCarro() throws Exception {
        Carro salvo = carroRepository.save(carroValido());

        mockMvc.perform(delete("/carro/" + salvo.getId()))
                .andExpect(status().isNoContent());

        assertEquals(0, carroRepository.count());
    }

    @Test
    void deveRetornar400ComPrecoNegativo() throws Exception {
        Carro carro = carroValido();
        carro.setPreco(-5000.0);

        mockMvc.perform(post("/carro/salvar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(carro)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar404ComIdInexistente() throws Exception {
        mockMvc.perform(get("/carro/9999"))
                .andExpect(status().isNotFound());
    }
}