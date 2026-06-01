<#import "/layout.ftl" as layout>
<@layout.page title="Ordini staff" active="staff-orders">
<section class="page-heading">
    <p class="eyebrow">Area personale</p>
    <h1>Ordini staff</h1>
    <p>Ordini correnti da seguire in preparazione, consegna e chiusura.</p>
</section>

<#if successMessage?has_content>
    <p class="notice notice--success">${successMessage}</p>
</#if>
<#if errorMessage?has_content>
    <p class="notice notice--error">${errorMessage}</p>
</#if>

<section class="filter-section" aria-label="Filtri ordini staff">
    <form class="filter-bar filter-bar--staff-orders" method="get" action="${contextPath}/staff/ordini">
        <label>
            <span>Stato</span>
            <select name="stato">
                <option value="" <#if !(filters.stato?has_content)>selected</#if>>Operativi</option>
                <#list statiOrdine as stato>
                    <option value="${stato}" <#if filters.stato == stato>selected</#if>>${stato}</option>
                </#list>
            </select>
        </label>

        <div class="filter-bar__actions">
            <button class="button button--primary" type="submit">Filtra</button>
            <a class="button button--ghost" href="${contextPath}/staff/ordini">Reset</a>
        </div>
    </form>
</section>

<#if ordini?has_content>
    <section class="orders-table-wrap" aria-label="Ordini operativi">
        <table class="orders-table staff-orders-table">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Cliente</th>
                    <th>Inserimento</th>
                    <th>Consegna richiesta</th>
                    <th>Stato</th>
                    <th>Totale</th>
                    <th>Tempo stimato</th>
                    <th>Prodotti</th>
                    <th>Dettaglio</th>
                </tr>
            </thead>
            <tbody>
                <#list ordini as ordine>
                    <tr>
                        <td>#${ordine.id?c}</td>
                        <td>
                            <strong>${ordine.cliente}</strong>
                            <#if ordine.usernameCliente?has_content>
                                <span class="staff-table-meta">@${ordine.usernameCliente}</span>
                            </#if>
                        </td>
                        <td>${layout.displayDate(ordine.dataInserimento!"")}</td>
                        <td>${layout.displayDate(ordine.orarioConsegnaRichiesto!"")}</td>
                        <td>
                            <span class="status-pill <#if !ordine.operativo>status-pill--muted</#if>">${ordine.stato}</span>
                            <#if !ordine.operativo>
                                <span class="staff-state-note">Non operativo</span>
                            </#if>
                        </td>
                        <td>EUR ${layout.price(ordine.prezzoTotale)}</td>
                        <td>
                            <#if ordine.minutiConsegnaStimati??>
                                ${ordine.minutiConsegnaStimati} min
                            <#else>
                                -
                            </#if>
                        </td>
                        <td>${ordine.numeroProdotti}</td>
                        <td>
                            <a class="text-link" href="${contextPath}/staff/ordini/${ordine.id?c}">Apri</a>
                        </td>
                    </tr>
                </#list>
            </tbody>
        </table>
    </section>
<#else>
    <section class="empty-state">
        <h2>Nessun ordine trovato</h2>
        <p>Non ci sono ordini per il filtro selezionato.</p>
    </section>
</#if>
</@layout.page>
