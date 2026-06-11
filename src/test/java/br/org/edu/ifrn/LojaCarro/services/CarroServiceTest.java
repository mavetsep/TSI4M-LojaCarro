package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.model.Carro;
import br.org.edu.ifrn.LojaCarro.repository.CarroRepository;
import br.org.edu.ifrn.LojaCarro.services.CarroService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class CarroServiceTest {

    @Autowired
    private CarroService service;

    @Autowired
    private CarroRepository repository;

    @BeforeEach
    void limparBanco() {
        repository.deleteAll();
    }

    @Test
    void deveSalvarCarro() {
        Carro salvo = service.save(new Carro("Toyota", "Corolla", 2023, 95000.0));
        assertNotNull(salvo.getId());
        assertEquals("Toyota", salvo.getMarca());
    }

    @Test
    void deveBuscarPorId() {
        Carro salvo = repository.save(new Carro("BMW", "320i", 2023, 200000.0));
        Optional<Carro> resultado = service.findById(salvo.getId());

        assertTrue(resultado.isPresent());
        assertEquals("320i", resultado.get().getModelo());
    }

    @Test
    void deveRetornarVazioQuandoIdNaoExiste() {
        Optional<Carro> resultado = service.findById(9999L);
        assertFalse(resultado.isPresent());
    }

    @Test
    void deveListarTodos() {
        repository.save(new Carro("Fiat", "Uno", 2020, 45000.0));
        repository.save(new Carro("Honda", "Civic", 2022, 88000.0));

        List<Carro> carros = service.findAll();
        assertEquals(2, carros.size());
    }

    @Test
    void deveRetornarListaVazia() {
        List<Carro> carros = service.findAll();
        assertTrue(carros.isEmpty());
    }

    @Test
    void deveAtualizarCarro() {
        Carro salvo = repository.save(new Carro("Hyundai", "HB20", 2021, 60000.0));
        salvo.setModelo("HB20S");
        salvo.setPreco(65000.0);

        Carro atualizado = service.update(salvo);

        assertEquals("HB20S", atualizado.getModelo());
        assertEquals(65000.0, atualizado.getPreco());
    }

    @Test
    void deveDeletarCarro() {
        Carro salvo = repository.save(new Carro("Chevrolet", "Onix", 2021, 70000.0));
        service.deleteById(salvo.getId());

        assertFalse(repository.existsById(salvo.getId()));
    }

    @Test
    void deveRetornarTrueParaLoginValido() {
        assertTrue(service.login("admin", "1234"));
    }

    @Test
    void deveRetornarFalseParaLoginInvalido() {
        assertFalse(service.login("admin", "errada"));
    }
}