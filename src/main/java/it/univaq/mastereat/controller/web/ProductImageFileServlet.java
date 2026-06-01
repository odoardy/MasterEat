package it.univaq.mastereat.controller.web;

import it.univaq.mastereat.util.ProductImageStorage;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@WebServlet(name = "ProductImageFileServlet", urlPatterns = "/uploads/prodotti/*")
public class ProductImageFileServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Optional<Path> file = ProductImageStorage.resolveForServing(request.getPathInfo());
        if (file.isEmpty()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String contentType = getServletContext().getMimeType(file.get().getFileName().toString());
        if (contentType == null) {
            contentType = Files.probeContentType(file.get());
        }
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        response.setContentType(contentType);
        response.setContentLengthLong(Files.size(file.get()));
        response.setHeader("Cache-Control", "public, max-age=86400");
        Files.copy(file.get(), response.getOutputStream());
    }
}
