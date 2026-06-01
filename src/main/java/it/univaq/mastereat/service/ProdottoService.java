package it.univaq.mastereat.service;

import it.univaq.mastereat.dao.CaratteristicaDAO;
import it.univaq.mastereat.dao.CategoriaProdottoDAO;
import it.univaq.mastereat.dao.ImmagineProdottoDAO;
import it.univaq.mastereat.dao.IngredienteDAO;
import it.univaq.mastereat.dao.ProdottoDAO;
import it.univaq.mastereat.dao.UtenteDAO;
import it.univaq.mastereat.dao.impl.CaratteristicaDAOImpl;
import it.univaq.mastereat.dao.impl.CategoriaProdottoDAOImpl;
import it.univaq.mastereat.dao.impl.ImmagineProdottoDAOImpl;
import it.univaq.mastereat.dao.impl.IngredienteDAOImpl;
import it.univaq.mastereat.dao.impl.ProdottoDAOImpl;
import it.univaq.mastereat.dao.impl.UtenteDAOImpl;
import it.univaq.mastereat.dto.common.CaratteristicaPubblicaResponse;
import it.univaq.mastereat.dto.common.CategoriaMenuResponse;
import it.univaq.mastereat.dto.common.ImmagineProdottoPubblicaResponse;
import it.univaq.mastereat.dto.common.IngredienteProdottoResponse;
import it.univaq.mastereat.dto.api.menu.IngredientiProdottoResponse;
import it.univaq.mastereat.dto.common.MenuPubblicoResponse;
import it.univaq.mastereat.dto.web.owner.OwnerCaratteristicaResponse;
import it.univaq.mastereat.dto.web.owner.OwnerCategoriaProdottoResponse;
import it.univaq.mastereat.dto.web.owner.OwnerIngredienteResponse;
import it.univaq.mastereat.dto.web.owner.OwnerProdottoResponse;
import it.univaq.mastereat.dto.web.owner.OwnerProdottoSaveRequest;
import it.univaq.mastereat.dto.common.ProdottoPubblicoResponse;
import it.univaq.mastereat.model.Caratteristica;
import it.univaq.mastereat.model.CategoriaProdotto;
import it.univaq.mastereat.model.ImmagineProdotto;
import it.univaq.mastereat.model.Ingrediente;
import it.univaq.mastereat.model.Prodotto;
import it.univaq.mastereat.model.Utente;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Service per menù pubblico e gestione owner dei prodotti.
 *
 * Compone prodotti, categorie, immagini, caratteristiche e ingredienti nei DTO
 * usati da WE e SWA, lasciando ai DAO il solo accesso ai dati.
 */
public class ProdottoService {

    private static final BigDecimal PREZZO_MINIMO = BigDecimal.ZERO;
    private static final int NOME_MAX_LENGTH = 150;
    private static final int DESCRIZIONE_MAX_LENGTH = 2_000;
    private static final int DESCRIZIONE_PREPARAZIONE_MAX_LENGTH = 2_000;
    private static final String RUOLO_PERSONALE = "PERSONALE";
    private static final String RUOLO_PROPRIETARIO = "PROPRIETARIO";

    private final ProdottoDAO prodottoDAO;
    private final CaratteristicaDAO caratteristicaDAO;
    private final ImmagineProdottoDAO immagineProdottoDAO;
    private final CategoriaProdottoDAO categoriaProdottoDAO;
    private final IngredienteDAO ingredienteDAO;
    private final UtenteDAO utenteDAO;

    public ProdottoService() {
        this.prodottoDAO = new ProdottoDAOImpl();
        this.caratteristicaDAO = new CaratteristicaDAOImpl();
        this.immagineProdottoDAO = new ImmagineProdottoDAOImpl();
        this.categoriaProdottoDAO = new CategoriaProdottoDAOImpl();
        this.ingredienteDAO = new IngredienteDAOImpl();
        this.utenteDAO = new UtenteDAOImpl();
    }

