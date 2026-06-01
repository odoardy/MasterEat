<#import "/layout.ftl" as layout>
<@layout.page title="Account" active="account">
<#if errorMessage?has_content && !(cliente??)>
    <section class="status-page">
        <p class="eyebrow">Area cliente</p>
        <h1>Account</h1>
        <p>${errorMessage}</p>
        <a class="button button--primary" href="${contextPath}/home">Torna alla home</a>
    </section>
<#else>
    <section class="page-heading">
        <p class="eyebrow">Area cliente</p>
        <h1>Account</h1>
        <p>Profilo, dati di consegna e riepilogo degli ordini recenti.</p>
    </section>

    <#if successMessage?has_content>
        <p class="notice notice--success">${successMessage}</p>
    </#if>
    <#if errorMessage?has_content>
        <p class="notice notice--error">${errorMessage}</p>
    </#if>

    <div class="account-grid">
        <section class="account-panel" aria-labelledby="dati-account-title">
            <div class="section__header">
                <div>
                    <p class="eyebrow">Profilo</p>
                    <h2 id="dati-account-title">${cliente.nome} ${cliente.cognome}</h2>
                </div>
                <a class="button button--ghost" href="${contextPath}/cliente/profilo">Modifica profilo</a>
            </div>

            <dl class="account-data-list">
                <div>
                    <dt>Username</dt>
                    <dd>${cliente.username}</dd>
                </div>
                <div>
                    <dt>Email</dt>
                    <dd>${cliente.email}</dd>
                </div>
                <div>
                    <dt>Telefono</dt>
                    <dd>${cliente.telefono!""}</dd>
                </div>
                <div>
                    <dt>Indirizzo</dt>
                    <dd>
                        ${cliente.indirizzo!""}<br>
                        ${cliente.citta!""}<#if cliente.cap?has_content> ${cliente.cap}</#if>
                    </dd>
                </div>
            </dl>

            <div class="summary-actions summary-actions--inline">
                <a class="button button--primary" href="${contextPath}/cliente/ordini">I miei ordini</a>
                <a class="button button--ghost" href="${contextPath}/cliente/carrello">Carrello</a>
            </div>
        </section>

        <section class="account-panel" aria-labelledby="ultimi-ordini-title">
            <div class="section__header">
                <div>
                    <p class="eyebrow">Ordini</p>
                    <h2 id="ultimi-ordini-title">Ultimi ordini</h2>
                </div>
                <a class="text-link" href="${contextPath}/cliente/ordini">Storico completo</a>
            </div>

            <#if ultimiOrdini?has_content>
                <div class="order-list-compact">
                    <#list ultimiOrdini as ordine>
                        <#assign dataOrdine = ordine.creatoIl!"">
                        <#if ordine.confermatoIl?has_content>
                            <#assign dataOrdine = ordine.confermatoIl>
                        </#if>
                        <article class="order-row-compact">
                            <div class="order-row-compact__body">
                                <h3>Ordine #${ordine.id?c}</h3>
                                <p>${layout.displayDate(dataOrdine)}</p>
                            </div>
                            <div class="order-row-compact__meta">
                                <span class="status-pill">${ordine.stato}</span>
                                <strong>EUR ${layout.price(ordine.prezzoTotale)}</strong>
                                <a class="text-link" href="${contextPath}/cliente/ordini/${ordine.id?c}">Dettaglio</a>
                            </div>
                        </article>
                    </#list>
                </div>
            <#else>
                <div class="empty-state empty-state--compact">
                    <h3>Nessun ordine</h3>
                    <p>Gli ordini confermati dal checkout compariranno qui.</p>
                    <a class="button button--primary" href="${contextPath}/menu">Vai al men&ugrave;</a>
                </div>
            </#if>
        </section>
    </div>
</#if>
</@layout.page>
