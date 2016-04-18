
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<title><g:message code="navigation.glo.dataCorrection" />
</title>
</head>
<body>
	<!-- Page commands -->
	<div id="pagetitle">
		<div class="wrapper">
			<div class="nav">
				<h1>
					<g:message code="navigation.glo.dataCorrection" />
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
				
				<form controller="dataCorrection" method="post" enctype="multipart/form-data">
					<p>
						<b>Identify the variable:</b>
					</p>
					<label for="pctg">Category:</label>
					<input type="text" name="pctg" value="" id="category" style='width: 200px;' />
        			<input type="hidden" name="pctg_required" value="true" /><br>
					
					<label for="pkey">Process:</label>
					<input type="text" name="pkey" value="" id="process" style='width: 200px;' />
        			<input type="hidden" name="pkey_required" value="true" /><br>
					
					<label for="tkey">Process Step:</label>
					<input type="text" name="tkey" value="" id="process step" style='width: 200px;' />
					
					<label for="insertStep">Insert process step if needed:</label>
					<input type="checkbox" name="insertStep" value="${true}" /><br>
					
					<label for="varName">Variable Name:</label>
					<input type="text" name="varName" value="" id="variable" style='width: 250px;' />
        			<input type="hidden" name="varName_required" value="true" /><br>
					
					<label for="varDigits">Digits (for 'float' only):</label>
					<input type="text" name="varDigits" value="" id="digits" style='width: 50px;'/>
        			<input type="hidden" name="digits" value="true" /><br>
					
					<p>
						<b>Select CSV file (column A: code, column B: new value), then initiate data correction:</b>
					</p>
					<label for="file">File:</label>
					<input type="file" name="file" id="file" />
					<!--  <input class="btn btn-green small" type="submit" value="Upload" action="upload" /> -->
					<g:actionSubmit	class="btn btn-green small" controller="dataCorrection" value="Upload" action="upload" />
					
					<hr />
					<p>
						<b>Check all variables for specific mis-configurations:</b>
					</p>
					<g:actionSubmit	class="btn btn-green small" controller="dataCorrection" value="Check" action="check" />
					<p>
					</p>
				</form>
			
			</div>
		</div>
	</div>
</body>
</html>