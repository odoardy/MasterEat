package it.univaq.mastereat.service;

import it.univaq.mastereat.dao.CaratteristicaDAO;
import it.univaq.mastereat.dao.GruppoCaratteristicheDAO;
import it.univaq.mastereat.dao.IngredienteDAO;
import it.univaq.mastereat.dao.OrdineDAO;
import it.univaq.mastereat.dao.ProdottoDAO;
import it.univaq.mastereat.dao.UtenteDAO;
import it.univaq.mastereat.dao.impl.CaratteristicaDAOImpl;
import it.univaq.mastereat.dao.impl.GruppoCaratteristicheDAOImpl;
import it.univaq.mastereat.dao.impl.IngredienteDAOImpl;
import it.univaq.mastereat.dao.impl.OrdineDAOImpl;
import it.univaq.mastereat.dao.impl.ProdottoDAOImpl;
import it.univaq.mastereat.dao.impl.UtenteDAOImpl;
import it.univaq.mastereat.dto.api.ordini.AggiornaStatoOrdineRequest;
import it.univaq.mastereat.dto.common.AggiungiProdottoOrdineRequest;
import it.univaq.mastereat.dto.api.ordini.CambioStatoOperatoreResponse;
import it.univaq.mastereat.dto.common.CaratteristicaOrdineResponse;
import it.univaq.mastereat.dto.api.ordini.OperatoreOrdineResponse;
import it.univaq.mastereat.dto.api.ordini.OperatoriOrdineResponse;
import it.univaq.mastereat.dto.common.OrdineResponse;
import it.univaq.mastereat.dto.web.owner.OwnerOrdineResponse;
import it.univaq.mastereat.dto.common.ProdottiOrdineResponse;
import it.univaq.mastereat.dto.common.RigaOrdineResponse;
import it.univaq.mastereat.dto.common.IngredienteProdottoResponse;
import it.univaq.mastereat.dto.web.staff.StaffOrdineResponse;
import it.univaq.mastereat.dto.web.staff.StaffRigaOrdineResponse;
import it.univaq.mastereat.dto.api.ordini.TempoConsegnaOrdineResponse;
import it.univaq.mastereat.dto.api.ordini.TotaleOrdineResponse;
import it.univaq.mastereat.model.Caratteristica;
import it.univaq.mastereat.model.CaratteristicaRigaOrdine;
import it.univaq.mastereat.model.GruppoCaratteristiche;
import it.univaq.mastereat.model.Ingrediente;
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
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

/**
 * Service di dominio per ordini WE e SWA.
 *
 * Centralizza autorizzazioni per cliente/staff/proprietario, transizioni di
 * stato, validazione del checkout e notifiche email collegate agli eventi
 * principali dell'ordine.
 */
public class OrdineService {

    private static final String RUOLO_CLIENTE = "CLIENTE";
    private static final String RUOLO_PERSONALE = "PERSONALE";
    private static final String RUOLO_PROPRIETARIO = "PROPRIETARIO";
    private static final ZoneId ZONA_ORARIA_CONSEGNA = ZoneId.of("Europe/Rome");
    // Configurazione semplice finche non esiste un orario di chiusura persistito.
    private static final LocalTime ORARIO_CHIUSURA_ATTIVITA = LocalTime.of(23, 0);
    private static final DateTimeFormatter FORMATO_ORARIO_UTENTE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final OrdineDAO ordineDAO;
    private final ProdottoDAO prodottoDAO;
    private final CaratteristicaDAO caratteristicaDAO;
    private final GruppoCaratteristicheDAO gruppoCaratteristicheDAO;
    private final IngredienteDAO ingredienteDAO;
    private final UtenteDAO utenteDAO;
    private final EmailNotificationService emailNotificationService;

    public OrdineService() {
        this.ordineDAO = new OrdineDAOImpl();
        this.prodottoDAO = new ProdottoDAOImpl();
        this.caratteristicaDAO = new CaratteristicaDAOImpl();
        this.gruppoCaratteristicheDAO = new GruppoCaratteristicheDAOImpl();
        this.ingredienteDAO = new IngredienteDAOImpl();
        this.utenteDAO = new UtenteDAOImpl();
        this.emailNotificationService = new EmailNotificationService();
    }

    public OrdineResponse creaOrdine(long idUtente) {
        Utente cliente = requireCliente(idUtente);
        validaDatiConsegnaCliente(cliente);

        try {
            return toOrdineResponse(ordineDAO.create(cliente, StatoOrdine.BOZZA));
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante la creazione dell'ordine", exception);
        }
    }

    public void validaConfigurazioneProdotto(long idProdotto, List<Long> idCaratteristiche) {
        try {
            Prodotto prodotto = prodottoDAO.findById(toProdottoDaoId(idProdotto))
                    .orElseThrow(() -> new NoSuchElementException("Prodotto non trovato"));

            validaCaratteristicheProdotto(prodotto.getId(), idCaratteristiche);
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante la validazione della configurazione prodotto", exception);
        }
    }

    public OrdineResponse creaOrdineConfermato(long idUtente,
                                               List<AggiungiProdottoOrdineRequest> prodotti) {
        return creaOrdineConfermatoInterno(idUtente, prodotti, null, false);
    }

    /**
     * Crea e conferma in un'unica transazione l'ordine proveniente dal checkout,
     * includendo la validazione dell'orario di consegna richiesto.
     */
    public OrdineResponse creaOrdineConfermatoWeb(long idUtente,
                                                  List<AggiungiProdottoOrdineRequest> prodotti,
                                                  LocalDateTime orarioConsegnaRichiesto) {
        return creaOrdineConfermatoInterno(idUtente, prodotti, orarioConsegnaRichiesto, true);
    }

    public LocalDateTime getOrarioConsegnaMinimoWeb(int minutiConsegnaStimati) {
        return calcolaOrarioConsegnaMinimo(minutiConsegnaStimati);
    }

