<#import "/layout.ftl" as layout>
<@layout.page title="Ordine confermato" active="cart">
<#if errorMessage?has_content>
    <section class="status-page">
        <p class="eyebrow">Ordine</p>
        <h1>Conferma non disponibile</h1>
        <p>${errorMessage}</p>
        <a class="button button--primary" href="${contextPath}/menu">Torna al men&ugrave;</a>
    </section>
<#elseif ordine??>
    <section class="status-page order-confirmation">
        <p class="eyebrow">Ordine confermato</p>
        <h1>Ordine #${ordine.id?c}</h1>
        <p>Il tuo ordine e stato registrato con stato ${ordine.stato}.</p>

        <dl class="summary-list summary-list--wide">
            <div>
                <dt>Totale</dt>
                <dd>EUR ${layout.price(ordine.prezzoTotale)}</dd>
            </div>
            <#if ordine.minutiConsegnaStimati??>
                <div>
                    <dt>Tempo stimato</dt>
                    <dd>${ordine.minutiConsegnaStimati} min</dd>
                </div>
            </#if>
            <div>
                <dt>Orario richiesto</dt>
                <dd>${layout.displayDate(orarioConsegnaRichiesto!"")}</dd>
            </div>
            <div>
                <dt>Consegna</dt>
                <dd>
                    ${ordine.indirizzoConsegnaSnapshot!""},
                    ${ordine.cittaConsegnaSnapshot!""}
                    <#if ordine.capConsegnaSnapshot?has_content>${ordine.capConsegnaSnapshot}</#if>
                </dd>
            </div>
            <div>
                <dt>Telefono</dt>
                <dd>${ordine.telefonoConsegnaSnapshot!""}</dd>
            </div>
        </dl>

        <div class="summary-actions summary-actions--inline">
            <a class="button button--primary" href="${contextPath}/menu">Torna al men&ugrave;</a>
            <a class="button button--ghost" href="${contextPath}/home">Home</a>
        </div>
    </section>
</#if>
</@layout.page>
