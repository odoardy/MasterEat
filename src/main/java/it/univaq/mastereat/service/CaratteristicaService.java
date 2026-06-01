package it.univaq.mastereat.service;

import it.univaq.mastereat.dao.CaratteristicaDAO;
import it.univaq.mastereat.dao.GruppoCaratteristicheDAO;
import it.univaq.mastereat.dao.ProdottoDAO;
import it.univaq.mastereat.dao.UtenteDAO;
import it.univaq.mastereat.dao.impl.CaratteristicaDAOImpl;
import it.univaq.mastereat.dao.impl.GruppoCaratteristicheDAOImpl;
import it.univaq.mastereat.dao.impl.ProdottoDAOImpl;
import it.univaq.mastereat.dao.impl.UtenteDAOImpl;
import it.univaq.mastereat.dto.web.owner.OwnerCaratteristicaResponse;
import it.univaq.mastereat.dto.web.owner.OwnerCaratteristicaSaveRequest;
import it.univaq.mastereat.dto.web.owner.OwnerGruppoCaratteristicheResponse;
import it.univaq.mastereat.model.Caratteristica;
import it.univaq.mastereat.model.GruppoCaratteristiche;
import it.univaq.mastereat.model.Prodotto;
import it.univaq.mastereat.model.Utente;

import java.math.BigDecimal;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Service owner per le caratteristiche configurabili dei prodotti.
 *
 * Applica le regole sui gruppi di caratteristiche e prepara i DTO usati dalle
 * schermate proprietario.
 */
public class CaratteristicaService {

    private static final int NOME_MAX_LENGTH = 100;
    private static final int DESCRIZIONE_MAX_LENGTH = 2_000;
    private static final String RUOLO_PROPRIETARIO = "PROPRIETARIO";

    private final CaratteristicaDAO caratteristicaDAO;
    private final GruppoCaratteristicheDAO gruppoCaratteristicheDAO;
    private final ProdottoDAO prodottoDAO;
    private final UtenteDAO utenteDAO;

    public CaratteristicaService() {
        this.caratteristicaDAO = new CaratteristicaDAOImpl();
        this.gruppoCaratteristicheDAO = new GruppoCaratteristicheDAOImpl();
        this.prodottoDAO = new ProdottoDAOImpl();
        this.utenteDAO = new UtenteDAOImpl();
    }

