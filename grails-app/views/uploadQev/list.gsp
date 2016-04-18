
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<title><g:message code="navigation.glo.upload" />
</title>
</head>
<body>
	<!-- Page commands -->
	<div id="pagetitle">
		<div class="wrapper">
			<div class="nav">
				<h1>
					<g:message code="navigation.glo.upload" />
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
				<g:form action="upload"  method="post" enctype="multipart/form-data">
					<label for="file">File:</label>
					<input type="file" name="file" id="file" />
					<input class="btn btn-green small" type="submit" value="Upload" />
				</g:form>

				<p>
					<b>NOTE: Select excel file containing QEV data</b>
				</p>
				<hr />
		
			</div>
		</div>
	</div>
</body>
</html>