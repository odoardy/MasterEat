<#import "/layout.ftl" as layout>
<@layout.page title=pageTitle!"Ingrediente prodotto" active="owner-menu">
<#if errorMessage?has_content && !(prodotto??)>
    <section class="status-page">
        <p class="eyebrow">Area proprietario</p>
        <h1>Ingrediente</h1>
        <p>${errorMessage}</p>
        <a class="button button--primary" href="${contextPath}/proprietario/menu">Torna al men&ugrave;</a>
    </section>
<#else>
    <section class="page-heading">
        <p class="eyebrow">Area proprietario</p>
        <h1>${pageTitle}</h1>
        <p>Configura ingrediente e quantita associati a ${prodotto.nome}.</p>
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

        <#if showCatalogSelect>
            <div class="owner-ingredient-form-stack">
                <section class="owner-ingredient-form-section" aria-labelledby="associa-ingrediente-title">
                    <h2 id="associa-ingrediente-title">Associa ingrediente esistente</h2>
                    <form class="auth-form owner-product-form owner-ingredient-form" method="post" action="${action}">
                        <div class="auth-form__grid">
                            <label class="auth-form__field-wide">
                                <span>Ingrediente esistente</span>
                                <#assign selectedIngredient = form.idIngrediente!"">
                                <#assign selectedIngredientFound = false>
                                <#list ingredientiCatalogo as ingredienteCatalogo>
                                    <#if selectedIngredient?has_content && selectedIngredient == ingredienteCatalogo.id?c>
                                        <#assign selectedIngredientFound = true>
                                    </#if>
                                </#list>
                                <select name="idIngrediente" required>
                                    <option value="" <#if !(selectedIngredient?has_content)>selected</#if>>Seleziona ingrediente</option>
                                    <#if selectedIngredient?has_content && !selectedIngredientFound>
                                        <option value="${selectedIngredient}" selected>Ingrediente #${selectedIngredient}</option>
                                    </#if>
                                    <#list ingredientiCatalogo as ingredienteCatalogo>
                                        <option value="${ingredienteCatalogo.id?c}" <#if selectedIngredient == ingredienteCatalogo.id?c>selected</#if>>
                                            ${ingredienteCatalogo.nome} - ${ingredienteCatalogo.unitaMisura}
                                        </option>
                                    </#list>
                                </select>
                            </label>

                            <label>
                                <span>Quantita</span>
                                <input type="number" name="quantita" value="${form.quantita!}" min="0.001" step="0.001" required>
                            </label>
                        </div>

                        <div class="auth-form__actions">
                            <button class="button button--primary" type="submit">Associa ingrediente</button>
                            <a class="button button--ghost" href="${cancelUrl}">Annulla</a>
                        </div>
                    </form>
                </section>

                <section class="owner-ingredient-form-section" aria-labelledby="crea-ingrediente-title">
                    <h2 id="crea-ingrediente-title">Crea nuovo ingrediente e associalo</h2>
                    <form class="auth-form owner-product-form owner-ingredient-form" method="post" action="${action}">
                        <div class="auth-form__grid">
                            <label class="auth-form__field-wide">
                                <span>Nome ingrediente</span>
                                <input type="text" name="nome" value="${form.nome!}" maxlength="120" required>
                            </label>

                            <label>
                                <span>Quantita</span>
                                <input type="number" name="quantita" value="${form.quantita!}" min="0.001" step="0.001" required>
                            </label>

                            <label>
                                <span>Unita di misura</span>
                                <#assign selectedUnit = form.unitaMisura!"">
                                <select name="unitaMisura" required>
                                    <option value="" <#if !(selectedUnit?has_content)>selected</#if>>Seleziona unita</option>
                                    <#if selectedUnit?has_content
                                        && selectedUnit != "g"
                                        && selectedUnit != "kg"
                                        && selectedUnit != "ml"
                                        && selectedUnit != "l"
                                        && selectedUnit != "pz">
                                        <option value="${selectedUnit}" selected>${selectedUnit}</option>
                                    </#if>
                                    <option value="g" <#if selectedUnit == "g">selected</#if>>g</option>
                                    <option value="kg" <#if selectedUnit == "kg">selected</#if>>kg</option>
                                    <option value="ml" <#if selectedUnit == "ml">selected</#if>>ml</option>
                                    <option value="l" <#if selectedUnit == "l">selected</#if>>l</option>
                                    <option value="pz" <#if selectedUnit == "pz">selected</#if>>pz</option>
                                </select>
                            </label>

                            <div class="auth-form__field-wide owner-characteristic-form__checks">
                                <label>
                                    <input type="checkbox" name="allergene" value="true"
                                           <#if (form.allergene!"false") == "true">checked</#if>>
                                    <span>Allergene</span>
                                </label>
                                <label>
                                    <input type="checkbox" name="attivo" value="true"
                                           <#if (form.attivo!"true") == "true">checked</#if>>
                                    <span>Ingrediente attivo</span>
                                </label>
                            </div>
                        </div>

                        <div class="auth-form__actions">
                            <button class="button button--primary" type="submit">Crea e associa ingrediente</button>
                            <a class="button button--ghost" href="${cancelUrl}">Annulla</a>
                        </div>
                    </form>
                </section>
            </div>
        <#else>
            <p class="notice owner-global-data-note">
                Nome, unita di misura, allergene e stato sono dati globali dell'ingrediente e possono influenzare altri prodotti che lo usano.
            </p>

            <form class="auth-form owner-product-form owner-ingredient-form" method="post" action="${action}">
                <div class="auth-form__grid">
                    <label>
                        <span>Quantita</span>
                        <input type="number" name="quantita" value="${form.quantita!}" min="0.001" step="0.001" required>
                    </label>

                    <label>
                        <span>Unita di misura</span>
                        <#assign selectedUnit = form.unitaMisura!"">
                        <select name="unitaMisura" required>
                            <option value="" <#if !(selectedUnit?has_content)>selected</#if>>Seleziona unita</option>
                            <#if selectedUnit?has_content
                                && selectedUnit != "g"
                                && selectedUnit != "kg"
                                && selectedUnit != "ml"
                                && selectedUnit != "l"
                                && selectedUnit != "pz">
                                <option value="${selectedUnit}" selected>${selectedUnit}</option>
                            </#if>
                            <option value="g" <#if selectedUnit == "g">selected</#if>>g</option>
                            <option value="kg" <#if selectedUnit == "kg">selected</#if>>kg</option>
                            <option value="ml" <#if selectedUnit == "ml">selected</#if>>ml</option>
                            <option value="l" <#if selectedUnit == "l">selected</#if>>l</option>
                            <option value="pz" <#if selectedUnit == "pz">selected</#if>>pz</option>
                        </select>
                    </label>

                    <label class="auth-form__field-wide">
                        <span>Nome ingrediente</span>
                        <input type="text" name="nome" value="${form.nome!}" maxlength="120" required>
                    </label>

                    <div class="auth-form__field-wide owner-characteristic-form__checks">
                        <label>
                            <input type="checkbox" name="allergene" value="true"
                                   <#if (form.allergene!"false") == "true">checked</#if>>
                            <span>Allergene</span>
                        </label>
                        <label>
                            <input type="checkbox" name="attivo" value="true"
                                   <#if (form.attivo!"true") == "true">checked</#if>>
                            <span>Ingrediente attivo</span>
                        </label>
                    </div>
                </div>

                <div class="auth-form__actions">
                    <button class="button button--primary" type="submit">${submitLabel}</button>
                    <a class="button button--ghost" href="${cancelUrl}">Annulla</a>
                </div>
            </form>
        </#if>
    </section>
</#if>
</@layout.page>
