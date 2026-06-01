package it.univaq.mastereat.model;

public class ImmagineProdotto {

    private long id;
    private long idProdotto;
    private String nomeFileOriginale;
    private String nomeFileSalvato;
    private String percorsoFile;
    private String tipoContenuto;
    private long dimensioneByte;
    private String testoAlternativo;
    private int ordineVisualizzazione;
    private boolean principale;
    private String caricataIl;

    public ImmagineProdotto() {
    }

    public ImmagineProdotto(long id,
                            long idProdotto,
                            String nomeFileSalvato,
                            String percorsoFile,
                            String testoAlternativo,
                            int ordineVisualizzazione,
                            boolean principale) {
        this(id, idProdotto, null, nomeFileSalvato, percorsoFile, null, 0, testoAlternativo,
                ordineVisualizzazione, principale, null);
    }

    public ImmagineProdotto(long id,
                            long idProdotto,
                            String nomeFileOriginale,
                            String nomeFileSalvato,
                            String percorsoFile,
                            String tipoContenuto,
                            long dimensioneByte,
                            String testoAlternativo,
                            int ordineVisualizzazione,
                            boolean principale,
                            String caricataIl) {
        this.id = id;
        this.idProdotto = idProdotto;
        this.nomeFileOriginale = nomeFileOriginale;
        this.nomeFileSalvato = nomeFileSalvato;
        this.percorsoFile = percorsoFile;
        this.tipoContenuto = tipoContenuto;
        this.dimensioneByte = dimensioneByte;
        this.testoAlternativo = testoAlternativo;
        this.ordineVisualizzazione = ordineVisualizzazione;
        this.principale = principale;
        this.caricataIl = caricataIl;
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

    public String getNomeFileOriginale() {
        return nomeFileOriginale;
    }

    public void setNomeFileOriginale(String nomeFileOriginale) {
        this.nomeFileOriginale = nomeFileOriginale;
    }

    public String getNomeFileSalvato() {
        return nomeFileSalvato;
    }

    public void setNomeFileSalvato(String nomeFileSalvato) {
        this.nomeFileSalvato = nomeFileSalvato;
    }

    public String getPercorsoFile() {
        return percorsoFile;
    }

    public void setPercorsoFile(String percorsoFile) {
        this.percorsoFile = percorsoFile;
    }

    public String getTipoContenuto() {
        return tipoContenuto;
    }

    public void setTipoContenuto(String tipoContenuto) {
        this.tipoContenuto = tipoContenuto;
    }

    public long getDimensioneByte() {
        return dimensioneByte;
    }

    public void setDimensioneByte(long dimensioneByte) {
        this.dimensioneByte = dimensioneByte;
    }

    public String getTestoAlternativo() {
        return testoAlternativo;
    }

    public void setTestoAlternativo(String testoAlternativo) {
        this.testoAlternativo = testoAlternativo;
    }

    public int getOrdineVisualizzazione() {
        return ordineVisualizzazione;
    }

    public void setOrdineVisualizzazione(int ordineVisualizzazione) {
        this.ordineVisualizzazione = ordineVisualizzazione;
    }

    public boolean isPrincipale() {
        return principale;
    }

    public void setPrincipale(boolean principale) {
        this.principale = principale;
    }

    public String getCaricataIl() {
        return caricataIl;
    }

    public void setCaricataIl(String caricataIl) {
        this.caricataIl = caricataIl;
    }
}
