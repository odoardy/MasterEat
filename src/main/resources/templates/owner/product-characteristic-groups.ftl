<#import "/layout.ftl" as layout>
<@layout.page title="Gruppi caratteristiche prodotto" active="owner-menu">
<#if errorMessage?has_content && !(prodotto??)>
    <section class="status-page">
        <p class="eyebrow">Area proprietario</p>
        <h1>Gruppi caratteristiche</h1>
        <p>${errorMessage}</p>
        <#if productId??>
            <a class="button button--primary" href="${contextPath}/proprietario/menu/prodotti/${productId?c}">Torna al prodotto</a>
        <#else>
            <a class="button button--primary" href="${contextPath}/proprietario/menu">Torna al men&ugrave;</a>
        </#if>
    </section>
<#else>
    <section class="page-heading">
        <p class="eyebrow">Area proprietario</p>
        <h1>Gruppi caratteristiche</h1>
        <p>Gestione delle famiglie di alternative associate a ${prodotto.nome}.</p>
    </section>

    <#if successMessage?has_content>
        <p class="notice notice--success">${successMessage}</p>
    </#if>
    <#if errorMessage?has_content>
        <p class="notice notice--error">${errorMessage}</p>
    </#if>

    <div class="summary-actions summary-actions--inline owner-actions">
        <a class="button button--primary" href="${contextPath}/proprietario/menu/prodotti/${prodotto.id?c}/gruppi-caratteristiche/nuovo">Nuovo gruppo</a>
        <a class="button button--ghost" href="${contextPath}/proprietario/menu/prodotti/${prodotto.id?c}/caratteristiche">Gestisci caratteristiche</a>
        <a class="button button--ghost" href="${contextPath}/proprietario/menu/prodotti/${prodotto.id?c}">Torna al prodotto</a>
    </div>

    <section class="account-panel" aria-labelledby="gruppi-caratteristiche-prodotto-title">
        <div class="section__header">
            <div>
                <p class="eyebrow">Prodotto #${prodotto.id?c}</p>
                <h2 id="gruppi-caratteristiche-prodotto-title">${prodotto.nome}</h2>
            </div>
            <span class="status-pill <#if !prodotto.attivo>status-pill--muted</#if>">
                <#if prodotto.attivo>Prodotto attivo<#else>Prodotto non attivo</#if>
            </span>
        </div>

        <#if gruppi?has_content>
            <div class="orders-table-wrap owner-detail-table-wrap">
                <table class="orders-table owner-characteristic-groups-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Nome</th>
                            <th>Descrizione</th>
                            <th>Obbligatorio</th>
                            <th>Stato</th>
                            <th>Azioni</th>
                        </tr>
                    </thead>
                    <tbody>
                        <#list gruppi as gruppo>
                            <tr>
                                <td>#${gruppo.id?c}</td>
                                <td><strong>${gruppo.nome}</strong></td>
                                <td class="owner-menu-description">${gruppo.descrizione!"-"}</td>
                                <td><#if gruppo.obbligatorio>Si<#else>No</#if></td>
                                <td>
                                    <span class="status-pill <#if !gruppo.attivo>status-pill--muted</#if>">
                                        <#if gruppo.attivo>Attivo<#else>Non attivo</#if>
                                    </span>
                                </td>
                                <td>
                                    <div class="owner-table-actions">
                                        <a class="text-link" href="${contextPath}/proprietario/menu/prodotti/${prodotto.id?c}/gruppi-caratteristiche/${gruppo.id?c}/modifica">Modifica</a>
                                        <#if gruppo.attivo>
                                            <form method="post" action="${contextPath}/proprietario/menu/prodotti/${prodotto.id?c}/gruppi-caratteristiche/${gruppo.id?c}/rimuovi">
                                                <button class="text-link owner-link-button" type="submit">Disattiva</button>
                                            </form>
                                        </#if>
                                    </div>
                                </td>
                            </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        <#else>
            <section class="empty-state empty-state--compact">
                <h3>Nessun gruppo</h3>
                <p>Non ci sono ancora gruppi caratteristiche associati a questo prodotto.</p>
                <a class="button button--primary" href="${contextPath}/proprietario/menu/prodotti/${prodotto.id?c}/gruppi-caratteristiche/nuovo">Crea il primo gruppo</a>
            </section>
        </#if>
    </section>
</#if>
</@layout.page>
