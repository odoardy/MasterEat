<#import "/layout.ftl" as layout>
<@layout.page title="Personale" active="owner-staff">
<section class="page-heading">
    <p class="eyebrow">Area proprietario</p>
    <h1>Personale</h1>
    <p>Elenco dei membri del personale abilitati all'area operativa.</p>
</section>

<#if successMessage?has_content>
    <p class="notice notice--success">${successMessage}</p>
</#if>
<#if errorMessage?has_content>
    <p class="notice notice--error">${errorMessage}</p>
</#if>

<div class="summary-actions summary-actions--inline owner-actions">
    <a class="button button--primary" href="${contextPath}/proprietario/personale/nuovo">Nuovo membro personale</a>
    <a class="button button--ghost" href="${contextPath}/proprietario/ordini">Monitora ordini</a>
</div>

<#if personale?has_content>
    <section class="orders-table-wrap" aria-label="Personale">
        <table class="orders-table owner-staff-table">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Username</th>
                    <th>Nome</th>
                    <th>Cognome</th>
                    <th>Email</th>
                    <th>Telefono</th>
                    <th>Creazione</th>
                </tr>
            </thead>
            <tbody>
                <#list personale as membro>
                    <tr>
                        <td>#${membro.id?c}</td>
                        <td><strong>${membro.username}</strong></td>
                        <td>${membro.nome}</td>
                        <td>${membro.cognome}</td>
                        <td>${membro.email!"-"}</td>
                        <td>${membro.telefono!"-"}</td>
                        <td>${layout.displayDate(membro.creatoIl!"")}</td>
                    </tr>
                </#list>
            </tbody>
        </table>
    </section>
<#else>
    <section class="empty-state">
        <h2>Nessun membro personale</h2>
        <p>Non ci sono ancora utenti con ruolo PERSONALE.</p>
        <a class="button button--primary" href="${contextPath}/proprietario/personale/nuovo">Crea il primo membro</a>
    </section>
</#if>
</@layout.page>
