package br.org.edu.ifrn.LojaCarro.services;

import br.org.edu.ifrn.LojaCarro.dto.NomeUsuarioApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Component
public class UsuarioApiClient {

    private final RestClient restClient;

    public UsuarioApiClient(@Value("${usuario.api.url}") String apiUrl) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(2));
        requestFactory.setReadTimeout(Duration.ofSeconds(2));
        this.restClient = RestClient.builder()
                .baseUrl(apiUrl)
                .requestFactory(requestFactory)
                .build();
    }

    public NomeUsuarioApiResponse buscarNome(Long usuarioId) {
        return restClient.get()
                .uri("/api/usuarios/{id}/nome", usuarioId)
                .retrieve()
                .body(NomeUsuarioApiResponse.class);
    }
}
