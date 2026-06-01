package it.univaq.mastereat.dto.common;

import java.util.ArrayList;
import java.util.List;

public class CategoriaMenuResponse {

    private long id;
    private String nome;
    private String descrizione;
    private List<ProdottoPubblicoResponse> prodotti = new ArrayList<>();

    public CategoriaMenuResponse() {
    }

    public CategoriaMenuResponse(long id,
                                 String nome,
                                 String descrizione,
                                 List<ProdottoPubblicoResponse> prodotti) {
        this.id = id;
        this.nome = nome;
        this.descrizione = descrizione;
        setProdotti(prodotti);
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

    public List<ProdottoPubblicoResponse> getProdotti() {
        return prodotti;
    }

    public void setProdotti(List<ProdottoPubblicoResponse> prodotti) {
        this.prodotti = prodotti != null ? new ArrayList<>(prodotti) : new ArrayList<>();
    }
}
