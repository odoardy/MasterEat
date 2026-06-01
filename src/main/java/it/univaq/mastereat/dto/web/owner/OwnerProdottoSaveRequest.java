package it.univaq.mastereat.dto.web.owner;

public class OwnerProdottoSaveRequest {

    private String nome;
    private String descrizione;
    private String prezzoBase;
    private String minutiPreparazione;
    private String idCategoria;
    private String descrizionePreparazione;
    private String attivo;

    public OwnerProdottoSaveRequest() {
    }

    public OwnerProdottoSaveRequest(String nome,
                                    String descrizione,
                                    String prezzoBase,
                                    String minutiPreparazione,
                                    String idCategoria,
                                    String descrizionePreparazione,
                                    String attivo) {
        this.nome = nome;
        this.descrizione = descrizione;
        this.prezzoBase = prezzoBase;
        this.minutiPreparazione = minutiPreparazione;
        this.idCategoria = idCategoria;
        this.descrizionePreparazione = descrizionePreparazione;
        this.attivo = attivo;
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

    public String getPrezzoBase() {
        return prezzoBase;
    }

    public void setPrezzoBase(String prezzoBase) {
        this.prezzoBase = prezzoBase;
    }

    public String getMinutiPreparazione() {
        return minutiPreparazione;
    }

    public void setMinutiPreparazione(String minutiPreparazione) {
        this.minutiPreparazione = minutiPreparazione;
    }

    public String getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(String idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getDescrizionePreparazione() {
        return descrizionePreparazione;
    }

    public void setDescrizionePreparazione(String descrizionePreparazione) {
        this.descrizionePreparazione = descrizionePreparazione;
    }

    public String getAttivo() {
        return attivo;
    }

    public void setAttivo(String attivo) {
        this.attivo = attivo;
    }
}
