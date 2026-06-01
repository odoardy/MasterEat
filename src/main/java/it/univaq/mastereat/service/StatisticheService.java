package it.univaq.mastereat.service;

import it.univaq.mastereat.dao.StatisticheDAO;
import it.univaq.mastereat.dao.UtenteDAO;
import it.univaq.mastereat.dao.impl.StatisticheDAOImpl;
import it.univaq.mastereat.dao.impl.UtenteDAOImpl;
import it.univaq.mastereat.dto.web.owner.OwnerStatisticheResponse;
import it.univaq.mastereat.model.Utente;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;

public class StatisticheService {

    private static final String RUOLO_PROPRIETARIO = "PROPRIETARIO";
    private static final int CLASSIFICA_PRODOTTI_LIMIT = 5;

    private final StatisticheDAO statisticheDAO;
    private final UtenteDAO utenteDAO;

    public StatisticheService() {
        this.statisticheDAO = new StatisticheDAOImpl();
        this.utenteDAO = new UtenteDAOImpl();
    }

    public OwnerStatisticheResponse getStatisticheProprietario(long idUtente, LocalDate dataSelezionata) {
        Utente utente = requireUtenteAutenticato(idUtente);
        requireProprietario(utente);

        LocalDate data = dataSelezionata != null ? dataSelezionata : LocalDate.now();
        LocalDate giornoFineEsclusivo = data.plusDays(1);
        YearMonth mese = YearMonth.from(data);
        LocalDate meseInizio = mese.atDay(1);
        LocalDate meseFineEsclusivo = mese.plusMonths(1).atDay(1);

        try {
            return new OwnerStatisticheResponse(
                    data,
                    meseInizio,
                    meseFineEsclusivo.minusDays(1),
                    statisticheDAO.getRiepilogoOrdini(data, giornoFineEsclusivo),
                    statisticheDAO.getRiepilogoOrdini(meseInizio, meseFineEsclusivo),
                    statisticheDAO.findProdottiPiuOrdinati(meseInizio, meseFineEsclusivo, CLASSIFICA_PRODOTTI_LIMIT),
                    statisticheDAO.findProdottiMenoOrdinati(meseInizio, meseFineEsclusivo, CLASSIFICA_PRODOTTI_LIMIT)
            );
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il recupero delle statistiche proprietario", exception);
        }
    }

    private Utente requireUtenteAutenticato(long idUtente) {
        try {
            return utenteDAO.findById(Math.toIntExact(idUtente))
                    .orElseThrow(() -> new SecurityException("Utente non autorizzato"));
        } catch (ArithmeticException exception) {
            throw new SecurityException("Utente non autorizzato");
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il recupero dell'utente", exception);
        }
    }

    private void requireProprietario(Utente utente) {
        if (RUOLO_PROPRIETARIO.equals(utente.getRuolo())) {
            return;
        }

        throw new SecurityException("Operazione consentita solo a PROPRIETARIO");
    }
}
