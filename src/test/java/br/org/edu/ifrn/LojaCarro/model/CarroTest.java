package br.org.edu.ifrn.LojaCarro.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CarroTest {

    @Test
    void deveCriarCarroComConstrutor() {
        Carro carro = new Carro("Toyota", "Corolla", 2023, 95000.0);

        assertNotNull(carro);
        assertEquals("Toyota", carro.getMarca());
        assertEquals("Corolla", carro.getModelo());
        assertEquals(2023, carro.getAno());
        assertEquals(95000.0, carro.getPreco());
    }

    @Test
    void deveAlterarAtributosComSetters() {
        Carro carro = new Carro();

        carro.setMarca("Honda");
        carro.setModelo("Civic");
        carro.setAno(2022);
        carro.setPreco(88000.0);

        assertEquals("Honda", carro.getMarca());
        assertEquals("Civic", carro.getModelo());
        assertEquals(2022, carro.getAno());
        assertEquals(88000.0, carro.getPreco());
    }

    @Test
    void deveAceitarIdNuloAntesDePersistir() {
        Carro carro = new Carro("Fiat", "Uno", 2020, 45000.0);

        assertNull(carro.getId());
    }
}