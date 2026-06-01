package it.univaq.mastereat.dto.common;

import java.util.ArrayList;
import java.util.List;

public class MenuPubblicoResponse {

    private List<CategoriaMenuResponse> categorie = new ArrayList<>();
    private List<ProdottoPubblicoResponse> prodottiSenzaCategoria = new ArrayList<>();

    public MenuPubblicoResponse() {
    }

    public MenuPubblicoResponse(List<CategoriaMenuResponse> categorie,
                                List<ProdottoPubblicoResponse> prodottiSenzaCategoria) {
        setCategorie(categorie);
        setProdottiSenzaCategoria(prodottiSenzaCategoria);
    }

    public List<CategoriaMenuResponse> getCategorie() {
        return categorie;
    }

    public void setCategorie(List<CategoriaMenuResponse> categorie) {
        this.categorie = categorie != null ? new ArrayList<>(categorie) : new ArrayList<>();
    }

    public List<ProdottoPubblicoResponse> getProdottiSenzaCategoria() {
        return prodottiSenzaCategoria;
    }

    public void setProdottiSenzaCategoria(List<ProdottoPubblicoResponse> prodottiSenzaCategoria) {
        this.prodottiSenzaCategoria = prodottiSenzaCategoria != null
                ? new ArrayList<>(prodottiSenzaCategoria)
                : new ArrayList<>();
    }
}
