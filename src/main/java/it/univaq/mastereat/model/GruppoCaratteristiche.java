package it.univaq.mastereat.model;

public class GruppoCaratteristiche {

    private long id;
    private long idProdotto;
    private String nome;
    private String descrizione;
    private boolean obbligatorio;
    private boolean attivo;
    private String creatoIl;
    private String aggiornatoIl;

    public GruppoCaratteristiche() {
    }

    public GruppoCaratteristiche(long id,
                                 long idProdotto,
                                 String nome,
                                 String descrizione,
                                 boolean obbligatorio,
                                 boolean attivo,
                                 String creatoIl,
                                 String aggiornatoIl) {
        this.id = id;
        this.idProdotto = idProdotto;
        this.nome = nome;
        this.descrizione = descrizione;
        this.obbligatorio = obbligatorio;
        this.attivo = attivo;
        this.creatoIl = creatoIl;
        this.aggiornatoIl = aggiornatoIl;
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

    public String getCreatoIl() {
        return creatoIl;
    }

    public void setCreatoIl(String creatoIl) {
        this.creatoIl = creatoIl;
    }

    public String getAggiornatoIl() {
        return aggiornatoIl;
    }

    public void setAggiornatoIl(String aggiornatoIl) {
        this.aggiornatoIl = aggiornatoIl;
    }
}
