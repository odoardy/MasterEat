<#import "/layout.ftl" as layout>
<@layout.page title="Ingredienti prodotto" active="owner-menu">
<#if errorMessage?has_content && !(prodotto??)>
    <section class="status-page">
        <p class="eyebrow">Area proprietario</p>
        <h1>Ingredienti</h1>
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
        <h1>Ingredienti</h1>
        <p>Gestione degli ingredienti associati a ${prodotto.nome}.</p>
    </section>

    <#if successMessage?has_content>
        <p class="notice notice--success">${successMessage}</p>
    </#if>
    <#if errorMessage?has_content>
        <p class="notice notice--error">${errorMessage}</p>
    </#if>

    <div class="summary-actions summary-actions--inline owner-actions">
        <a class="button button--primary" href="${contextPath}/proprietario/menu/prodotti/${prodotto.id?c}/ingredienti/nuovo">Nuovo ingrediente</a>
        <a class="button button--ghost" href="${contextPath}/proprietario/menu/prodotti/${prodotto.id?c}">Torna al prodotto</a>
    </div>

    <section class="account-panel" aria-labelledby="ingredienti-prodotto-title">
        <div class="section__header">
            <div>
                <p class="eyebrow">Prodotto #${prodotto.id?c}</p>
                <h2 id="ingredienti-prodotto-title">${prodotto.nome}</h2>
            </div>
            <span class="status-pill <#if !prodotto.attivo>status-pill--muted</#if>">
                <#if prodotto.attivo>Prodotto attivo<#else>Prodotto non attivo</#if>
            </span>
        </div>

        <#if ingredienti?has_content>
            <div class="orders-table-wrap owner-detail-table-wrap">
                <table class="orders-table owner-ingredients-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Nome</th>
                            <th>Quantita</th>
                            <th>Unita</th>
                            <th>Allergene</th>
                            <th>Stato</th>
                            <th>Azioni</th>
                        </tr>
                    </thead>
                    <tbody>
                        <#list ingredienti as ingrediente>
                            <tr>
                                <td>#${ingrediente.id?c}</td>
                                <td><strong>${ingrediente.nome}</strong></td>
                                <td>
                                    <#if ingrediente.quantita??>
                                        ${ingrediente.quantita?string["0.###"]}
                                    <#else>
                                        -
                                    </#if>
                                </td>
                                <td>${ingrediente.unitaMisura!"-"}</td>
                                <td><#if ingrediente.allergene>Si<#else>No</#if></td>
                                <td>
                                    <span class="status-pill <#if !ingrediente.attivo>status-pill--muted</#if>">
                                        <#if ingrediente.attivo>Attivo<#else>Non attivo</#if>
                                    </span>
                                </td>
                                <td>
                                    <div class="owner-table-actions">
                                        <a class="text-link" href="${contextPath}/proprietario/menu/prodotti/${prodotto.id?c}/ingredienti/${ingrediente.id?c}/modifica">Modifica</a>
                                        <form method="post" action="${contextPath}/proprietario/menu/prodotti/${prodotto.id?c}/ingredienti/${ingrediente.id?c}/rimuovi">
                                            <button class="text-link owner-link-button" type="submit">Rimuovi</button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        <#else>
            <section class="empty-state empty-state--compact">
                <h3>Nessun ingrediente</h3>
                <p>Non ci sono ancora ingredienti associati a questo prodotto.</p>
                <a class="button button--primary" href="${contextPath}/proprietario/menu/prodotti/${prodotto.id?c}/ingredienti/nuovo">Aggiungi il primo ingrediente</a>
            </section>
        </#if>
    </section>
</#if>
</@layout.page>
