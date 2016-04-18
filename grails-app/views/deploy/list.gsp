
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<title><g:message code="navigation.glo.deploy" />
</title>
</head>
<body>
	<!-- Page commands -->
	<div id="pagetitle">
		<div class="wrapper">
			<div class="nav">
				<h1>
					<g:message code="navigation.glo.deploy" />
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
				<g:form action="deploy" method="post" enctype="multipart/form-data">
					<label for="file">File:</label>
					<input type="file" name="file" id="file" />
					<input class="btn btn-green small" type="submit" value="Upload" />
				</g:form>

				<p>
					<b>NOTE: Import Activiti BMP process definitions. Select .bar
						file.</b>
				</p>
				<hr />
			
				<table class="stylized display">
					<thead>
						<tr>
							<g:sortableColumn property="id"
								title="${message(code: 'deployment.id.label', default: 'ID')}" />

							<g:sortableColumn property="name"
								title="${message(code: 'deployment.name.label', default: 'Deploy name')}" />

							<g:sortableColumn property="deploymentTime"
								title="${message(code: 'deployment.label.date', default: 'Date deployed')}" />
								
								<th></th>

						</tr>
					</thead>
					<tbody>
						<g:each in="${deployments}" status="i" var="deploy">
							<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

								<td>
									${fieldValue(bean: deploy, field: "id")}
								</td>

								<td>
									${fieldValue(bean: deploy, field: "name")}
								</td>

								<td>
									${fieldValue(bean: deploy, field: "deploymentTime")}
								</td>
								<td>
									<g:form controller="deploy" action="delete" method="post" params="${[did:deploy.id]}">
									<g:actionSubmit class="btn btn-red small" value="delete" onclick="return confirm('Are you sure???')" />
									</g:form>
								</td>

							</tr>
						</g:each>
					</tbody>
				</table>
				<div class="paginateButtons">
					<g:paginate total="${deploymentsTotal}" />
				</div>
			</div>
		</div>
	</div>
</body>
</html>