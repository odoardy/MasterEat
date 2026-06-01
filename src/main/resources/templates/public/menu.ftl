<#import "/layout.ftl" as layout>
<@layout.page title="Menù" active="menu">
<section class="page-heading">
    <p class="eyebrow">MEN&Ugrave;</p>
    <h1>Scegli il tuo prossimo piatto</h1>
    <p>Consulta categorie, prodotti e prezzi.</p>
</section>

<section class="filter-section" aria-label="Filtra prodotti">
    <form class="filter-bar" method="get" action="${contextPath}/menu">
        <label>
            <span>Cerca</span>
            <input type="search" name="q" value="${q!}" placeholder="Pizza, bowl, dolce">
        </label>
        <label>
            <span>Prezzo min</span>
            <input type="number" name="prezzoMin" value="${prezzoMin!}" min="0" step="0.01" placeholder="0.00">
        </label>
        <label>
            <span>Prezzo max</span>
            <input type="number" name="prezzoMax" value="${prezzoMax!}" min="0" step="0.01" placeholder="25.00">
        </label>
        <div class="filter-bar__actions">
            <button class="button button--primary" type="submit">Filtra</button>
            <a class="button button--ghost" href="${contextPath}/menu">Reset</a>
        </div>
    </form>
</section>

<#if filterError?has_content>
    <p class="notice notice--error">${filterError}</p>
</#if>
<#if loadError?has_content>
    <p class="notice notice--error">${loadError}</p>
</#if>

<#if hasFilters>
    <section class="section">
        <div class="section__header">
            <p class="eyebrow">Risultati</p>
            <h2>Prodotti trovati</h2>
        </div>

        <#if prodottiFiltrati?has_content>
            <div class="product-grid">
                <#list prodottiFiltrati as prodotto>
                    <#assign imageUrl = "">
                    <#list prodotto.immagini as immagine>
                        <#if immagine.principale && immagine.url?has_content>
                            <#assign imageUrl = immagine.url>
                            <#break>
                        </#if>
                    </#list>
                    <#if !imageUrl?has_content && prodotto.immagini?has_content && prodotto.immagini[0].url?has_content>
                        <#assign imageUrl = prodotto.immagini[0].url>
                    </#if>

                    <article class="product-card">
                        <a class="product-card__media" href="${contextPath}/prodotti/${prodotto.id}">
                            <img src="${layout.publicUrl(contextPath, imageUrl)}" alt="${prodotto.nome}">
                        </a>
                        <div class="product-card__body">
                            <h3><a href="${contextPath}/prodotti/${prodotto.id}">${prodotto.nome}</a></h3>
                            <p>${prodotto.descrizione!"Specialita MasterEat pronta da gustare."}</p>
                            <div class="product-card__meta">
                                <strong>EUR ${layout.price(prodotto.prezzoBase)}</strong>
                                <a class="button button--compact" href="${contextPath}/prodotti/${prodotto.id}">Dettagli</a>
                            </div>
                        </div>
                    </article>
                </#list>
            </div>
        <#else>
            <div class="empty-state">
                <h3>Nessun prodotto trovato</h3>
                <p>Prova a modificare i filtri o torna al men&ugrave; completo.</p>
            </div>
        </#if>
    </section>
<#else>
    <#if menu.categorie?has_content>
        <#list menu.categorie as categoria>
            <section class="section menu-category">
                <div class="section__header">
                    <div>
                        <p class="eyebrow">Categoria</p>
                        <h2>${categoria.nome}</h2>
                        <#if categoria.descrizione?has_content>
                            <p>${categoria.descrizione}</p>
                        </#if>
                    </div>
                </div>

                <#if categoria.prodotti?has_content>
                    <div class="product-grid">
                        <#list categoria.prodotti as prodotto>
                            <#assign imageUrl = "">
                            <#list prodotto.immagini as immagine>
                                <#if immagine.principale && immagine.url?has_content>
                                    <#assign imageUrl = immagine.url>
                                    <#break>
                                </#if>
                            </#list>
                            <#if !imageUrl?has_content && prodotto.immagini?has_content && prodotto.immagini[0].url?has_content>
                                <#assign imageUrl = prodotto.immagini[0].url>
                            </#if>

                            <article class="product-card">
                                <a class="product-card__media" href="${contextPath}/prodotti/${prodotto.id}">
                                    <img src="${layout.publicUrl(contextPath, imageUrl)}" alt="${prodotto.nome}">
                                </a>
                                <div class="product-card__body">
                                    <h3><a href="${contextPath}/prodotti/${prodotto.id}">${prodotto.nome}</a></h3>
                                    <p>${prodotto.descrizione!"Specialita MasterEat pronta da gustare."}</p>
                                    <div class="product-card__meta">
                                        <strong>EUR ${layout.price(prodotto.prezzoBase)}</strong>
                                        <a class="button button--compact" href="${contextPath}/prodotti/${prodotto.id}">Dettagli</a>
                                    </div>
                                </div>
                            </article>
                        </#list>
                    </div>
                <#else>
                    <div class="empty-state empty-state--compact">
                        <h3>Nessun prodotto in questa categoria</h3>
                        <p>I prodotti compariranno qui appena disponibili.</p>
                    </div>
                </#if>
            </section>
        </#list>
    </#if>

    <#if menu.prodottiSenzaCategoria?has_content>
        <section class="section menu-category">
            <div class="section__header">
                <div>
                    <p class="eyebrow">Extra</p>
                    <h2>Altri prodotti</h2>
                </div>
            </div>

            <div class="product-grid">
                <#list menu.prodottiSenzaCategoria as prodotto>
                    <#assign imageUrl = "">
                    <#list prodotto.immagini as immagine>
                        <#if immagine.principale && immagine.url?has_content>
                            <#assign imageUrl = immagine.url>
                            <#break>
                        </#if>
                    </#list>
                    <#if !imageUrl?has_content && prodotto.immagini?has_content && prodotto.immagini[0].url?has_content>
                        <#assign imageUrl = prodotto.immagini[0].url>
                    </#if>

                    <article class="product-card">
                        <a class="product-card__media" href="${contextPath}/prodotti/${prodotto.id}">
                            <img src="${layout.publicUrl(contextPath, imageUrl)}" alt="${prodotto.nome}">
                        </a>
                        <div class="product-card__body">
                            <h3><a href="${contextPath}/prodotti/${prodotto.id}">${prodotto.nome}</a></h3>
                            <p>${prodotto.descrizione!"Specialita MasterEat pronta da gustare."}</p>
                            <div class="product-card__meta">
                                <strong>EUR ${layout.price(prodotto.prezzoBase)}</strong>
                                <a class="button button--compact" href="${contextPath}/prodotti/${prodotto.id}">Dettagli</a>
                            </div>
                        </div>
                    </article>
                </#list>
            </div>
        </section>
    </#if>

    <#if !menu.categorie?has_content && !menu.prodottiSenzaCategoria?has_content && !loadError?has_content>
        <section class="section">
            <div class="empty-state">
                <h3>Men&ugrave; in aggiornamento</h3>
                <p>I prodotti pubblici compariranno qui appena disponibili nel database.</p>
            </div>
        </section>
    </#if>
</#if>
</@layout.page>
