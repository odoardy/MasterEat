<#import "/layout.ftl" as layout>
<@layout.page title="Caratteristiche prodotto" active="owner-menu">
<#if errorMessage?has_content && !(prodotto??)>
    <section class="status-page">
        <p class="eyebrow">Area proprietario</p>
        <h1>Caratteristiche</h1>
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
        <h1>Caratteristiche</h1>
        <p>Gestione delle opzioni associate a ${prodotto.nome}.</p>
    </section>

    <#if successMessage?has_content>
        <p class="notice notice--success">${successMessage}</p>
    </#if>
    <#if errorMessage?has_content>
        <p class="notice notice--error">${errorMessage}</p>
    </#if>

    <div class="summary-actions summary-actions--inline owner-actions">
        <a class="button button--primary" href="${contextPath}/proprietario/menu/prodotti/${prodotto.id?c}/caratteristiche/nuova">Nuova caratteristica</a>
        <a class="button button--ghost" href="${contextPath}/proprietario/menu/prodotti/${prodotto.id?c}/gruppi-caratteristiche">Gestisci gruppi</a>
        <a class="button button--ghost" href="${contextPath}/proprietario/menu/prodotti/${prodotto.id?c}">Torna al prodotto</a>
    </div>

    <section class="account-panel" aria-labelledby="caratteristiche-prodotto-title">
        <div class="section__header">
            <div>
                <p class="eyebrow">Prodotto #${prodotto.id?c}</p>
                <h2 id="caratteristiche-prodotto-title">${prodotto.nome}</h2>
            </div>
            <span class="status-pill <#if !prodotto.attivo>status-pill--muted</#if>">
                <#if prodotto.attivo>Prodotto attivo<#else>Prodotto non attivo</#if>
            </span>
        </div>

        <#if caratteristiche?has_content>
            <div class="orders-table-wrap owner-detail-table-wrap">
                <table class="orders-table owner-characteristics-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Gruppo</th>
                            <th>Nome</th>
                            <th>Descrizione</th>
                            <th>Differenza prezzo</th>
                            <th>Default</th>
                            <th>Stato</th>
                            <th>Azioni</th>
                        </tr>
                    </thead>
                    <tbody>
                        <#list caratteristiche as caratteristica>
                            <tr>
                                <td>#${caratteristica.id?c}</td>
                                <td>
                                    <#if caratteristica.nomeGruppoCaratteristiche?has_content>
                                        ${caratteristica.nomeGruppoCaratteristiche}
                                    <#else>
                                        -
                                    </#if>
                                </td>
                                <td><strong>${caratteristica.nome}</strong></td>
                                <td class="owner-menu-description">${caratteristica.descrizione!"-"}</td>
                                <td>
                                    <#if caratteristica.differenzaPrezzo?? && caratteristica.differenzaPrezzo != 0>
                                        <#if caratteristica.differenzaPrezzo gt 0>+</#if> EUR ${layout.price(caratteristica.differenzaPrezzo)}
                                    <#else>
                                        Inclusa
                                    </#if>
                                </td>
                                <td><#if caratteristica.selezionataDefault>Si<#else>No</#if></td>
                                <td>
                                    <span class="status-pill <#if !caratteristica.attiva>status-pill--muted</#if>">
                                        <#if caratteristica.attiva>Attiva<#else>Non attiva</#if>
                                    </span>
                                </td>
                                <td>
                                    <div class="owner-table-actions">
                                        <a class="text-link" href="${contextPath}/proprietario/menu/prodotti/${prodotto.id?c}/caratteristiche/${caratteristica.id?c}/modifica">Modifica</a>
                                        <#if caratteristica.attiva>
                                            <form method="post" action="${contextPath}/proprietario/menu/prodotti/${prodotto.id?c}/caratteristiche/${caratteristica.id?c}/rimuovi">
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
                <h3>Nessuna caratteristica</h3>
                <p>Non ci sono ancora caratteristiche associate a questo prodotto.</p>
                <a class="button button--primary" href="${contextPath}/proprietario/menu/prodotti/${prodotto.id?c}/caratteristiche/nuova">Crea la prima caratteristica</a>
            </section>
        </#if>
    </section>
</#if>
</@layout.page>
