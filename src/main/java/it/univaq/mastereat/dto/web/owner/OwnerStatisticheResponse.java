package it.univaq.mastereat.dto.web.owner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class OwnerStatisticheResponse {

    private static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MM/yyyy");

    private LocalDate dataSelezionata;
    private LocalDate meseInizio;
    private LocalDate meseFine;
    private RiepilogoStatisticheResponse riepilogoGiornaliero;
    private RiepilogoStatisticheResponse riepilogoMensile;
    private List<ProdottoStatisticaResponse> prodottiPiuOrdinati = new ArrayList<>();
    private List<ProdottoStatisticaResponse> prodottiMenoOrdinati = new ArrayList<>();

    public OwnerStatisticheResponse() {
        this.riepilogoGiornaliero = new RiepilogoStatisticheResponse();
        this.riepilogoMensile = new RiepilogoStatisticheResponse();
    }

    public OwnerStatisticheResponse(LocalDate dataSelezionata,
                                    LocalDate meseInizio,
                                    LocalDate meseFine,
                                    RiepilogoStatisticheResponse riepilogoGiornaliero,
                                    RiepilogoStatisticheResponse riepilogoMensile,
                                    List<ProdottoStatisticaResponse> prodottiPiuOrdinati,
                                    List<ProdottoStatisticaResponse> prodottiMenoOrdinati) {
        this.dataSelezionata = dataSelezionata;
        this.meseInizio = meseInizio;
        this.meseFine = meseFine;
        this.riepilogoGiornaliero = riepilogoGiornaliero != null
                ? riepilogoGiornaliero
                : new RiepilogoStatisticheResponse();
        this.riepilogoMensile = riepilogoMensile != null
                ? riepilogoMensile
                : new RiepilogoStatisticheResponse();
        setProdottiPiuOrdinati(prodottiPiuOrdinati);
        setProdottiMenoOrdinati(prodottiMenoOrdinati);
    }

    public static OwnerStatisticheResponse empty(LocalDate dataSelezionata) {
        LocalDate normalizedDate = dataSelezionata != null ? dataSelezionata : LocalDate.now();
        LocalDate meseInizio = normalizedDate.withDayOfMonth(1);
        LocalDate meseFine = meseInizio.plusMonths(1).minusDays(1);

        return new OwnerStatisticheResponse(
                normalizedDate,
                meseInizio,
                meseFine,
                new RiepilogoStatisticheResponse(),
                new RiepilogoStatisticheResponse(),
                List.of(),
                List.of()
        );
    }

    public LocalDate getDataSelezionata() {
        return dataSelezionata;
    }

    public void setDataSelezionata(LocalDate dataSelezionata) {
        this.dataSelezionata = dataSelezionata;
    }

    public String getDataSelezionataIso() {
        return dataSelezionata != null ? dataSelezionata.toString() : "";
    }

    public String getDataSelezionataLabel() {
        return dataSelezionata != null ? dataSelezionata.format(DISPLAY_DATE_FORMATTER) : "";
    }

    public LocalDate getMeseInizio() {
        return meseInizio;
    }

    public void setMeseInizio(LocalDate meseInizio) {
        this.meseInizio = meseInizio;
    }

    public String getMeseInizioLabel() {
        return meseInizio != null ? meseInizio.format(DISPLAY_DATE_FORMATTER) : "";
    }

    public LocalDate getMeseFine() {
        return meseFine;
    }

    public void setMeseFine(LocalDate meseFine) {
        this.meseFine = meseFine;
    }

    public String getMeseFineLabel() {
        return meseFine != null ? meseFine.format(DISPLAY_DATE_FORMATTER) : "";
    }

    public String getMeseLabel() {
        return meseInizio != null ? meseInizio.format(MONTH_FORMATTER) : "";
    }

    public RiepilogoStatisticheResponse getRiepilogoGiornaliero() {
        return riepilogoGiornaliero;
    }

    public void setRiepilogoGiornaliero(RiepilogoStatisticheResponse riepilogoGiornaliero) {
        this.riepilogoGiornaliero = riepilogoGiornaliero != null
                ? riepilogoGiornaliero
                : new RiepilogoStatisticheResponse();
    }

    public RiepilogoStatisticheResponse getRiepilogoMensile() {
        return riepilogoMensile;
    }

    public void setRiepilogoMensile(RiepilogoStatisticheResponse riepilogoMensile) {
        this.riepilogoMensile = riepilogoMensile != null
                ? riepilogoMensile
                : new RiepilogoStatisticheResponse();
    }

    public List<ProdottoStatisticaResponse> getProdottiPiuOrdinati() {
        return prodottiPiuOrdinati;
    }

    public void setProdottiPiuOrdinati(List<ProdottoStatisticaResponse> prodottiPiuOrdinati) {
        this.prodottiPiuOrdinati = prodottiPiuOrdinati != null
                ? new ArrayList<>(prodottiPiuOrdinati)
                : new ArrayList<>();
    }

    public List<ProdottoStatisticaResponse> getProdottiMenoOrdinati() {
        return prodottiMenoOrdinati;
    }

    public void setProdottiMenoOrdinati(List<ProdottoStatisticaResponse> prodottiMenoOrdinati) {
        this.prodottiMenoOrdinati = prodottiMenoOrdinati != null
                ? new ArrayList<>(prodottiMenoOrdinati)
                : new ArrayList<>();
    }

    public static class RiepilogoStatisticheResponse {

        private BigDecimal incassoTotale;
        private int numeroOrdini;

        public RiepilogoStatisticheResponse() {
            this(BigDecimal.ZERO, 0);
        }

        public RiepilogoStatisticheResponse(BigDecimal incassoTotale, int numeroOrdini) {
            this.incassoTotale = incassoTotale != null ? incassoTotale : BigDecimal.ZERO;
            this.numeroOrdini = numeroOrdini;
        }

        public BigDecimal getIncassoTotale() {
            return incassoTotale;
        }

        public void setIncassoTotale(BigDecimal incassoTotale) {
            this.incassoTotale = incassoTotale != null ? incassoTotale : BigDecimal.ZERO;
        }

        public int getNumeroOrdini() {
            return numeroOrdini;
        }

        public void setNumeroOrdini(int numeroOrdini) {
            this.numeroOrdini = numeroOrdini;
        }
    }

    public static class ProdottoStatisticaResponse {

        private long idProdotto;
        private String nomeProdotto;
        private int quantitaOrdinata;
        private BigDecimal ricavoGenerato;

        public ProdottoStatisticaResponse() {
            this.ricavoGenerato = BigDecimal.ZERO;
        }

        public ProdottoStatisticaResponse(long idProdotto,
                                          String nomeProdotto,
                                          int quantitaOrdinata,
                                          BigDecimal ricavoGenerato) {
            this.idProdotto = idProdotto;
            this.nomeProdotto = nomeProdotto;
            this.quantitaOrdinata = quantitaOrdinata;
            this.ricavoGenerato = ricavoGenerato != null ? ricavoGenerato : BigDecimal.ZERO;
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

        public int getQuantitaOrdinata() {
            return quantitaOrdinata;
        }

        public void setQuantitaOrdinata(int quantitaOrdinata) {
            this.quantitaOrdinata = quantitaOrdinata;
        }

        public BigDecimal getRicavoGenerato() {
            return ricavoGenerato;
        }

        public void setRicavoGenerato(BigDecimal ricavoGenerato) {
            this.ricavoGenerato = ricavoGenerato != null ? ricavoGenerato : BigDecimal.ZERO;
        }
    }
}
