package it.univaq.mastereat.service;

import it.univaq.mastereat.dao.IngredienteDAO;
import it.univaq.mastereat.dao.ProdottoDAO;
import it.univaq.mastereat.dao.UtenteDAO;
import it.univaq.mastereat.dao.impl.IngredienteDAOImpl;
import it.univaq.mastereat.dao.impl.ProdottoDAOImpl;
import it.univaq.mastereat.dao.impl.UtenteDAOImpl;
import it.univaq.mastereat.dto.web.owner.OwnerIngredienteCatalogoResponse;
import it.univaq.mastereat.dto.web.owner.OwnerIngredienteResponse;
import it.univaq.mastereat.dto.web.owner.OwnerIngredienteSaveRequest;
import it.univaq.mastereat.model.Ingrediente;
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
 * Service owner per ingredienti e associazioni ingrediente-prodotto.
 *
 * Distingue il riuso di ingredienti già presenti nel catalogo dalla creazione
 * di un nuovo ingrediente durante l'associazione al prodotto.
 */
public class IngredienteService {

    private static final BigDecimal QUANTITA_MINIMA = BigDecimal.ZERO;
    private static final int NOME_MAX_LENGTH = 120;
    private static final int UNITA_MISURA_MAX_LENGTH = 20;
    private static final String RUOLO_PROPRIETARIO = "PROPRIETARIO";

    private final IngredienteDAO ingredienteDAO;
    private final ProdottoDAO prodottoDAO;
    private final UtenteDAO utenteDAO;

    public IngredienteService() {
        this.ingredienteDAO = new IngredienteDAOImpl();
        this.prodottoDAO = new ProdottoDAOImpl();
        this.utenteDAO = new UtenteDAOImpl();
    }

