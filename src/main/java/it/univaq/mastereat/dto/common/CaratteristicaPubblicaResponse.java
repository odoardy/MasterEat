package it.univaq.mastereat.dto.common;

import java.math.BigDecimal;

public class CaratteristicaPubblicaResponse {

    private long id;
    private String nome;
    private String descrizione;
    private BigDecimal differenzaPrezzo;
    private boolean selezionataDefault;

    public CaratteristicaPubblicaResponse() {
    }

    public CaratteristicaPubblicaResponse(long id,
                                          String nome,
                                          String descrizione,
                                          BigDecimal differenzaPrezzo,
                                          boolean selezionataDefault) {
        this.id = id;
        this.nome = nome;
        this.descrizione = descrizione;
        this.differenzaPrezzo = differenzaPrezzo;
        this.selezionataDefault = selezionataDefault;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public BigDecimal getDifferenzaPrezzo() {
        return differenzaPrezzo;
    }

    public void setDifferenzaPrezzo(BigDecimal differenzaPrezzo) {
        this.differenzaPrezzo = differenzaPrezzo;
    }

    public boolean isSelezionataDefault() {
        return selezionataDefault;
    }

    public void setSelezionataDefault(boolean selezionataDefault) {
        this.selezionataDefault = selezionataDefault;
    }
}
