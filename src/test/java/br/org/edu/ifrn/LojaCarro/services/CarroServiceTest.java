package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.model.Carro;
import br.org.edu.ifrn.LojaCarro.repository.CarroRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CarroServiceTest {

    @Test
    void deveSalvarModeloComMaisDeCincoCaracteres() {
        CarroRepository repository = mock(CarroRepository.class);
        CarroService service = new CarroService(repository);
        Carro carro = new Carro("Corolla Cross", 2026, 180000.0);
        when(repository.save(carro)).thenReturn(carro);

        Carro salvo = service.save(carro);

        assertSame(carro, salvo);
        verify(repository).save(carro);
    }
}
