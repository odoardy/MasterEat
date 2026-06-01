<#import "/layout.ftl" as layout>
<@layout.page title="Dettaglio ordine proprietario" active="owner-orders">
<#if errorMessage?has_content && !(ordine??)>
    <section class="status-page">
        <p class="eyebrow">Area proprietario</p>
        <h1>Ordine</h1>
        <p>${errorMessage}</p>
        <a class="button button--primary" href="${contextPath}/proprietario/ordini">Torna agli ordini</a>
    </section>
<#else>
    <section class="page-heading">
        <p class="eyebrow">Area proprietario</p>
        <h1>#${ordine.id?c}</h1>
        <p>Vista read-only per monitorare cliente, prodotti e storico stati.</p>
    </section>

    <div class="order-detail-grid">
        <section class="account-panel" aria-labelledby="riepilogo-ordine-owner-title">
            <div class="section__header">
                <div>
                    <p class="eyebrow">Riepilogo</p>
                    <h2 id="riepilogo-ordine-owner-title">Stato ${ordine.stato}</h2>
                </div>
                <span class="status-pill">${ordine.stato}</span>
            </div>

            <dl class="account-data-list">
                <div>
                    <dt>Cliente</dt>
                    <dd>
                        ${ordine.cliente}
                        <#if ordine.usernameCliente?has_content>
                            <span class="owner-inline-muted">@${ordine.usernameCliente}</span>
                        </#if>
                    </dd>
                </div>
                <div>
                    <dt>Creato il</dt>
                    <dd>${layout.displayDate(ordine.creatoIl!"")}</dd>
                </div>
                <div>
                    <dt>Confermato il</dt>
                    <dd>${layout.displayDate(ordine.confermatoIl!"")}</dd>
                </div>
                <div>
                    <dt>Consegna richiesta</dt>
                    <dd>${layout.displayDate(ordine.orarioConsegnaRichiesto!"")}</dd>
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
                <div>
                    <dt>Prodotti</dt>
                    <dd>${ordine.numeroProdotti}</dd>
                </div>
                <div>
                    <dt>Operatori</dt>
                    <dd>${ordine.operatoriRiepilogo!"Nessun operatore"}</dd>
                </div>
            </dl>
        </section>

        <aside class="order-summary" aria-label="Consegna ordine">
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
            </dl>
        </aside>
    </div>

    <section class="account-panel" aria-labelledby="prodotti-owner-title">
        <div class="section__header">
            <div>
                <p class="eyebrow">Prodotti</p>
                <h2 id="prodotti-owner-title">Righe ordine</h2>
            </div>
        </div>

        <#if righe?has_content>
            <div class="order-lines">
                <#list righe as riga>
                    <article class="order-line-detail">
                        <div class="order-line-detail__header">
                            <div>
                                <h3>${riga.nomeProdotto}</h3>
                                <p>Quantità ${riga.quantita} · prodotto #${riga.idProdotto?c}</p>
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

    <section class="account-panel" aria-labelledby="storico-owner-title">
        <div class="section__header">
            <div>
                <p class="eyebrow">Stati</p>
                <h2 id="storico-owner-title">Storico cambi stato</h2>
            </div>
        </div>

        <#if storico?has_content>
            <ol class="order-history owner-order-history">
                <#list storico as cambio>
                    <#assign nomeOperatore = "Sistema">
                    <#if cambio.nomeUtenteModifica?has_content || cambio.cognomeUtenteModifica?has_content>
                        <#assign nomeOperatore = ((cambio.nomeUtenteModifica!"") + " " + (cambio.cognomeUtenteModifica!""))?trim>
                    <#elseif cambio.usernameUtenteModifica?has_content>
                        <#assign nomeOperatore = cambio.usernameUtenteModifica>
                    </#if>
                    <li>
                        <div>
                            <strong>
                                <#if cambio.statoPrecedente??>${cambio.statoPrecedente} -> </#if>${cambio.statoNuovo}
                            </strong>
                            <span>${layout.displayDate(cambio.modificatoIl!"")}</span>
                            <span>${nomeOperatore}<#if cambio.ruoloUtenteModifica?has_content> · ${cambio.ruoloUtenteModifica}</#if></span>
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
        <a class="button button--primary" href="${contextPath}/proprietario/ordini">Torna agli ordini</a>
    </div>
</#if>
</@layout.page>