    public List<Caratteristica> getCaratteristicheByProdottoId(int idProdotto) {
        try {
            return caratteristicaDAO.findByProdottoId(idProdotto);
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "Errore durante il recupero delle caratteristiche del prodotto " + idProdotto,
                    exception
            );
        }
    }

    public boolean eliminaCaratteristicaDaProdotto(int idProdotto, int idCaratteristica) {
        try {
            return caratteristicaDAO.deleteFromProdotto(idProdotto, idCaratteristica);
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "Errore durante l'eliminazione della caratteristica " + idCaratteristica,
                    exception
            );
        }
    }

    public List<OwnerCaratteristicaResponse> getCaratteristicheProprietario(long idProprietario,
                                                                            int idProdotto) {
        validaIdProdotto(idProdotto);

        try {
            requireProprietario(requireUtenteAutenticato(idProprietario));
            requireProdottoOwner(idProdotto);

            return toOwnerCaratteristicheResponse(caratteristicaDAO.findAllByProdottoIdForOwner(idProdotto));
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il recupero delle caratteristiche proprietario", exception);
        }
    }

    public Optional<OwnerCaratteristicaResponse> getCaratteristicaProprietarioById(long idProprietario,
                                                                                   int idProdotto,
                                                                                   int idCaratteristica) {
        validaIdProdotto(idProdotto);
        validaIdCaratteristica(idCaratteristica);

        try {
            requireProprietario(requireUtenteAutenticato(idProprietario));
            requireProdottoOwner(idProdotto);

            return caratteristicaDAO.findByProdottoIdAndIdForOwner(idProdotto, idCaratteristica)
                    .map(this::toOwnerCaratteristicaResponse);
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il recupero della caratteristica proprietario", exception);
        }
    }

    public List<OwnerGruppoCaratteristicheResponse> getGruppiCaratteristicheProprietario(long idProprietario,
                                                                                         int idProdotto) {
        validaIdProdotto(idProdotto);

        try {
            requireProprietario(requireUtenteAutenticato(idProprietario));
            requireProdottoOwner(idProdotto);

            return toOwnerGruppiCaratteristicheResponse(gruppoCaratteristicheDAO.findActiveByProdottoId(idProdotto));
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il recupero dei gruppi caratteristiche", exception);
        }
    }

    public OwnerCaratteristicaResponse creaCaratteristicaProprietario(long idProprietario,
                                                                      int idProdotto,
                                                                      OwnerCaratteristicaSaveRequest request) {
        validaIdProdotto(idProdotto);

        try {
            requireProprietario(requireUtenteAutenticato(idProprietario));
            requireProdottoOwner(idProdotto);

            OwnerCaratteristicaData data = validaCaratteristicaRequest(idProdotto, request);
            validaDefaultUnicoNelGruppo(idProdotto, data, null);
            return toOwnerCaratteristicaResponse(caratteristicaDAO.createForOwner(
                    idProdotto,
                    data.idGruppoCaratteristiche,
                    data.nome,
                    data.descrizione,
                    data.differenzaPrezzo,
                    data.selezionataDefault,
                    data.attiva
            ));
        } catch (SQLIntegrityConstraintViolationException exception) {
            throw new IllegalArgumentException("Nome caratteristica gia presente per questo prodotto.");
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante la creazione della caratteristica proprietario", exception);
        }
    }

    public OwnerCaratteristicaResponse aggiornaCaratteristicaProprietario(long idProprietario,
                                                                          int idProdotto,
                                                                          int idCaratteristica,
                                                                          OwnerCaratteristicaSaveRequest request) {
        validaIdProdotto(idProdotto);
        validaIdCaratteristica(idCaratteristica);

        try {
            requireProprietario(requireUtenteAutenticato(idProprietario));
            requireProdottoOwner(idProdotto);

            OwnerCaratteristicaData data = validaCaratteristicaRequest(idProdotto, request);
            validaDefaultUnicoNelGruppo(idProdotto, data, (long) idCaratteristica);
            Caratteristica caratteristica = caratteristicaDAO.updateForOwner(
                    idProdotto,
                    idCaratteristica,
                    data.idGruppoCaratteristiche,
                    data.nome,
                    data.descrizione,
                    data.differenzaPrezzo,
                    data.selezionataDefault,
                    data.attiva
            ).orElseThrow(() -> new NoSuchElementException("Caratteristica non trovata"));

            return toOwnerCaratteristicaResponse(caratteristica);
        } catch (SQLIntegrityConstraintViolationException exception) {
            throw new IllegalArgumentException("Nome caratteristica gia presente per questo prodotto.");
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante l'aggiornamento della caratteristica proprietario", exception);
        }
    }

    public void rimuoviCaratteristicaProprietario(long idProprietario,
                                                  int idProdotto,
                                                  int idCaratteristica) {
        validaIdProdotto(idProdotto);
        validaIdCaratteristica(idCaratteristica);

        try {
            requireProprietario(requireUtenteAutenticato(idProprietario));
            requireProdottoOwner(idProdotto);

            if (!caratteristicaDAO.deleteFromProdotto(idProdotto, idCaratteristica)) {
                throw new NoSuchElementException("Caratteristica non trovata");
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante la rimozione della caratteristica proprietario", exception);
        }
    }

    private OwnerCaratteristicaData validaCaratteristicaRequest(int idProdotto,
                                                                OwnerCaratteristicaSaveRequest request)
            throws SQLException {
        if (request == null) {
            throw new IllegalArgumentException("Dati caratteristica non validi.");
        }

        String nome = normalizeRequired(request.getNome(), "Nome");
        validateLength(nome, "Nome", NOME_MAX_LENGTH);

        String descrizione = normalizeOptional(request.getDescrizione());
        validateLength(descrizione, "Descrizione", DESCRIZIONE_MAX_LENGTH);

        BigDecimal differenzaPrezzo = parseDifferenzaPrezzo(request.getDifferenzaPrezzo());
        Long idGruppoCaratteristiche = parseIdGruppoCaratteristiche(request.getIdGruppoCaratteristiche());
        if (idGruppoCaratteristiche != null
                && !gruppoCaratteristicheDAO.existsActiveByProdottoIdAndId(idProdotto, idGruppoCaratteristiche)) {
            throw new IllegalArgumentException("Gruppo caratteristiche non valido.");
        }

        return new OwnerCaratteristicaData(
                idGruppoCaratteristiche,
                nome,
                descrizione,
                differenzaPrezzo,
                parseBoolean(request.getSelezionataDefault()),
                parseBoolean(request.getAttiva())
        );
    }

    /**
     * In un gruppo puo esistere una sola caratteristica attiva marcata come
     * default, cosi il checkout non riceve configurazioni ambigue.
     */
    private void validaDefaultUnicoNelGruppo(int idProdotto,
                                             OwnerCaratteristicaData data,
                                             Long idCaratteristicaDaEscludere) throws SQLException {
        if (data.idGruppoCaratteristiche == null || !data.selezionataDefault || !data.attiva) {
            return;
        }

        if (caratteristicaDAO.existsOtherDefaultInGroupForOwner(
                idProdotto,
                data.idGruppoCaratteristiche,
                idCaratteristicaDaEscludere
        )) {
            throw new IllegalArgumentException("Esiste gia una caratteristica di default attiva per questo gruppo.");
        }
    }

    private List<OwnerCaratteristicaResponse> toOwnerCaratteristicheResponse(List<Caratteristica> caratteristiche) {
        if (caratteristiche == null || caratteristiche.isEmpty()) {
            return Collections.emptyList();
        }

        List<OwnerCaratteristicaResponse> response = new ArrayList<>();
        for (Caratteristica caratteristica : caratteristiche) {
            response.add(toOwnerCaratteristicaResponse(caratteristica));
        }
        return response;
    }

    private OwnerCaratteristicaResponse toOwnerCaratteristicaResponse(Caratteristica caratteristica) {
        return new OwnerCaratteristicaResponse(
                caratteristica.getId(),
                caratteristica.getIdProdotto(),
                caratteristica.getIdGruppoCaratteristiche(),
                caratteristica.getNomeGruppoCaratteristiche(),
                caratteristica.getDescrizioneGruppoCaratteristiche(),
                caratteristica.getNome(),
                caratteristica.getDescrizione(),
                caratteristica.getDifferenzaPrezzo(),
                caratteristica.isSelezionataDefault(),
                caratteristica.isAttiva()
        );
    }

    private List<OwnerGruppoCaratteristicheResponse> toOwnerGruppiCaratteristicheResponse(
            List<GruppoCaratteristiche> gruppi) {
        if (gruppi == null || gruppi.isEmpty()) {
            return Collections.emptyList();
        }

        List<OwnerGruppoCaratteristicheResponse> response = new ArrayList<>();
        for (GruppoCaratteristiche gruppo : gruppi) {
            response.add(new OwnerGruppoCaratteristicheResponse(
                    gruppo.getId(),
                    gruppo.getIdProdotto(),
                    gruppo.getNome(),
                    gruppo.getDescrizione(),
                    gruppo.isObbligatorio(),
                    gruppo.isAttivo()
            ));
        }
        return response;
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

    private void validaIdCaratteristica(int idCaratteristica) {
        if (idCaratteristica <= 0) {
            throw new IllegalArgumentException("Id caratteristica non valido.");
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

    private BigDecimal parseDifferenzaPrezzo(String value) {
        String normalizedValue = normalizeRequired(value, "Differenza prezzo");

        try {
            return new BigDecimal(normalizedValue.replace(',', '.'));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Differenza prezzo non valida.");
        }
    }

    private Long parseIdGruppoCaratteristiche(String value) {
        String normalizedValue = normalize(value);
        if (normalizedValue == null) {
            return null;
        }

        try {
            long idGruppoCaratteristiche = Long.parseLong(normalizedValue);
            if (idGruppoCaratteristiche <= 0) {
                throw new IllegalArgumentException("Gruppo caratteristiche non valido.");
            }
            return idGruppoCaratteristiche;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Gruppo caratteristiche non valido.");
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

    private static class OwnerCaratteristicaData {

        private final Long idGruppoCaratteristiche;
        private final String nome;
        private final String descrizione;
        private final BigDecimal differenzaPrezzo;
        private final boolean selezionataDefault;
        private final boolean attiva;

        private OwnerCaratteristicaData(Long idGruppoCaratteristiche,
                                        String nome,
                                        String descrizione,
                                        BigDecimal differenzaPrezzo,
                                        boolean selezionataDefault,
                                        boolean attiva) {
            this.idGruppoCaratteristiche = idGruppoCaratteristiche;
            this.nome = nome;
            this.descrizione = descrizione;
            this.differenzaPrezzo = differenzaPrezzo;
            this.selezionataDefault = selezionataDefault;
            this.attiva = attiva;
        }
    }
}
