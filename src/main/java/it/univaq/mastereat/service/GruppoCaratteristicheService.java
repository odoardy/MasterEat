package it.univaq.mastereat.service;

import it.univaq.mastereat.dao.GruppoCaratteristicheDAO;
import it.univaq.mastereat.dao.ProdottoDAO;
import it.univaq.mastereat.dao.UtenteDAO;
import it.univaq.mastereat.dao.impl.GruppoCaratteristicheDAOImpl;
import it.univaq.mastereat.dao.impl.ProdottoDAOImpl;
import it.univaq.mastereat.dao.impl.UtenteDAOImpl;
import it.univaq.mastereat.dto.web.owner.OwnerGruppoCaratteristicheResponse;
import it.univaq.mastereat.dto.web.owner.OwnerGruppoCaratteristicheSaveRequest;
import it.univaq.mastereat.model.GruppoCaratteristiche;
import it.univaq.mastereat.model.Prodotto;
import it.univaq.mastereat.model.Utente;

import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Service owner per i gruppi di caratteristiche prodotto.
 *
 * Gestisce gruppi obbligatori/opzionali e protegge la coerenza delle
 * caratteristiche già associate.
 */
public class GruppoCaratteristicheService {

    private static final int NOME_MAX_LENGTH = 100;
    private static final int DESCRIZIONE_MAX_LENGTH = 2_000;
    private static final String RUOLO_PROPRIETARIO = "PROPRIETARIO";
    private static final String MESSAGE_GRUPPO_CON_CARATTERISTICHE_ATTIVE =
            "Non puoi disattivare un gruppo con caratteristiche attive associate. "
                    + "Disattiva o sposta prima le caratteristiche.";

    private final GruppoCaratteristicheDAO gruppoCaratteristicheDAO;
    private final ProdottoDAO prodottoDAO;
    private final UtenteDAO utenteDAO;

    public GruppoCaratteristicheService() {
        this.gruppoCaratteristicheDAO = new GruppoCaratteristicheDAOImpl();
        this.prodottoDAO = new ProdottoDAOImpl();
        this.utenteDAO = new UtenteDAOImpl();
    }

    public List<OwnerGruppoCaratteristicheResponse> getGruppiCaratteristicheProprietario(long idProprietario,
                                                                                         int idProdotto) {
        validaIdProdotto(idProdotto);

        try {
            requireProprietario(requireUtenteAutenticato(idProprietario));
            requireProdottoOwner(idProdotto);

            return toOwnerGruppiCaratteristicheResponse(
                    gruppoCaratteristicheDAO.findAllByProdottoIdForOwner(idProdotto)
            );
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il recupero dei gruppi caratteristiche proprietario", exception);
        }
    }

    public Optional<OwnerGruppoCaratteristicheResponse> getGruppoCaratteristicheProprietarioById(long idProprietario,
                                                                                                  int idProdotto,
                                                                                                  long idGruppo) {
        validaIdProdotto(idProdotto);
        validaIdGruppo(idGruppo);

        try {
            requireProprietario(requireUtenteAutenticato(idProprietario));
            requireProdottoOwner(idProdotto);

            return gruppoCaratteristicheDAO.findByProdottoIdAndIdForOwner(idProdotto, idGruppo)
                    .map(this::toOwnerGruppoCaratteristicheResponse);
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il recupero del gruppo caratteristiche proprietario", exception);
        }
    }

