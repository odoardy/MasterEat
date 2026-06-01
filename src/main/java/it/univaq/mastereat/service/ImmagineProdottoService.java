package it.univaq.mastereat.service;

import it.univaq.mastereat.dao.ImmagineProdottoDAO;
import it.univaq.mastereat.dao.impl.ImmagineProdottoDAOImpl;
import it.univaq.mastereat.dto.web.owner.OwnerImmagineProdottoResponse;
import it.univaq.mastereat.model.ImmagineProdotto;
import it.univaq.mastereat.util.ProductImageStorage;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Service owner per la gestione delle immagini prodotto.
 *
 * Valida file e autorizzazione, salva il binario su file system e registra i
 * metadati nel database mantenendo coerente l'immagine principale del prodotto.
 */
public class ImmagineProdottoService {

    public static final long MAX_IMAGE_SIZE_BYTES = 3L * 1024L * 1024L; // 3 MB

    private static final Logger LOGGER = Logger.getLogger(ImmagineProdottoService.class.getName());
    private static final Set<String> TIPI_CONTENUTO_AMMESSI = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );
    private static final int NOME_FILE_ORIGINALE_MAX_LENGTH = 255;
    private static final int TESTO_ALTERNATIVO_MAX_LENGTH = 255;

    private final ImmagineProdottoDAO immagineProdottoDAO;
    private final ProdottoService prodottoService;

    public ImmagineProdottoService() {
        this.immagineProdottoDAO = new ImmagineProdottoDAOImpl();
        this.prodottoService = new ProdottoService();
    }

    public List<OwnerImmagineProdottoResponse> getImmaginiProdottoProprietario(long idProprietario,
                                                                               int idProdotto) {
        requireProdottoProprietario(idProprietario, idProdotto);

        try {
            return toOwnerImmagini(immagineProdottoDAO.findByProdottoId(idProdotto));
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante il recupero delle immagini prodotto", exception);
        }
    }

    /**
     * Valida MIME type e dimensione, salva il file e registra i metadati. Se la
     * persistenza su database fallisce, il file appena scritto viene rimosso.
     */
    public OwnerImmagineProdottoResponse caricaImmagineProdotto(long idProprietario,
                                                                int idProdotto,
                                                                Part filePart,
                                                                String testoAlternativo,
                                                                boolean principale) throws IOException {
        requireProdottoProprietario(idProprietario, idProdotto);

        String tipoContenuto = validaFile(filePart);
        String nomeFileOriginale = normalizeNomeFile(filePart.getSubmittedFileName());
        String testoAlternativoNormalizzato = normalizeOptional(testoAlternativo);
        validateLength(testoAlternativoNormalizzato, "Testo alternativo", TESTO_ALTERNATIVO_MAX_LENGTH);

        ProductImageStorage.StoredImage storedImage = ProductImageStorage.store(
                idProdotto,
                filePart,
                nomeFileOriginale,
                tipoContenuto
        );

        try {
            long immaginiEsistenti = immagineProdottoDAO.countByProdottoId(idProdotto);
            boolean principaleEffettiva = principale || immaginiEsistenti == 0;
            ImmagineProdotto immagine = immagineProdottoDAO.create(
                    idProdotto,
                    storedImage.getNomeFileOriginale(),
                    storedImage.getNomeFileSalvato(),
                    storedImage.getPercorsoPubblico(),
                    tipoContenuto,
                    filePart.getSize(),
                    testoAlternativoNormalizzato,
                    toOrdineVisualizzazione(immaginiEsistenti),
                    principaleEffettiva
            );
            return toOwnerImmagine(immagine);
        } catch (SQLException exception) {
            ProductImageStorage.deleteQuietly(storedImage.getPercorsoPubblico(), LOGGER);
            throw new RuntimeException("Errore durante il salvataggio dell'immagine prodotto", exception);
        } catch (RuntimeException exception) {
            ProductImageStorage.deleteQuietly(storedImage.getPercorsoPubblico(), LOGGER);
            throw exception;
        }
    }

    public void impostaImmaginePrincipale(long idProprietario, int idProdotto, long idImmagine) {
        requireProdottoProprietario(idProprietario, idProdotto);
        validaIdImmagine(idImmagine);

        try {
            boolean aggiornata = immagineProdottoDAO.setPrincipale(idProdotto, idImmagine);
            if (!aggiornata) {
                throw new NoSuchElementException("Immagine non trovata per il prodotto");
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante l'aggiornamento dell'immagine principale", exception);
        }
    }

    /**
     * Rimuove prima il record applicativo e poi elimina il file associato in
     * modalita best-effort, lasciando al DAO il ripristino dell'immagine
     * principale quando necessario.
     */
    public void rimuoviImmagineProdotto(long idProprietario, int idProdotto, long idImmagine) {
        requireProdottoProprietario(idProprietario, idProdotto);
        validaIdImmagine(idImmagine);

        try {
            ImmagineProdotto immagine = immagineProdottoDAO.deleteByProdottoIdAndId(idProdotto, idImmagine)
                    .orElseThrow(() -> new NoSuchElementException("Immagine non trovata per il prodotto"));
            ProductImageStorage.deleteQuietly(immagine.getPercorsoFile(), LOGGER);
        } catch (SQLException exception) {
            throw new RuntimeException("Errore durante la rimozione dell'immagine prodotto", exception);
        }
    }

    private void requireProdottoProprietario(long idProprietario, int idProdotto) {
        prodottoService.getProdottoProprietarioById(idProprietario, idProdotto)
                .orElseThrow(() -> new NoSuchElementException("Prodotto non trovato"));
    }

    private String validaFile(Part filePart) {
        if (filePart == null || filePart.getSize() == 0) {
            throw new IllegalArgumentException("Seleziona un file immagine da caricare.");
        }
        if (filePart.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new IllegalArgumentException("Il file immagine deve pesare al massimo 3 MB.");
        }

        String tipoContenuto = normalizeOptional(filePart.getContentType());
        if (tipoContenuto == null) {
            throw new IllegalArgumentException("Tipo file non riconosciuto.");
        }

        tipoContenuto = tipoContenuto.toLowerCase(Locale.ROOT);
        if (!TIPI_CONTENUTO_AMMESSI.contains(tipoContenuto)) {
            throw new IllegalArgumentException("Formato immagine non supportato.");
        }

        return tipoContenuto;
    }

    private String normalizeNomeFile(String nomeFile) {
        String normalized = normalizeOptional(nomeFile);
        if (normalized == null) {
            return "immagine";
        }

        normalized = normalized.replace('\\', '/');
        int slashIndex = normalized.lastIndexOf('/');
        if (slashIndex >= 0) {
            normalized = normalized.substring(slashIndex + 1);
        }
        normalized = normalized.replace("\r", "").replace("\n", "").trim();
        if (normalized.isBlank()) {
            normalized = "immagine";
        }
        if (normalized.length() > NOME_FILE_ORIGINALE_MAX_LENGTH) {
            normalized = normalized.substring(0, NOME_FILE_ORIGINALE_MAX_LENGTH);
        }
        return normalized;
    }

    private int toOrdineVisualizzazione(long immaginiEsistenti) {
        if (immaginiEsistenti >= Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) immaginiEsistenti;
    }

    private void validaIdImmagine(long idImmagine) {
        if (idImmagine <= 0) {
            throw new IllegalArgumentException("Id immagine non valido.");
        }
    }

    private void validateLength(String value, String label, int maxLength) {
        if (value != null && value.length() > maxLength) {
            throw new IllegalArgumentException(label + " deve contenere al massimo " + maxLength + " caratteri.");
        }
    }

    private List<OwnerImmagineProdottoResponse> toOwnerImmagini(List<ImmagineProdotto> immagini) {
        if (immagini == null || immagini.isEmpty()) {
            return Collections.emptyList();
        }

        List<OwnerImmagineProdottoResponse> response = new ArrayList<>();
        for (ImmagineProdotto immagine : immagini) {
            response.add(toOwnerImmagine(immagine));
        }
        return response;
    }

    private OwnerImmagineProdottoResponse toOwnerImmagine(ImmagineProdotto immagine) {
        return new OwnerImmagineProdottoResponse(
                immagine.getId(),
                immagine.getIdProdotto(),
                toPublicImageUrl(immagine.getPercorsoFile()),
                immagine.getNomeFileOriginale(),
                immagine.getNomeFileSalvato(),
                immagine.getTipoContenuto(),
                immagine.getDimensioneByte(),
                immagine.getTestoAlternativo(),
                immagine.getOrdineVisualizzazione(),
                immagine.isPrincipale(),
                immagine.getCaricataIl()
        );
    }

    private String toPublicImageUrl(String percorsoFile) {
        String percorsoNormalizzato = normalizeOptional(percorsoFile);
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

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
