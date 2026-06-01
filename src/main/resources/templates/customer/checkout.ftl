<#import "/layout.ftl" as layout>
<@layout.page title="Checkout" active="cart">
<section class="page-heading">
    <p class="eyebrow">Area cliente</p>
    <h1>Checkout</h1>
    <p>Verifica riepilogo e dati di consegna prima di confermare l'ordine.</p>
</section>

<#if errorMessage?has_content>
    <p class="notice notice--error">${errorMessage}</p>
</#if>

<div class="checkout-grid">
    <section class="cart-list" aria-label="Riepilogo prodotti">
        <div class="section__header">
            <div>
                <p class="eyebrow">Prodotti</p>
                <h2>Riepilogo ordine</h2>
            </div>
        </div>

        <#list cart.items as item>
            <article class="cart-line cart-line--compact">
                <div class="cart-line__body">
                    <div class="cart-line__header">
                        <div>
                            <h3>${item.nomeProdotto}</h3>
                        </div>
                    </div>

                    <dl class="cart-price-breakdown">
                        <div>
                            <dt>Prezzo base</dt>
                            <dd>EUR ${layout.price(item.prezzoBase)}</dd>
                        </div>
                        <div>
                            <dt>Extra selezionati</dt>
                            <dd class="cart-price-breakdown__options">
                                <#if item.caratteristiche?has_content>
                                    <#list item.caratteristiche as caratteristica>
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
                                    Nessuno
                                </#if>
                            </dd>
                        </div>
                        <div>
                            <dt>Prezzo unitario</dt>
                            <dd>EUR ${layout.price(item.prezzoUnitario)}</dd>
                        </div>
                        <div>
                            <dt>Quantita</dt>
                            <dd>${item.quantita}</dd>
                        </div>
                        <div>
                            <dt>Subtotale</dt>
                            <dd>EUR ${layout.price(item.subtotaleRiga)}</dd>
                        </div>
                    </dl>
                </div>
            </article>
        </#list>
    </section>

    <aside class="order-summary checkout-sticky" aria-label="Conferma checkout">
        <h2>Conferma</h2>
        <dl class="summary-list">
            <div>
                <dt>Totale ordine</dt>
                <dd>EUR ${layout.price(cart.totale)}</dd>
            </div>
            <#if cart.tempoPreparazioneStimato gt 0>
                <div>
                    <dt>Tempo stimato</dt>
                    <dd>${cart.tempoPreparazioneStimato} min</dd>
                </div>
            </#if>
        </dl>

        <section class="address-summary" aria-labelledby="indirizzo-title">
            <h3 id="indirizzo-title">Consegna</h3>
            <#if cliente??>
                <p>
                    ${cliente.indirizzo!""}<br>
                    ${cliente.citta!""}<#if cliente.cap?has_content> ${cliente.cap}</#if><br>
                    Tel. ${cliente.telefono!""}
                </p>
            <#else>
                <p>Dati cliente non disponibili.</p>
            </#if>
        </section>

        <form class="auth-form" method="post" action="${contextPath}/cliente/checkout">
            <label>
                <span>Data e ora di consegna richiesta</span>
                <input type="datetime-local"
                       name="orarioConsegnaRichiesto"
                       value="${selectedDeliveryDateTime!''}"
                       min="${minDeliveryDateTime!''}"
                       step="60"
                       required>
                <small class="field-help">
                    Orario minimo: ora attuale + tempo stimato<#if minDeliveryDateTimeDisplay?has_content>
                        (${layout.displayDate(minDeliveryDateTimeDisplay)})
                    </#if>.
                </small>
            </label>
            <div class="auth-form__actions">
                <button class="button button--primary" type="submit">Conferma ordine</button>
                <a class="button button--ghost" href="${contextPath}/cliente/carrello">Torna al carrello</a>
            </div>
        </form>
    </aside>
</div>
</@layout.page>
