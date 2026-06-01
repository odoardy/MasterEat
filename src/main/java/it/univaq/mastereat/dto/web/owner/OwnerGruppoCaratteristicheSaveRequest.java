package it.univaq.mastereat.dto.web.owner;

public class OwnerGruppoCaratteristicheSaveRequest {

    private String nome;
    private String descrizione;
    private String obbligatorio;
    private String attivo;

    public OwnerGruppoCaratteristicheSaveRequest() {
    }

    public OwnerGruppoCaratteristicheSaveRequest(String nome,
                                                 String descrizione,
                                                 String obbligatorio,
                                                 String attivo) {
        this.nome = nome;
        this.descrizione = descrizione;
        this.obbligatorio = obbligatorio;
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

    public String getObbligatorio() {
        return obbligatorio;
    }

    public void setObbligatorio(String obbligatorio) {
        this.obbligatorio = obbligatorio;
    }

    public String getAttivo() {
        return attivo;
    }

    public void setAttivo(String attivo) {
        this.attivo = attivo;
    }
}
