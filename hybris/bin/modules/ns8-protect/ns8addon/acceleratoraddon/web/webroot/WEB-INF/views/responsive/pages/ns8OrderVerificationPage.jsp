<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/responsive/template" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<template:page pageTitle="${pageTitle}">
    <script>
        window.onload = function () {
            $("<input type='hidden' name='CSRFToken' value='" + ACC.config.CSRFToken + "'/>")
                .appendTo($('#ns8Form').find("form"));
        }
    </script>
    <div id="ns8Form" class="container">
            ${ns8VerifcationContent}
    </div>
</template:page>
