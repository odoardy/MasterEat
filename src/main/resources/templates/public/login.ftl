<#import "/layout.ftl" as layout>
<@layout.page title="Login" active="login">
<section class="page-heading">
    <p class="eyebrow">Area riservata</p>
    <h1>Login</h1>
    <p>Accedi con le credenziali utente gia presenti nel database MasterEat.</p>
</section>

<section class="auth-panel" aria-label="Login web">
    <#if successMessage?has_content>
        <p class="notice notice--success">${successMessage}</p>
    </#if>

    <#if errorMessage?has_content>
        <p class="notice notice--error">${errorMessage}</p>
    </#if>

    <form class="auth-form" method="post" action="${contextPath}/login">
        <label>
            <span>Username</span>
            <input type="text" name="username" value="${username!}" autocomplete="username"
                   maxlength="50" required>
        </label>

        <label>
            <span>Password</span>
            <input type="password" name="password" autocomplete="current-password"
                   maxlength="128" required>
        </label>

        <div class="auth-form__actions">
            <button class="button button--primary" type="submit">Accedi</button>
            <a class="button button--ghost" href="${contextPath}/registrazione">Registrati</a>
            <a class="button button--ghost" href="${contextPath}/menu">Vai al men&ugrave;</a>
            <a class="text-link" href="${contextPath}/home">Torna alla home</a>
        </div>
    </form>
</section>
</@layout.page>
