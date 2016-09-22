
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



        <div class="column width4 first">
            <g:if test="${flash.message}">
                <div class="message">
                    ${flash.message}
                </div>
            </g:if>
            <hr />
            <h4>Supplier specific wafer imports</h4>
            <p>Excel file with NGan import wafers from UNID
            </p>
            <g:form action="uploadNganUnid" controller="uploadWaferStartInventory" method="post" enctype="multipart/form-data">
                <input type="file" name="file" id="file"  width=250 />
                <input class="btn btn-green small" type="submit" value="Upload" />
            </g:form>
            <p>
                Excel file with NGan import wafers from Sanan
            </p>
            <g:form action="uploadNganSanan" controller="uploadWaferStartInventory" method="post" enctype="multipart/form-data">
                <input type="file" name="file" id="file"  width=250 />
                <input class="btn btn-green small" type="submit" value="Upload" />
            </g:form>
            <p>
                Excel file with completed EPI from SANAN
            </p>
            <g:form action="uploadEpiSanan" controller="uploadWaferStartInventory" method="post" enctype="multipart/form-data">
                <input type="file" name="file" id="file"  width=250 />
                <input class="btn btn-green small" type="submit" value="Upload" />
            </g:form>
            <hr />
            <h4>Generic wafer imports</h4>
            <p>Excel file with wafers for NGaN import
            </p>
            <g:form action="uploadNgan" controller="uploadWaferStartInventory" method="post" enctype="multipart/form-data">
                 <input type="file" name="file" id="file"  width=250 />
                <input class="btn btn-green small" type="submit" value="Upload" />
            </g:form>
            <p>
                Excel file with bare wafers
            </p>
            <g:form action="uploadBare" controller="uploadWaferStartInventory" method="post" enctype="multipart/form-data">
                <input type="file" name="file" id="file" width=250 />
                <input class="btn btn-green small" type="submit" value="Upload" />
            </g:form>
            <hr />
            <h4>iBLU package imports</h4>
            <p>OEM (SAE) test data imports
            </p>
            <g:form action="uploadOemPackageTestData" controller="uploadWaferStartInventory" method="post" enctype="multipart/form-data">
                <input type="file" name="file" id="file"  width=250 />
                <input class="btn btn-blue small" type="submit" value="Upload" />
            </g:form>
            <p>OEM (SAE) imports without test data
            </p>
            <g:form action="uploadOemPackage" controller="uploadWaferStartInventory" method="post" enctype="multipart/form-data">
                <input type="file" name="file" id="file"  width=250 />
                <input class="btn btn-blue small" type="submit" value="Upload" />
            </g:form>
            <p>Upload iLGP data
            </p>
            <g:form action="uploadLightBar" controller="uploadWaferStartInventory" method="post" enctype="multipart/form-data">
                <input type="file" name="file" id="file"  width=250 />
                <input class="btn btn-blue small" type="submit" value="Upload" />
            </g:form>
            <p>Upload Package data
            </p>
            <g:form action="uploadPackageData" controller="uploadWaferStartInventory" method="post" enctype="multipart/form-data">
                <input type="file" name="file" id="file"  width=250 />
                <input class="btn btn-blue small" type="submit" value="Upload" />
            </g:form>


            <hr />
            </div>

            <div class="column width4">
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