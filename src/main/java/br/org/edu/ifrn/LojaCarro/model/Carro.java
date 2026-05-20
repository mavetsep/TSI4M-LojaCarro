package br.org.edu.ifrn.LojaCarro.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
public class Carro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Modelo é obrigatório")
    @Size(max = 10, message = "Modelo não pode ter mais de 10 caracteres")
    String modelo;

    @Max(value = 2026, message = "Ano não pode ser futuro")
    @Min(value = 1886, message = "Ano inválido")
    int ano;

    @Positive(message = "Preço deve ser positivo")
    Double preco;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public int getAno() { return ano; }
    public void setAno(int ano) { this.ano = ano; }
    public Double getPreco() { return preco; }
    public void setPreco(Double preco) { this.preco = preco; }
}