    private OrdineResponse creaOrdineConfermatoInterno(long idUtente,
                                                       List<AggiungiProdottoOrdineRequest> prodotti,
                                                       LocalDateTime orarioConsegnaRichiesto,
                                                       boolean validaOrarioConsegnaRichiesto) {
        Utente cliente = requireCliente(idUtente);
        validaDatiConsegnaCliente(cliente);
        if (prodotti == null || prodotti.isEmpty()) {
            throw new IllegalArgumentException("Il carrello e vuoto");
        }
        if (validaOrarioConsegnaRichiesto && orarioConsegnaRichiesto == null) {
            throw new IllegalArgumentException("Orario di consegna richiesto obbligatorio");
        }

        try {
            List<RigaOrdineDaCreare> righe = new ArrayList<>();
            for (AggiungiProdottoOrdineRequest request : prodotti) {
                validaRichiestaAggiuntaProdotto(request);

                Prodotto prodotto = prodottoDAO.findById(toProdottoDaoId(request.getIdProdotto()))
                        .orElseThrow(() -> new NoSuchElementException("Prodotto non trovato"));

                List<Caratteristica> caratteristiche = validaCaratteristicheProdotto(
                        prodotto.getId(),
                        request.getCaratteristiche()
                );
                righe.add(new RigaOrdineDaCreare(prodotto, request.getQuantita(), caratteristiche));
            }

            Ordine ordineConfermato;
            if (validaOrarioConsegnaRichiesto) {
                validaOrarioConsegnaRichiesto(orarioConsegnaRichiesto, righe);
                ordineConfermato = ordineDAO.createConfermato(
                        cliente,
                        righe,
                        idUtente,
                        orarioConsegnaRichiesto
                );
            } else {
                ordineConfermato = ordineDAO.createConfermato(cliente, righe, idUtente);
            }

            OrdineResponse response = toOrdineResponse(ordineConfermato);
            emailNotificationService.notificaOrdineConfermato(ordineConfermato.getId());
            return response;
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante la conferma dell'ordine web", exception);
        }
    }

    public OrdineResponse getOrdineCliente(long idUtente, long idOrdine) {
        validaIdOrdine(idOrdine);

        try {
            Ordine ordine = requireOrdine(idOrdine);
            requireProprietarioOrdine(ordine, idUtente);
            return toOrdineResponse(ordine);
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il recupero dell'ordine", exception);
        }
    }

    public String getOrarioConsegnaRichiestoCliente(long idUtente, long idOrdine) {
        validaIdOrdine(idOrdine);

        try {
            Ordine ordine = requireOrdine(idOrdine);
            requireProprietarioOrdine(ordine, idUtente);
            return ordine.getOrarioConsegnaRichiesto();
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il recupero dell'orario di consegna richiesto", exception);
        }
    }

    public RigaOrdineResponse aggiungiProdotto(long idUtente,
                                               long idOrdine,
                                               AggiungiProdottoOrdineRequest request) {
        validaIdOrdine(idOrdine);
        validaRichiestaAggiuntaProdotto(request);

        try {
            Ordine ordine = requireOrdine(idOrdine);
            requireProprietarioOrdine(ordine, idUtente);
            requireOrdineModificabile(ordine);

            Prodotto prodotto = prodottoDAO.findById(toProdottoDaoId(request.getIdProdotto()))
                    .orElseThrow(() -> new NoSuchElementException("Prodotto non trovato"));

            List<Caratteristica> caratteristiche = validaCaratteristicheProdotto(
                    prodotto.getId(),
                    request.getCaratteristiche()
            );

            RigaOrdine rigaOrdine = ordineDAO.addRiga(
                    idOrdine,
                    prodotto,
                    request.getQuantita(),
                    caratteristiche
            );
            return toRigaOrdineResponse(rigaOrdine);
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante l'inserimento del prodotto nell'ordine", exception);
        }
    }

    public ProdottiOrdineResponse getProdottiOrdine(long idUtente, long idOrdine) {
        validaIdOrdine(idOrdine);

        try {
            Ordine ordine = requireOrdine(idOrdine);
            requireProprietarioOrdine(ordine, idUtente);

            List<RigaOrdineResponse> prodotti = new ArrayList<>();
            for (RigaOrdine rigaOrdine : ordineDAO.findRigheByOrdineId(idOrdine)) {
                prodotti.add(toRigaOrdineResponse(rigaOrdine));
            }

            return new ProdottiOrdineResponse(idOrdine, prodotti);
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il recupero dei prodotti dell'ordine", exception);
        }
    }

    public TotaleOrdineResponse getTotaleOrdine(long idUtente, long idOrdine) {
        validaIdOrdine(idOrdine);

        try {
            Ordine ordine = requireOrdine(idOrdine);
            requireProprietarioOrdine(ordine, idUtente);

            BigDecimal totale = ordineDAO.calculateTotale(idOrdine);
            return new TotaleOrdineResponse(idOrdine, totale);
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il calcolo del totale dell'ordine", exception);
        }
    }

    public TempoConsegnaOrdineResponse getTempoConsegnaOrdine(long idUtente, long idOrdine) {
        validaIdOrdine(idOrdine);

        try {
            Ordine ordine = requireOrdine(idOrdine);
            requireProprietarioOrdine(ordine, idUtente);

            int minuti = ordineDAO.calculateTempoPreparazione(idOrdine);
            return new TempoConsegnaOrdineResponse(idOrdine, minuti);
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il calcolo del tempo stimato dell'ordine", exception);
        }
    }

    public OrdineResponse annullaOrdine(long idUtente, long idOrdine) {
        validaIdOrdine(idOrdine);

        try {
            Ordine ordine = requireOrdine(idOrdine);
            requireProprietarioOrdine(ordine, idUtente);
            requireOrdineAnnullabile(ordine);

            Optional<Ordine> ordineAnnullato = ordineDAO.annulla(
                    idOrdine,
                    idUtente,
                    ordine.getStato(),
                    "Annullamento richiesto dal cliente"
            );
            if (ordineAnnullato.isEmpty()) {
                throw new IllegalStateException("Ordine non piu annullabile");
            }

            return toOrdineResponse(ordineAnnullato.get());
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante l'annullamento dell'ordine", exception);
        }
    }

