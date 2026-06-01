package it.univaq.mastereat.util;

import it.univaq.mastereat.dto.web.cart.WebCart;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class CartSessionUtils {

    public static final String CART_ATTRIBUTE = "webCart";
    public static final String FLASH_CART_SUCCESS_ATTRIBUTE = "flashCartSuccessMessage";
    public static final String FLASH_CART_ERROR_ATTRIBUTE = "flashCartErrorMessage";

    private CartSessionUtils() {
    }

    public static WebCart getCart(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        Object existingCart = session.getAttribute(CART_ATTRIBUTE);
        if (existingCart instanceof WebCart webCart) {
            return webCart;
        }

        WebCart webCart = new WebCart();
        session.setAttribute(CART_ATTRIBUTE, webCart);
        return webCart;
    }

    public static WebCart getExistingCart(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        Object existingCart = session.getAttribute(CART_ATTRIBUTE);
        if (existingCart instanceof WebCart webCart) {
            return webCart;
        }

        return null;
    }

    public static void clearCart(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(CART_ATTRIBUTE);
        }
    }

    public static void setSuccessMessage(HttpServletRequest request, String message) {
        SessionUtils.setFlashMessage(request, FLASH_CART_SUCCESS_ATTRIBUTE, message);
    }

    public static void setErrorMessage(HttpServletRequest request, String message) {
        SessionUtils.setFlashMessage(request, FLASH_CART_ERROR_ATTRIBUTE, message);
    }

    public static String consumeSuccessMessage(HttpServletRequest request) {
        return SessionUtils.consumeFlashMessage(request, FLASH_CART_SUCCESS_ATTRIBUTE);
    }

    public static String consumeErrorMessage(HttpServletRequest request) {
        return SessionUtils.consumeFlashMessage(request, FLASH_CART_ERROR_ATTRIBUTE);
    }
}
