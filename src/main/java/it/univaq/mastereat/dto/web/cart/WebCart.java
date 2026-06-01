package it.univaq.mastereat.dto.web.cart;

import it.univaq.mastereat.dto.common.AggiungiProdottoOrdineRequest;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WebCart implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Map<String, WebCartItem> itemsByKey = new LinkedHashMap<>();

    public void addItem(WebCartItem item) {
        if (item == null) {
            return;
        }

        WebCartItem existingItem = itemsByKey.get(item.getItemKey());
        if (existingItem != null) {
            existingItem.incrementaQuantita(item.getQuantita());
            return;
        }

        itemsByKey.put(item.getItemKey(), item);
    }

    public boolean removeItem(String itemKey) {
        if (itemKey == null || itemKey.isBlank()) {
            return false;
        }
        return itemsByKey.remove(itemKey) != null;
    }

    public boolean incrementItem(String itemKey) {
        WebCartItem item = getItem(itemKey);
        if (item == null) {
            return false;
        }

        item.incrementaQuantita(1);
        return true;
    }

    public boolean decrementItem(String itemKey) {
        WebCartItem item = getItem(itemKey);
        if (item == null) {
            return false;
        }

        if (item.getQuantita() <= 1) {
            return removeItem(itemKey);
        }

        item.setQuantita(item.getQuantita() - 1);
        return true;
    }

    private WebCartItem getItem(String itemKey) {
        if (itemKey == null || itemKey.isBlank()) {
            return null;
        }
        return itemsByKey.get(itemKey);
    }

    public void clear() {
        itemsByKey.clear();
    }

    public boolean isEmpty() {
        return itemsByKey.isEmpty();
    }

    public List<WebCartItem> getItems() {
        return new ArrayList<>(itemsByKey.values());
    }

    public int getItemCount() {
        return itemsByKey.size();
    }

    public int getQuantitaTotale() {
        int totale = 0;
        for (WebCartItem item : itemsByKey.values()) {
            totale += item.getQuantita();
        }
        return totale;
    }

    public BigDecimal getTotale() {
        BigDecimal totale = BigDecimal.ZERO;
        for (WebCartItem item : itemsByKey.values()) {
            totale = totale.add(item.getSubtotaleRiga());
        }
        return totale;
    }

    public int getTempoPreparazioneStimato() {
        int minuti = 0;
        for (WebCartItem item : itemsByKey.values()) {
            minuti += item.getMinutiPreparazioneTotali();
        }
        return minuti;
    }

    public List<AggiungiProdottoOrdineRequest> toOrderRequests() {
        List<AggiungiProdottoOrdineRequest> requests = new ArrayList<>();
        for (WebCartItem item : itemsByKey.values()) {
            requests.add(new AggiungiProdottoOrdineRequest(
                    item.getIdProdotto(),
                    item.getQuantita(),
                    item.getIdCaratteristiche()
            ));
        }
        return requests;
    }
}
