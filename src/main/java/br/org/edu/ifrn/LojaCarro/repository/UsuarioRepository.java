package br.org.edu.ifrn.LojaCarro.repository;

import br.org.edu.ifrn.LojaCarro.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
}