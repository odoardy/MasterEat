<#import "/layout.ftl" as layout>
<@layout.page title="Profilo" active="profile">
<section class="page-heading">
    <p class="eyebrow">Area cliente</p>
    <h1>Profilo</h1>
    <p>Aggiorna i dati usati per account e consegna.</p>
</section>

<#if successMessage?has_content>
    <p class="notice notice--success">${successMessage}</p>
</#if>
<#if errorMessage?has_content>
    <p class="notice notice--error">${errorMessage}</p>
</#if>

<section class="auth-panel auth-panel--wide" aria-label="Modifica profilo cliente">
    <#if errors?has_content>
        <div class="notice notice--error" role="alert">
            <p>Controlla i dati inseriti:</p>
            <ul class="form-errors">
                <#list errors as error>
                    <li>${error}</li>
                </#list>
            </ul>
        </div>
    </#if>

    <#if cliente??>
        <dl class="readonly-account-data">
            <div>
                <dt>ID utente</dt>
                <dd>${cliente.id?c}</dd>
            </div>
            <div>
                <dt>Username</dt>
                <dd>${cliente.username}</dd>
            </div>
            <div>
                <dt>Ruolo</dt>
                <dd>${cliente.ruolo}</dd>
            </div>
        </dl>
    </#if>

    <form class="auth-form" method="post" action="${contextPath}/cliente/profilo">
        <div class="auth-form__grid">
            <label>
                <span>Nome</span>
                <input type="text" name="nome" value="${form.nome!}" autocomplete="given-name"
                       maxlength="80" required>
            </label>

            <label>
                <span>Cognome</span>
                <input type="text" name="cognome" value="${form.cognome!}" autocomplete="family-name"
                       maxlength="80" required>
            </label>

            <label>
                <span>Email</span>
                <input type="email" name="email" value="${form.email!}" autocomplete="email"
                       maxlength="255" required>
            </label>

            <label>
                <span>Telefono</span>
                <input type="tel" name="telefono" value="${form.telefono!}" autocomplete="tel"
                       minlength="6" maxlength="30" pattern="[+0-9 .()\-]{6,30}" required>
            </label>

            <label class="auth-form__field-wide">
                <span>Indirizzo</span>
                <input type="text" name="indirizzo" value="${form.indirizzo!}" autocomplete="address-line1"
                       maxlength="255" required>
            </label>

            <label>
                <span>Citta</span>
                <input type="text" name="citta" value="${form.citta!}" autocomplete="address-level2"
                       maxlength="100" required>
            </label>

            <label>
                <span>CAP</span>
                <input type="text" name="cap" value="${form.cap!}" autocomplete="postal-code"
                       maxlength="20">
            </label>
        </div>

        <div class="auth-form__actions">
            <button class="button button--primary" type="submit">Salva modifiche</button>
            <a class="button button--ghost" href="${contextPath}/cliente/account">Torna all'account</a>
        </div>
    </form>
</section>
</@layout.page>
