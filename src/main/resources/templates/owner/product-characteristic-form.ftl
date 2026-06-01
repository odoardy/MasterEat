<#import "/layout.ftl" as layout>
<@layout.page title=pageTitle!"Caratteristica prodotto" active="owner-menu">
<#if errorMessage?has_content && !(prodotto??)>
    <section class="status-page">
        <p class="eyebrow">Area proprietario</p>
        <h1>Caratteristica</h1>
        <p>${errorMessage}</p>
        <a class="button button--primary" href="${contextPath}/proprietario/menu">Torna al men&ugrave;</a>
    </section>
<#else>
    <section class="page-heading">
        <p class="eyebrow">Area proprietario</p>
        <h1>${pageTitle}</h1>
        <p>Configura i dati della caratteristica associata a ${prodotto.nome}.</p>
    </section>

    <section class="auth-panel auth-panel--wide" aria-label="${pageTitle}">
        <#if loadError?has_content>
            <p class="notice notice--error">${loadError}</p>
        </#if>

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

        <form class="auth-form owner-product-form owner-characteristic-form" method="post" action="${action}">
            <div class="auth-form__grid">
                <label class="auth-form__field-wide">
                    <span>Nome caratteristica</span>
                    <input type="text" name="nome" value="${form.nome!}" maxlength="100" required>
                </label>

                <label class="auth-form__field-wide">
                    <span>Descrizione</span>
                    <textarea name="descrizione" maxlength="2000">${form.descrizione!}</textarea>
                </label>

                <label>
                    <span>Differenza prezzo</span>
                    <input type="number" name="differenzaPrezzo" value="${form.differenzaPrezzo!}" step="0.01" required>
                </label>

                <label>
                    <span>Gruppo</span>
                    <#assign selectedGroup = form.idGruppoCaratteristiche!"">
                    <#assign selectedGroupFound = false>
                    <#list gruppi as gruppo>
                        <#if selectedGroup?has_content && selectedGroup == gruppo.id?c>
                            <#assign selectedGroupFound = true>
                        </#if>
                    </#list>
                    <select name="idGruppoCaratteristiche">
                        <option value="" <#if !(selectedGroup?has_content)>selected</#if>>Nessun gruppo</option>
                        <#if selectedGroup?has_content && !selectedGroupFound>
                            <option value="${selectedGroup}" selected>Gruppo #${selectedGroup}</option>
                        </#if>
                        <#list gruppi as gruppo>
                            <option value="${gruppo.id?c}" <#if selectedGroup == gruppo.id?c>selected</#if>>
                                ${gruppo.nome}<#if gruppo.obbligatorio> · obbligatorio</#if>
                            </option>
                        </#list>
                    </select>
                </label>

                <div class="auth-form__field-wide owner-characteristic-form__checks">
                    <label>
                        <input type="checkbox" name="selezionataDefault" value="true"
                               <#if (form.selezionataDefault!"false") == "true">checked</#if>>
                        <span>Selezionata di default</span>
                    </label>
                    <label>
                        <input type="checkbox" name="attiva" value="true"
                               <#if (form.attiva!"true") == "true">checked</#if>>
                        <span>Caratteristica attiva</span>
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
