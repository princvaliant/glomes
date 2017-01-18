
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
            <div class="column width8">
                <g:if test="${flash.message}">
                    <div class="message">
                        ${flash.message}
                    </div>
                </g:if>
                <hr />
                <h4>Wafer imports to new flow</h4>
                <p>Excel file should contain only one sheet named <b>Sheet1</b></p>
                <p>Data should start from row 1 in excel</p>
                <p>Columns should be defined as follows</p>
                <table border="1">
                    <tr>
                        <td style="border: 1px dotted #999999;">
                            A - code
                        </td>
                        <td style="border: 1px dotted #999999;">
                            B - supplier
                        </td>
                        <td style="border: 1px dotted #999999;">
                            C - product
                        </td>
                        <td style="border: 1px dotted #999999;">
                            D - polish
                        </td>
                        <td style="border: 1px dotted #999999;">
                            E - thickness
                        </td>
                    </tr>
                    <tr>
                        <td style="border: 1px dotted #999999;">
                            Any code (only uppercase characters and numbers)
                        </td>
                        <td style="border: 1px dotted #999999;">
                            Any of the following<br/>
                            <br/>
                            Sanan<br/>
                            University Wafers<br/>
                            EPISTAR<br/>
                            PWA Wafers<br/>
                        </td>
                        <td style="border: 1px dotted #999999;">
                            Any of the following<br/>
                            <br/>
                            4 inch SI wafer<br/>
                            2 inch Si Wafer<br/>
                            2 inch Al2O3 wafer<br/>
                            4 inch Al2O3 wafer<br/>
                            6 inch Al2O3 wafer<br/>
                        </td>
                        <td style="border: 1px dotted #999999;">
                            polish - any string value (PSS)
                        </td>
                        <td style="border: 1px dotted #999999;">
                            thickness - any string value
                        </td>
                    </tr>
                </table>
                <g:form action="uploadNewInventory" controller="uploadWaferStartInventory" method="post" enctype="multipart/form-data">
                    <input type="file" name="file" id="file"  width=250 />
                    <input class="btn btn-green small" type="submit" value="Upload" />
                </g:form>
            </div>
        </div>
    </div>
    <div id="pageBody2">
        <div class="wrapper">
            <div class="column width8">
                <hr />
                <h4>Other data imports</h4>
                <p>Excel file containing inventory</p>
                <g:form action="upload2" controller="uploadWaferStartInventory" method="post" enctype="multipart/form-data">
                    <input type="file" name="file" id="file"  width=250  />
                    <input class="btn btn-green small" type="submit" value="Upload" />
                </g:form>
                <hr />
                <p>
                    Excel file containing recipe data
                </p>
                <g:form action="upload3" controller="uploadWaferStartInventory" method="post" enctype="multipart/form-data">
                    <input type="file" name="file" id="file"  width=250 />
                    <input class="btn btn-green small" type="submit" value="Upload" />
                </g:form>
                <hr />
                <p>Excel file SEM data
                </p>
                <g:form action="upload4" controller="uploadWaferStartInventory" method="post" enctype="multipart/form-data">
                    <input type="file" name="file" id="file"  width=250 />
                    <input class="btn btn-green small" type="submit" value="Upload" />
                </g:form>
                <hr />
                <p>Excel file for product and suppliers
                </p>
                <g:form action="upload5" controller="uploadWaferStartInventory" method="post" enctype="multipart/form-data">
                    <input type="file" name="file" id="file"  width=250 />
                    <input class="btn btn-green small" type="submit" value="Upload" />
                </g:form>
                <hr />
                <p>Custom upload
                </p>
                <g:form action="upload6" controller="uploadWaferStartInventory" method="post" enctype="multipart/form-data">
                    <input type="file" name="file" id="file"  width=250 />
                    <input class="btn btn-green small" type="submit" value="Upload custom" />
                </g:form>
                <hr />
            </div>
        </div>
    </div>

</body>
</html>