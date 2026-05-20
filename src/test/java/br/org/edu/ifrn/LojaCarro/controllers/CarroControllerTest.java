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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CarroController.class)
public class CarroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CarroService carroService;

    @Autowired
    private ObjectMapper objectMapper;

    // ===================== HAPPY PATH =====================

    @Test
    void testSalvar() throws Exception {
        Carro carro = new Carro();
        carro.setId(1L);
        carro.setModelo("Civic");
        carro.setAno(2022);
        carro.setPreco(120000.00);

        when(carroService.save(any(Carro.class))).thenReturn(carro);

        mockMvc.perform(post("/carro/salvar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(carro)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelo").value("Civic"));
    }

    @Test
    void testDeletar() throws Exception {
        doNothing().when(carroService).deleteById(1L);

        mockMvc.perform(delete("/carro/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testAtualizar() throws Exception {
        Carro carro = new Carro();
        carro.setId(1L);
        carro.setModelo("Gol G6");
        carro.setAno(2020);
        carro.setPreco(55000.00);

        when(carroService.update(any(Carro.class))).thenReturn(carro);

        mockMvc.perform(put("/carro/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(carro)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelo").value("Gol G6"));
    }

    @Test
    void testBuscarPorId() throws Exception {
        Carro carro = new Carro();
        carro.setId(1L);
        carro.setModelo("Corolla");
        carro.setAno(2021);
        carro.setPreco(115000.00);

        when(carroService.findById(1L)).thenReturn(Optional.of(carro));

        mockMvc.perform(get("/carro/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelo").value("Corolla"));
    }

    @Test
    void testListarTodos() throws Exception {
        Carro c1 = new Carro(); c1.setModelo("Fusca"); c1.setAno(1980); c1.setPreco(25000.00);
        Carro c2 = new Carro(); c2.setModelo("HB20");  c2.setAno(2023); c2.setPreco(85000.00);

        when(carroService.findAll()).thenReturn(List.of(c1, c2));

        mockMvc.perform(get("/carro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // ===================== BAD PATH =====================

    @Test
    void testBuscarIdInexistenteRetorna404() throws Exception {
        when(carroService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/carro/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSalvarCarroSemModeloRetorna400() throws Exception {
        Carro carro = new Carro();
        carro.setModelo(null);
        carro.setAno(2022);
        carro.setPreco(50000.00);

        mockMvc.perform(post("/carro/salvar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(carro)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSalvarCarroComModeloMaiorQue10CaracteresRetorna400() throws Exception {
        Carro carro = new Carro();
        carro.setModelo("NomeExtremamenteLongo");
        carro.setAno(2022);
        carro.setPreco(50000.00);

        mockMvc.perform(post("/carro/salvar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(carro)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSalvarCarroComAnoFuturoRetorna400() throws Exception {
        Carro carro = new Carro();
        carro.setModelo("Civic");
        carro.setAno(2027);
        carro.setPreco(50000.00);

        mockMvc.perform(post("/carro/salvar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(carro)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testSalvarCarroComPrecoNegativoRetorna400() throws Exception {
        Carro carro = new Carro();
        carro.setModelo("Civic");
        carro.setAno(2022);
        carro.setPreco(-5000.00);

        mockMvc.perform(post("/carro/salvar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(carro)))
                .andExpect(status().isBadRequest());
    }
}