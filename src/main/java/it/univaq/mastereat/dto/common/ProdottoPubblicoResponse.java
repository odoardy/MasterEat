package it.univaq.mastereat.dto.common;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProdottoPubblicoResponse {

    private long id;
    private String nome;
    private String descrizione;
    private BigDecimal prezzoBase;
    private List<ImmagineProdottoPubblicaResponse> immagini = new ArrayList<>();
    private List<CaratteristicaPubblicaResponse> caratteristiche = new ArrayList<>();

    public ProdottoPubblicoResponse() {
    }

    public ProdottoPubblicoResponse(long id,
                                    String nome,
                                    String descrizione,
                                    BigDecimal prezzoBase,
                                    List<ImmagineProdottoPubblicaResponse> immagini,
                                    List<CaratteristicaPubblicaResponse> caratteristiche) {
        this.id = id;
        this.nome = nome;
        this.descrizione = descrizione;
        this.prezzoBase = prezzoBase;
        setImmagini(immagini);
        setCaratteristiche(caratteristiche);
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

    public BigDecimal getPrezzoBase() {
        return prezzoBase;
    }

    public void setPrezzoBase(BigDecimal prezzoBase) {
        this.prezzoBase = prezzoBase;
    }

    public List<ImmagineProdottoPubblicaResponse> getImmagini() {
        return immagini;
    }

    public void setImmagini(List<ImmagineProdottoPubblicaResponse> immagini) {
        this.immagini = immagini != null ? new ArrayList<>(immagini) : new ArrayList<>();
    }

    public List<CaratteristicaPubblicaResponse> getCaratteristiche() {
        return caratteristiche;
    }

    public void setCaratteristiche(List<CaratteristicaPubblicaResponse> caratteristiche) {
        this.caratteristiche = caratteristiche != null ? new ArrayList<>(caratteristiche) : new ArrayList<>();
    }
}