    public List<Ingrediente> getIngredientiByProdottoId(int idProdotto) {
        try {
            return ingredienteDAO.findByProdottoId(idProdotto);
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "Errore durante il recupero degli ingredienti del prodotto " + idProdotto,
                    exception
            );
        }
    }

    public List<OwnerIngredienteResponse> getIngredientiProprietario(long idProprietario,
                                                                     int idProdotto) {
        validaIdProdotto(idProdotto);

        try {
            requireProprietario(requireUtenteAutenticato(idProprietario));
            requireProdottoOwner(idProdotto);

            return toOwnerIngredientiResponse(ingredienteDAO.findAllByProdottoIdForOwner(idProdotto));
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il recupero degli ingredienti proprietario", exception);
        }
    }

    public Optional<OwnerIngredienteResponse> getIngredienteProprietarioById(long idProprietario,
                                                                             int idProdotto,
                                                                             long idIngrediente) {
        validaIdProdotto(idProdotto);
        validaIdIngrediente(idIngrediente);

        try {
            requireProprietario(requireUtenteAutenticato(idProprietario));
            requireProdottoOwner(idProdotto);

            return ingredienteDAO.findByProdottoIdAndIdForOwner(idProdotto, idIngrediente)
                    .map(this::toOwnerIngredienteResponse);
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il recupero dell'ingrediente proprietario", exception);
        }
    }

    public List<OwnerIngredienteCatalogoResponse> getIngredientiCatalogoProprietario(long idProprietario) {
        try {
            requireProprietario(requireUtenteAutenticato(idProprietario));

            return toOwnerIngredientiCatalogoResponse(ingredienteDAO.findAllActiveForOwner());
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il recupero del catalogo ingredienti", exception);
        }
    }

    public OwnerIngredienteResponse creaIngredienteProprietario(long idProprietario,
                                                                int idProdotto,
                                                                OwnerIngredienteSaveRequest request) {
        validaIdProdotto(idProdotto);

        try {
            requireProprietario(requireUtenteAutenticato(idProprietario));
            requireProdottoOwner(idProdotto);

            OwnerIngredienteData data = validaIngredienteRequest(request, true);
            long idIngrediente = data.idIngrediente != null
                    ? requireIngredienteCatalogoAttivo(data.idIngrediente).getId()
                    : ingredienteDAO.createForOwner(
                            data.nome,
                            data.unitaMisura,
                            data.allergene,
                            data.attivo
                    ).getId();

            if (ingredienteDAO.existsAssociationForOwner(idProdotto, idIngrediente)) {
                throw new IllegalArgumentException("Ingrediente già associato a questo prodotto.");
            }

            return toOwnerIngredienteResponse(ingredienteDAO.associateForOwner(
                    idProdotto,
                    idIngrediente,
                    data.quantita
            ));
        } catch (SQLIntegrityConstraintViolationException exception) {
            throw new IllegalArgumentException("Ingrediente già presente o già associato a questo prodotto.");
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il salvataggio dell'ingrediente proprietario", exception);
        }
    }

    public OwnerIngredienteResponse aggiornaIngredienteProprietario(long idProprietario,
                                                                    int idProdotto,
                                                                    long idIngrediente,
                                                                    OwnerIngredienteSaveRequest request) {
        validaIdProdotto(idProdotto);
        validaIdIngrediente(idIngrediente);

        try {
            requireProprietario(requireUtenteAutenticato(idProprietario));
            requireProdottoOwner(idProdotto);

            OwnerIngredienteData data = validaIngredienteRequest(request, false);
            Ingrediente ingrediente = ingredienteDAO.updateForOwner(
                    idProdotto,
                    idIngrediente,
                    data.nome,
                    data.unitaMisura,
                    data.quantita,
                    data.allergene,
                    data.attivo
            ).orElseThrow(() -> new NoSuchElementException("Ingrediente non trovato"));

            return toOwnerIngredienteResponse(ingrediente);
        } catch (SQLIntegrityConstraintViolationException exception) {
            throw new IllegalArgumentException("Nome ingrediente gia presente.");
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante l'aggiornamento dell'ingrediente proprietario", exception);
        }
    }

    public void rimuoviIngredienteProprietario(long idProprietario,
                                               int idProdotto,
                                               long idIngrediente) {
        validaIdProdotto(idProdotto);
        validaIdIngrediente(idIngrediente);

        try {
            requireProprietario(requireUtenteAutenticato(idProprietario));
            requireProdottoOwner(idProdotto);

            if (!ingredienteDAO.removeFromProdottoForOwner(idProdotto, idIngrediente)) {
                throw new NoSuchElementException("Ingrediente non trovato");
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante la rimozione dell'ingrediente proprietario", exception);
        }
    }

    /**
     * Valida sia la selezione da catalogo sia la creazione di un nuovo
     * ingrediente, condividendo la regola sulla quantità associata al prodotto.
     */
    private OwnerIngredienteData validaIngredienteRequest(OwnerIngredienteSaveRequest request,
                                                          boolean allowCatalogSelection) {
        if (request == null) {
            throw new IllegalArgumentException("Dati ingrediente non validi.");
        }

        Long idIngrediente = allowCatalogSelection ? parseIdIngredienteOptional(request.getIdIngrediente()) : null;
        BigDecimal quantita = parseQuantita(request.getQuantita());

        if (idIngrediente != null) {
            return new OwnerIngredienteData(
                    idIngrediente,
                    null,
                    null,
                    quantita,
                    false,
                    true
            );
        }

        String nome = normalizeRequired(request.getNome(), "Nome ingrediente");
        validateLength(nome, "Nome ingrediente", NOME_MAX_LENGTH);

        String unitaMisura = normalizeRequired(request.getUnitaMisura(), "Unita di misura");
        validateLength(unitaMisura, "Unita di misura", UNITA_MISURA_MAX_LENGTH);

        return new OwnerIngredienteData(
                null,
                nome,
                unitaMisura,
                quantita,
                parseBoolean(request.getAllergene()),
                parseBoolean(request.getAttivo())
        );
    }

    private Ingrediente requireIngredienteCatalogoAttivo(long idIngrediente) throws SQLException {
        return ingredienteDAO.findActiveByIdForOwner(idIngrediente)
                .orElseThrow(() -> new IllegalArgumentException("Ingrediente selezionato non valido."));
    }

    private List<OwnerIngredienteResponse> toOwnerIngredientiResponse(List<Ingrediente> ingredienti) {
        if (ingredienti == null || ingredienti.isEmpty()) {
            return Collections.emptyList();
        }

        List<OwnerIngredienteResponse> response = new ArrayList<>();
        for (Ingrediente ingrediente : ingredienti) {
            response.add(toOwnerIngredienteResponse(ingrediente));
        }
        return response;
    }

    private OwnerIngredienteResponse toOwnerIngredienteResponse(Ingrediente ingrediente) {
        return new OwnerIngredienteResponse(
                ingrediente.getId(),
                ingrediente.getIdProdotto(),
                ingrediente.getNome(),
                ingrediente.getUnitaMisura(),
                ingrediente.getQuantita(),
                ingrediente.isAllergene(),
                ingrediente.isAttivo()
        );
    }

    private List<OwnerIngredienteCatalogoResponse> toOwnerIngredientiCatalogoResponse(List<Ingrediente> ingredienti) {
        if (ingredienti == null || ingredienti.isEmpty()) {
            return Collections.emptyList();
        }

        List<OwnerIngredienteCatalogoResponse> response = new ArrayList<>();
        for (Ingrediente ingrediente : ingredienti) {
            response.add(new OwnerIngredienteCatalogoResponse(
                    ingrediente.getId(),
                    ingrediente.getNome(),
                    ingrediente.getUnitaMisura(),
                    ingrediente.isAllergene()
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

    private void validaIdIngrediente(long idIngrediente) {
        if (idIngrediente <= 0) {
            throw new IllegalArgumentException("Id ingrediente non valido.");
        }
    }

    private Long parseIdIngredienteOptional(String value) {
        String normalizedValue = normalize(value);
        if (normalizedValue == null) {
            return null;
        }

        try {
            long idIngrediente = Long.parseLong(normalizedValue);
            if (idIngrediente <= 0) {
                throw new IllegalArgumentException("Ingrediente selezionato non valido.");
            }
            return idIngrediente;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Ingrediente selezionato non valido.");
        }
    }

    private BigDecimal parseQuantita(String value) {
        String normalizedValue = normalizeRequired(value, "Quantita");
        BigDecimal quantita;

        try {
            quantita = new BigDecimal(normalizedValue.replace(',', '.'));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Quantita non valida.");
        }

        if (quantita.compareTo(QUANTITA_MINIMA) <= 0) {
            throw new IllegalArgumentException("Quantita deve essere maggiore di 0.");
        }

        return quantita;
    }

    private String normalizeRequired(String value, String label) {
        String normalizedValue = normalize(value);
        if (normalizedValue == null) {
            throw new IllegalArgumentException(label + " obbligatorio.");
        }

        return normalizedValue;
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

    private static class OwnerIngredienteData {

        private final Long idIngrediente;
        private final String nome;
        private final String unitaMisura;
        private final BigDecimal quantita;
        private final boolean allergene;
        private final boolean attivo;

        private OwnerIngredienteData(Long idIngrediente,
                                     String nome,
                                     String unitaMisura,
                                     BigDecimal quantita,
                                     boolean allergene,
                                     boolean attivo) {
            this.idIngrediente = idIngrediente;
            this.nome = nome;
            this.unitaMisura = unitaMisura;
            this.quantita = quantita;
            this.allergene = allergene;
            this.attivo = attivo;
        }
    }
}
