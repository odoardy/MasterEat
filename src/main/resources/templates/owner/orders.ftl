<#import "/layout.ftl" as layout>
<@layout.page title="Monitora ordini" active="owner-orders">
<section class="page-heading">
    <p class="eyebrow">Area proprietario</p>
    <h1>Ordini</h1>
    <p>Monitoraggio completo degli ordini, inclusi stati operativi e terminali.</p>
</section>

<#if errorMessage?has_content>
    <p class="notice notice--error">${errorMessage}</p>
</#if>

<section class="filter-section" aria-label="Filtri ordini proprietario">
    <form class="filter-bar filter-bar--owner-orders" method="get" action="${contextPath}/proprietario/ordini">
        <label>
            <span>Stato</span>
            <select name="stato">
                <option value="" <#if !(filters.stato?has_content)>selected</#if>>Tutti</option>
                <#list statiOrdine as stato>
                    <option value="${stato}" <#if filters.stato == stato>selected</#if>>${stato}</option>
                </#list>
            </select>
        </label>

        <label>
            <span>Dal</span>
            <input type="date" name="dal" value="${filters.dal!}">
        </label>

        <label>
            <span>Al</span>
            <input type="date" name="al" value="${filters.al!}">
        </label>

        <div class="filter-bar__actions">
            <button class="button button--primary" type="submit">Filtra</button>
            <a class="button button--ghost" href="${contextPath}/proprietario/ordini">Reset</a>
        </div>
    </form>
</section>

<#if ordini?has_content>
    <section class="orders-table-wrap" aria-label="Monitora ordini">
        <table class="orders-table owner-orders-table">
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
                    <th>Operatori</th>
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
                                <span class="owner-table-meta">@${ordine.usernameCliente}</span>
                            </#if>
                        </td>
                        <td>${layout.displayDate(ordine.dataInserimento!"")}</td>
                        <td>${layout.displayDate(ordine.orarioConsegnaRichiesto!"")}</td>
                        <td><span class="status-pill">${ordine.stato}</span></td>
                        <td>EUR ${layout.price(ordine.prezzoTotale)}</td>
                        <td>
                            <#if ordine.minutiConsegnaStimati??>
                                ${ordine.minutiConsegnaStimati} min
                            <#else>
                                -
                            </#if>
                        </td>
                        <td>${ordine.numeroProdotti}</td>
                        <td class="owner-operator-summary">${ordine.operatoriRiepilogo!"Nessun operatore"}</td>
                        <td>
                            <a class="text-link" href="${contextPath}/proprietario/ordini/${ordine.id?c}">Apri</a>
                        </td>
                    </tr>
                </#list>
            </tbody>
        </table>
    </section>
<#else>
    <section class="empty-state">
        <h2>Nessun ordine trovato</h2>
        <p>Non ci sono ordini per i filtri selezionati.</p>
    </section>
</#if>
</@layout.page>
