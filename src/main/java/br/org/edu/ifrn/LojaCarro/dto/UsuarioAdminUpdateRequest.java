package br.org.edu.ifrn.LojaCarro.dto;

import br.org.edu.ifrn.LojaCarro.model.Perfil;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UsuarioAdminUpdateRequest(
        @NotBlank(message = "Nome é obrigatório") String nome,
        @NotBlank(message = "Nome de usuário é obrigatório") String usuario,
        String senha,
        @NotNull(message = "Perfil é obrigatório") Perfil perfil) {
}
