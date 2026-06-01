<#import "/layout.ftl" as layout>
<@layout.page title="I miei ordini" active="orders">
<section class="page-heading">
    <p class="eyebrow">Area cliente</p>
    <h1>Ordini</h1>
    <p>Storico degli ordini associati al tuo account.</p>
</section>

<#if successMessage?has_content>
    <p class="notice notice--success">${successMessage}</p>
</#if>
<#if errorMessage?has_content>
    <p class="notice notice--error">${errorMessage}</p>
</#if>

<section class="filter-section" aria-label="Filtri ordini">
    <form class="filter-bar filter-bar--orders" method="get" action="${contextPath}/cliente/ordini">
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
            <span>Data da</span>
            <input type="date" name="dataDa" value="${filters.dataDa!}">
        </label>

        <label>
            <span>Data a</span>
            <input type="date" name="dataA" value="${filters.dataA!}">
        </label>

        <div class="filter-bar__actions">
            <button class="button button--primary" type="submit">Filtra</button>
            <a class="button button--ghost" href="${contextPath}/cliente/ordini">Reset</a>
        </div>
    </form>
</section>

<#if ordini?has_content>
    <section class="orders-table-wrap" aria-label="Storico ordini">
        <table class="orders-table">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Data</th>
                    <th>Stato</th>
                    <th>Totale</th>
                    <th>Tempo stimato</th>
                    <th>Dettaglio</th>
                </tr>
            </thead>
            <tbody>
                <#list ordini as ordine>
                    <#assign dataOrdine = ordine.creatoIl!"">
                    <#if ordine.confermatoIl?has_content>
                        <#assign dataOrdine = ordine.confermatoIl>
                    </#if>
                    <tr>
                        <td>#${ordine.id?c}</td>
                        <td>${layout.displayDate(dataOrdine)}</td>
                        <td><span class="status-pill">${ordine.stato}</span></td>
                        <td>EUR ${layout.price(ordine.prezzoTotale)}</td>
                        <td>
                            <#if ordine.minutiConsegnaStimati??>
                                ${ordine.minutiConsegnaStimati} min
                            <#else>
                                -
                            </#if>
                        </td>
                        <td>
                            <a class="text-link" href="${contextPath}/cliente/ordini/${ordine.id?c}">Apri</a>
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
        <a class="button button--primary" href="${contextPath}/menu">Vai al men&ugrave;</a>
    </section>
</#if>
</@layout.page>
