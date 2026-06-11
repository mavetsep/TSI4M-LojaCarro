package br.org.edu.ifrn.LojaCarro.controllers;

import br.org.edu.ifrn.LojaCarro.model.Carro;
import br.org.edu.ifrn.LojaCarro.services.CarroService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CarroController.class)
class CarroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CarroService carroService;

    @Autowired
    private ObjectMapper objectMapper;

    private Carro carroValido() {
        Carro carro = new Carro();
        carro.setId(1L);
        carro.setMarca("Toyota");
        carro.setModelo("Corolla");
        carro.setAno(2023);
        carro.setPreco(95000.0);
        return carro;
    }

    @Test
    void deveSalvarCarro() throws Exception {
        Carro carro = carroValido();
        when(carroService.save(any(Carro.class))).thenReturn(carro);

        mockMvc.perform(post("/carro/salvar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(carro)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.marca").value("Toyota"))
                .andExpect(jsonPath("$.modelo").value("Corolla"));
    }

    @Test
    void deveListarTodos() throws Exception {
        Carro c1 = carroValido();
        Carro c2 = new Carro();
        c2.setId(2L);
        c2.setMarca("Honda");
        c2.setModelo("Civic");
        c2.setAno(2022);
        c2.setPreco(88000.0);

        when(carroService.findAll()).thenReturn(List.of(c1, c2));

        mockMvc.perform(get("/carro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void deveBuscarPorId() throws Exception {
        when(carroService.findById(1L)).thenReturn(Optional.of(carroValido()));

        mockMvc.perform(get("/carro/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelo").value("Corolla"));
    }

    @Test
    void deveRetornar404QuandoIdNaoExiste() throws Exception {
        when(carroService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/carro/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAtualizarCarro() throws Exception {
        Carro carro = carroValido();
        carro.setModelo("Corolla Altis");

        when(carroService.update(any(Carro.class))).thenReturn(carro);

        mockMvc.perform(put("/carro/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(carro)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelo").value("Corolla Altis"));
    }

    @Test
    void deveDeletarCarro() throws Exception {
        doNothing().when(carroService).deleteById(1L);

        mockMvc.perform(delete("/carro/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deveRetornar400SemMarca() throws Exception {
        Carro carro = carroValido();
        carro.setMarca(null);

        mockMvc.perform(post("/carro/salvar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(carro)))
                .andExpect(status().isBadRequest());
    }
}