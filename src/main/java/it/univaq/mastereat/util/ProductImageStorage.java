package it.univaq.mastereat.util;

import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gestisce la persistenza su file system delle immagini prodotto.
 *
 * Normalizza i percorsi di upload, genera nomi file non prevedibili e risolve
 * solo URL pubblici interni a {@code /uploads/prodotti}, prevenendo path
 * traversal quando i file vengono serviti dalla servlet WE.
 */
public final class ProductImageStorage {

    public static final String PUBLIC_PREFIX = "/uploads/prodotti";

    private static final String UPLOAD_DIRECTORY_PROPERTY = "mastereat.uploads.products.dir";
    private static final Path DEFAULT_BASE_DIRECTORY = Paths.get(
            System.getProperty("user.home"),
            "mastereat-uploads",
            "products"
    );
    private static final Path BASE_DIRECTORY = Paths.get(
            System.getProperty(UPLOAD_DIRECTORY_PROPERTY, DEFAULT_BASE_DIRECTORY.toString())
    ).toAbsolutePath().normalize();

    private ProductImageStorage() {
    }

    public static Path getBaseDirectory() {
        return BASE_DIRECTORY;
    }

    public static StoredImage store(int idProdotto,
                                    Part filePart,
                                    String nomeFileOriginale,
                                    String tipoContenuto) throws IOException {
        Path productDirectory = productDirectory(idProdotto);
        Files.createDirectories(productDirectory);

        String nomeFileSalvato = UUID.randomUUID() + extensionFor(tipoContenuto);
        Path target = productDirectory.resolve(nomeFileSalvato).normalize();
        if (!target.startsWith(productDirectory)) {
            throw new IOException("Percorso immagine non valido");
        }

        try (InputStream inputStream = filePart.getInputStream()) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        }

        String percorsoPubblico = PUBLIC_PREFIX + "/" + idProdotto + "/" + nomeFileSalvato;
        return new StoredImage(nomeFileOriginale, nomeFileSalvato, percorsoPubblico, target);
    }

    public static Optional<Path> resolveForServing(String pathInfo) {
        if (pathInfo == null || pathInfo.isBlank()) {
            return Optional.empty();
        }

        String normalizedPath = pathInfo.replace('\\', '/');
        if (normalizedPath.startsWith("/")) {
            normalizedPath = normalizedPath.substring(1);
        }
        if (normalizedPath.isBlank()) {
            return Optional.empty();
        }

        String[] segments = normalizedPath.split("/");
        if (segments.length != 2) {
            return Optional.empty();
        }

        Integer idProdotto = parsePositiveInt(segments[0]);
        String nomeFile = segments[1];
        if (idProdotto == null || !isSafeFileName(nomeFile)) {
            return Optional.empty();
        }

        Path productDirectory = productDirectory(idProdotto);
        Path file = productDirectory.resolve(nomeFile).normalize();
        if (!file.startsWith(productDirectory) || !file.startsWith(BASE_DIRECTORY)) {
            return Optional.empty();
        }
        if (!Files.isRegularFile(file)) {
            return Optional.empty();
        }

        return Optional.of(file);
    }

    public static Optional<Path> resolvePublicUrl(String publicUrl) {
        if (publicUrl == null || publicUrl.isBlank()) {
            return Optional.empty();
        }

        String normalizedUrl = publicUrl.replace('\\', '/');
        int publicPrefixIndex = normalizedUrl.indexOf(PUBLIC_PREFIX + "/");
        if (publicPrefixIndex < 0) {
            return Optional.empty();
        }

        String pathInfo = normalizedUrl.substring(publicPrefixIndex + PUBLIC_PREFIX.length());
        return resolveForServing(pathInfo);
    }

    public static void deleteQuietly(String publicUrl, Logger logger) {
        Optional<Path> file = resolvePublicUrl(publicUrl);
        if (file.isEmpty()) {
            return;
        }

        try {
            Files.deleteIfExists(file.get());
        } catch (IOException | SecurityException exception) {
            if (logger != null) {
                logger.log(Level.WARNING, "Impossibile eliminare il file immagine " + file.get(), exception);
            }
        }
    }

    private static Path productDirectory(int idProdotto) {
        return BASE_DIRECTORY.resolve(Integer.toString(idProdotto)).normalize();
    }

    private static String extensionFor(String tipoContenuto) {
        if ("image/png".equals(tipoContenuto)) {
            return ".png";
        }
        if ("image/webp".equals(tipoContenuto)) {
            return ".webp";
        }
        if ("image/gif".equals(tipoContenuto)) {
            return ".gif";
        }
        return ".jpg";
    }

    private static Integer parsePositiveInt(String value) {
        try {
            int id = Integer.parseInt(value);
            return id > 0 ? id : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private static boolean isSafeFileName(String fileName) {
        return fileName != null
                && !fileName.isBlank()
                && !".".equals(fileName)
                && !"..".equals(fileName)
                && !fileName.contains("/")
                && !fileName.contains("\\");
    }

    public static class StoredImage {

        private final String nomeFileOriginale;
        private final String nomeFileSalvato;
        private final String percorsoPubblico;
        private final Path file;

        private StoredImage(String nomeFileOriginale,
                            String nomeFileSalvato,
                            String percorsoPubblico,
                            Path file) {
            this.nomeFileOriginale = nomeFileOriginale;
            this.nomeFileSalvato = nomeFileSalvato;
            this.percorsoPubblico = percorsoPubblico;
            this.file = file;
        }

        public String getNomeFileOriginale() {
            return nomeFileOriginale;
        }

        public String getNomeFileSalvato() {
            return nomeFileSalvato;
        }

        public String getPercorsoPubblico() {
            return percorsoPubblico;
        }

        public Path getFile() {
            return file;
        }
    }
}
