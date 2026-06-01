<#import "/layout.ftl" as layout>
<@layout.page title="Dettaglio ordine staff" active="staff-orders">
<#if errorMessage?has_content && !(ordine??)>
    <section class="status-page">
        <p class="eyebrow">Area personale</p>
        <h1>Ordine</h1>
        <p>${errorMessage}</p>
        <a class="button button--primary" href="${contextPath}/staff/ordini">Torna agli ordini staff</a>
    </section>
<#else>
    <section class="page-heading">
        <p class="eyebrow">Area personale</p>
        <h1>#${ordine.id?c}</h1>
        <p>Dettaglio operativo per preparazione, consegna e avanzamento stato.</p>
    </section>

    <#if successMessage?has_content>
        <p class="notice notice--success">${successMessage}</p>
    </#if>
    <#if errorMessage?has_content>
        <p class="notice notice--error">${errorMessage}</p>
    </#if>

    <div class="order-detail-grid">
        <section class="account-panel" aria-labelledby="riepilogo-ordine-staff-title">
            <div class="section__header">
                <div>
                    <p class="eyebrow">Riepilogo</p>
                    <h2 id="riepilogo-ordine-staff-title">Stato ${ordine.stato}</h2>
                </div>
                <span class="status-pill <#if !ordine.operativo>status-pill--muted</#if>">${ordine.stato}</span>
            </div>

            <dl class="account-data-list">
                <div>
                    <dt>Cliente</dt>
                    <dd>
                        ${ordine.cliente}
                        <#if ordine.usernameCliente?has_content>
                            <span class="staff-inline-muted">@${ordine.usernameCliente}</span>
                        </#if>
                    </dd>
                </div>
                <div>
                    <dt>Inserimento</dt>
                    <dd>${layout.displayDate(ordine.dataInserimento!"")}</dd>
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
            </dl>

            <#if canAdvance>
                <form class="staff-order-action" method="post"
                      action="${contextPath}/staff/ordini/${ordine.id?c}/stato"
                      data-confirm="Avanzare lo stato dell'ordine a ${ordine.prossimoStato}?">
                    <button class="button button--primary" type="submit">Avanza a ${ordine.prossimoStato}</button>
                </form>
            <#else>
                <p class="notice">Questo ordine non prevede ulteriori avanzamenti operativi.</p>
            </#if>
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

    <section class="account-panel" aria-labelledby="prodotti-staff-title">
        <div class="section__header">
            <div>
                <p class="eyebrow">Preparazione</p>
                <h2 id="prodotti-staff-title">Prodotti ordine</h2>
            </div>
        </div>

        <#if righe?has_content>
            <div class="order-lines">
                <#list righe as riga>
                    <article class="order-line-detail staff-order-line">
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

                        <div class="staff-prep-grid">
                            <section aria-label="Tempi preparazione">
                                <h4>Tempi</h4>
                                <p>${riga.minutiPreparazione} min cad. · ${riga.minutiPreparazione * riga.quantita} min totali</p>
                            </section>
                            <section aria-label="Ingredienti">
                                <h4>Ingredienti</h4>
                                <#if riga.ingredienti?has_content>
                                    <div class="ingredient-tags">
                                        <#list riga.ingredienti as ingrediente>
                                            <span>
                                                ${ingrediente.nome}
                                                <small>${ingrediente.quantita?string["0.###"]} ${ingrediente.unitaMisura}</small>
                                            </span>
                                        </#list>
                                    </div>
                                <#else>
                                    <p>Nessun ingrediente censito.</p>
                                </#if>
                            </section>
                            <#if riga.descrizionePreparazione?has_content>
                                <section class="staff-prep-grid__wide" aria-label="Procedura">
                                    <h4>Procedura</h4>
                                    <p>${riga.descrizionePreparazione}</p>
                                </section>
                            </#if>
                        </div>
                    </article>
                </#list>
            </div>
        <#else>
            <p class="notice notice--error">Nessuna riga ordine disponibile.</p>
        </#if>
    </section>

    <section class="account-panel" aria-labelledby="storico-staff-title">
        <div class="section__header">
            <div>
                <p class="eyebrow">Stati</p>
                <h2 id="storico-staff-title">Storico operatori</h2>
            </div>
        </div>

        <#if storico?has_content>
            <ol class="order-history staff-order-history">
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
        <a class="button button--primary" href="${contextPath}/staff/ordini">Torna agli ordini staff</a>
    </div>
</#if>
</@layout.page>
