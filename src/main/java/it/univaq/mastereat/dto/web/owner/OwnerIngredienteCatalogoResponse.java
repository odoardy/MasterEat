package it.univaq.mastereat.dto.web.owner;

public class OwnerIngredienteCatalogoResponse {

    private long id;
    private String nome;
    private String unitaMisura;
    private boolean allergene;

    public OwnerIngredienteCatalogoResponse() {
    }

    public OwnerIngredienteCatalogoResponse(long id,
                                            String nome,
                                            String unitaMisura,
                                            boolean allergene) {
        this.id = id;
        this.nome = nome;
        this.unitaMisura = unitaMisura;
        this.allergene = allergene;
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

    public String getUnitaMisura() {
        return unitaMisura;
    }

    public void setUnitaMisura(String unitaMisura) {
        this.unitaMisura = unitaMisura;
    }

    public boolean isAllergene() {
        return allergene;
    }

    public void setAllergene(boolean allergene) {
        this.allergene = allergene;
    }
}
