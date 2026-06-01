<#import "/layout.ftl" as layout>
<@layout.page title="Nuovo personale" active="owner-staff">
<section class="page-heading">
    <p class="eyebrow">Area proprietario</p>
    <h1>Nuovo personale</h1>
    <p>Crea un account per il nuovo membro del personale.</p>
</section>

<section class="auth-panel auth-panel--wide" aria-label="Nuovo membro personale">
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

    <form class="auth-form" method="post" action="${contextPath}/proprietario/personale">
        <div class="auth-form__grid">
            <label>
                <span>Username</span>
                <input type="text" name="username" value="${form.username!}" autocomplete="username"
                       minlength="3" maxlength="50" pattern="[A-Za-z0-9._-]+" required>
            </label>

            <label>
                <span>Email</span>
                <input type="email" name="email" value="${form.email!}" autocomplete="email"
                       maxlength="255" required>
            </label>

            <label>
                <span class="field-label-with-info">
                    Password
                    <span class="field-info" title="Almeno 8 caratteri, una maiuscola, una minuscola e un numero.">ⓘ</span>
                </span>
                <input type="password" name="password" autocomplete="new-password"
                       minlength="8" maxlength="128" pattern="^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$"
                       title="Almeno 8 caratteri, una maiuscola, una minuscola e un numero."
                       required>
            </label>

            <label>
                <span>Conferma password</span>
                <input type="password" name="confermaPassword" autocomplete="new-password"
                       minlength="8" maxlength="128" pattern="^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$"
                       title="Ripeti la password con gli stessi requisiti." required>
            </label>

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
                <span>Telefono</span>
                <input type="tel" name="telefono" value="${form.telefono!}" autocomplete="tel"
                       minlength="6" maxlength="30" pattern="[+0-9 .()\-]{6,30}">
            </label>
        </div>

        <div class="auth-form__actions">
            <button class="button button--primary" type="submit">Crea personale</button>
            <a class="button button--ghost" href="${contextPath}/proprietario/personale">Annulla</a>
        </div>
    </form>
</section>
</@layout.page>
