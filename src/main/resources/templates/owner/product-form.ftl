<#import "/layout.ftl" as layout>
<@layout.page title=pageTitle active="owner-menu">
<section class="page-heading">
    <p class="eyebrow">Area proprietario</p>
    <h1>${pageTitle}</h1>
    <p>Gestisci solo i dati base del prodotto. Caratteristiche, ingredienti e immagini restano in sola consultazione.</p>
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

    <form class="auth-form owner-product-form" method="post" action="${action}">
        <div class="auth-form__grid">
            <label class="auth-form__field-wide">
                <span>Nome</span>
                <input type="text" name="nome" value="${form.nome!}" maxlength="150" required>
            </label>

            <label class="auth-form__field-wide">
                <span>Descrizione</span>
                <textarea name="descrizione" maxlength="2000" required>${form.descrizione!}</textarea>
            </label>

            <label>
                <span>Prezzo base</span>
                <input type="number" name="prezzoBase" value="${form.prezzoBase!}" min="0" step="0.01" required>
            </label>

            <label>
                <span>Tempo preparazione</span>
                <input type="number" name="minutiPreparazione" value="${form.minutiPreparazione!}" min="1" step="1" required>
            </label>

            <label>
                <span>Categoria</span>
                <#assign selectedCategory = form.idCategoria!"">
                <#assign selectedCategoryFound = false>
                <#list categorie as categoria>
                    <#if selectedCategory?has_content && selectedCategory == categoria.id?c>
                        <#assign selectedCategoryFound = true>
                    </#if>
                </#list>
                <select name="idCategoria">
                    <option value="" <#if !(selectedCategory?has_content)>selected</#if>>Nessuna categoria</option>
                    <#if selectedCategory?has_content && !selectedCategoryFound>
                        <option value="${selectedCategory}" selected>Categoria #${selectedCategory}</option>
                    </#if>
                    <#list categorie as categoria>
                        <option value="${categoria.id?c}" <#if selectedCategory == categoria.id?c>selected</#if>>
                            ${categoria.nome}
                        </option>
                    </#list>
                </select>
            </label>

            <label class="owner-product-form__check">
                <input type="checkbox" name="attivo" value="true" <#if (form.attivo!"true") == "true">checked</#if>>
                <span>Prodotto attivo</span>
            </label>

            <label class="auth-form__field-wide">
                <span>Note preparazione</span>
                <textarea name="descrizionePreparazione" maxlength="2000">${form.descrizionePreparazione!}</textarea>
            </label>
        </div>

        <div class="auth-form__actions">
            <button class="button button--primary" type="submit">${submitLabel}</button>
            <a class="button button--ghost" href="${cancelUrl}">Annulla</a>
        </div>
    </form>
</section>
</@layout.page>
