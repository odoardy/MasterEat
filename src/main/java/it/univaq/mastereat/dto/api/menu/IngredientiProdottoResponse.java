package it.univaq.mastereat.dto.api.menu;

import it.univaq.mastereat.dto.common.IngredienteProdottoResponse;

import java.util.ArrayList;
import java.util.List;

public class IngredientiProdottoResponse {

    private long idProdotto;
    private String nomeProdotto;
    private List<IngredienteProdottoResponse> ingredienti = new ArrayList<>();

    public IngredientiProdottoResponse() {
    }

    public IngredientiProdottoResponse(long idProdotto,
                                       String nomeProdotto,
                                       List<IngredienteProdottoResponse> ingredienti) {
        this.idProdotto = idProdotto;
        this.nomeProdotto = nomeProdotto;
        setIngredienti(ingredienti);
    }

    public long getIdProdotto() {
        return idProdotto;
    }

    public void setIdProdotto(long idProdotto) {
        this.idProdotto = idProdotto;
    }

    public String getNomeProdotto() {
        return nomeProdotto;
    }

    public void setNomeProdotto(String nomeProdotto) {
        this.nomeProdotto = nomeProdotto;
    }

    public List<IngredienteProdottoResponse> getIngredienti() {
        return ingredienti;
    }

    public void setIngredienti(List<IngredienteProdottoResponse> ingredienti) {
        this.ingredienti = ingredienti != null ? new ArrayList<>(ingredienti) : new ArrayList<>();
    }
}
