<#import "/layout.ftl" as layout>
<@layout.page title=pageTitle!"Gruppo caratteristiche" active="owner-menu">
<#if errorMessage?has_content && !(prodotto??)>
    <section class="status-page">
        <p class="eyebrow">Area proprietario</p>
        <h1>Gruppo caratteristiche</h1>
        <p>${errorMessage}</p>
        <a class="button button--primary" href="${contextPath}/proprietario/menu">Torna al men&ugrave;</a>
    </section>
<#else>
    <section class="page-heading">
        <p class="eyebrow">Area proprietario</p>
        <h1>${pageTitle}</h1>
        <p>Configura una famiglia di alternative per ${prodotto.nome}.</p>
    </section>

    <section class="auth-panel auth-panel--wide" aria-label="${pageTitle}">
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

        <form class="auth-form owner-product-form owner-characteristic-group-form" method="post" action="${action}">
            <div class="auth-form__grid">
                <label class="auth-form__field-wide">
                    <span>Nome gruppo</span>
                    <input type="text" name="nome" value="${form.nome!}" maxlength="100" required>
                </label>

                <label class="auth-form__field-wide">
                    <span>Descrizione</span>
                    <textarea name="descrizione" maxlength="2000">${form.descrizione!}</textarea>
                </label>

                <div class="auth-form__field-wide owner-characteristic-form__checks">
                    <label>
                        <input type="checkbox" name="obbligatorio" value="true"
                               <#if (form.obbligatorio!"false") == "true">checked</#if>>
                        <span>Gruppo obbligatorio</span>
                    </label>
                    <label>
                        <input type="checkbox" name="attivo" value="true"
                               <#if (form.attivo!"true") == "true">checked</#if>>
                        <span>Gruppo attivo</span>
                    </label>
                </div>
            </div>

            <div class="auth-form__actions">
                <button class="button button--primary" type="submit">${submitLabel}</button>
                <a class="button button--ghost" href="${cancelUrl}">Annulla</a>
            </div>
        </form>
    </section>
</#if>
</@layout.page>
