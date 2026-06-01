package it.univaq.mastereat.dto.web.owner;

public class OwnerCategoriaProdottoResponse {

    private long id;
    private String nome;
    private String descrizione;

    public OwnerCategoriaProdottoResponse() {
    }

    public OwnerCategoriaProdottoResponse(long id, String nome, String descrizione) {
        this.id = id;
        this.nome = nome;
        this.descrizione = descrizione;
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
}
