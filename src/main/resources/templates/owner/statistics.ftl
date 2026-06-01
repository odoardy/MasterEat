<#import "/layout.ftl" as layout>
<@layout.page title="Statistiche proprietario" active="owner-statistics">
<section class="page-heading">
    <p class="eyebrow">Area proprietario</p>
    <h1>Statistiche</h1>
    <p>Riepilogo read-only degli incassi, degli ordini e dei prodotti ordinati nel periodo selezionato.</p>
</section>

<#if errorMessage?has_content>
    <p class="notice notice--error">${errorMessage}</p>
</#if>

<section class="filter-section" aria-label="Filtro statistiche proprietario">
    <form class="filter-bar filter-bar--owner-statistics" method="get" action="${contextPath}/proprietario/statistiche">
        <label>
            <span>Data</span>
            <input type="date" name="data" value="${dataFiltro!statistiche.dataSelezionataIso}">
        </label>

        <div class="filter-bar__actions">
            <button class="button button--primary" type="submit">Filtra</button>
            <a class="button button--ghost" href="${contextPath}/proprietario/statistiche">Oggi</a>
        </div>
    </form>
</section>

<section class="owner-stat-summary" aria-label="Riepilogo statistiche">
    <article class="owner-stat-card">
        <div class="owner-stat-card__header">
            <h2>Giorno</h2>
            <span>${statistiche.dataSelezionataLabel}</span>
        </div>
        <div class="owner-stat-card__metrics">
            <div>
                <span>Incasso</span>
                <strong>EUR ${layout.price(statistiche.riepilogoGiornaliero.incassoTotale)}</strong>
            </div>
            <div>
                <span>Ordini</span>
                <strong>${statistiche.riepilogoGiornaliero.numeroOrdini?c}</strong>
            </div>
        </div>
    </article>

    <article class="owner-stat-card">
        <div class="owner-stat-card__header">
            <h2>Mese</h2>
            <span>${statistiche.meseInizioLabel} - ${statistiche.meseFineLabel}</span>
        </div>
        <div class="owner-stat-card__metrics">
            <div>
                <span>Incasso</span>
                <strong>EUR ${layout.price(statistiche.riepilogoMensile.incassoTotale)}</strong>
            </div>
            <div>
                <span>Ordini</span>
                <strong>${statistiche.riepilogoMensile.numeroOrdini?c}</strong>
            </div>
        </div>
    </article>
</section>

<#if statistiche.riepilogoMensile.numeroOrdini == 0>
    <section class="empty-state empty-state--compact owner-stat-empty-summary">
        <h3>Nessun dato statistico</h3>
        <p>Non risultano ordini nel mese selezionato.</p>
    </section>
</#if>

<div class="owner-stat-tables">
    <section class="owner-stat-table-section" aria-label="Prodotti più ordinati">
        <div class="section__header owner-stat-section-header">
            <div>
                <h2>Prodotti più ordinati</h2>
                <p>Mese ${statistiche.meseLabel}</p>
            </div>
        </div>

        <#if statistiche.prodottiPiuOrdinati?has_content>
            <div class="orders-table-wrap owner-stat-table-wrap">
                <table class="orders-table owner-stat-products-table">
                    <thead>
                        <tr>
                            <th>Prodotto</th>
                            <th>Quantità</th>
                            <th>Ricavo</th>
                        </tr>
                    </thead>
                    <tbody>
                        <#list statistiche.prodottiPiuOrdinati as prodotto>
                            <tr>
                                <td><strong>${prodotto.nomeProdotto}</strong></td>
                                <td>${prodotto.quantitaOrdinata?c}</td>
                                <td>EUR ${layout.price(prodotto.ricavoGenerato)}</td>
                            </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        <#else>
            <section class="empty-state empty-state--compact">
                <h3>Nessun prodotto venduto</h3>
                <p>Non risultano vendite nel mese selezionato.</p>
            </section>
        </#if>
    </section>

    <section class="owner-stat-table-section" aria-label="Prodotti meno ordinati">
        <div class="section__header owner-stat-section-header">
            <div>
                <h2>Prodotti meno ordinati</h2>
                <p>Solo prodotti con almeno una vendita nel mese.</p>
            </div>
        </div>

        <#if statistiche.prodottiMenoOrdinati?has_content>
            <div class="orders-table-wrap owner-stat-table-wrap">
                <table class="orders-table owner-stat-products-table">
                    <thead>
                        <tr>
                            <th>Prodotto</th>
                            <th>Quantità</th>
                            <th>Ricavo</th>
                        </tr>
                    </thead>
                    <tbody>
                        <#list statistiche.prodottiMenoOrdinati as prodotto>
                            <tr>
                                <td><strong>${prodotto.nomeProdotto}</strong></td>
                                <td>${prodotto.quantitaOrdinata?c}</td>
                                <td>EUR ${layout.price(prodotto.ricavoGenerato)}</td>
                            </tr>
                        </#list>
                    </tbody>
                </table>
            </div>
        <#else>
            <section class="empty-state empty-state--compact">
                <h3>Nessun prodotto venduto</h3>
                <p>Non risultano vendite nel mese selezionato.</p>
            </section>
        </#if>
    </section>
</div>
</@layout.page>
