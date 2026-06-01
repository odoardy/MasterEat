package it.univaq.mastereat.dto.common;

import java.math.BigDecimal;

public class CaratteristicaOrdineResponse {

    private long idCaratteristica;
    private String nome;
    private BigDecimal differenzaPrezzo;

    public CaratteristicaOrdineResponse() {
    }

    public CaratteristicaOrdineResponse(long idCaratteristica, String nome, BigDecimal differenzaPrezzo) {
        this.idCaratteristica = idCaratteristica;
        this.nome = nome;
        this.differenzaPrezzo = differenzaPrezzo;
    }

    public long getIdCaratteristica() {
        return idCaratteristica;
    }

    public void setIdCaratteristica(long idCaratteristica) {
        this.idCaratteristica = idCaratteristica;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public BigDecimal getDifferenzaPrezzo() {
        return differenzaPrezzo;
    }

    public void setDifferenzaPrezzo(BigDecimal differenzaPrezzo) {
        this.differenzaPrezzo = differenzaPrezzo;
    }
}
