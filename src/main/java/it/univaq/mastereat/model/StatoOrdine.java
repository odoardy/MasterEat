package it.univaq.mastereat.model;

public enum StatoOrdine {
    BOZZA,
    INSERITO,
    IN_PREPARAZIONE,
    PRONTO,
    IN_CONSEGNA,
    CONSEGNATO,
    ANNULLATO;

    public boolean isModificabile() {
        return this == BOZZA;
    }

    public boolean isAnnullabile() {
        return this == BOZZA || this == INSERITO;
    }
}
