package it.univaq.mastereat.dto.web.owner;

public class OwnerGruppoCaratteristicheResponse {

    private long id;
    private long idProdotto;
    private String nome;
    private String descrizione;
    private boolean obbligatorio;
    private boolean attivo;

    public OwnerGruppoCaratteristicheResponse() {
    }

    public OwnerGruppoCaratteristicheResponse(long id,
                                              long idProdotto,
                                              String nome,
                                              String descrizione,
                                              boolean obbligatorio,
                                              boolean attivo) {
        this.id = id;
        this.idProdotto = idProdotto;
        this.nome = nome;
        this.descrizione = descrizione;
        this.obbligatorio = obbligatorio;
        this.attivo = attivo;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getIdProdotto() {
        return idProdotto;
    }

    public void setIdProdotto(long idProdotto) {
        this.idProdotto = idProdotto;
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

    public boolean isObbligatorio() {
        return obbligatorio;
    }

    public void setObbligatorio(boolean obbligatorio) {
        this.obbligatorio = obbligatorio;
    }

    public boolean isAttivo() {
        return attivo;
    }

    public void setAttivo(boolean attivo) {
        this.attivo = attivo;
    }
}
