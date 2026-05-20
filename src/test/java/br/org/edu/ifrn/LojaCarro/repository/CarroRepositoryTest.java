package br.org.edu.ifrn.LojaCarro.repository;

import br.org.edu.ifrn.LojaCarro.model.Carro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CarroRepositoryTest {

    @Autowired
    private CarroRepository carroRepository;

    @BeforeEach
    void limpar() {
        carroRepository.deleteAll();
    }

    @Test
    void testSalvar() {
        Carro carro = new Carro();
        carro.setModelo("Civic");
        carro.setAno(2022);
        carro.setPreco(120000.00);

        Carro salvo = carroRepository.save(carro);

        assertNotNull(salvo.getId());
        assertEquals("Civic", salvo.getModelo());
    }

    @Test
    void testDeletar() {
        Carro carro = new Carro();
        carro.setModelo("Palio");
        carro.setAno(2005);
        carro.setPreco(30000.00);

        Carro salvo = carroRepository.save(carro);
        carroRepository.deleteById(salvo.getId());

        assertFalse(carroRepository.findById(salvo.getId()).isPresent());
    }

    @Test
    void testAtualizar() {
        Carro carro = new Carro();
        carro.setModelo("Gol");
        carro.setAno(2010);
        carro.setPreco(50000.00);

        Carro salvo = carroRepository.save(carro);
        salvo.setModelo("Gol G6");
        Carro atualizado = carroRepository.save(salvo);

        assertEquals("Gol G6", atualizado.getModelo());
    }

    @Test
    void testBuscarPorId() {
        Carro carro = new Carro();
        carro.setModelo("Corolla");
        carro.setAno(2021);
        carro.setPreco(115000.00);

        Carro salvo = carroRepository.save(carro);
        Optional<Carro> encontrado = carroRepository.findById(salvo.getId());

        assertTrue(encontrado.isPresent());
        assertEquals("Corolla", encontrado.get().getModelo());
    }

    @Test
    void testListarTodos() {
        Carro c1 = new Carro(); c1.setModelo("Fusca");  c1.setAno(1980); c1.setPreco(25000.00);
        Carro c2 = new Carro(); c2.setModelo("HB20");   c2.setAno(2023); c2.setPreco(85000.00);

        carroRepository.save(c1);
        carroRepository.save(c2);

        List<Carro> carros = carroRepository.findAll();
        assertEquals(2, carros.size());
    }
}