<#import "/layout.ftl" as layout>
<@layout.page title="Dettaglio ordine" active="orders">
<#if errorMessage?has_content && !(ordine??)>
    <section class="status-page">
        <p class="eyebrow">Ordine</p>
        <h1>Dettaglio</h1>
        <p>${errorMessage}</p>
        <a class="button button--primary" href="${contextPath}/cliente/ordini">Torna agli ordini</a>
    </section>
<#else>
    <section class="page-heading">
        <p class="eyebrow">Ordine</p>
        <h1>#${ordine.id?c}</h1>
        <p>Dettaglio completo dell'ordine e dei prodotti selezionati.</p>
    </section>

    <#if successMessage?has_content>
        <p class="notice notice--success">${successMessage}</p>
    </#if>
    <#if errorMessage?has_content>
        <p class="notice notice--error">${errorMessage}</p>
    </#if>

    <div class="order-detail-grid customer-order-detail-grid">
        <section class="account-panel" aria-labelledby="riepilogo-ordine-title">
            <div class="section__header">
                <div>
                    <p class="eyebrow">Riepilogo</p>
                    <h2 id="riepilogo-ordine-title">Stato ${ordine.stato}</h2>
                </div>
                <span class="status-pill">${ordine.stato}</span>
            </div>

            <dl class="account-data-list">
                <div>
                    <dt>Creato il</dt>
                    <dd>${layout.displayDate(ordine.creatoIl!"")}</dd>
                </div>
                <div>
                    <dt>Confermato il</dt>
                    <dd>${layout.displayDate(ordine.confermatoIl!"")}</dd>
                </div>
                <div>
                    <dt>Totale</dt>
                    <dd>EUR ${layout.price(ordine.prezzoTotale)}</dd>
                </div>
                <div>
                    <dt>Tempo stimato</dt>
                    <dd>
                        <#if ordine.minutiConsegnaStimati??>
                            ${ordine.minutiConsegnaStimati} min
                        <#else>
                            -
                        </#if>
                    </dd>
                </div>
                <#if ordine.annullatoIl?has_content>
                    <div>
                        <dt>Annullato il</dt>
                        <dd>${layout.displayDate(ordine.annullatoIl)}</dd>
                    </div>
                </#if>
            </dl>

            <#if canCancel>
                <form class="order-cancel-form" method="post"
                      action="${contextPath}/cliente/ordini/${ordine.id?c}/annulla"
                      data-confirm="Annullare questo ordine?">
                    <button class="button button--ghost" type="submit">Annulla ordine</button>
                </form>
            </#if>
        </section>

        <aside class="account-panel customer-delivery-panel" aria-label="Consegna ordine">
            <h2>Consegna</h2>
            <dl class="summary-list">
                <div>
                    <dt>Indirizzo</dt>
                    <dd>
                        ${ordine.indirizzoConsegnaSnapshot!""}<br>
                        ${ordine.cittaConsegnaSnapshot!""}
                        <#if ordine.capConsegnaSnapshot?has_content>${ordine.capConsegnaSnapshot}</#if>
                    </dd>
                </div>
                <div>
                    <dt>Telefono</dt>
                    <dd>${ordine.telefonoConsegnaSnapshot!""}</dd>
                </div>
                <div>
                    <dt>Orario richiesto</dt>
                    <dd>${layout.displayDate(orarioConsegnaRichiesto!"")}</dd>
                </div>
            </dl>
        </aside>
    </div>

    <section class="account-panel" aria-labelledby="prodotti-ordine-title">
        <div class="section__header">
            <div>
                <p class="eyebrow">Prodotti</p>
                <h2 id="prodotti-ordine-title">Righe ordine</h2>
            </div>
        </div>

        <#if righe?has_content>
            <div class="order-lines">
                <#list righe as riga>
                    <article class="order-line-detail">
                        <div class="order-line-detail__header">
                            <div>
                                <h3>${riga.nomeProdotto}</h3>
                                <p>Quantita ${riga.quantita} · prodotto #${riga.idProdotto?c}</p>
                            </div>
                            <strong>EUR ${layout.price(riga.subtotaleRiga)}</strong>
                        </div>

                        <dl class="cart-price-breakdown">
                            <div>
                                <dt>Prezzo base snapshot</dt>
                                <dd>EUR ${layout.price(riga.prezzoBase)}</dd>
                            </div>
                            <div>
                                <dt>Caratteristiche selezionate</dt>
                                <dd class="cart-price-breakdown__options">
                                    <#if riga.caratteristiche?has_content>
                                        <#list riga.caratteristiche as caratteristica>
                                            <span>
                                                ${caratteristica.nome}
                                                <small>
                                                    <#if caratteristica.differenzaPrezzo?? && caratteristica.differenzaPrezzo != 0>
                                                        + EUR ${layout.price(caratteristica.differenzaPrezzo)}
                                                    <#else>
                                                        Inclusa
                                                    </#if>
                                                </small>
                                            </span>
                                        </#list>
                                    <#else>
                                        Nessuna
                                    </#if>
                                </dd>
                            </div>
                            <div>
                                <dt>Subtotale</dt>
                                <dd>EUR ${layout.price(riga.subtotaleRiga)}</dd>
                            </div>
                        </dl>
                    </article>
                </#list>
            </div>
        <#else>
            <p class="notice notice--error">Nessuna riga ordine disponibile.</p>
        </#if>
    </section>

    <section class="account-panel" aria-labelledby="storico-ordine-title">
        <div class="section__header">
            <div>
                <p class="eyebrow">Stati</p>
                <h2 id="storico-ordine-title">Storico</h2>
            </div>
        </div>

        <#if storico?has_content>
            <ol class="order-history">
                <#list storico as cambio>
                    <li>
                        <div>
                            <strong>
                                <#if cambio.statoPrecedente??>${cambio.statoPrecedente} -> </#if>${cambio.statoNuovo}
                            </strong>
                            <span>${layout.displayDate(cambio.modificatoIl!"")}</span>
                        </div>
                        <#if cambio.nota?has_content>
                            <p>${cambio.nota}</p>
                        </#if>
                    </li>
                </#list>
            </ol>
        <#else>
            <p class="notice">Nessuno storico disponibile.</p>
        </#if>
    </section>

    <div class="summary-actions summary-actions--inline">
        <a class="button button--primary" href="${contextPath}/cliente/ordini">Torna agli ordini</a>
        <a class="button button--ghost" href="${contextPath}/cliente/account">Account</a>
    </div>
</#if>
</@layout.page>
