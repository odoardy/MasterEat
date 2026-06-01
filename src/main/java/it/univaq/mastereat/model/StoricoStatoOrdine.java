package it.univaq.mastereat.model;

public class StoricoStatoOrdine {

    private long id;
    private long idOrdine;
    private StatoOrdine statoPrecedente;
    private StatoOrdine statoNuovo;
    private Long idUtenteModifica;
    private String usernameUtenteModifica;
    private String nomeUtenteModifica;
    private String cognomeUtenteModifica;
    private String ruoloUtenteModifica;
    private String modificatoIl;
    private String nota;

    public StoricoStatoOrdine() {
    }

    public StoricoStatoOrdine(long id,
                              long idOrdine,
                              StatoOrdine statoPrecedente,
                              StatoOrdine statoNuovo,
                              Long idUtenteModifica,
                              String usernameUtenteModifica,
                              String nomeUtenteModifica,
                              String cognomeUtenteModifica,
                              String ruoloUtenteModifica,
                              String modificatoIl,
                              String nota) {
        this.id = id;
        this.idOrdine = idOrdine;
        this.statoPrecedente = statoPrecedente;
        this.statoNuovo = statoNuovo;
        this.idUtenteModifica = idUtenteModifica;
        this.usernameUtenteModifica = usernameUtenteModifica;
        this.nomeUtenteModifica = nomeUtenteModifica;
        this.cognomeUtenteModifica = cognomeUtenteModifica;
        this.ruoloUtenteModifica = ruoloUtenteModifica;
        this.modificatoIl = modificatoIl;
        this.nota = nota;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getIdOrdine() {
        return idOrdine;
    }

    public void setIdOrdine(long idOrdine) {
        this.idOrdine = idOrdine;
    }

    public StatoOrdine getStatoPrecedente() {
        return statoPrecedente;
    }

    public void setStatoPrecedente(StatoOrdine statoPrecedente) {
        this.statoPrecedente = statoPrecedente;
    }

    public StatoOrdine getStatoNuovo() {
        return statoNuovo;
    }

    public void setStatoNuovo(StatoOrdine statoNuovo) {
        this.statoNuovo = statoNuovo;
    }

    public Long getIdUtenteModifica() {
        return idUtenteModifica;
    }

    public void setIdUtenteModifica(Long idUtenteModifica) {
        this.idUtenteModifica = idUtenteModifica;
    }

    public String getUsernameUtenteModifica() {
        return usernameUtenteModifica;
    }

    public void setUsernameUtenteModifica(String usernameUtenteModifica) {
        this.usernameUtenteModifica = usernameUtenteModifica;
    }

    public String getNomeUtenteModifica() {
        return nomeUtenteModifica;
    }

    public void setNomeUtenteModifica(String nomeUtenteModifica) {
        this.nomeUtenteModifica = nomeUtenteModifica;
    }

    public String getCognomeUtenteModifica() {
        return cognomeUtenteModifica;
    }

    public void setCognomeUtenteModifica(String cognomeUtenteModifica) {
        this.cognomeUtenteModifica = cognomeUtenteModifica;
    }

    public String getRuoloUtenteModifica() {
        return ruoloUtenteModifica;
    }

    public void setRuoloUtenteModifica(String ruoloUtenteModifica) {
        this.ruoloUtenteModifica = ruoloUtenteModifica;
    }

    public String getModificatoIl() {
        return modificatoIl;
    }

    public void setModificatoIl(String modificatoIl) {
        this.modificatoIl = modificatoIl;
    }

    public String getNota() {
        return nota;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }
}