    public OwnerGruppoCaratteristicheResponse creaGruppoCaratteristicheProprietario(
            long idProprietario,
            int idProdotto,
            OwnerGruppoCaratteristicheSaveRequest request) {
        validaIdProdotto(idProdotto);

        try {
            requireProprietario(requireUtenteAutenticato(idProprietario));
            requireProdottoOwner(idProdotto);

            OwnerGruppoCaratteristicheData data = validaGruppoRequest(idProdotto, request, null);
            return toOwnerGruppoCaratteristicheResponse(gruppoCaratteristicheDAO.createForOwner(
                    idProdotto,
                    data.nome,
                    data.descrizione,
                    data.obbligatorio,
                    data.attivo
            ));
        } catch (SQLIntegrityConstraintViolationException exception) {
            throw new IllegalArgumentException("Nome gruppo gia presente per questo prodotto.");
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "Errore durante la creazione del gruppo caratteristiche proprietario",
                    exception
            );
        }
    }

    public OwnerGruppoCaratteristicheResponse aggiornaGruppoCaratteristicheProprietario(
            long idProprietario,
            int idProdotto,
            long idGruppo,
            OwnerGruppoCaratteristicheSaveRequest request) {
        validaIdProdotto(idProdotto);
        validaIdGruppo(idGruppo);

        try {
            requireProprietario(requireUtenteAutenticato(idProprietario));
            requireProdottoOwner(idProdotto);

            GruppoCaratteristiche gruppo = gruppoCaratteristicheDAO
                    .findByProdottoIdAndIdForOwner(idProdotto, idGruppo)
                    .orElseThrow(() -> new NoSuchElementException("Gruppo caratteristiche non trovato"));

            OwnerGruppoCaratteristicheData data = validaGruppoRequest(idProdotto, request, idGruppo);
            if (gruppo.isAttivo() && !data.attivo) {
                validaDisattivazioneGruppo(idProdotto, idGruppo);
            }

            return toOwnerGruppoCaratteristicheResponse(gruppoCaratteristicheDAO.updateForOwner(
                    idProdotto,
                    idGruppo,
                    data.nome,
                    data.descrizione,
                    data.obbligatorio,
                    data.attivo
            ).orElseThrow(() -> new NoSuchElementException("Gruppo caratteristiche non trovato")));
        } catch (SQLIntegrityConstraintViolationException exception) {
            throw new IllegalArgumentException("Nome gruppo gia presente per questo prodotto.");
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "Errore durante l'aggiornamento del gruppo caratteristiche proprietario",
                    exception
            );
        }
    }

    public void rimuoviGruppoCaratteristicheProprietario(long idProprietario,
                                                         int idProdotto,
                                                         long idGruppo) {
        validaIdProdotto(idProdotto);
        validaIdGruppo(idGruppo);

        try {
            requireProprietario(requireUtenteAutenticato(idProprietario));
            requireProdottoOwner(idProdotto);
            gruppoCaratteristicheDAO.findByProdottoIdAndIdForOwner(idProdotto, idGruppo)
                    .orElseThrow(() -> new NoSuchElementException("Gruppo caratteristiche non trovato"));

            validaDisattivazioneGruppo(idProdotto, idGruppo);
            if (!gruppoCaratteristicheDAO.deactivateForOwner(idProdotto, idGruppo)) {
                throw new NoSuchElementException("Gruppo caratteristiche non trovato");
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "Errore durante la rimozione del gruppo caratteristiche proprietario",
                    exception
            );
        }
    }

    private OwnerGruppoCaratteristicheData validaGruppoRequest(int idProdotto,
                                                               OwnerGruppoCaratteristicheSaveRequest request,
                                                               Long idGruppoDaEscludere)
            throws SQLException {
        if (request == null) {
            throw new IllegalArgumentException("Dati gruppo caratteristiche non validi.");
        }

        String nome = normalizeRequired(request.getNome(), "Nome");
        validateLength(nome, "Nome", NOME_MAX_LENGTH);

        String descrizione = normalizeOptional(request.getDescrizione());
        validateLength(descrizione, "Descrizione", DESCRIZIONE_MAX_LENGTH);

        if (gruppoCaratteristicheDAO.existsActiveByProdottoIdAndNome(
                idProdotto,
                nome,
                idGruppoDaEscludere
        )) {
            throw new IllegalArgumentException("Esiste gia un gruppo attivo con questo nome per il prodotto.");
        }

        return new OwnerGruppoCaratteristicheData(
                nome,
                descrizione,
                parseBoolean(request.getObbligatorio()),
                parseBoolean(request.getAttivo())
        );
    }

    /**
     * Evita di disattivare un gruppo ancora referenziato da caratteristiche
     * attive, per non rendere incoerenti le configurazioni del prodotto.
     */
    private void validaDisattivazioneGruppo(int idProdotto, long idGruppo) throws SQLException {
        if (gruppoCaratteristicheDAO.existsActiveCaratteristicheByGruppoForOwner(idProdotto, idGruppo)) {
            throw new IllegalArgumentException(MESSAGE_GRUPPO_CON_CARATTERISTICHE_ATTIVE);
        }
    }

    private List<OwnerGruppoCaratteristicheResponse> toOwnerGruppiCaratteristicheResponse(
            List<GruppoCaratteristiche> gruppi) {
        if (gruppi == null || gruppi.isEmpty()) {
            return Collections.emptyList();
        }

        List<OwnerGruppoCaratteristicheResponse> response = new ArrayList<>();
        for (GruppoCaratteristiche gruppo : gruppi) {
            response.add(toOwnerGruppoCaratteristicheResponse(gruppo));
        }
        return response;
    }

    private OwnerGruppoCaratteristicheResponse toOwnerGruppoCaratteristicheResponse(GruppoCaratteristiche gruppo) {
        return new OwnerGruppoCaratteristicheResponse(
                gruppo.getId(),
                gruppo.getIdProdotto(),
                gruppo.getNome(),
                gruppo.getDescrizione(),
                gruppo.isObbligatorio(),
                gruppo.isAttivo()
        );
    }

    private Utente requireUtenteAutenticato(long idUtente) throws SQLException {
        try {
            return utenteDAO.findById(Math.toIntExact(idUtente))
                    .orElseThrow(() -> new SecurityException("Utente non autorizzato"));
        } catch (ArithmeticException exception) {
            throw new SecurityException("Utente non autorizzato");
        }
    }

    private void requireProprietario(Utente utente) {
        if (RUOLO_PROPRIETARIO.equals(utente.getRuolo())) {
            return;
        }

        throw new SecurityException("Operazione consentita solo a PROPRIETARIO");
    }

    private Prodotto requireProdottoOwner(int idProdotto) throws SQLException {
        return prodottoDAO.findByIdForOwner(idProdotto)
                .orElseThrow(() -> new NoSuchElementException("Prodotto non trovato"));
    }

    private void validaIdProdotto(int idProdotto) {
        if (idProdotto <= 0) {
            throw new IllegalArgumentException("Id prodotto non valido.");
        }
    }

    private void validaIdGruppo(long idGruppo) {
        if (idGruppo <= 0) {
            throw new IllegalArgumentException("Id gruppo caratteristiche non valido.");
        }
    }

    private String normalizeRequired(String value, String label) {
        String normalizedValue = normalize(value);
        if (normalizedValue == null) {
            throw new IllegalArgumentException(label + " obbligatorio.");
        }

        return normalizedValue;
    }

    private String normalizeOptional(String value) {
        return normalize(value);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private void validateLength(String value, String label, int maxLength) {
        if (value != null && value.length() > maxLength) {
            throw new IllegalArgumentException(label + " deve contenere al massimo " + maxLength + " caratteri.");
        }
    }

    private boolean parseBoolean(String value) {
        String normalizedValue = normalize(value);
        if (normalizedValue == null) {
            return false;
        }

        return "true".equalsIgnoreCase(normalizedValue)
                || "on".equalsIgnoreCase(normalizedValue)
                || "1".equals(normalizedValue)
                || "si".equalsIgnoreCase(normalizedValue);
    }

    private static class OwnerGruppoCaratteristicheData {

        private final String nome;
        private final String descrizione;
        private final boolean obbligatorio;
        private final boolean attivo;

        private OwnerGruppoCaratteristicheData(String nome,
                                               String descrizione,
                                               boolean obbligatorio,
                                               boolean attivo) {
            this.nome = nome;
            this.descrizione = descrizione;
            this.obbligatorio = obbligatorio;
            this.attivo = attivo;
        }
    }
}
