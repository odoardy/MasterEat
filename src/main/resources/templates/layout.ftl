<#function publicUrl contextPath url fallback="/assets/img/placeholders/meal.svg">
    <#if url?has_content>
        <#assign cleaned = url?trim>
        <#if cleaned?starts_with("http://") || cleaned?starts_with("https://")>
            <#return cleaned>
        </#if>
        <#if cleaned?starts_with("/")>
            <#return contextPath + cleaned>
        </#if>
        <#return contextPath + "/" + cleaned>
    </#if>
    <#return contextPath + fallback>
</#function>

<#function price value>
    <#if value??>
        <#return value?string["0.00"]>
    </#if>
    <#return "0.00">
</#function>

<#function displayDate value fallback="-">
    <#assign text = value?string?trim?replace("T", " ")>
    <#if !(text?has_content)>
        <#return fallback>
    </#if>
    <#if text?length gte 16
        && text?substring(4, 5) == "-"
        && text?substring(7, 8) == "-"
        && text?substring(10, 11) == " ">
        <#return text?substring(8, 10) + "/" + text?substring(5, 7) + "/" + text?substring(0, 4) + " " + text?substring(11, 16)>
    </#if>
    <#if text?length gte 16>
        <#return text?substring(0, 16)>
    </#if>
    <#return text>
</#function>

<#macro page title active="">
<!doctype html>
<html lang="it" class="no-js">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${title} | MasterEat</title>
    <link rel="icon" href="${contextPath}/assets/img/logo/favicon.ico" sizes="any">
    <link rel="icon" type="image/png" sizes="32x32" href="${contextPath}/assets/img/logo/mastereat-favicon-32.png">
    <link rel="stylesheet" href="${contextPath}/assets/css/base.css?v=we2">
    <link rel="stylesheet" href="${contextPath}/assets/css/layout.css?v=we2">
    <link rel="stylesheet" href="${contextPath}/assets/css/components.css?v=we4">
    <link rel="stylesheet" href="${contextPath}/assets/css/forms.css?v=we4">
    <link rel="stylesheet" href="${contextPath}/assets/css/pages-public.css?v=we3">
    <link rel="stylesheet" href="${contextPath}/assets/css/pages-customer.css?v=we4">
    <link rel="stylesheet" href="${contextPath}/assets/css/pages-staff.css?v=we2">
    <link rel="stylesheet" href="${contextPath}/assets/css/pages-owner.css?v=we4">
    <link rel="stylesheet" href="${contextPath}/assets/css/responsive.css?v=we5">
</head>
<body>
<#import "/partials/header.ftl" as header>
<#import "/partials/footer.ftl" as footer>
<@header.siteHeader active=active />
<main id="contenuto" class="site-main">
    <#nested>
</main>
<@footer.siteFooter />
<button class="back-to-top" type="button" aria-label="Torna su" hidden>&uarr;</button>
<script src="${contextPath}/assets/js/main.js?v=we3" defer></script>
</body>
</html>
</#macro>
