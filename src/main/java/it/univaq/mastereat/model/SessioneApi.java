package it.univaq.mastereat.model;

public class SessioneApi {

    private long id;
    private long idUtente;
    private String tokenHash;
    private String creatoIl;
    private String scadeIl;
    private String revocatoIl;

    public SessioneApi() {
    }

    public SessioneApi(long id,
                       long idUtente,
                       String tokenHash,
                       String creatoIl,
                       String scadeIl,
                       String revocatoIl) {
        this.id = id;
        this.idUtente = idUtente;
        this.tokenHash = tokenHash;
        this.creatoIl = creatoIl;
        this.scadeIl = scadeIl;
        this.revocatoIl = revocatoIl;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getIdUtente() {
        return idUtente;
    }

    public void setIdUtente(long idUtente) {
        this.idUtente = idUtente;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public String getCreatoIl() {
        return creatoIl;
    }

    public void setCreatoIl(String creatoIl) {
        this.creatoIl = creatoIl;
    }

    public String getScadeIl() {
        return scadeIl;
    }

    public void setScadeIl(String scadeIl) {
        this.scadeIl = scadeIl;
    }

    public String getRevocatoIl() {
        return revocatoIl;
    }

    public void setRevocatoIl(String revocatoIl) {
        this.revocatoIl = revocatoIl;
    }
}
