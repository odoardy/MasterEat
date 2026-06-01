<#import "/layout.ftl" as layout>
<@layout.page title="Home" active="home">
<section class="hero">
    <div class="hero__copy">
        <p class="eyebrow">Food delivery locale</p>
        <h1>MasterEat</h1>
        <p class="hero__lead">Ordina cibo e molto altro, in pochi click.</p>
        <div class="hero__actions">
            <a class="button button--primary" href="${contextPath}/menu">Sfoglia il men&ugrave;</a>
        </div>
    </div>
    <div class="hero__visual hero__visual--icon">
        <img class="hero__logo-mark" src="${contextPath}/assets/img/logo/mastereat-icon.svg" alt="Simbolo MasterEat">
    </div>
</section>

<section class="section service-hours" aria-labelledby="service-hours-title">
    <div class="section__header">
        <div>
            <p class="eyebrow">Orari cucina</p>
            <h2 id="service-hours-title">Mercoled&igrave; - Domenica</h2>
        </div>
    </div>

    <div class="service-hours__content">
        <div class="service-hours__list">
            <p><strong>Pranzo</strong><span>12:00 - 15:00</span></p>
            <p><strong>Cena</strong><span>20:00 - 23:00</span></p>
        </div>
        <p>Le consegne possono essere programmate in base al tempo stimato di preparazione e alla disponibilit&agrave; del servizio.</p>
    </div>
</section>

<section class="section">
    <div class="section__header">
        <p class="eyebrow">In evidenza</p>
        <h2>Proposte dal men&ugrave;</h2>
        <a class="text-link" href="${contextPath}/menu">Vedi tutto</a>
    </div>

    <#if loadError?has_content>
        <p class="notice notice--error">${loadError}</p>
    </#if>

    <#if prodottiInEvidenza?has_content>
        <div class="product-grid">
            <#list prodottiInEvidenza as prodotto>
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
            <h3>Men&ugrave; in aggiornamento</h3>
            <p>I prodotti pubblici compariranno qui appena disponibili nel database.</p>
        </div>
    </#if>
</section>
</@layout.page>
