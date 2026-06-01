<#import "/layout.ftl" as layout>
<@layout.page title="Dettaglio prodotto" active="menu">
<#if loadError?has_content>
    <section class="status-page">
        <p class="eyebrow">Errore</p>
        <h1>Prodotto non disponibile</h1>
        <p>${loadError}</p>
        <a class="button button--primary" href="${contextPath}/menu">Torna al men&ugrave;</a>
    </section>
<#elseif notFound?? && notFound>
    <section class="status-page">
        <p class="eyebrow">404</p>
        <h1>Prodotto non trovato</h1>
        <p>Il prodotto richiesto non e presente nel men&ugrave; pubblico.</p>
        <a class="button button--primary" href="${contextPath}/menu">Torna al men&ugrave;</a>
    </section>
<#elseif prodotto??>
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

    <article class="product-detail">
        <div class="product-detail__media">
            <img src="${layout.publicUrl(contextPath, imageUrl)}" alt="${prodotto.nome}">
        </div>
        <div class="product-detail__content">
            <a class="text-link" href="${contextPath}/menu">Torna al men&ugrave;</a>
            <p class="eyebrow">Dettaglio prodotto</p>
            <h1>${prodotto.nome}</h1>
            <p class="product-detail__description">${prodotto.descrizione!"Specialita MasterEat pronta da gustare."}</p>
            <p class="product-detail__price">EUR ${layout.price(prodotto.prezzoBase)}</p>

            <#if cartSuccessMessage?has_content>
                <p class="notice notice--success">${cartSuccessMessage}</p>
            </#if>
            <#if cartErrorMessage?has_content>
                <p class="notice notice--error">${cartErrorMessage}</p>
            </#if>

            <#if prodotto.caratteristiche?has_content>
                <section class="detail-section" aria-labelledby="opzioni-title">
                    <h2 id="opzioni-title">Opzioni disponibili</h2>
                    <div class="option-list">
                        <#list prodotto.caratteristiche as caratteristica>
                            <div class="option-item">
                                <div>
                                    <strong>${caratteristica.nome}</strong>
                                    <#if caratteristica.descrizione?has_content>
                                        <p>${caratteristica.descrizione}</p>
                                    </#if>
                                </div>
                                <#if caratteristica.differenzaPrezzo?? && caratteristica.differenzaPrezzo != 0>
                                    <span>+ EUR ${layout.price(caratteristica.differenzaPrezzo)}</span>
                                <#elseif caratteristica.selezionataDefault>
                                    <span>Inclusa</span>
                                </#if>
                            </div>
                        </#list>
                    </div>
                </section>
            </#if>

            <section class="detail-section" aria-labelledby="ordine-title">
                <h2 id="ordine-title">Ordine</h2>
                <#if !isAuthenticated>
                    <div class="order-access-box">
                        <p>Accedi con un account cliente per aggiungere questo prodotto al carrello.</p>
                        <a class="button button--primary" href="${contextPath}/login">Login</a>
                    </div>
                <#elseif currentRole == "CLIENTE">
                    <form class="auth-form product-cart-form" method="post" action="${contextPath}/cliente/carrello/aggiungi">
                        <input type="hidden" name="idProdotto" value="${prodotto.id?c}">

                        <label>
                            <span>Quantita</span>
                            <input type="number" name="quantita" min="1" step="1" value="1" required>
                            <small class="field-help">
                                La quantita indica il numero di prodotti con le stesse opzioni selezionate.
                            </small>
                        </label>

                        <#if caratteristicheGruppi?has_content>
                            <#list caratteristicheGruppi as gruppo>
                                <fieldset class="choice-group">
                                    <legend>${gruppo.nomeGruppo}</legend>
                                    <#if gruppo.descrizioneGruppo?has_content>
                                        <p class="choice-group__description">${gruppo.descrizioneGruppo}</p>
                                    </#if>
                                    <div class="choice-group__options">
                                        <#list gruppo.caratteristiche as caratteristica>
                                            <label class="choice-option">
                                                <input type="radio"
                                                       name="caratteristicheGruppo_${gruppo.idGruppo?c}"
                                                       value="${caratteristica.id?c}"
                                                       <#if caratteristica.selezionataDefault>checked</#if>>
                                                <span class="choice-option__text">
                                                    <strong>${caratteristica.nome}</strong>
                                                    <#if caratteristica.descrizione?has_content>
                                                        <small>${caratteristica.descrizione}</small>
                                                    </#if>
                                                </span>
                                                <#if caratteristica.differenzaPrezzo?? && caratteristica.differenzaPrezzo != 0>
                                                    <span class="choice-option__price">+ EUR ${layout.price(caratteristica.differenzaPrezzo)}</span>
                                                <#elseif caratteristica.selezionataDefault>
                                                    <span class="choice-option__price">Inclusa</span>
                                                </#if>
                                            </label>
                                        </#list>
                                    </div>
                                </fieldset>
                            </#list>
                        </#if>

                        <#if caratteristicheLibere?has_content>
                            <fieldset class="choice-group">
                                <legend>Extra</legend>
                                <div class="choice-group__options">
                                    <#list caratteristicheLibere as caratteristica>
                                        <label class="choice-option">
                                            <input type="checkbox"
                                                   name="caratteristiche"
                                                   value="${caratteristica.id?c}"
                                                   <#if caratteristica.selezionataDefault>checked</#if>>
                                            <span class="choice-option__text">
                                                <strong>${caratteristica.nome}</strong>
                                                <#if caratteristica.descrizione?has_content>
                                                    <small>${caratteristica.descrizione}</small>
                                                </#if>
                                            </span>
                                            <#if caratteristica.differenzaPrezzo?? && caratteristica.differenzaPrezzo != 0>
                                                <span class="choice-option__price">+ EUR ${layout.price(caratteristica.differenzaPrezzo)}</span>
                                            <#elseif caratteristica.selezionataDefault>
                                                <span class="choice-option__price">Inclusa</span>
                                            </#if>
                                        </label>
                                    </#list>
                                </div>
                            </fieldset>
                        </#if>

                        <div class="auth-form__actions">
                            <button class="button button--primary" type="submit">Aggiungi al carrello</button>
                        </div>
                    </form>
                <#else>
                    <p class="notice">Gli ordini web sono disponibili solo per gli account cliente.</p>
                </#if>
            </section>
        </div>
    </article>
</#if>
</@layout.page>
