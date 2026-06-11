package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.entity.Carro;
import br.org.edu.ifrn.LojaCarro.repository.CarroRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CarroService {

    private final CarroRepository carroRepository;

    public CarroService(CarroRepository carroRepository) {
        this.carroRepository = carroRepository;
    }

    @PreAuthorize("hasAnyRole('VENDEDOR','GERENTE')")
    public Carro save(Carro carro) {
        return carroRepository.save(carro);
    }

    @PreAuthorize("hasAnyRole('VENDEDOR','GERENTE')")
    public Carro update(Carro carro) {
        return carroRepository.save(carro);
    }

    @PreAuthorize("hasAnyRole('VENDEDOR','GERENTE')")
    public void deleteById(Long id) {
        carroRepository.deleteById(id);
    }

    @PreAuthorize("hasAnyRole('VENDEDOR','GERENTE','CLIENTE')")
    public Optional<Carro> findById(Long id) {
        return carroRepository.findById(id);
    }

    @PreAuthorize("hasAnyRole('VENDEDOR','GERENTE','CLIENTE')")
    public List<Carro> findAll() {
        return carroRepository.findAll();
    }
}