    public List<Prodotto> getProdotti() {
        try {
            return prodottoDAO.findAll();
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il recupero dei prodotti", exception);
        }
    }

    public Optional<Prodotto> getProdottoById(int id) {
        try {
            return prodottoDAO.findById(id);
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il recupero del prodotto con id " + id, exception);
        }
    }

    public List<Prodotto> cercaProdotti(String nome, BigDecimal prezzoMin, BigDecimal prezzoMax) {
        validaPrezzi(prezzoMin, prezzoMax);

        try {
            return prodottoDAO.search(nome, prezzoMin, prezzoMax);
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante la ricerca dei prodotti", exception);
        }
    }

    /**
     * Costruisce il menù pubblico raggruppando i prodotti attivi per categoria,
     * mantenendo separati quelli senza categoria.
     */
    public MenuPubblicoResponse getMenuPubblico() {
        try {
            List<CategoriaProdotto> categorie = categoriaProdottoDAO.findAllActive();
            List<Prodotto> prodotti = prodottoDAO.findAll();
            List<ProdottoPubblicoResponse> prodottiPubblici = toProdottiPubblici(prodotti);

            Map<Long, CategoriaMenuResponse> categorieById = new LinkedHashMap<>();
            for (CategoriaProdotto categoria : categorie) {
                categorieById.put(
                        categoria.getId(),
                        new CategoriaMenuResponse(
                                categoria.getId(),
                                categoria.getNome(),
                                categoria.getDescrizione(),
                                new ArrayList<>()
                        )
                );
            }

            List<ProdottoPubblicoResponse> prodottiSenzaCategoria = new ArrayList<>();
            for (int index = 0; index < prodotti.size(); index++) {
                Prodotto prodotto = prodotti.get(index);
                ProdottoPubblicoResponse prodottoPubblico = prodottiPubblici.get(index);
                Long idCategoria = prodotto.getIdCategoria();

                if (idCategoria == null) {
                    prodottiSenzaCategoria.add(prodottoPubblico);
                    continue;
                }

                CategoriaMenuResponse categoria = categorieById.get(idCategoria);
                if (categoria != null) {
                    categoria.getProdotti().add(prodottoPubblico);
                }
            }

            List<CategoriaMenuResponse> categorieConProdotti = new ArrayList<>();
            for (CategoriaMenuResponse categoria : categorieById.values()) {
                if (!categoria.getProdotti().isEmpty()) {
                    categorieConProdotti.add(categoria);
                }
            }

            return new MenuPubblicoResponse(categorieConProdotti, prodottiSenzaCategoria);
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il recupero del menu pubblico", exception);
        }
    }

    public List<ProdottoPubblicoResponse> getProdottiPubblici() {
        try {
            return toProdottiPubblici(prodottoDAO.findAll());
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il recupero dei prodotti pubblici", exception);
        }
    }

    public Optional<ProdottoPubblicoResponse> getProdottoPubblicoById(int id) {
        try {
            Optional<Prodotto> prodotto = prodottoDAO.findById(id);
            if (prodotto.isEmpty()) {
                return Optional.empty();
            }

            List<Caratteristica> caratteristiche = caratteristicaDAO.findByProdottoId(id);
            List<ImmagineProdotto> immagini = immagineProdottoDAO.findByProdottoId(id);

            return Optional.of(toProdottoPubblico(prodotto.get(), caratteristiche, immagini));
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il recupero del prodotto pubblico con id " + id, exception);
        }
    }

    public List<OwnerProdottoResponse> getProdottiProprietario(long idProprietario) {
        try {
            Utente proprietario = requireUtenteAutenticato(idProprietario);
            requireProprietario(proprietario);

            return toOwnerProdottiResponse(prodottoDAO.findAllForOwner());
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il recupero dei prodotti proprietario", exception);
        }
    }

