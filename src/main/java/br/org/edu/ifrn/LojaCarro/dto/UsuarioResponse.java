package br.org.edu.ifrn.LojaCarro.dto;

import br.org.edu.ifrn.LojaCarro.model.Perfil;

public record UsuarioResponse(Long id, String nome, String usuario, Perfil perfil) {
}
