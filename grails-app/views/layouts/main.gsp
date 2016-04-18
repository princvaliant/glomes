<html>
<head>
<meta charset="utf-8" />
<title><g:layoutTitle default="GLO manufacturing execution" /></title>
<meta name="description" content="GLO manufacturing execution" />
<meta name="keywords" content="Dashboard" />
<!-- We need to emulate IE7 only when we are to use excanvas -->
<!--[if IE]>
<meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7" />
<![endif]-->
<g:layoutHead />
<r:layoutResources />
<g:javascript library="application" />
<nav:resources override="true" />
<!-- Favicons -->
<link rel="shortcut icon"
	href="${resource(dir:'img/favicons',file:'favicon.png')}"
	type="image/png" />
<link rel="icon"
	href="${resource(dir:'img/favicons',file:'favicon.png')}"
	type="image/png" />
<link rel="apple-touch-icon"
	href="${resource(dir:'img/favicons',file:'apple.png')}"
	type="image/png" />
<!-- Main Stylesheet -->
<link rel="stylesheet" href="${resource(dir:'css',file:'style.css')}" />
<link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />
<!-- Your Custom Stylesheet -->
<link rel="stylesheet" href="${resource(dir:'css',file:'custom.css')}" />
<!--swfobject - needed only if you require <video> tag support for older browsers -->
<g:javascript src="swfobject.js"></g:javascript>

<g:javascript src="jquery-1.5.1.min.js"></g:javascript>
<!-- jQuery with plugins -->
<g:javascript src="jquery-ui-1.8.11.custom.min.js"></g:javascript>
<!-- jQuery tooltips -->
<g:javascript src="jquery.tipTip.min.js"></g:javascript>
<!-- Superfish navigation -->
<g:javascript src="jquery.superfish.min.js"></g:javascript>
<g:javascript src="jquery.supersubs.min.js"></g:javascript>
<!-- jQuery form validation -->
<g:javascript src="jquery.validate_pack.js"></g:javascript>
<!-- jQuery popup box -->
<g:javascript src="jquery.nyroModal.pack.js"></g:javascript>
<!-- jQuery graph plugins -->
<!--[if IE]><g:javascript src="flot/excanvas.min.js"></g:javascript><![endif]-->
<!-- Internet Explorer Fixes -->
<!--[if IE]>
<link rel="stylesheet" href="${resource(dir:'css',file:'ie.css')}" type="text/css" media="all"/>
<g:javascript src="html5.js"></g:javascript>
<![endif]-->
<!--Upgrade MSIE5.5-7 to be compatible with MSIE8: http://ie7-js.googlecode.com/svn/version/2.1(beta3)/IE8.js -->
<!--[if lt IE 8]>
<g:javascript src="IE8.js"></g:javascript>
<![endif]-->
<!--[if lt IE 9]>
<g:javascript src="IE9.js"></g:javascript>
<![endif]-->
<!-- Admin template javascript load -->
<script type="text/javascript">
	$(document).ready(function() {

		/* setup navigation, content boxes, etc... */
		Administry.setup();

		/* tabs */

		$("#tabs, #tabs2").tabs();

	});
</script>
<g:javascript src="administry.js" />

</head>
<body class="yui-skin-sam">
	<!-- Header -->
	<header id="top">
		<div class="wrapper ">
			<!-- Title/Logo - can use text instead of image -->
			<div id="title">
				<img src="${resource(dir:'img',file:'logo.png')}" width="184"
					alt="GLO laboratory manager" />
			</div>
			<!-- Top navigation -->
			<div id="topnav">
				<sec:ifLoggedIn>
					<g:message code="navigation.welcome.link" />
					<b><sec:username /></b>,
				<span>|</span>
					<g:link controller='logout'>
						<g:message code="navigation.logout.link" />
					</g:link>
					<sec:ifAllGranted roles="ROLE_ADMIN">
						<span>|</span>
						<g:link controller='admin' action="index">
							<g:message code="navigation.settings.link" />
						</g:link>
					</sec:ifAllGranted>
					<span>|</span>
					<a href="#"><g:message code="navigation.help.link" /></a>
				</sec:ifLoggedIn>
				<sec:ifNotLoggedIn>
					<g:link controller='login' action='auth'>
						<g:message code="navigation.login.link" />
					</g:link>
				</sec:ifNotLoggedIn>
			</div>
			<!-- End of Top navigation -->
			<!-- Main navigation -->
			<nav id="menu">
				<ul class="sf-menu">

					<sec:ifAnyGranted
						roles="ROLE_ADMIN,ROLE_FAB_ADMIN,ROLE_EQUIPMENT_ADMIN">
						<nav:eachItem group="admin" var="item">
							<li class="${item.active ? 'current' : ''}"><g:link
									controller="${item.controller}" action="${item.action}">
									<g:message code="${item.title}" />
								</g:link></li>
						</nav:eachItem>
					</sec:ifAnyGranted>
					<sec:ifAnyGranted
						roles="ROLE_USER,ROLE_ADMIN,ROLE_OPERATOR,ROLE_FAB_ADMIN,ROLE_FAB_TECHNICIAN,ROLE_EQUIPMENT_ADMIN,ROLE_EQUIPMENT_ENG">
						<li><a
							href='${grailsApplication.config.webdomain ?: "http://localhost:8080/glo"}/userConsole'><g:message
									code="navigation.user_console.link" /></a></li>
					</sec:ifAnyGranted>

				</ul>
			</nav>
			<!-- End of Main navigation -->
			<!-- Aside links -->
			<aside></aside>
			<!-- End of Aside links -->
		</div>
	</header>
	<!-- End of Header -->


	<!-- Page content -->
	<div id="page">
		<g:layoutBody />
		<r:layoutResources />
	</div>

	<!-- Page footer -->
	<footer id="bottom">
		<div class="wrapper">
			<!--  		<nav>
				<a href="#">Dashboard</a> &middot;
			</nav>  -->
			<p>
				Powered by <b><a href="http://www.glo.se" title="GLO">GLO</a></b> -
				<b>version <g:meta name="app.version" /> - Server: "${grailsApplication.config.glo.tomcatServer}"
				</b>
			</p>

		</div>
	</footer>
	<!-- End of Page footer -->

	<!-- Scroll to top link -->
	<a href="#" id="totop">^ scroll to top</a>

</body>
</html>