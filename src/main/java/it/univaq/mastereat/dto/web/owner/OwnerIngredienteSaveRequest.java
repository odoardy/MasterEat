package it.univaq.mastereat.dto.web.owner;

public class OwnerIngredienteSaveRequest {

    private String idIngrediente;
    private String nome;
    private String quantita;
    private String unitaMisura;
    private String allergene;
    private String attivo;

    public OwnerIngredienteSaveRequest() {
    }

    public OwnerIngredienteSaveRequest(String idIngrediente,
                                       String nome,
                                       String quantita,
                                       String unitaMisura,
                                       String allergene,
                                       String attivo) {
        this.idIngrediente = idIngrediente;
        this.nome = nome;
        this.quantita = quantita;
        this.unitaMisura = unitaMisura;
        this.allergene = allergene;
        this.attivo = attivo;
    }

    public String getIdIngrediente() {
        return idIngrediente;
    }

    public void setIdIngrediente(String idIngrediente) {
        this.idIngrediente = idIngrediente;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getQuantita() {
        return quantita;
    }

    public void setQuantita(String quantita) {
        this.quantita = quantita;
    }

    public String getUnitaMisura() {
        return unitaMisura;
    }

    public void setUnitaMisura(String unitaMisura) {
        this.unitaMisura = unitaMisura;
    }

    public String getAllergene() {
        return allergene;
    }

    public void setAllergene(String allergene) {
        this.allergene = allergene;
    }

    public String getAttivo() {
        return attivo;
    }

    public void setAttivo(String attivo) {
        this.attivo = attivo;
    }
}
