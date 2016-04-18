<html>
<head>
    <meta name="layout" content="main"/>
</head>

<body>
<!-- Page commands -->
<div id="pagetitle">
    <div class="wrapper">
        <div class="nav">
            <h1>
                <g:message code="navigation.welcome.link"/>
            </h1>
        </div>
    </div>
</div>
<!-- End of Page Commands -->

<div id="pageBody">

    <div class="column width10 ">

        <g:if test="${flash.message}">
            <div class="message"><b>
                ${flash.message}
                </b>
            </div>
        </g:if>
        <p align="center">

            <a href="http://mes/glo">Click here to post another request</a>

        </p>

    </div>

    <div class="colgroup leading">
        <div class="column width3 first"></div>

        <div class="column width3"></div>
    </div>

    <div class="clear">&nbsp;</div>
</div>
</section>
<!-- End of Left column/section -->

</div>
<!-- End of Wrapper -->

</div>
</body>
</html>
