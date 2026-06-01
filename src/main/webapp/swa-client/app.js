(function () {
    "use strict"; // Tutto ciò che segue è in strict mode, quindi variabili e funzioni dichiarate qui non inquinano lo scope globale.

    // Configurazione condivisa: URL API di default e nomi usati per salvare dati nel browser.
    var DEFAULT_BASE_URL = "../api";
    var STORAGE_KEYS = {
        baseUrl: "mastereat.swa.baseUrl",
        token: "mastereat.swa.token",
        authInfo: "mastereat.swa.authInfo"
    };

    var token = readStorage(STORAGE_KEYS.token) || "";
    var authInfo = parseStoredJson(readStorage(STORAGE_KEYS.authInfo)) || {};

    // Scorciatoia per recuperare elementi del DOM senza ripetere document.getElementById.
    function byId(id) {
        return document.getElementById(id);
    }

    // Wrapper su localStorage: se lo storage e bloccato, il client continua a funzionare in memoria.
    function readStorage(key) {
        try {
            return window.localStorage.getItem(key);
        } catch (error) {
            return null;
        }
    }

    function writeStorage(key, value) {
        try {
            window.localStorage.setItem(key, value);
        } catch (error) {
            // Il client resta usabile anche senza localStorage.
        }
    }

    function removeStorage(key) {
        try {
            window.localStorage.removeItem(key);
        } catch (error) {
            // Il client resta usabile anche senza localStorage.
        }
    }

    function parseStoredJson(value) {
        if (!value) {
            return null;
        }

        try {
            return JSON.parse(value);
        } catch (error) {
            return null;
        }
    }

    // Evita URL duplicati tipo ../api//menu quando l'utente inserisce slash finali.
    function normalizeBaseUrl(value) {
        var normalized = (value || DEFAULT_BASE_URL).trim();
        if (!normalized) {
            normalized = DEFAULT_BASE_URL;
        }

        return normalized.replace(/\/+$/, "");
    }

    function getBaseUrl() {
        return normalizeBaseUrl(byId("baseUrl").value);
    }

    function saveBaseUrl() {
        var baseUrl = getBaseUrl();
        byId("baseUrl").value = baseUrl;
        writeStorage(STORAGE_KEYS.baseUrl, baseUrl);
        renderLocalResult("Configurazione salvata", {
            messaggio: "Base URL API salvata in localStorage.",
            baseUrl: baseUrl
        });
    }

    // Il token resta in variabile per l'uso immediato e in localStorage per sopravvivere al refresh.
    function getToken() {
        return token || readStorage(STORAGE_KEYS.token) || "";
    }

    function setToken(newToken, newAuthInfo) {
        token = newToken || "";
        authInfo = newAuthInfo || {};

        if (token) {
            writeStorage(STORAGE_KEYS.token, token);
            writeStorage(STORAGE_KEYS.authInfo, JSON.stringify(authInfo));
        } else {
            removeStorage(STORAGE_KEYS.token);
            removeStorage(STORAGE_KEYS.authInfo);
        }

        updateAuthView();
    }

    function clearToken() {
        setToken("", {});
    }

    function updateAuthView() {
        var currentToken = getToken();
        var currentInfo = authInfo || {};

        // I valori dell'utente vengono escapati prima di inserirli come HTML.
        byId("currentToken").textContent = currentToken || "Nessun token salvato";
        byId("currentUser").innerHTML =
            "<dt>Username</dt><dd>" + escapeHtml(currentInfo.username || "-") + "</dd>" +
            "<dt>ID utente</dt><dd>" + escapeHtml(formatValue(currentInfo.idUtente)) + "</dd>" +
            "<dt>Ruolo</dt><dd>" + escapeHtml(currentInfo.ruolo || "-") + "</dd>";
    }

    function escapeHtml(value) {
        return String(value)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    function formatValue(value) {
        return value === undefined || value === null || value === "" ? "-" : value;
    }

    // Crea la query string ignorando i campi vuoti, cosi non vengono inviati filtri inutili.
    function buildQuery(params) {
        var searchParams = new URLSearchParams();

        Object.keys(params || {}).forEach(function (key) {
            var value = params[key];
            if (value !== undefined && value !== null && String(value).trim() !== "") {
                searchParams.append(key, String(value).trim());
            }
        });

        var query = searchParams.toString();
        return query ? "?" + query : "";
    }

    function buildUrl(path, queryParams) {
        var normalizedPath = path.charAt(0) === "/" ? path : "/" + path;
        return getBaseUrl() + normalizedPath + buildQuery(queryParams);
    }

    // Piccolo proxy verso le API REST di MasterEat.
    // Centralizza URL, query string, token Bearer, body JSON, fetch(),
    // header HTTP comuni, parsing della risposta e gestione uniforme degli errori.
    function apiRequest(method, path, options) {
        var requestOptions = options || {};
        var url = buildUrl(path, requestOptions.query);
        var headers = {
            Accept: "application/json"
        };
        var fetchOptions = {
            method: method,
            headers: headers
        };

        if (requestOptions.body !== undefined && requestOptions.body !== null) {
            headers["Content-Type"] = "application/json";
            fetchOptions.body = JSON.stringify(requestOptions.body);
        }

        // Gli endpoint protetti ricevono il token nel formato Authorization: Bearer <token>.
        if (requestOptions.auth) {
            var currentToken = getToken();
            if (currentToken) {
                headers.Authorization = "Bearer " + currentToken;
            }
        }

        return fetch(url, fetchOptions)
            .then(function (response) {
                return response.text().then(function (text) {
                    var parsedBody = parseResponseBody(text);

                    return {
                        method: method,
                        url: url,
                        status: response.status,
                        ok: response.ok,
                        hasBody: text.length > 0,
                        data: parsedBody
                    };
                });
            })
            .catch(function (error) {
                return {
                    method: method,
                    url: url,
                    status: null,
                    ok: false,
                    hasBody: true,
                    networkError: true,
                    data: {
                        errore: "Errore di rete: richiesta non completata.",
                        dettaglio: error.message
                    }
                };
            });
    }

    function parseResponseBody(text) {
        if (!text) {
            return null;
        }

        try {
            return JSON.parse(text);
        } catch (error) {
            return text;
        }
    }

    function scrollToOutput() {
        var outputPanel = byId("outputPanel");
        var prefersReducedMotion = window.matchMedia
            && window.matchMedia("(prefers-reduced-motion: reduce)").matches;

        outputPanel.scrollIntoView({
            behavior: prefersReducedMotion ? "auto" : "smooth",
            block: "start"
        });
    }

    // Rendering centralizzato: tutte le risposte vengono mostrate con la stessa struttura JSON.
    function renderResult(title, method, url, status, data, flags) {
        var resultFlags = flags || {};
        var isOk = resultFlags.ok !== undefined ? resultFlags.ok : true;
        var hasBody = resultFlags.hasBody !== undefined ? resultFlags.hasBody : data !== null && data !== undefined;
        var statusText = status === null ? "Errore di rete" : String(status);
        var body = hasBody ? data : "Nessun contenuto nella risposta";
        var output = {
            operazione: title,
            richiesta: {
                metodo: method,
                url: url
            },
            risposta: {
                status: statusText,
                body: body
            }
        };

        byId("resultStatus").className = "result-status " + (isOk ? "success" : "error");
        byId("resultStatus").textContent = title + " - " + method + " " + url + " - status: " + statusText;
        byId("resultOutput").textContent = JSON.stringify(output, null, 2);
        scrollToOutput();
    }

    function renderApiResult(title, result) { // Serve a renderizzare i risultati delle chiamate API, estraendo i campi necessari e delegando a renderResult.
        renderResult(title, result.method, result.url, result.status, result.data, {
            ok: result.ok,
            hasBody: result.hasBody
        });
    }

    function renderLocalResult(title, detail, isOk) {
        var localOk = isOk !== undefined ? isOk : true;
        var esito = localOk ? "OK" : "ERRORE";
        var output = {
            operazione: title,
            tipo: "LOCALE",
            esito: esito,
            dettaglio: detail || {}
        };

        byId("resultStatus").className = "result-status " + (localOk ? "success" : "error");
        byId("resultStatus").textContent = title + " - operazione locale - esito: " + esito;
        byId("resultOutput").textContent = JSON.stringify(output, null, 2);
        scrollToOutput();
    }

    function renderClientError(title, message) {
        renderLocalResult(title, {
            errore: message
        }, false);
    }

    function requestAndRender(title, method, path, options) {
        apiRequest(method, path, options).then(function (result) {
            renderApiResult(title, result);
        });
    }

    function requireInputValue(id, title, label) {
        var value = byId(id).value.trim();
        if (!value) {
            renderClientError(title, "Inserire " + label + " prima di inviare la richiesta.");
            return null;
        }

        return value;
    }

    function buildPathWithId(basePath, id, suffix) {
        return basePath + "/" + encodeURIComponent(id) + (suffix || "");
    }

    function requirePositiveNumber(id, title, label) {
        var value = requireInputValue(id, title, label);
        var parsedValue;

        if (!value) {
            return null;
        }

        parsedValue = Number(value);
        if (!Number.isInteger(parsedValue) || parsedValue <= 0) {
            renderClientError(title, label + " deve essere un numero intero positivo.");
            return null;
        }

        return parsedValue;
    }

    function parseOptionalPositiveNumberList(id, title, label) {
        var value = byId(id).value.trim();
        var values = [];

        if (!value) {
            return values;
        }

        value.split(",").forEach(function (part) {
            var normalized = part.trim();
            var parsedValue = Number(normalized);

            if (values === null) {
                return;
            }

            if (!normalized || !Number.isInteger(parsedValue) || parsedValue <= 0) {
                values = null;
                return;
            }

            values.push(parsedValue);
        });

        if (values === null) {
            renderClientError(title, label + " deve contenere solo numeri interi positivi separati da virgola.");
        }

        return values;
    }

    // Le funzioni sotto leggono i form, chiamano l'API corretta e delegano il risultato al renderer.
    function login() {
        var username = byId("username").value.trim();
        var password = byId("password").value;

        apiRequest("POST", "/auth/login", {
            body: {
                username: username,
                password: password
            }
        }).then(function (result) {
            if (result.ok && result.data && result.data.token) {
                setToken(result.data.token, {
                    username: result.data.username,
                    idUtente: result.data.idUtente,
                    ruolo: result.data.ruolo
                });
            }

            renderApiResult("Login", result);
        });
    }

    function logout() {
        apiRequest("POST", "/auth/logout", {
            auth: true
        }).then(function (result) {
            if (result.ok) {
                clearToken();
            }

            renderApiResult("Logout", result);
        });
    }

    function loadMenu() {
        requestAndRender("Menu completo", "GET", "/menu");
    }

    function searchProducts() {
        requestAndRender("Ricerca prodotti", "GET", "/prodotti", {
            query: {
                nome: byId("productName").value,
                prezzoMin: byId("priceMin").value,
                prezzoMax: byId("priceMax").value
            }
        });
    }

    function loadProductDetail() {
        var idProdotto = requireInputValue("productDetailId", "Dettaglio prodotto", "un ID prodotto");
        if (!idProdotto) {
            return;
        }

        requestAndRender("Dettaglio prodotto", "GET", buildPathWithId("/prodotti", idProdotto));
    }

    function loadProductCharacteristics() {
        var idProdotto = requireInputValue("characteristicsProductId", "Caratteristiche prodotto", "un ID prodotto");
        if (!idProdotto) {
            return;
        }

        requestAndRender(
            "Caratteristiche prodotto",
            "GET",
            buildPathWithId("/prodotti", idProdotto, "/caratteristiche")
        );
    }

    function loadProductIngredients() {
        var idProdotto = requireInputValue("ingredientsProductId", "Ingredienti prodotto", "un ID prodotto");
        if (!idProdotto) {
            return;
        }

        requestAndRender(
            "Ingredienti prodotto",
            "GET",
            buildPathWithId("/prodotti", idProdotto, "/ingredienti"),
            { auth: true }
        );
    }

    function createOrder() {
        requestAndRender("Creazione ordine", "POST", "/ordini", {
            auth: true
        });
    }

    function addProductToOrder() {
        var title = "Aggiunta prodotto all'ordine";
        var idOrdine = requirePositiveNumber("addProductOrderId", title, "ID ordine");
        var idProdotto = requirePositiveNumber("addProductProductId", title, "ID prodotto");
        var quantita = requirePositiveNumber("addProductQuantity", title, "Quantita");
        var caratteristiche = parseOptionalPositiveNumberList("addProductCharacteristics", title, "Caratteristiche");

        if (!idOrdine || !idProdotto || !quantita || caratteristiche === null) {
            return;
        }

        requestAndRender(
            title,
            "POST",
            buildPathWithId("/ordini", idOrdine, "/prodotti"),
            {
                auth: true,
                body: {
                    idProdotto: idProdotto,
                    quantita: quantita,
                    caratteristiche: caratteristiche
                }
            }
        );
    }

    function loadOrderProducts() {
        var idOrdine = requireInputValue("orderProductsId", "Prodotti nell'ordine", "un ID ordine");
        if (!idOrdine) {
            return;
        }

        requestAndRender("Prodotti nell'ordine", "GET", buildPathWithId("/ordini", idOrdine, "/prodotti"), {
            auth: true
        });
    }

    function loadOrderTotal() {
        var idOrdine = requireInputValue("orderTotalId", "Totale ordine", "un ID ordine");
        if (!idOrdine) {
            return;
        }

        requestAndRender("Totale ordine", "GET", buildPathWithId("/ordini", idOrdine, "/totale"), {
            auth: true
        });
    }

    function loadOrderTime() {
        var idOrdine = requireInputValue("orderTimeId", "Tempo consegna", "un ID ordine");
        if (!idOrdine) {
            return;
        }

        requestAndRender("Tempo consegna", "GET", buildPathWithId("/ordini", idOrdine, "/tempo-consegna"), {
            auth: true
        });
    }

    function confirmOrder() {
        var idOrdine = requireInputValue("confirmOrderId", "Conferma ordine", "un ID ordine");
        if (!idOrdine) {
            return;
        }

        requestAndRender("Conferma ordine", "POST", buildPathWithId("/ordini", idOrdine, "/conferma"), {
            auth: true
        });
    }

    function searchOrders() {
        requestAndRender("Ordini filtrati", "GET", "/ordini", {
            auth: true,
            query: {
                stato: byId("orderStatus").value,
                dataDa: byId("dateFrom").value,
                dataA: byId("dateTo").value
            }
        });
    }

    function updateOrderStatus() {
        var idOrdine = requireInputValue("statusOrderId", "Aggiornamento stato ordine", "un ID ordine");
        if (!idOrdine) {
            return;
        }

        requestAndRender("Aggiornamento stato ordine", "PUT", buildPathWithId("/ordini", idOrdine, "/stato"), {
            auth: true,
            body: {
                nuovoStato: byId("newOrderStatus").value
            }
        });
    }

    function loadOrderOperators() {
        var idOrdine = requireInputValue("operatorsOrderId", "Operatori ordine", "un ID ordine");
        if (!idOrdine) {
            return;
        }

        requestAndRender("Operatori ordine", "GET", buildPathWithId("/ordini", idOrdine, "/operatori"), {
            auth: true
        });
    }

    function cancelOrder() {
        var idOrdine = requireInputValue("cancelOrderId", "Annullamento ordine", "un ID ordine");
        if (!idOrdine) {
            return;
        }

        requestAndRender("Annullamento ordine", "DELETE", buildPathWithId("/ordini", idOrdine), {
            auth: true
        });
    }

    function bindSubmit(formId, handler) {
        byId(formId).addEventListener("submit", function (event) {
            event.preventDefault();
            handler();
        });
    }

    // Associa form e pulsanti alle funzioni JavaScript, bloccando il submit HTML tradizionale.
    function bindEvents() {
        bindSubmit("configForm", saveBaseUrl);
        bindSubmit("loginForm", login);

        byId("logoutButton").addEventListener("click", logout);

        byId("clearTokenButton").addEventListener("click", function () {
            clearToken();
            renderLocalResult("Token pulito localmente", {
                messaggio: "Token rimosso dalla variabile JavaScript e da localStorage."
            });
        });

        byId("loadMenuButton").addEventListener("click", loadMenu);
        byId("createOrderButton").addEventListener("click", createOrder);

        bindSubmit("productsForm", searchProducts);
        bindSubmit("productDetailForm", loadProductDetail);
        bindSubmit("productCharacteristicsForm", loadProductCharacteristics);
        bindSubmit("productIngredientsForm", loadProductIngredients);
        bindSubmit("addOrderProductForm", addProductToOrder);
        bindSubmit("orderProductsForm", loadOrderProducts);
        bindSubmit("orderTotalForm", loadOrderTotal);
        bindSubmit("orderTimeForm", loadOrderTime);
        bindSubmit("confirmOrderForm", confirmOrder);
        bindSubmit("cancelForm", cancelOrder);
        bindSubmit("ordersForm", searchOrders);
        bindSubmit("updateOrderStatusForm", updateOrderStatus);
        bindSubmit("orderOperatorsForm", loadOrderOperators);
    }

    // Avvio della pagina: ripristina la configurazione salvata, aggiorna lo stato auth e collega gli eventi.
    document.addEventListener("DOMContentLoaded", function () {
        byId("baseUrl").value = normalizeBaseUrl(readStorage(STORAGE_KEYS.baseUrl) || DEFAULT_BASE_URL);
        updateAuthView(); // Assicura che lo stato di autenticazione salvato venga mostrato correttamente al caricamento della pagina.
        bindEvents(); // Collega i form e i pulsanti alle funzioni JavaScript dopo che il DOM è pronto.
    });
}());
