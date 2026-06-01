package it.univaq.mastereat.model;

public class CategoriaProdotto {

    private long id;
    private String nome;
    private String descrizione;
    private int ordineVisualizzazione;

    public CategoriaProdotto() {
    }

    public CategoriaProdotto(long id,
                             String nome,
                             String descrizione,
                             int ordineVisualizzazione) {
        this.id = id;
        this.nome = nome;
        this.descrizione = descrizione;
        this.ordineVisualizzazione = ordineVisualizzazione;
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

    public int getOrdineVisualizzazione() {
        return ordineVisualizzazione;
    }

    public void setOrdineVisualizzazione(int ordineVisualizzazione) {
        this.ordineVisualizzazione = ordineVisualizzazione;
    }
}