    public OrdineResponse confermaOrdine(long idUtente, long idOrdine) {
        validaIdOrdine(idOrdine);

        try {
            Utente utente = requireUtenteAutenticato(idUtente);
            Ordine ordineCorrente = requireOrdine(idOrdine);
            requireAutorizzatoConferma(utente, ordineCorrente);

            if (ordineCorrente.getStato() != StatoOrdine.BOZZA) {
                throw new IllegalStateException("Solo un ordine in stato BOZZA puo essere confermato");
            }

            Optional<Ordine> ordineConfermato = ordineDAO.conferma(
                    idOrdine,
                    idUtente,
                    ordineCorrente.getStato()
            );
            if (ordineConfermato.isEmpty()) {
                throw new IllegalStateException("Ordine non piu confermabile");
            }

            Ordine ordineConfermatoValue = ordineConfermato.get();
            OrdineResponse response = toOrdineResponse(ordineConfermatoValue);
            emailNotificationService.notificaOrdineConfermato(ordineConfermatoValue.getId());
            return response;
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante la conferma dell'ordine", exception);
        }
    }

    /**
     * Aggiorna lo stato ordine dalla parte SWA ammettendo solo la prossima
     * transizione operativa prevista dal flusso di cucina/consegna.
     */
    public OrdineResponse aggiornaStatoOrdine(long idUtente,
                                              long idOrdine,
                                              AggiornaStatoOrdineRequest request) {
        validaIdOrdine(idOrdine);
        if (request == null || isBlank(request.getNuovoStato())) {
            throw new IllegalArgumentException("Nuovo stato obbligatorio");
        }

        StatoOrdine nuovoStato = parseStato(request.getNuovoStato());

        try {
            Utente utente = requireUtenteAutenticato(idUtente);
            requirePersonaleOProprietario(utente);

            Ordine ordineCorrente = requireOrdine(idOrdine);
            validaTransizioneOperativa(ordineCorrente.getStato(), nuovoStato);

            Optional<Ordine> ordineAggiornato = ordineDAO.aggiornaStato(
                    idOrdine,
                    idUtente,
                    ordineCorrente.getStato(),
                    nuovoStato
            );
            if (ordineAggiornato.isEmpty()) {
                throw new IllegalStateException("Ordine non piu aggiornabile");
            }

            Ordine ordineAggiornatoValue = ordineAggiornato.get();
            OrdineResponse response = toOrdineResponse(ordineAggiornatoValue);
            notificaOrdineInConsegnaSeNecessario(ordineAggiornatoValue);
            return response;
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante l'aggiornamento dello stato ordine", exception);
        }
    }

    public List<StaffOrdineResponse> getOrdiniStaff(long idUtente, String statoParam) {
        Utente utente = requireUtenteAutenticato(idUtente);
        requirePersonale(utente);

        StatoOrdine stato = parseStatoOptional(statoParam);
        if (stato != null && !isStatoOperativo(stato)) {
            throw new IllegalArgumentException("Stato non valido per area staff");
        }

        try {
            List<Ordine> ordini = ordineDAO.findByFilters(stato, null, null);
            if (stato == null) {
                ordini = filtraOrdiniOperativi(ordini);
            }

            return toStaffOrdiniResponse(ordini);
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il recupero degli ordini staff", exception);
        }
    }

    public StaffOrdineResponse getOrdineStaff(long idUtente, long idOrdine) {
        validaIdOrdine(idOrdine);

        try {
            Utente utente = requireUtenteAutenticato(idUtente);
            requirePersonale(utente);

            Ordine ordine = requireOrdine(idOrdine);
            return toStaffOrdineResponse(
                    ordine,
                    ordineDAO.countProdottiByOrdineIds(List.of(idOrdine))
            );
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il recupero del dettaglio ordine staff", exception);
        }
    }

    public List<StaffRigaOrdineResponse> getRigheOrdineStaff(long idUtente, long idOrdine) {
        validaIdOrdine(idOrdine);

        try {
            Utente utente = requireUtenteAutenticato(idUtente);
            requirePersonale(utente);
            requireOrdine(idOrdine);

            return toStaffRigheOrdineResponse(ordineDAO.findRigheByOrdineId(idOrdine));
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il recupero delle righe ordine staff", exception);
        }
    }

    public List<StoricoStatoOrdine> getStoricoOrdineStaff(long idUtente, long idOrdine) {
        validaIdOrdine(idOrdine);

        try {
            Utente utente = requireUtenteAutenticato(idUtente);
            requirePersonale(utente);
            requireOrdine(idOrdine);

            return ordineDAO.findStoricoByOrdineId(idOrdine);
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il recupero dello storico ordine staff", exception);
        }
    }

    /**
     * Avanza l'ordine dallo staff alla prossima fase operativa senza esporre al
     * form WE la scelta arbitraria dello stato successivo.
     */
    public OrdineResponse avanzaStatoOrdineStaff(long idUtente, long idOrdine) {
        validaIdOrdine(idOrdine);

        try {
            Utente utente = requireUtenteAutenticato(idUtente);
            requirePersonale(utente);

            Ordine ordine = requireOrdine(idOrdine);
            StatoOrdine nuovoStato = getProssimoStatoOperativo(ordine.getStato());
            if (nuovoStato == null) {
                throw new IllegalStateException("Ordine non modificabile nello stato " + ordine.getStato().name());
            }

            Optional<Ordine> ordineAggiornato = ordineDAO.aggiornaStato(
                    idOrdine,
                    idUtente,
                    ordine.getStato(),
                    nuovoStato
            );
            if (ordineAggiornato.isEmpty()) {
                throw new IllegalStateException("Ordine non più aggiornabile");
            }

            Ordine ordineAggiornatoValue = ordineAggiornato.get();
            OrdineResponse response = toOrdineResponse(ordineAggiornatoValue);
            notificaOrdineInConsegnaSeNecessario(ordineAggiornatoValue);
            return response;
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante l'avanzamento dello stato ordine staff", exception);
        }
    }

