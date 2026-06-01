package it.univaq.mastereat.dto.web.owner;

public class OwnerCaratteristicaSaveRequest {

    private String nome;
    private String descrizione;
    private String differenzaPrezzo;
    private String idGruppoCaratteristiche;
    private String selezionataDefault;
    private String attiva;

    public OwnerCaratteristicaSaveRequest() {
    }

    public OwnerCaratteristicaSaveRequest(String nome,
                                          String descrizione,
                                          String differenzaPrezzo,
                                          String idGruppoCaratteristiche,
                                          String selezionataDefault,
                                          String attiva) {
        this.nome = nome;
        this.descrizione = descrizione;
        this.differenzaPrezzo = differenzaPrezzo;
        this.idGruppoCaratteristiche = idGruppoCaratteristiche;
        this.selezionataDefault = selezionataDefault;
        this.attiva = attiva;
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

    public String getDifferenzaPrezzo() {
        return differenzaPrezzo;
    }

    public void setDifferenzaPrezzo(String differenzaPrezzo) {
        this.differenzaPrezzo = differenzaPrezzo;
    }

    public String getIdGruppoCaratteristiche() {
        return idGruppoCaratteristiche;
    }

    public void setIdGruppoCaratteristiche(String idGruppoCaratteristiche) {
        this.idGruppoCaratteristiche = idGruppoCaratteristiche;
    }

    public String getSelezionataDefault() {
        return selezionataDefault;
    }

    public void setSelezionataDefault(String selezionataDefault) {
        this.selezionataDefault = selezionataDefault;
    }

    public String getAttiva() {
        return attiva;
    }

    public void setAttiva(String attiva) {
        this.attiva = attiva;
    }
}
