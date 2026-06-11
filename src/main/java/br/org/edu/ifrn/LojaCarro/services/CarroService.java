package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.model.Carro;
import br.org.edu.ifrn.LojaCarro.repository.CarroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CarroService {

    @Autowired
    private CarroRepository carroRepository;

    public Carro save(Carro c) {
        return carroRepository.save(c);
    }

    public void deleteById(Long id) {
        carroRepository.deleteById(id);
    }

    public Optional<Carro> findById(Long id) {
        return carroRepository.findById(id);
    }

    public List<Carro> findAll() {
        return carroRepository.findAll();
    }

    public Carro update(Carro c) {
        return carroRepository.save(c);
    }

    public boolean existsById(Long id) {
        return carroRepository.existsById(id);
    }

    public boolean login(String usuario, String senha) {
        return "admin".equals(usuario) && "1234".equals(senha);
    }
}