    public List<OwnerOrdineResponse> getOrdiniProprietario(long idUtente,
                                                           String statoParam,
                                                           String dalParam,
                                                           String alParam) {
        Utente utente = requireUtenteAutenticato(idUtente);
        requireProprietario(utente);

        StatoOrdine stato = parseStatoOptional(statoParam);
        if (stato == StatoOrdine.BOZZA) {
            throw new IllegalArgumentException("Stato BOZZA non disponibile nei filtri proprietario");
        }
        LocalDate dal = parseDataOptional(dalParam, "dal");
        LocalDate al = parseDataOptional(alParam, "al");
        if (dal != null && al != null && dal.isAfter(al)) {
            throw new IllegalArgumentException("dal non può essere successivo ad al");
        }

        try {
            return toOwnerOrdiniResponse(ordineDAO.findAllByFilters(stato, dal, al));
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il recupero degli ordini proprietario", exception);
        }
    }

    public OwnerOrdineResponse getOrdineProprietario(long idUtente, long idOrdine) {
        validaIdOrdine(idOrdine);

        try {
            Utente utente = requireUtenteAutenticato(idUtente);
            requireProprietario(utente);

            Ordine ordine = requireOrdine(idOrdine);
            return toOwnerOrdineResponse(
                    ordine,
                    ordineDAO.countProdottiByOrdineIds(List.of(idOrdine))
            );
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il recupero del dettaglio ordine proprietario", exception);
        }
    }

    public List<RigaOrdineResponse> getRigheOrdineProprietario(long idUtente, long idOrdine) {
        validaIdOrdine(idOrdine);

        try {
            Utente utente = requireUtenteAutenticato(idUtente);
            requireProprietario(utente);
            requireOrdine(idOrdine);

            return toRigheOrdineResponse(ordineDAO.findRigheByOrdineId(idOrdine));
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il recupero delle righe ordine proprietario", exception);
        }
    }

    public List<StoricoStatoOrdine> getStoricoOrdineProprietario(long idUtente, long idOrdine) {
        validaIdOrdine(idOrdine);

        try {
            Utente utente = requireUtenteAutenticato(idUtente);
            requireProprietario(utente);
            requireOrdine(idOrdine);

            return ordineDAO.findStoricoByOrdineId(idOrdine);
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il recupero dello storico ordine proprietario", exception);
        }
    }

    public List<OrdineResponse> getOrdiniOperativi(long idUtente,
                                                   String statoParam,
                                                   String dataDaParam,
                                                   String dataAParam) {
        Utente utente = requireUtenteAutenticato(idUtente);
        requirePersonaleOProprietario(utente);

        StatoOrdine stato = parseStatoOptional(statoParam);
        LocalDate dataDa = parseDataOptional(dataDaParam, "dataDa");
        LocalDate dataA = parseDataOptional(dataAParam, "dataA");
        if (dataDa != null && dataA != null && dataDa.isAfter(dataA)) {
            throw new IllegalArgumentException("dataDa non puo essere successiva a dataA");
        }

        try {
            return toOrdiniResponse(ordineDAO.findByFilters(stato, dataDa, dataA));
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il recupero degli ordini", exception);
        }
    }

    public List<OrdineResponse> getOrdiniCliente(long idUtente,
                                                 String statoParam,
                                                 String dataDaParam,
                                                 String dataAParam) {
        requireCliente(idUtente);

        StatoOrdine stato = parseStatoOptional(statoParam);
        LocalDate dataDa = parseDataOptional(dataDaParam, "dataDa");
        LocalDate dataA = parseDataOptional(dataAParam, "dataA");
        if (dataDa != null && dataA != null && dataDa.isAfter(dataA)) {
            throw new IllegalArgumentException("dataDa non puo essere successiva a dataA");
        }

        try {
            return toOrdiniResponse(ordineDAO.findByClienteIdAndFilters(idUtente, stato, dataDa, dataA));
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il recupero degli ordini cliente", exception);
        }
    }

    public List<OrdineResponse> getOrdiniUtente(long idUtenteAutenticato, long idUtenteRichiesto) {
        if (idUtenteRichiesto <= 0) {
            throw new IllegalArgumentException("Id utente non valido");
        }

        Utente utenteAutenticato = requireUtenteAutenticato(idUtenteAutenticato);
        if (RUOLO_CLIENTE.equals(utenteAutenticato.getRuolo()) && utenteAutenticato.getId() != idUtenteRichiesto) {
            throw new SecurityException("Utente non autorizzato sugli ordini richiesti");
        }
        if (!RUOLO_CLIENTE.equals(utenteAutenticato.getRuolo())
                && !RUOLO_PROPRIETARIO.equals(utenteAutenticato.getRuolo())) {
            throw new SecurityException("Operazione consentita solo a CLIENTE proprietario o PROPRIETARIO");
        }

        if (RUOLO_PROPRIETARIO.equals(utenteAutenticato.getRuolo())) {
            requireUtenteEsistente(idUtenteRichiesto);
        }

        try {
            return toOrdiniResponse(ordineDAO.findByClienteId(idUtenteRichiesto));
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il recupero degli ordini dell'utente", exception);
        }
    }

    public List<StoricoStatoOrdine> getStoricoOrdineCliente(long idUtente, long idOrdine) {
        validaIdOrdine(idOrdine);

        try {
            Ordine ordine = requireOrdine(idOrdine);
            requireProprietarioOrdine(ordine, idUtente);
            return ordineDAO.findStoricoByOrdineId(idOrdine);
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il recupero dello storico ordine", exception);
        }
    }

