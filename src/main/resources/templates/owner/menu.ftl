<#import "/layout.ftl" as layout>
<@layout.page title="Menù proprietario" active="owner-menu">
<section class="page-heading">
    <p class="eyebrow">Area proprietario</p>
    <h1>Men&ugrave;</h1>
    <p>Gestione dei dati base dei prodotti, con prezzi, tempi di preparazione e stato di disponibilita.</p>
</section>

<#if successMessage?has_content>
    <p class="notice notice--success">${successMessage}</p>
</#if>
<#if errorMessage?has_content>
    <p class="notice notice--error">${errorMessage}</p>
</#if>

<div class="summary-actions summary-actions--inline owner-actions">
    <a class="button button--primary" href="${contextPath}/proprietario/menu/prodotti/nuovo">Nuovo prodotto</a>
    <a class="button button--ghost" href="${contextPath}/proprietario/ordini">Monitora ordini</a>
</div>

<#if prodotti?has_content>
    <section class="orders-table-wrap" aria-label="Men&ugrave; proprietario">
        <table class="orders-table owner-menu-table">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Nome</th>
                    <th>Descrizione</th>
                    <th>Prezzo base</th>
                    <th>Preparazione</th>
                    <th>Stato</th>
                    <th>Dettaglio</th>
                </tr>
            </thead>
            <tbody>
                <#list prodotti as prodotto>
                    <tr>
                        <td>#${prodotto.id?c}</td>
                        <td><strong>${prodotto.nome}</strong></td>
                        <td class="owner-menu-description">${prodotto.descrizione!"-"}</td>
                        <td>EUR ${layout.price(prodotto.prezzoBase)}</td>
                        <td>
                            <#if prodotto.minutiPreparazione gt 0>
                                ${prodotto.minutiPreparazione} min
                            <#else>
                                -
                            </#if>
                        </td>
                        <td>
                            <span class="status-pill <#if !prodotto.attivo>status-pill--muted</#if>">
                                <#if prodotto.attivo>Attivo<#else>Non attivo</#if>
                            </span>
                        </td>
                        <td>
                            <a class="text-link" href="${contextPath}/proprietario/menu/prodotti/${prodotto.id?c}">Apri</a>
                        </td>
                    </tr>
                </#list>
            </tbody>
        </table>
    </section>
<#else>
    <section class="empty-state">
        <h2>Nessun prodotto nel men&ugrave;</h2>
        <p>Non ci sono prodotti registrati da visualizzare.</p>
    </section>
</#if>
</@layout.page>