    public Optional<OwnerProdottoResponse> getProdottoProprietarioById(long idProprietario, int idProdotto) {
        validaIdProdotto(idProdotto);

        try {
            Utente proprietario = requireUtenteAutenticato(idProprietario);
            requireProprietario(proprietario);

            Optional<Prodotto> prodotto = prodottoDAO.findByIdForOwner(idProdotto);
            if (prodotto.isEmpty()) {
                return Optional.empty();
            }

            List<Caratteristica> caratteristiche = caratteristicaDAO.findByProdottoId(idProdotto);
            List<Ingrediente> ingredienti = ingredienteDAO.findByProdottoId(idProdotto);
            List<ImmagineProdotto> immagini = immagineProdottoDAO.findByProdottoId(idProdotto);
            return Optional.of(toOwnerProdottoResponse(prodotto.get(), caratteristiche, ingredienti, immagini));
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "Errore durante il recupero del prodotto proprietario con id " + idProdotto,
                    exception
            );
        }
    }

    public List<OwnerCategoriaProdottoResponse> getCategorieProdottoProprietario(long idProprietario) {
        try {
            Utente proprietario = requireUtenteAutenticato(idProprietario);
            requireProprietario(proprietario);

            return toOwnerCategorieProdottoResponse(categoriaProdottoDAO.findAllActive());
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il recupero delle categorie prodotto", exception);
        }
    }

    public OwnerProdottoResponse creaProdottoProprietario(long idProprietario,
                                                          OwnerProdottoSaveRequest request) {
        try {
            Utente proprietario = requireUtenteAutenticato(idProprietario);
            requireProprietario(proprietario);

            OwnerProdottoData data = validaProdottoOwnerRequest(request);
            Prodotto prodotto = prodottoDAO.createForOwner(
                    data.idCategoria,
                    data.nome,
                    data.descrizione,
                    data.prezzoBase,
                    data.minutiPreparazione,
                    data.descrizionePreparazione,
                    data.attivo
            );

            return toOwnerProdottoResponse(
                    prodotto,
                    Collections.emptyList(),
                    Collections.emptyList()
            );
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante la creazione del prodotto proprietario", exception);
        }
    }

    public OwnerProdottoResponse aggiornaProdottoProprietario(long idProprietario,
                                                              int idProdotto,
                                                              OwnerProdottoSaveRequest request) {
        validaIdProdotto(idProdotto);

        try {
            Utente proprietario = requireUtenteAutenticato(idProprietario);
            requireProprietario(proprietario);

            OwnerProdottoData data = validaProdottoOwnerRequest(request);
            Prodotto prodotto = prodottoDAO.updateForOwner(
                    idProdotto,
                    data.idCategoria,
                    data.nome,
                    data.descrizione,
                    data.prezzoBase,
                    data.minutiPreparazione,
                    data.descrizionePreparazione,
                    data.attivo
            ).orElseThrow(() -> new NoSuchElementException("Prodotto non trovato"));

            return toOwnerProdottoResponse(
                    prodotto,
                    caratteristicaDAO.findByProdottoId(idProdotto),
                    ingredienteDAO.findByProdottoId(idProdotto),
                    immagineProdottoDAO.findByProdottoId(idProdotto)
            );
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante l'aggiornamento del prodotto proprietario", exception);
        }
    }

    public IngredientiProdottoResponse getIngredientiProdotto(long idUtente, int idProdotto) {
        validaIdProdotto(idProdotto);

        try {
            Utente utente = requireUtenteAutenticato(idUtente);
            requirePersonaleOProprietario(utente);

            Prodotto prodotto = prodottoDAO.findById(idProdotto)
                    .orElseThrow(() -> new NoSuchElementException("Prodotto non trovato"));

            return toIngredientiProdottoResponse(prodotto, ingredienteDAO.findByProdottoId(idProdotto));
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "Errore durante il recupero degli ingredienti del prodotto " + idProdotto,
                    exception
            );
        }
    }

    public void eliminaCaratteristicaDaProdotto(long idUtente, int idProdotto, int idCaratteristica) {
        validaIdProdotto(idProdotto);
        validaIdCaratteristica(idCaratteristica);

        try {
            Utente utente = requireUtenteAutenticato(idUtente);
            requireProprietario(utente);

            prodottoDAO.findById(idProdotto)
                    .orElseThrow(() -> new NoSuchElementException("Prodotto non trovato"));

            boolean eliminata = caratteristicaDAO.deleteFromProdotto(idProdotto, idCaratteristica);
            if (!eliminata) {
                throw new NoSuchElementException("Caratteristica non trovata per il prodotto");
            }
        } catch (SQLException exception) {
            throw new RuntimeException(
                    "Errore durante l'eliminazione della caratteristica " + idCaratteristica,
                    exception
            );
        }
    }

    public List<ProdottoPubblicoResponse> cercaProdottiPubblici(String nome,
                                                               BigDecimal prezzoMin,
                                                               BigDecimal prezzoMax) {
        validaPrezzi(prezzoMin, prezzoMax);

        try {
            return toProdottiPubblici(prodottoDAO.search(nome, prezzoMin, prezzoMax));
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante la ricerca dei prodotti pubblici", exception);
        }
    }

    private List<ProdottoPubblicoResponse> toProdottiPubblici(List<Prodotto> prodotti) throws SQLException {
        if (prodotti == null || prodotti.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> idProdotti = new ArrayList<>();
        for (Prodotto prodotto : prodotti) {
            idProdotti.add(prodotto.getId());
        }

        Map<Long, List<Caratteristica>> caratteristicheByProdottoId =
                caratteristicaDAO.findByProdottoIds(idProdotti);
        Map<Long, List<ImmagineProdotto>> immaginiByProdottoId =
                immagineProdottoDAO.findByProdottoIds(idProdotti);

        List<ProdottoPubblicoResponse> prodottiPubblici = new ArrayList<>();
        for (Prodotto prodotto : prodotti) {
            prodottiPubblici.add(toProdottoPubblico(
                    prodotto,
                    caratteristicheByProdottoId.getOrDefault(prodotto.getId(), Collections.emptyList()),
                    immaginiByProdottoId.getOrDefault(prodotto.getId(), Collections.emptyList())
            ));
        }

        return prodottiPubblici;
    }

    private ProdottoPubblicoResponse toProdottoPubblico(Prodotto prodotto,
                                                        List<Caratteristica> caratteristiche,
                                                        List<ImmagineProdotto> immagini) {
        return new ProdottoPubblicoResponse(
                prodotto.getId(),
                prodotto.getNome(),
                prodotto.getDescrizione(),
                prodotto.getPrezzoBase(),
                toImmaginiPubbliche(immagini),
                toCaratteristichePubbliche(caratteristiche)
        );
    }

    private List<CaratteristicaPubblicaResponse> toCaratteristichePubbliche(List<Caratteristica> caratteristiche) {
        if (caratteristiche == null || caratteristiche.isEmpty()) {
            return Collections.emptyList();
        }

        List<CaratteristicaPubblicaResponse> response = new ArrayList<>();
        for (Caratteristica caratteristica : caratteristiche) {
            response.add(new CaratteristicaPubblicaResponse(
                    caratteristica.getId(),
                    caratteristica.getNome(),
                    caratteristica.getDescrizione(),
                    caratteristica.getDifferenzaPrezzo(),
                    caratteristica.isSelezionataDefault()
            ));
        }
        return response;
    }

    private IngredientiProdottoResponse toIngredientiProdottoResponse(Prodotto prodotto,
                                                                      List<Ingrediente> ingredienti) {
        List<IngredienteProdottoResponse> ingredientiResponse = new ArrayList<>();
        if (ingredienti != null) {
            for (Ingrediente ingrediente : ingredienti) {
                ingredientiResponse.add(new IngredienteProdottoResponse(
                        ingrediente.getId(),
                        ingrediente.getNome(),
                        ingrediente.getQuantita(),
                        ingrediente.getUnitaMisura()
                ));
            }
        }

        return new IngredientiProdottoResponse(
                prodotto.getId(),
                prodotto.getNome(),
                ingredientiResponse
        );
    }

    private List<OwnerCategoriaProdottoResponse> toOwnerCategorieProdottoResponse(List<CategoriaProdotto> categorie) {
        if (categorie == null || categorie.isEmpty()) {
            return Collections.emptyList();
        }

        List<OwnerCategoriaProdottoResponse> response = new ArrayList<>();
        for (CategoriaProdotto categoria : categorie) {
            response.add(new OwnerCategoriaProdottoResponse(
                    categoria.getId(),
                    categoria.getNome(),
                    categoria.getDescrizione()
            ));
        }
        return response;
    }

    private List<OwnerProdottoResponse> toOwnerProdottiResponse(List<Prodotto> prodotti) {
        if (prodotti == null || prodotti.isEmpty()) {
            return Collections.emptyList();
        }

        List<OwnerProdottoResponse> response = new ArrayList<>();
        for (Prodotto prodotto : prodotti) {
            response.add(toOwnerProdottoResponse(
                    prodotto,
                    Collections.emptyList(),
                    Collections.emptyList()
            ));
        }
        return response;
    }

    private OwnerProdottoResponse toOwnerProdottoResponse(Prodotto prodotto,
                                                          List<Caratteristica> caratteristiche,
                                                          List<Ingrediente> ingredienti) {
        return toOwnerProdottoResponse(prodotto, caratteristiche, ingredienti, Collections.emptyList());
    }

    private OwnerProdottoResponse toOwnerProdottoResponse(Prodotto prodotto,
                                                          List<Caratteristica> caratteristiche,
                                                          List<Ingrediente> ingredienti,
                                                          List<ImmagineProdotto> immagini) {
        return new OwnerProdottoResponse(
                prodotto.getId(),
                prodotto.getIdCategoria(),
                prodotto.getNome(),
                prodotto.getDescrizione(),
                prodotto.getPrezzoBase(),
                prodotto.getMinutiPreparazione(),
                prodotto.getDescrizionePreparazione(),
                prodotto.isAttivo(),
                prodotto.getCreatoIl(),
                prodotto.getAggiornatoIl(),
                toImmaginiPubbliche(immagini),
                toOwnerCaratteristicheResponse(caratteristiche),
                toOwnerIngredientiResponse(ingredienti)
        );
    }

    private List<OwnerCaratteristicaResponse> toOwnerCaratteristicheResponse(List<Caratteristica> caratteristiche) {
        if (caratteristiche == null || caratteristiche.isEmpty()) {
            return Collections.emptyList();
        }

        List<OwnerCaratteristicaResponse> response = new ArrayList<>();
        for (Caratteristica caratteristica : caratteristiche) {
            response.add(new OwnerCaratteristicaResponse(
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
            ));
        }
        return response;
    }

    private List<OwnerIngredienteResponse> toOwnerIngredientiResponse(List<Ingrediente> ingredienti) {
        if (ingredienti == null || ingredienti.isEmpty()) {
            return Collections.emptyList();
        }

        List<OwnerIngredienteResponse> response = new ArrayList<>();
        for (Ingrediente ingrediente : ingredienti) {
            response.add(new OwnerIngredienteResponse(
                    ingrediente.getId(),
                    ingrediente.getIdProdotto(),
                    ingrediente.getNome(),
                    ingrediente.getUnitaMisura(),
                    ingrediente.getQuantita(),
                    ingrediente.isAllergene(),
                    ingrediente.isAttivo()
            ));
        }
        return response;
    }

    private List<ImmagineProdottoPubblicaResponse> toImmaginiPubbliche(List<ImmagineProdotto> immagini) {
        if (immagini == null || immagini.isEmpty()) {
            return Collections.emptyList();
        }

        List<ImmagineProdottoPubblicaResponse> response = new ArrayList<>();
        for (ImmagineProdotto immagine : immagini) {
            response.add(new ImmagineProdottoPubblicaResponse(
                    immagine.getId(),
                    toPublicImageUrl(immagine.getPercorsoFile()),
                    immagine.getTestoAlternativo(),
                    immagine.isPrincipale()
            ));
        }
        return response;
    }

    /**
     * Converte percorsi assoluti o interni di storage nell'URL pubblico usato
     * da template e client REST.
     */
    private String toPublicImageUrl(String percorsoFile) {
        String percorsoNormalizzato = normalize(percorsoFile);
        if (percorsoNormalizzato == null) {
            return null;
        }

        String url = percorsoNormalizzato.replace("\\", "/");
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }

        int webappIndex = url.indexOf("/webapp/");
        if (webappIndex >= 0) {
            return ensureLeadingSlash(url.substring(webappIndex + "/webapp".length()));
        }

        int uploadsIndex = url.indexOf("/uploads/");
        if (uploadsIndex >= 0) {
            return url.substring(uploadsIndex);
        }

        return ensureLeadingSlash(url);
    }

    private String ensureLeadingSlash(String value) {
        return value.startsWith("/") ? value : "/" + value;
    }

    /**
     * Valida il form owner del prodotto e risolve i valori tipizzati usati dal
     * DAO, inclusa l'eventuale categoria attiva.
     */
    private OwnerProdottoData validaProdottoOwnerRequest(OwnerProdottoSaveRequest request) throws SQLException {
        if (request == null) {
            throw new IllegalArgumentException("Dati prodotto non validi.");
        }

        String nome = normalizeRequired(request.getNome(), "Nome");
        validateLength(nome, "Nome", NOME_MAX_LENGTH);

        String descrizione = normalizeRequired(request.getDescrizione(), "Descrizione");
        validateLength(descrizione, "Descrizione", DESCRIZIONE_MAX_LENGTH);

        BigDecimal prezzoBase = parsePrezzo(request.getPrezzoBase());
        int minutiPreparazione = parseMinutiPreparazione(request.getMinutiPreparazione());
        Long idCategoria = parseIdCategoria(request.getIdCategoria());
        if (idCategoria != null && !categoriaProdottoDAO.existsActiveById(idCategoria)) {
            throw new IllegalArgumentException("Categoria prodotto non valida.");
        }

        String descrizionePreparazione = normalizeOptional(request.getDescrizionePreparazione());
        validateLength(
                descrizionePreparazione,
                "Note preparazione",
                DESCRIZIONE_PREPARAZIONE_MAX_LENGTH
        );

        return new OwnerProdottoData(
                idCategoria,
                nome,
                descrizione,
                prezzoBase,
                minutiPreparazione,
                descrizionePreparazione,
                parseAttivo(request.getAttivo())
        );
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

    private void validateLength(String value, String label, int maxLength) {
        if (value != null && value.length() > maxLength) {
            throw new IllegalArgumentException(label + " deve contenere al massimo " + maxLength + " caratteri.");
        }
    }

    private BigDecimal parsePrezzo(String value) {
        String normalizedValue = normalizeRequired(value, "Prezzo base");
        BigDecimal prezzo;

        try {
            prezzo = new BigDecimal(normalizedValue.replace(',', '.'));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Prezzo base non valido.");
        }

        if (prezzo.compareTo(PREZZO_MINIMO) < 0) {
            throw new IllegalArgumentException("Prezzo base deve essere maggiore o uguale a 0.");
        }

        return prezzo;
    }

    private int parseMinutiPreparazione(String value) {
        String normalizedValue = normalizeRequired(value, "Tempo preparazione");
        int minutiPreparazione;

        try {
            minutiPreparazione = Integer.parseInt(normalizedValue);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Tempo preparazione non valido.");
        }

        if (minutiPreparazione <= 0) {
            throw new IllegalArgumentException("Tempo preparazione deve essere maggiore di 0 minuti.");
        }

        return minutiPreparazione;
    }

    private Long parseIdCategoria(String value) {
        String normalizedValue = normalize(value);
        if (normalizedValue == null) {
            return null;
        }

        try {
            long idCategoria = Long.parseLong(normalizedValue);
            if (idCategoria <= 0) {
                throw new IllegalArgumentException("Categoria prodotto non valida.");
            }
            return idCategoria;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Categoria prodotto non valida.");
        }
    }

    private boolean parseAttivo(String value) {
        String normalizedValue = normalize(value);
        if (normalizedValue == null) {
            return false;
        }

        return "true".equalsIgnoreCase(normalizedValue)
                || "on".equalsIgnoreCase(normalizedValue)
                || "1".equals(normalizedValue)
                || "si".equalsIgnoreCase(normalizedValue);
    }

    private void validaPrezzi(BigDecimal prezzoMin, BigDecimal prezzoMax) {
        if (prezzoMin != null && prezzoMin.compareTo(PREZZO_MINIMO) < 0) {
            throw new IllegalArgumentException("Parametro prezzoMin non valido");
        }
        if (prezzoMax != null && prezzoMax.compareTo(PREZZO_MINIMO) < 0) {
            throw new IllegalArgumentException("Parametro prezzoMax non valido");
        }
        if (prezzoMin != null && prezzoMax != null && prezzoMin.compareTo(prezzoMax) > 0) {
            throw new IllegalArgumentException("prezzoMin non puo essere maggiore di prezzoMax");
        }
    }

    private void validaIdProdotto(int idProdotto) {
        if (idProdotto <= 0) {
            throw new IllegalArgumentException("Id prodotto non valido");
        }
    }

    private void validaIdCaratteristica(int idCaratteristica) {
        if (idCaratteristica <= 0) {
            throw new IllegalArgumentException("Id caratteristica non valido");
        }
    }

    private Utente requireUtenteAutenticato(long idUtente) throws SQLException {
        try {
            return utenteDAO.findById(Math.toIntExact(idUtente))
                    .orElseThrow(() -> new SecurityException("Utente non autorizzato"));
        } catch (ArithmeticException exception) {
            throw new SecurityException("Utente non autorizzato");
        }
    }

    private void requirePersonaleOProprietario(Utente utente) {
        if (RUOLO_PERSONALE.equals(utente.getRuolo()) || RUOLO_PROPRIETARIO.equals(utente.getRuolo())) {
            return;
        }

        throw new SecurityException("Operazione consentita solo a PERSONALE o PROPRIETARIO");
    }

    private void requireProprietario(Utente utente) {
        if (RUOLO_PROPRIETARIO.equals(utente.getRuolo())) {
            return;
        }

        throw new SecurityException("Operazione consentita solo a PROPRIETARIO");
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static class OwnerProdottoData {

        private final Long idCategoria;
        private final String nome;
        private final String descrizione;
        private final BigDecimal prezzoBase;
        private final int minutiPreparazione;
        private final String descrizionePreparazione;
        private final boolean attivo;

        private OwnerProdottoData(Long idCategoria,
                                  String nome,
                                  String descrizione,
                                  BigDecimal prezzoBase,
                                  int minutiPreparazione,
                                  String descrizionePreparazione,
                                  boolean attivo) {
            this.idCategoria = idCategoria;
            this.nome = nome;
            this.descrizione = descrizione;
            this.prezzoBase = prezzoBase;
            this.minutiPreparazione = minutiPreparazione;
            this.descrizionePreparazione = descrizionePreparazione;
            this.attivo = attivo;
        }
    }
}
