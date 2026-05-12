package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.model.Carro;
import br.org.edu.ifrn.LojaCarro.repository.CarroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CarroServiceTest {

    @Mock
    private CarroRepository carroRepository;

    @InjectMocks
    private CarroService carroService;

    private Carro carroPadrao;

    @BeforeEach
    void setUp() {
        carroPadrao = new Carro();
        carroPadrao.setId(1L);
        carroPadrao.setModelo("Renault Clio");
        carroPadrao.setAno(2024);
    }

    @Test
    void testSalvarCarroSucesso() {
        when(carroRepository.save(any(Carro.class))).thenReturn(carroPadrao);

        Carro carroSalvo = carroService.save(new Carro());

        assertNotNull(carroSalvo);
        assertEquals("Renault Clio", carroSalvo.getModelo());
        verify(carroRepository, times(1)).save(any(Carro.class));
    }

    @Test
    void testPesquisarCarroPorIdComSucesso() {
        when(carroRepository.findById(1L)).thenReturn(Optional.of(carroPadrao));

        Optional<Carro> carroEncontrado = carroService.findById(1L);

        assertTrue(carroEncontrado.isPresent());
        assertEquals(2024, carroEncontrado.get().getAno());
    }

    @Test
    void testPesquisarTodosCarros() {
        Carro carro2 = new Carro();
        carro2.setId(2L);
        carro2.setModelo("Honda Civic");
        carro2.setAno(2023);

        when(carroRepository.findAll()).thenReturn(Arrays.asList(carroPadrao, carro2));

        List<Carro> carros = carroService.findAll();

        assertEquals(2, carros.size());
        assertEquals("Renault Clio", carros.get(0).getModelo());
        assertEquals("Honda Civic", carros.get(1).getModelo());
    }

    @Test
    void testAtualizarCarro() {
        when(carroRepository.save(any(Carro.class))).thenReturn(carroPadrao);

        Carro carroAtualizado = carroService.update(carroPadrao);

        assertNotNull(carroAtualizado);
        assertEquals(1L, carroAtualizado.getId());
        verify(carroRepository, times(1)).save(carroPadrao);
    }

    @Test
    void testDeletarCarroPorId() {
        doNothing().when(carroRepository).deleteById(1L);

        assertDoesNotThrow(() -> carroService.deleteById(1L));
        verify(carroRepository, times(1)).deleteById(1L);
    }
}