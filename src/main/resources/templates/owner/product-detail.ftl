<#import "/layout.ftl" as layout>
<@layout.page title="Dettaglio prodotto proprietario" active="owner-menu">
<#if errorMessage?has_content && !(prodotto??)>
    <section class="status-page">
        <p class="eyebrow">Area proprietario</p>
        <h1>Prodotto</h1>
        <p>${errorMessage}</p>
        <a class="button button--primary" href="${contextPath}/proprietario/menu">Torna al men&ugrave;</a>
    </section>
<#else>
    <section class="page-heading">
        <p class="eyebrow">Area proprietario</p>
        <h1>${prodotto.nome}</h1>
        <p>Dati base del prodotto, caratteristiche e ingredienti associati.</p>
    </section>

    <#if successMessage?has_content>
        <p class="notice notice--success">${successMessage}</p>
    </#if>

    <div class="summary-actions summary-actions--inline owner-actions">
        <a class="button button--primary" href="${contextPath}/proprietario/menu/prodotti/${prodotto.id?c}/modifica">Modifica prodotto</a>
        <a class="button button--ghost" href="${contextPath}/proprietario/menu/prodotti/${prodotto.id?c}/caratteristiche">Gestisci caratteristiche</a>
        <a class="button button--ghost" href="${contextPath}/proprietario/menu/prodotti/${prodotto.id?c}/gruppi-caratteristiche">Gestisci gruppi caratteristiche</a>
        <a class="button button--ghost" href="${contextPath}/proprietario/menu/prodotti/${prodotto.id?c}/ingredienti">Gestisci ingredienti</a>
        <a class="button button--ghost" href="${contextPath}/proprietario/menu/prodotti/${prodotto.id?c}/immagini">Gestisci immagini</a>
        <a class="button button--ghost" href="${contextPath}/proprietario/menu">Torna al men&ugrave;</a>
    </div>

    <div class="order-detail-grid owner-product-detail-grid">
        <section class="account-panel" aria-labelledby="dati-prodotto-owner-title">
            <div class="section__header">
                <div>
                    <p class="eyebrow">Prodotto #${prodotto.id?c}</p>
                    <h2 id="dati-prodotto-owner-title">Dati principali</h2>
                </div>
                <span class="status-pill <#if !prodotto.attivo>status-pill--muted</#if>">
                    <#if prodotto.attivo>Attivo<#else>Non attivo</#if>
                </span>
            </div>

            <dl class="account-data-list owner-product-data-list">
                <div>
                    <dt>ID</dt>
                    <dd>#${prodotto.id?c}</dd>
                </div>
                <div>
                    <dt>Categoria</dt>
                    <dd>
                        <#if prodotto.idCategoria??>
                            #${prodotto.idCategoria?c}
                        <#else>
                            -
                        </#if>
                    </dd>
                </div>
                <div>
                    <dt>Nome</dt>
                    <dd>${prodotto.nome}</dd>
                </div>
                <div>
                    <dt>Descrizione</dt>
                    <dd class="owner-menu-long-text">${prodotto.descrizione!"-"}</dd>
                </div>
                <div>
                    <dt>Prezzo base</dt>
                    <dd>EUR ${layout.price(prodotto.prezzoBase)}</dd>
                </div>
                <div>
                    <dt>Preparazione</dt>
                    <dd>
                        <#if prodotto.minutiPreparazione gt 0>
                            ${prodotto.minutiPreparazione} min
                        <#else>
                            -
                        </#if>
                    </dd>
                </div>
                <div>
                    <dt>Note preparazione</dt>
                    <dd class="owner-menu-long-text">${prodotto.descrizionePreparazione!"-"}</dd>
                </div>
                <div>
                    <dt>Creato il</dt>
                    <dd>${layout.displayDate(prodotto.creatoIl!"")}</dd>
                </div>
                <div>
                    <dt>Aggiornato il</dt>
                    <dd>${layout.displayDate(prodotto.aggiornatoIl!"")}</dd>
                </div>
            </dl>
        </section>

        <aside class="order-summary" aria-label="Riepilogo prodotto">
            <h2>Riepilogo</h2>
            <dl class="summary-list">
                <div>
                    <dt>Prezzo</dt>
                    <dd>EUR ${layout.price(prodotto.prezzoBase)}</dd>
                </div>
                <div>
                    <dt>Tempo</dt>
                    <dd>
                        <#if prodotto.minutiPreparazione gt 0>
                            ${prodotto.minutiPreparazione} min
                        <#else>
                            -
                        </#if>
                    </dd>
                </div>
                <div>
                    <dt>Caratteristiche</dt>
                    <dd>${prodotto.caratteristiche?size}</dd>
                </div>
                <div>
                    <dt>Ingredienti</dt>
                    <dd>${prodotto.ingredienti?size}</dd>
                </div>
            </dl>
        </aside>
    </div>

    <section class="account-panel" aria-labelledby="immagini-owner-title">
        <div class="section__header">
            <div>
                <p class="eyebrow">Media</p>
                <h2 id="immagini-owner-title">Immagini prodotto</h2>
            </div>
            <a class="text-link" href="${contextPath}/proprietario/menu/prodotti/${prodotto.id?c}/immagini">Gestisci immagini</a>
        </div>

        <#if prodotto.immagini?has_content>
            <div class="owner-product-image-grid">
                <#list prodotto.immagini as immagine>
                    <figure class="owner-product-image-card">
                        <img src="${layout.publicUrl(contextPath, immagine.url)}" alt="${prodotto.nome}">
                        <figcaption>
                            <span>
                                <#if immagine.principale>Principale<#else>Immagine #${immagine.id?c}</#if>
                            </span>
                            <#if immagine.testoAlternativo?has_content>
                                <small>${immagine.testoAlternativo}</small>
                            </#if>
                        </figcaption>
                    </figure>
                </#list>
            </div>
        <#else>
            <div class="owner-product-image-grid owner-product-image-grid--single">
                <figure class="owner-product-image-card owner-product-image-card--placeholder">
                    <img src="${layout.publicUrl(contextPath, "")}" alt="${prodotto.nome}">
                    <figcaption>
                        <span>Nessuna immagine associata</span>
                        <small>Nel men&ugrave; pubblico viene usato il fallback locale.</small>
                    </figcaption>
                </figure>
            </div>
        </#if>
    </section>

    <section class="account-panel" aria-labelledby="caratteristiche-owner-title">
        <div class="section__header">
            <div>
                <p class="eyebrow">Opzioni</p>
                <h2 id="caratteristiche-owner-title">Caratteristiche associate</h2>
            </div>
        </div>

        <#if prodotto.caratteristiche?has_content>
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
                        </tr>
                    </thead>
                    <tbody>
                        <#list prodotto.caratteristiche as caratteristica>
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
                                        + EUR ${layout.price(caratteristica.differenzaPrezzo)}
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
                            </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        <#else>
            <p class="notice">Nessuna caratteristica associata a questo prodotto.</p>
        </#if>
    </section>

    <section class="account-panel" aria-labelledby="ingredienti-owner-title">
        <div class="section__header">
            <div>
                <p class="eyebrow">Preparazione</p>
                <h2 id="ingredienti-owner-title">Ingredienti associati</h2>
            </div>
        </div>

        <#if prodotto.ingredienti?has_content>
            <div class="orders-table-wrap owner-detail-table-wrap">
                <table class="orders-table owner-ingredients-table">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Nome</th>
                            <th>Quantita</th>
                            <th>Allergene</th>
                            <th>Stato</th>
                        </tr>
                    </thead>
                    <tbody>
                        <#list prodotto.ingredienti as ingrediente>
                            <tr>
                                <td>#${ingrediente.id?c}</td>
                                <td><strong>${ingrediente.nome}</strong></td>
                                <td>
                                    <#if ingrediente.quantita??>
                                        ${ingrediente.quantita?string["0.###"]} ${ingrediente.unitaMisura!}
                                    <#else>
                                        -
                                    </#if>
                                </td>
                                <td><#if ingrediente.allergene>Si<#else>No</#if></td>
                                <td>
                                    <span class="status-pill <#if !ingrediente.attivo>status-pill--muted</#if>">
                                        <#if ingrediente.attivo>Attivo<#else>Non attivo</#if>
                                    </span>
                                </td>
                            </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        <#else>
            <p class="notice">Nessun ingrediente associato a questo prodotto.</p>
        </#if>
    </section>

    <div class="summary-actions summary-actions--inline">
        <a class="button button--primary" href="${contextPath}/proprietario/menu">Torna al men&ugrave;</a>
    </div>
</#if>
</@layout.page>
