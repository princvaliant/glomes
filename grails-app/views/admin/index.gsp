
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<title><g:message code="navigation.glo.admin" />
</title>
</head>
<body>
	<!-- Page commands -->
	<div id="pagetitle">
		<div class="wrapper">
			<div class="nav">
				<h1>
					<g:message code="navigation.glo.admin" />
				</h1>
			</div>
		</div>
	</div>
	<!-- End of Page Commands -->

	<div id="pageBody">
		<div class="wrapper">
			<div class="column width8 first">
				<g:if test="${flash.message}">
					<div class="message">
						${flash.message}
					</div>
				</g:if>
                <hr />
                <p>
                    <b><g:link controller='uploadWaferStartInventory' action="list" >Upload new inventory</g:link></b>
                </p>
        		<hr />
			    <p>
					<b><g:link controller='yieldLossReason' action="list" >Manage yield loss reasons</g:link></b>
				</p>
				<hr />
				<p>
					<b><g:link controller='bonusReason' action="list" >Manage bonus reasons</g:link></b>
				</p>
				<hr />
				<p>
					<b><g:link controller='reworkReason' action="list" >Manage rework reasons</g:link></b>
				</p>
				<hr />
				<p>
					<b><g:link controller='equipmentUnscheduled' action="list" >Manage equipment unscheduled statuses</g:link></b>
				</p>
				<hr />
				<p>
					<b><g:link controller='deploy' action="list" >Manage process deployment</g:link></b>
				</p>
				<hr />
				<p>
					<b><g:link controller='renameProduct' action="list" >Re-classify product</g:link></b>
				</p>
				<hr />
				<p>
					<b><g:link controller='dataCorrection' action="list" >Data corrections</g:link></b>
				</p>
				<hr />
			</div>
		</div>
	</div>
</body>
</html>