package br.org.edu.ifrn.LojaCarro.repository;

import br.org.edu.ifrn.LojaCarro.model.Carro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CarroRepositoryTest {

    @Autowired
    private CarroRepository carroRepository;

    @BeforeEach
    void limpar() {
        carroRepository.deleteAll();
    }

    @Test
    void deveSalvarCarro() {
        Carro carro = new Carro("Toyota", "Corolla", 2023, 95000.0);

        Carro salvo = carroRepository.save(carro);

        assertNotNull(salvo.getId());
        assertEquals("Toyota", salvo.getMarca());
        assertEquals("Corolla", salvo.getModelo());
    }

    @Test
    void deveBuscarPorId() {
        Carro salvo = carroRepository.save(new Carro("Honda", "Civic", 2022, 88000.0));

        Optional<Carro> encontrado = carroRepository.findById(salvo.getId());

        assertTrue(encontrado.isPresent());
        assertEquals("Civic", encontrado.get().getModelo());
    }

    @Test
    void deveListarTodos() {
        carroRepository.save(new Carro("Fiat", "Uno", 2020, 45000.0));
        carroRepository.save(new Carro("Chevrolet", "Onix", 2022, 70000.0));

        List<Carro> carros = carroRepository.findAll();

        assertEquals(2, carros.size());
    }

    @Test
    void deveDeletarCarro() {
        Carro salvo = carroRepository.save(new Carro("Volkswagen", "Gol", 2019, 55000.0));

        carroRepository.deleteById(salvo.getId());

        assertFalse(carroRepository.findById(salvo.getId()).isPresent());
    }
}