package it.univaq.mastereat.dao;

import it.univaq.mastereat.model.Caratteristica;
import it.univaq.mastereat.model.Ordine;
import it.univaq.mastereat.model.Prodotto;
import it.univaq.mastereat.model.RigaOrdine;
import it.univaq.mastereat.model.RigaOrdineDaCreare;
import it.univaq.mastereat.model.StatoOrdine;
import it.univaq.mastereat.model.StoricoStatoOrdine;
import it.univaq.mastereat.model.Utente;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OrdineDAO {

    Ordine create(Utente cliente, StatoOrdine stato) throws SQLException;

    Ordine createConfermato(Utente cliente,
                             List<RigaOrdineDaCreare> righe,
                             long idUtenteModifica) throws SQLException;

    Ordine createConfermato(Utente cliente,
                             List<RigaOrdineDaCreare> righe,
                             long idUtenteModifica,
                             LocalDateTime orarioConsegnaRichiesto) throws SQLException;

    Optional<Ordine> findById(long idOrdine) throws SQLException;

    RigaOrdine addRiga(long idOrdine,
                       Prodotto prodotto,
                       int quantita,
                       List<Caratteristica> caratteristiche) throws SQLException;

    List<RigaOrdine> findRigheByOrdineId(long idOrdine) throws SQLException;

    Map<Long, Integer> countProdottiByOrdineIds(List<Long> idOrdini) throws SQLException;

    BigDecimal calculateTotale(long idOrdine) throws SQLException;

    int calculateTempoPreparazione(long idOrdine) throws SQLException;

    Optional<Ordine> conferma(long idOrdine,
                              long idUtenteModifica,
                              StatoOrdine statoPrecedente) throws SQLException;

    Optional<Ordine> aggiornaStato(long idOrdine,
                                   long idUtenteModifica,
                                   StatoOrdine statoPrecedente,
                                   StatoOrdine statoNuovo) throws SQLException;

    List<Ordine> findByFilters(StatoOrdine stato,
                               LocalDate dataDa,
                               LocalDate dataA) throws SQLException;

    List<Ordine> findAllByFilters(StatoOrdine stato,
                                  LocalDate dataDa,
                                  LocalDate dataA) throws SQLException;

    List<Ordine> findByClienteId(long idCliente) throws SQLException;

    List<Ordine> findByClienteIdAndFilters(long idCliente,
                                           StatoOrdine stato,
                                           LocalDate dataDa,
                                           LocalDate dataA) throws SQLException;

    List<StoricoStatoOrdine> findStoricoByOrdineId(long idOrdine) throws SQLException;

    List<StoricoStatoOrdine> findStoricoOperatoriByOrdineId(long idOrdine) throws SQLException;

    Optional<Ordine> annulla(long idOrdine,
                             long idUtenteModifica,
                             StatoOrdine statoPrecedente,
                             String motivo) throws SQLException;
}
