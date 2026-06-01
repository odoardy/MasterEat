<#import "/layout.ftl" as layout>
<@layout.page title="Immagini prodotto" active="owner-menu">
<#if errorMessage?has_content && !(prodotto??)>
    <section class="status-page">
        <p class="eyebrow">Area proprietario</p>
        <h1>Immagini prodotto</h1>
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
        <h1>Immagini prodotto</h1>
        <p>Gestione delle immagini associate a ${prodotto.nome}.</p>
    </section>

    <#if successMessage?has_content>
        <p class="notice notice--success">${successMessage}</p>
    </#if>
    <#if errorMessage?has_content>
        <p class="notice notice--error">${errorMessage}</p>
    </#if>

    <div class="summary-actions summary-actions--inline owner-actions">
        <a class="button button--ghost" href="${contextPath}/proprietario/menu/prodotti/${prodotto.id?c}">Torna al prodotto</a>
        <a class="button button--ghost" href="${contextPath}/proprietario/menu">Torna al men&ugrave;</a>
    </div>

    <section class="account-panel owner-image-primary-panel" aria-labelledby="immagine-principale-title">
        <div class="section__header">
            <div>
                <p class="eyebrow">Prodotto #${prodotto.id?c}</p>
                <h2 id="immagine-principale-title">Anteprima principale</h2>
            </div>
            <span class="status-pill <#if !prodotto.attivo>status-pill--muted</#if>">
                <#if prodotto.attivo>Prodotto attivo<#else>Prodotto non attivo</#if>
            </span>
        </div>

        <#if immaginePrincipale??>
            <figure class="owner-product-image-card owner-image-primary-card">
                <img src="${layout.publicUrl(contextPath, immaginePrincipale.url)}" alt="${immaginePrincipale.testoAlternativo!prodotto.nome}">
                <figcaption>
                    <span>Principale</span>
                    <small>${immaginePrincipale.nomeFileOriginale!"File immagine"}</small>
                </figcaption>
            </figure>
        <#else>
            <figure class="owner-product-image-card owner-product-image-card--placeholder owner-image-primary-card">
                <img src="${layout.publicUrl(contextPath, "")}" alt="${prodotto.nome}">
                <figcaption>
                    <span>Nessuna immagine associata</span>
                    <small>Nel men&ugrave; pubblico viene usato il fallback locale.</small>
                </figcaption>
            </figure>
        </#if>
    </section>

    <section class="auth-panel auth-panel--wide owner-image-upload-panel" aria-labelledby="upload-immagine-title">
        <div class="section__header">
            <div>
                <p class="eyebrow">Upload</p>
                <h2 id="upload-immagine-title">Nuova immagine</h2>
            </div>
        </div>

        <form class="auth-form owner-image-upload-form"
              method="post"
              enctype="multipart/form-data"
              action="${contextPath}/proprietario/menu/prodotti/${prodotto.id?c}/immagini">
            <div class="auth-form__grid">
                <label class="auth-form__field-wide">
                    <span>File immagine</span>
                    <input type="file"
                           name="immagine"
                           accept="image/jpeg,image/png,image/webp,image/gif"
                           required>
                    <small class="field-help">Formati ammessi: JPG, PNG, WEBP, GIF. Dimensione massima: 3 MB.</small>
                </label>

                <label class="auth-form__field-wide">
                    <span>Testo alternativo</span>
                    <input type="text"
                           name="testoAlternativo"
                           maxlength="255"
                           placeholder="${prodotto.nome}">
                </label>

                <label class="owner-product-form__check">
                    <input type="checkbox" name="principale" value="true">
                    <span>Imposta come principale</span>
                </label>
            </div>

            <div class="auth-form__actions">
                <button class="button button--primary" type="submit">Carica immagine</button>
            </div>
        </form>
    </section>

    <section class="account-panel owner-images-list-panel" aria-labelledby="immagini-associate-title">
        <div class="section__header">
            <div>
                <p class="eyebrow">Media</p>
                <h2 id="immagini-associate-title">Immagini associate</h2>
            </div>
        </div>

        <#if immagini?has_content>
            <div class="owner-product-image-grid owner-product-image-grid--manage">
                <#list immagini as immagine>
                    <figure class="owner-product-image-card owner-product-image-card--manage">
                        <img src="${layout.publicUrl(contextPath, immagine.url)}" alt="${immagine.testoAlternativo!prodotto.nome}">
                        <figcaption>
                            <span>
                                <#if immagine.principale>Principale<#else>Immagine #${immagine.id?c}</#if>
                            </span>
                            <small>${immagine.nomeFileOriginale!"File immagine"}</small>
                            <small>${immagine.tipoContenuto!"-"} - ${(immagine.dimensioneByte / 1024)?string["0.#"]} KB</small>
                            <#if immagine.testoAlternativo?has_content>
                                <small>${immagine.testoAlternativo}</small>
                            </#if>
                            <div class="owner-table-actions owner-image-actions">
                                <#if !immagine.principale>
                                    <form method="post" action="${contextPath}/proprietario/menu/prodotti/${prodotto.id?c}/immagini/${immagine.id?c}/principale">
                                        <button class="button button--ghost button--compact" type="submit">Imposta come principale</button>
                                    </form>
                                </#if>
                                <form method="post"
                                      action="${contextPath}/proprietario/menu/prodotti/${prodotto.id?c}/immagini/${immagine.id?c}/rimuovi"
                                      data-confirm="Rimuovere questa immagine?">
                                    <button class="button button--danger button--compact" type="submit">Rimuovi</button>
                                </form>
                            </div>
                        </figcaption>
                    </figure>
                </#list>
            </div>
        <#else>
            <section class="empty-state empty-state--compact">
                <h3>Nessuna immagine</h3>
                <p>Non ci sono ancora immagini associate a questo prodotto.</p>
            </section>
        </#if>
    </section>
</#if>
</@layout.page>