    public OperatoriOrdineResponse getOperatoriOrdine(long idUtente, long idOrdine) {
        validaIdOrdine(idOrdine);

        try {
            Utente utente = requireUtenteAutenticato(idUtente);
            requirePersonaleOProprietario(utente);
            requireOrdine(idOrdine);

            return toOperatoriOrdineResponse(idOrdine, ordineDAO.findStoricoOperatoriByOrdineId(idOrdine));
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il recupero degli operatori dell'ordine", exception);
        }
    }

    private Utente requireCliente(long idUtente) {
        try {
            Optional<Utente> utente = utenteDAO.findById(Math.toIntExact(idUtente));
            if (utente.isEmpty() || !RUOLO_CLIENTE.equals(utente.get().getRuolo())) {
                throw new SecurityException("Operazione consentita solo a utenti CLIENTE");
            }

            return utente.get();
        } catch (ArithmeticException exception) {
            throw new SecurityException("Utente non autorizzato");
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il recupero dell'utente", exception);
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

    private Utente requireUtenteEsistente(long idUtente) {
        try {
            return utenteDAO.findById(Math.toIntExact(idUtente))
                    .orElseThrow(() -> new NoSuchElementException("Utente non trovato"));
        } catch (ArithmeticException exception) {
            throw new NoSuchElementException("Utente non trovato");
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il recupero dell'utente", exception);
        }
    }

    private Ordine requireOrdine(long idOrdine) throws SQLException {
        return ordineDAO.findById(idOrdine)
                .orElseThrow(() -> new NoSuchElementException("Ordine non trovato"));
    }

    private void requireProprietarioOrdine(Ordine ordine, long idUtente) {
        requireCliente(idUtente);
        if (ordine.getIdCliente() != idUtente) {
            throw new SecurityException("Utente non autorizzato sull'ordine");
        }
    }

    private void requireOrdineModificabile(Ordine ordine) {
        if (!ordine.getStato().isModificabile()) {
            throw new IllegalStateException("Ordine non modificabile nello stato " + ordine.getStato().name());
        }
    }

    private void requireOrdineAnnullabile(Ordine ordine) {
        if (!ordine.getStato().isAnnullabile()) {
            throw new IllegalStateException("Ordine non annullabile nello stato " + ordine.getStato().name());
        }
    }

    private void requireAutorizzatoConferma(Utente utente, Ordine ordine) {
        if (RUOLO_PROPRIETARIO.equals(utente.getRuolo())) {
            return;
        }
        if (RUOLO_CLIENTE.equals(utente.getRuolo()) && ordine.getIdCliente() == utente.getId()) {
            return;
        }

        throw new SecurityException("Utente non autorizzato a confermare l'ordine");
    }

    private void requirePersonaleOProprietario(Utente utente) {
        if (RUOLO_PERSONALE.equals(utente.getRuolo()) || RUOLO_PROPRIETARIO.equals(utente.getRuolo())) {
            return;
        }

        throw new SecurityException("Operazione consentita solo a PERSONALE o PROPRIETARIO");
    }

    private void requirePersonale(Utente utente) {
        if (RUOLO_PERSONALE.equals(utente.getRuolo())) {
            return;
        }

        throw new SecurityException("Operazione consentita solo a PERSONALE");
    }

    private void requireProprietario(Utente utente) {
        if (RUOLO_PROPRIETARIO.equals(utente.getRuolo())) {
            return;
        }

        throw new SecurityException("Operazione consentita solo a PROPRIETARIO");
    }

    private void validaDatiConsegnaCliente(Utente cliente) {
        if (isBlank(cliente.getIndirizzo()) || isBlank(cliente.getCitta()) || isBlank(cliente.getTelefono())) {
            throw new IllegalArgumentException("Dati di consegna cliente incompleti");
        }
    }

    /**
     * Controlla che l'orario richiesto rispetti il tempo minimo di preparazione
     * del carrello e non superi l'orario di chiusura configurato.
     */
    private void validaOrarioConsegnaRichiesto(LocalDateTime orarioConsegnaRichiesto,
                                               List<RigaOrdineDaCreare> righe) {
        if (orarioConsegnaRichiesto == null) {
            throw new IllegalArgumentException("Orario di consegna richiesto obbligatorio");
        }

        int minutiConsegnaStimati = calcolaTempoConsegnaStimato(righe);
        LocalDateTime orarioMinimo = calcolaOrarioConsegnaMinimo(minutiConsegnaStimati);
        if (orarioConsegnaRichiesto.isBefore(orarioMinimo)) {
            throw new IllegalArgumentException(
                    "L'orario di consegna richiesto deve essere almeno "
                            + FORMATO_ORARIO_UTENTE.format(orarioMinimo)
            );
        }

        if (orarioConsegnaRichiesto.toLocalTime().isAfter(ORARIO_CHIUSURA_ATTIVITA)) {
            throw new IllegalArgumentException(
                    "L'orario di consegna richiesto non puo essere oltre le "
                            + ORARIO_CHIUSURA_ATTIVITA
            );
        }
    }

    private LocalDateTime calcolaOrarioConsegnaMinimo(int minutiConsegnaStimati) {
        int minuti = Math.max(0, minutiConsegnaStimati);
        LocalDateTime orarioMinimo = LocalDateTime.now(ZONA_ORARIA_CONSEGNA).plusMinutes(minuti);
        LocalDateTime orarioMinimoAlMinuto = orarioMinimo.withSecond(0).withNano(0);
        if (orarioMinimo.getSecond() > 0 || orarioMinimo.getNano() > 0) {
            return orarioMinimoAlMinuto.plusMinutes(1);
        }
        return orarioMinimoAlMinuto;
    }

    private int calcolaTempoConsegnaStimato(List<RigaOrdineDaCreare> righe) {
        if (righe == null || righe.isEmpty()) {
            return 0;
        }

        int minuti = 0;
        for (RigaOrdineDaCreare riga : righe) {
            if (riga == null || riga.getProdotto() == null) {
                continue;
            }
            minuti += riga.getProdotto().getMinutiPreparazione() * riga.getQuantita();
        }
        return minuti;
    }

    private void validaIdOrdine(long idOrdine) {
        if (idOrdine <= 0) {
            throw new IllegalArgumentException("Id ordine non valido");
        }
    }

    private void validaRichiestaAggiuntaProdotto(AggiungiProdottoOrdineRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Richiesta non valida");
        }
        if (request.getIdProdotto() <= 0) {
            throw new IllegalArgumentException("Id prodotto non valido");
        }
        if (request.getQuantita() <= 0) {
            throw new IllegalArgumentException("La quantita deve essere maggiore di zero");
        }
        for (Long idCaratteristica : request.getCaratteristiche()) {
            if (idCaratteristica == null || idCaratteristica <= 0) {
                throw new IllegalArgumentException("Id caratteristica non valido");
            }
        }
    }

    private int toProdottoDaoId(long idProdotto) {
        if (idProdotto > Integer.MAX_VALUE) {
            throw new NoSuchElementException("Prodotto non trovato");
        }

        return (int) idProdotto;
    }

    private StatoOrdine parseStatoOptional(String statoParam) {
        String normalized = normalize(statoParam);
        if (normalized == null) {
            return null;
        }

        return parseStato(normalized);
    }

    private StatoOrdine parseStato(String statoParam) {
        String normalized = normalize(statoParam);
        if (normalized == null) {
            throw new IllegalArgumentException("Stato non valido");
        }

        try {
            return StatoOrdine.valueOf(normalized.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Stato non valido");
        }
    }

    private LocalDate parseDataOptional(String dataParam, String nomeParametro) {
        String normalized = normalize(dataParam);
        if (normalized == null) {
            return null;
        }

        try {
            return LocalDate.parse(normalized);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Parametro " + nomeParametro + " non valido, usare formato YYYY-MM-DD");
        }
    }

    private void validaTransizioneOperativa(StatoOrdine statoCorrente, StatoOrdine nuovoStato) {
        StatoOrdine prossimoStato = getProssimoStatoOperativo(statoCorrente);
        if (prossimoStato == null) {
            throw new IllegalStateException("Ordine non modificabile nello stato " + statoCorrente.name());
        }
        if (nuovoStato != prossimoStato) {
            throw new IllegalArgumentException(
                    "Transizione non valida: da " + statoCorrente.name() + " e consentito solo " + prossimoStato.name()
            );
        }
    }

    private StatoOrdine getProssimoStatoOperativo(StatoOrdine statoCorrente) {
        return switch (statoCorrente) {
            case INSERITO -> StatoOrdine.IN_PREPARAZIONE;
            case IN_PREPARAZIONE -> StatoOrdine.PRONTO;
            case PRONTO -> StatoOrdine.IN_CONSEGNA;
            case IN_CONSEGNA -> StatoOrdine.CONSEGNATO;
            default -> null;
        };
    }

    /**
     * Le notifiche email sono best-effort: un problema SMTP non deve bloccare
     * la transizione di stato gia completata.
     */
    private void notificaOrdineInConsegnaSeNecessario(Ordine ordine) {
        if (ordine != null && ordine.getStato() == StatoOrdine.IN_CONSEGNA) {
            emailNotificationService.notificaOrdineInConsegna(ordine.getId());
        }
    }

    /**
     * Valida la configurazione prodotto: caratteristiche esistenti, nessun
     * duplicato, una sola scelta per gruppo e gruppi obbligatori coperti.
     */
    private List<Caratteristica> validaCaratteristicheProdotto(long idProdotto,
                                                               List<Long> idCaratteristiche) throws SQLException {
        List<Long> idCaratteristicheRichieste = idCaratteristiche != null
                ? idCaratteristiche
                : Collections.emptyList();

        Set<Long> idRichiesti = new LinkedHashSet<>(idCaratteristicheRichieste);
        if (idRichiesti.size() != idCaratteristicheRichieste.size()) {
            throw new IllegalArgumentException("Caratteristiche duplicate nella richiesta");
        }

        Map<Long, Caratteristica> caratteristicheById = new LinkedHashMap<>();
        for (Caratteristica caratteristica : caratteristicaDAO.findByProdottoId(Math.toIntExact(idProdotto))) {
            caratteristicheById.put(caratteristica.getId(), caratteristica);
        }

        List<Caratteristica> selezionate = new ArrayList<>();
        Set<Long> gruppiSelezionati = new HashSet<>();

        for (Long idCaratteristica : idRichiesti) {
            Caratteristica caratteristica = caratteristicheById.get(idCaratteristica);
            if (caratteristica == null) {
                throw new NoSuchElementException("Caratteristica non trovata per il prodotto");
            }

            Long idGruppo = caratteristica.getIdGruppoCaratteristiche();
            if (idGruppo != null && !gruppiSelezionati.add(idGruppo)) {
                throw new IllegalArgumentException("Una sola caratteristica puo essere scelta per ciascun gruppo");
            }

            selezionate.add(caratteristica);
        }

        List<GruppoCaratteristiche> gruppi =
                gruppoCaratteristicheDAO.findActiveByProdottoId(Math.toIntExact(idProdotto));
        for (GruppoCaratteristiche gruppo : gruppi) {
            if (gruppo.isObbligatorio() && !gruppiSelezionati.contains(gruppo.getId())) {
                throw new IllegalArgumentException(
                        "Seleziona una caratteristica per il gruppo obbligatorio: " + gruppo.getNome() + "."
                );
            }
        }

        return selezionate;
    }

    private OrdineResponse toOrdineResponse(Ordine ordine) {
        return new OrdineResponse(
                ordine.getId(),
                ordine.getIdCliente(),
                ordine.getStato().name(),
                ordine.getPrezzoTotale(),
                ordine.getMinutiConsegnaStimati(),
                ordine.getIndirizzoConsegnaSnapshot(),
                ordine.getCittaConsegnaSnapshot(),
                ordine.getCapConsegnaSnapshot(),
                ordine.getTelefonoConsegnaSnapshot(),
                ordine.getCreatoIl(),
                ordine.getConfermatoIl(),
                ordine.getAnnullatoIl()
        );
    }

    private List<OrdineResponse> toOrdiniResponse(List<Ordine> ordini) {
        if (ordini == null || ordini.isEmpty()) {
            return Collections.emptyList();
        }

        List<OrdineResponse> response = new ArrayList<>();
        for (Ordine ordine : ordini) {
            response.add(toOrdineResponse(ordine));
        }
        return response;
    }

    private List<StaffOrdineResponse> toStaffOrdiniResponse(List<Ordine> ordini) throws SQLException {
        if (ordini == null || ordini.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> idOrdini = new ArrayList<>();
        for (Ordine ordine : ordini) {
            idOrdini.add(ordine.getId());
        }

        Map<Long, Integer> numeroProdottiByOrdineId = ordineDAO.countProdottiByOrdineIds(idOrdini);
        List<StaffOrdineResponse> response = new ArrayList<>();
        for (Ordine ordine : ordini) {
            response.add(toStaffOrdineResponse(ordine, numeroProdottiByOrdineId));
        }
        return response;
    }

    private List<OwnerOrdineResponse> toOwnerOrdiniResponse(List<Ordine> ordini) throws SQLException {
        if (ordini == null || ordini.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> idOrdini = new ArrayList<>();
        for (Ordine ordine : ordini) {
            idOrdini.add(ordine.getId());
        }

        Map<Long, Integer> numeroProdottiByOrdineId = ordineDAO.countProdottiByOrdineIds(idOrdini);
        List<OwnerOrdineResponse> response = new ArrayList<>();
        for (Ordine ordine : ordini) {
            response.add(toOwnerOrdineResponse(ordine, numeroProdottiByOrdineId));
        }
        return response;
    }

    private OwnerOrdineResponse toOwnerOrdineResponse(Ordine ordine,
                                                      Map<Long, Integer> numeroProdottiByOrdineId)
            throws SQLException {
        Optional<Utente> cliente = findUtenteByIdSafe(ordine.getIdCliente());

        return new OwnerOrdineResponse(
                ordine.getId(),
                ordine.getIdCliente(),
                formatCliente(cliente, ordine.getIdCliente()),
                cliente.map(Utente::getUsername).orElse(null),
                ordine.getStato().name(),
                ordine.getPrezzoTotale(),
                ordine.getMinutiConsegnaStimati(),
                ordine.getCreatoIl(),
                ordine.getConfermatoIl(),
                ordine.getOrarioConsegnaRichiesto(),
                ordine.getIndirizzoConsegnaSnapshot(),
                ordine.getCittaConsegnaSnapshot(),
                ordine.getCapConsegnaSnapshot(),
                ordine.getTelefonoConsegnaSnapshot(),
                numeroProdottiByOrdineId.getOrDefault(ordine.getId(), 0),
                formatOperatoriRiepilogo(ordine.getId())
        );
    }

    private StaffOrdineResponse toStaffOrdineResponse(Ordine ordine,
                                                      Map<Long, Integer> numeroProdottiByOrdineId)
            throws SQLException {
        Optional<Utente> cliente = findUtenteByIdSafe(ordine.getIdCliente());
        StatoOrdine prossimoStato = getProssimoStatoOperativo(ordine.getStato());

        return new StaffOrdineResponse(
                ordine.getId(),
                ordine.getIdCliente(),
                formatCliente(cliente, ordine.getIdCliente()),
                cliente.map(Utente::getUsername).orElse(null),
                ordine.getStato().name(),
                isStatoOperativo(ordine.getStato()),
                ordine.getPrezzoTotale(),
                ordine.getMinutiConsegnaStimati(),
                ordine.getCreatoIl(),
                ordine.getConfermatoIl(),
                ordine.getOrarioConsegnaRichiesto(),
                ordine.getIndirizzoConsegnaSnapshot(),
                ordine.getCittaConsegnaSnapshot(),
                ordine.getCapConsegnaSnapshot(),
                ordine.getTelefonoConsegnaSnapshot(),
                numeroProdottiByOrdineId.getOrDefault(ordine.getId(), 0),
                prossimoStato != null ? prossimoStato.name() : null
        );
    }

    private List<StaffRigaOrdineResponse> toStaffRigheOrdineResponse(List<RigaOrdine> righeOrdine)
            throws SQLException {
        if (righeOrdine == null || righeOrdine.isEmpty()) {
            return Collections.emptyList();
        }

        List<StaffRigaOrdineResponse> response = new ArrayList<>();
        for (RigaOrdine rigaOrdine : righeOrdine) {
            response.add(toStaffRigaOrdineResponse(rigaOrdine));
        }
        return response;
    }

    private StaffRigaOrdineResponse toStaffRigaOrdineResponse(RigaOrdine rigaOrdine) throws SQLException {
        RigaOrdineResponse rigaBase = toRigaOrdineResponse(rigaOrdine);
        String descrizionePreparazione = null;
        List<IngredienteProdottoResponse> ingredienti = Collections.emptyList();

        try {
            int idProdotto = toProdottoDaoId(rigaOrdine.getIdProdotto());
            descrizionePreparazione = prodottoDAO.findById(idProdotto)
                    .map(Prodotto::getDescrizionePreparazione)
                    .orElse(null);
            ingredienti = toIngredientiProdottoResponse(ingredienteDAO.findByProdottoId(idProdotto));
        } catch (NoSuchElementException ignored) {
            // Gli snapshot della riga restano mostrabili anche se il prodotto non e piu nel catalogo attivo.
        }

        return new StaffRigaOrdineResponse(
                rigaBase.getIdRigaOrdine(),
                rigaBase.getIdProdotto(),
                rigaBase.getNomeProdotto(),
                rigaBase.getQuantita(),
                rigaBase.getPrezzoBase(),
                rigaBase.getMinutiPreparazione(),
                rigaBase.getCaratteristiche(),
                rigaBase.getSubtotaleRiga(),
                ingredienti,
                descrizionePreparazione
        );
    }

    private RigaOrdineResponse toRigaOrdineResponse(RigaOrdine rigaOrdine) {
        List<CaratteristicaOrdineResponse> caratteristiche = new ArrayList<>();
        for (CaratteristicaRigaOrdine caratteristica : rigaOrdine.getCaratteristiche()) {
            caratteristiche.add(new CaratteristicaOrdineResponse(
                    caratteristica.getIdCaratteristica(),
                    caratteristica.getNomeCaratteristicaSnapshot(),
                    caratteristica.getDifferenzaPrezzoSnapshot()
            ));
        }

        return new RigaOrdineResponse(
                rigaOrdine.getId(),
                rigaOrdine.getIdProdotto(),
                rigaOrdine.getNomeProdottoSnapshot(),
                rigaOrdine.getQuantita(),
                rigaOrdine.getPrezzoBaseSnapshot(),
                rigaOrdine.getMinutiPreparazioneSnapshot(),
                caratteristiche,
                rigaOrdine.getTotaleRiga()
        );
    }

    private List<RigaOrdineResponse> toRigheOrdineResponse(List<RigaOrdine> righeOrdine) {
        if (righeOrdine == null || righeOrdine.isEmpty()) {
            return Collections.emptyList();
        }

        List<RigaOrdineResponse> response = new ArrayList<>();
        for (RigaOrdine rigaOrdine : righeOrdine) {
            response.add(toRigaOrdineResponse(rigaOrdine));
        }
        return response;
    }

    private List<IngredienteProdottoResponse> toIngredientiProdottoResponse(List<Ingrediente> ingredienti) {
        if (ingredienti == null || ingredienti.isEmpty()) {
            return Collections.emptyList();
        }

        List<IngredienteProdottoResponse> response = new ArrayList<>();
        for (Ingrediente ingrediente : ingredienti) {
            response.add(new IngredienteProdottoResponse(
                    ingrediente.getId(),
                    ingrediente.getNome(),
                    ingrediente.getQuantita(),
                    ingrediente.getUnitaMisura()
            ));
        }
        return response;
    }

    private OperatoriOrdineResponse toOperatoriOrdineResponse(long idOrdine,
                                                              List<StoricoStatoOrdine> storico) {
        Map<Long, OperatoreOrdineResponse> operatoriById = new LinkedHashMap<>();

        for (StoricoStatoOrdine cambio : storico) {
            Long idUtente = cambio.getIdUtenteModifica();
            if (idUtente == null) {
                continue;
            }

            OperatoreOrdineResponse operatore = operatoriById.computeIfAbsent(
                    idUtente,
                    id -> new OperatoreOrdineResponse(
                            id,
                            cambio.getUsernameUtenteModifica(),
                            cambio.getNomeUtenteModifica(),
                            cambio.getCognomeUtenteModifica(),
                            cambio.getRuoloUtenteModifica(),
                            new ArrayList<>()
                    )
            );

            operatore.getCambiStato().add(new CambioStatoOperatoreResponse(
                    cambio.getStatoPrecedente() != null ? cambio.getStatoPrecedente().name() : null,
                    cambio.getStatoNuovo().name(),
                    cambio.getModificatoIl()
            ));
        }

        return new OperatoriOrdineResponse(idOrdine, new ArrayList<>(operatoriById.values()));
    }

    private String formatOperatoriRiepilogo(long idOrdine) throws SQLException {
        List<StoricoStatoOrdine> storicoOperatori = ordineDAO.findStoricoOperatoriByOrdineId(idOrdine);
        if (storicoOperatori == null || storicoOperatori.isEmpty()) {
            return "Nessun operatore";
        }

        Set<String> operatori = new LinkedHashSet<>();
        for (StoricoStatoOrdine cambio : storicoOperatori) {
            operatori.add(formatOperatore(cambio));
        }

        String cambiLabel = storicoOperatori.size() == 1 ? "1 cambio" : storicoOperatori.size() + " cambi";
        return String.join(", ", operatori) + " · " + cambiLabel;
    }

    private String formatOperatore(StoricoStatoOrdine cambio) {
        String nomeCompleto = ((cambio.getNomeUtenteModifica() != null ? cambio.getNomeUtenteModifica() : "") + " "
                + (cambio.getCognomeUtenteModifica() != null ? cambio.getCognomeUtenteModifica() : "")).trim();
        if (!nomeCompleto.isBlank()) {
            return nomeCompleto;
        }
        if (!isBlank(cambio.getUsernameUtenteModifica())) {
            return cambio.getUsernameUtenteModifica();
        }
        if (cambio.getIdUtenteModifica() != null) {
            return "Utente #" + cambio.getIdUtenteModifica();
        }

        return "Sistema";
    }

    private List<Ordine> filtraOrdiniOperativi(List<Ordine> ordini) {
        if (ordini == null || ordini.isEmpty()) {
            return Collections.emptyList();
        }

        List<Ordine> operativi = new ArrayList<>();
        for (Ordine ordine : ordini) {
            if (isStatoOperativo(ordine.getStato())) {
                operativi.add(ordine);
            }
        }
        return operativi;
    }

    private boolean isStatoOperativo(StatoOrdine stato) {
        return getProssimoStatoOperativo(stato) != null;
    }

    private Optional<Utente> findUtenteByIdSafe(long idUtente) throws SQLException {
        try {
            return utenteDAO.findById(Math.toIntExact(idUtente));
        } catch (ArithmeticException exception) {
            return Optional.empty();
        }
    }

    private String formatCliente(Optional<Utente> cliente, long idCliente) {
        if (cliente.isEmpty()) {
            return "Cliente #" + idCliente;
        }

        String nomeCompleto = ((cliente.get().getNome() != null ? cliente.get().getNome() : "") + " "
                + (cliente.get().getCognome() != null ? cliente.get().getCognome() : "")).trim();
        if (!nomeCompleto.isBlank()) {
            return nomeCompleto;
        }
        if (!isBlank(cliente.get().getUsername())) {
            return cliente.get().getUsername();
        }

        return "Cliente #" + idCliente;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
