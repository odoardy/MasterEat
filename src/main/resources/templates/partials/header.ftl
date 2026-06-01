<#macro siteHeader active="">
<header class="site-header">
    <a class="skip-link" href="#contenuto">Vai al contenuto</a>
    <div class="site-header__inner">
        <a class="brand" href="${contextPath}/home" aria-label="MasterEat home">
            <img class="brand__mark" src="${contextPath}/assets/img/logo/mastereat-icon.svg" alt="" width="38" height="38" aria-hidden="true">
            <span class="brand__text">MasterEat</span>
        </a>

        <button class="nav-toggle" type="button" aria-expanded="false" aria-controls="site-navigation">
            <span class="sr-only">Men&ugrave;</span>
            <span aria-hidden="true"></span>
            <span aria-hidden="true"></span>
            <span aria-hidden="true"></span>
        </button>

        <nav class="site-nav" id="site-navigation" aria-label="Navigazione principale">
            <a class="site-nav__link <#if active == 'home'>is-active</#if>" href="${contextPath}/home">Home</a>
            <a class="site-nav__link <#if active == 'menu'>is-active</#if>" href="${contextPath}/menu">Men&ugrave;</a>
            <#if isAuthenticated && currentRole == "CLIENTE">
                <a class="site-nav__link <#if active == 'cart'>is-active</#if>" href="${contextPath}/cliente/carrello">
                    Carrello<#if cartItemCount?number gt 0> (${cartItemCount})</#if>
                </a>
            </#if>
            <#if isAuthenticated>
                <details class="account-dropdown">
                    <summary class="account-dropdown__trigger <#if active == 'account' || active == 'profile' || active == 'orders' || active == 'staff-orders' || active == 'owner-orders' || active == 'owner-staff' || active == 'owner-menu' || active == 'owner-statistics'>is-active</#if>">
                        <span class="account-dropdown__identity">
                            <span class="account-dropdown__username">${currentUser.username}</span>
                            <span class="account-dropdown__role">${currentRoleLabel}</span>
                        </span>
                        <span class="account-dropdown__chevron" aria-hidden="true">&#9662;</span>
                    </summary>
                    <nav class="account-dropdown__menu" aria-label="Men&ugrave; account">
                        <#if currentRole == "CLIENTE">
                            <a class="account-dropdown__link <#if active == 'account'>is-active</#if>" href="${contextPath}/cliente/account">Account</a>
                            <a class="account-dropdown__link <#if active == 'orders'>is-active</#if>" href="${contextPath}/cliente/ordini">I miei ordini</a>
                        </#if>
                        <#if currentRole == "PERSONALE">
                            <a class="account-dropdown__link <#if active == 'staff-orders'>is-active</#if>" href="${contextPath}/staff/ordini">Gestisci ordini</a>
                        </#if>
                        <#if currentRole == "PROPRIETARIO">
                            <a class="account-dropdown__link <#if active == 'owner-orders'>is-active</#if>" href="${contextPath}/proprietario/ordini">Monitora ordini</a>
                            <a class="account-dropdown__link <#if active == 'owner-statistics'>is-active</#if>" href="${contextPath}/proprietario/statistiche">Statistiche</a>
                            <a class="account-dropdown__link <#if active == 'owner-menu'>is-active</#if>" href="${contextPath}/proprietario/menu">Gestisci men&ugrave;</a>
                            <a class="account-dropdown__link <#if active == 'owner-staff'>is-active</#if>" href="${contextPath}/proprietario/personale">Personale</a>
                        </#if>
                        <form class="account-dropdown__logout-form" method="post" action="${contextPath}/logout">
                            <button class="account-dropdown__logout-button" type="submit">Logout</button>
                        </form>
                    </nav>
                </details>
            <#else>
                <a class="site-nav__link <#if active == 'login'>is-active</#if>" href="${contextPath}/login">Login</a>
                <a class="site-nav__link <#if active == 'registrazione'>is-active</#if>" href="${contextPath}/registrazione">Registrati</a>
            </#if>
        </nav>
    </div>
</header>
</#macro>
