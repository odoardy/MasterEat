<#import "/layout.ftl" as layout>
<@layout.page title="Carrello" active="cart">
<section class="page-heading">
    <p class="eyebrow">Area cliente</p>
    <h1>Carrello</h1>
    <p>Controlla prodotti, quantita e opzioni selezionate prima del checkout.</p>
</section>

<#if successMessage?has_content>
    <p class="notice notice--success">${successMessage}</p>
</#if>
<#if errorMessage?has_content>
    <p class="notice notice--error">${errorMessage}</p>
</#if>

<#if !cart.items?has_content>
    <section class="empty-state">
        <h2>Il carrello e vuoto</h2>
        <p>Aggiungi un prodotto dal men&ugrave; per procedere con l'ordine.</p>
        <a class="button button--primary" href="${contextPath}/menu">Vai al men&ugrave;</a>
    </section>
<#else>
    <div class="customer-layout">
        <section class="cart-list" aria-label="Prodotti nel carrello">
            <#list cart.items as item>
                <article class="cart-line">
                    <div class="cart-line__body">
                        <div class="cart-line__header">
                            <div>
                                <h2>${item.nomeProdotto}</h2>
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

                    <div class="cart-line__actions" aria-label="Azioni carrello">
                        <div class="cart-line__quantity-actions" aria-label="Modifica quantita">
                            <form method="post" action="${contextPath}/cliente/carrello/decrementa">
                                <input type="hidden" name="itemKey" value="${item.itemKey}">
                                <button class="button button--ghost button--quantity" type="submit"
                                        aria-label="Diminuisci quantita" title="Diminuisci quantita">&minus;</button>
                            </form>
                            <span class="cart-line__quantity-value">${item.quantita}</span>
                            <form method="post" action="${contextPath}/cliente/carrello/incrementa">
                                <input type="hidden" name="itemKey" value="${item.itemKey}">
                                <button class="button button--ghost button--quantity" type="submit"
                                        aria-label="Aumenta quantita" title="Aumenta quantita">+</button>
                            </form>
                        </div>
                        <form method="post" action="${contextPath}/cliente/carrello/rimuovi"
                              data-confirm="Rimuovere tutta questa configurazione dal carrello?">
                            <input type="hidden" name="itemKey" value="${item.itemKey}">
                            <button class="button button--ghost" type="submit">Rimuovi tutto</button>
                        </form>
                    </div>
                </article>
            </#list>
        </section>

        <aside class="order-summary" aria-label="Riepilogo carrello">
            <h2>Riepilogo</h2>
            <dl class="summary-list">
                <div>
                    <dt>Prodotti</dt>
                    <dd>${cart.itemCount}</dd>
                </div>
                <div>
                    <dt>Quantita totale</dt>
                    <dd>${cart.quantitaTotale}</dd>
                </div>
                <div>
                    <dt>Totale</dt>
                    <dd>EUR ${layout.price(cart.totale)}</dd>
                </div>
            </dl>

            <div class="summary-actions">
                <a class="button button--primary" href="${contextPath}/cliente/checkout">Checkout</a>
                <a class="button button--ghost" href="${contextPath}/menu">Continua dal men&ugrave;</a>
                <form method="post" action="${contextPath}/cliente/carrello/svuota" data-confirm="Svuotare il carrello?">
                    <button class="button button--secondary" type="submit">Svuota carrello</button>
                </form>
            </div>
        </aside>
    </div>
</#if>
</@layout.page>
