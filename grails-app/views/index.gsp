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

    <!-- Wrapper -->
    <div class="wrapper">
        <!-- Left column/section -->
        <section class="column width8 first">

            <div class="colgroup leading">

                <div class="column width4 first">
                    <h2>Features</h2>

                    <b>Inventory management and control</b>
                    <ul>
                        <li>Manage inventory (part numbers, BOMs, serialized or
                        bulk, vendors)</li>
                        <li>Manage locations (companies, locations, process steps)</li>
                        <li>Commission new inventory via UI or XLS uploads</li>
                        <li>Collect unit data via UI or automated data sync</li>
                        <li>Process flow moves via UI or barcode scanners</li>
                        <li>Split and merge units as per process flow or product
                        definitions</li>
                        <li>Complete unit history with all process steps and
                        variables</li>
                    </ul>

                    <b>Process control</b>
                    <ul>
                        <li>3-level process hierarchy (categories, processes,
                        process steps)</li>
                        <li>Process flow management</li>
                        <li>Flexible definition of variables</li>
                    </ul>

                    <b>Equipment management and control</b>
                    <ul>
                        <li>Schedule and manage maintenance tasks</li>
                        <li>Collect equipment data via UI or automated data sync</li>
                        <li>View equipment history and charts</li>
                    </ul>

                    <b>Reporting</b>
                    <ul>
                        <li>Unit test data visualizations with admin and
                        user-defined filters</li>
                        <li>Powerful user-created reports with formulas and filters</li>
                        <li>SPC charts for single variable monitoring</li>
                        <li>Dashboard charts for complex pivot graphs</li>
                        <li>Custom-programmed reports on as-needed basis</li>
                    </ul>

                    <b>System Admin</b>
                    <ul>
                        <li>System configuration via UI or XLS uploads</li>
                        <li>Manage access rights (users, roles)</li>
                        <li>Data corrections via XLS uploads</li>
                    </ul>

                </div>

                <div class="column width4 ">
                    <g:form controller="softwareRequest" method="post" enctype="multipart/form-data">

                        <h2>Create new request or report bug</h2>
                        <!-- <iframe src="releaseNotes.html" width="560" height="550"></iframe> -->
                        <b style="color:red;">Use this form to place new requests for MES new features, improvements and bug fixes.</b>

                        <p>

                        <div class="fieldcontain required">
                            <label for="requestType">
                                <g:message code="request.type.label" default="Request type"/>
                                <span class="required-indicator">*</span>
                            </label><br/>
                            <g:select name="requestType" id="requestType" from="${['support', 'bug', 'new feature', 'improvement']}"/>
                        </div>
                        </p>
                        <p>

                        <div class="fieldcontain">
                            <label for="prior">
                                <g:message code="request.prior.label"
                                           default="Priority --- Default 50, 10 - lowest, 100 - highest"/>
                            </label><br/>
                            <g:select name="prior" id="prior" from="${[50, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100]}"/>
                        </div>
                        </p>
                        <p>

                        <div class="fieldcontain required">
                            <label for="comment">
                                <g:message code="request.comment.label"
                                           default="Description --- Enter detailed information, if possible"/>
                            </label>
                            <g:textArea name="comment" id="comment" cols="70" rows="12" value="" style="width: 440px;"/>
                        </div>
                        </p>

                        <p>
                            <label for="prior">
                                <g:message code="request.file.label" default="Attach file(s) related to the request"/>
                            </label>
                            <input type="file" name="file1" id="file1" style="width:390;"/>
                            <input type="file" name="file2" id="file2" style="width:390;"/>
                            <input type="file" name="file3" id="file3" style="width:390;"/>
                        </p>
                        <p>
                            <sec:ifAllGranted roles="ROLE_ADMIN">
                                <g:select name="owner" id="owner" from="${com.glo.security.User.list().sort{it.username.toUpperCase()}}" optionKey="username" optionValue="username"/>
                            </sec:ifAllGranted>

                        </p>

                        <p>
                            <g:actionSubmit action="create" value="${message(code: 'request.label', default: 'Submit request')}" class="btn btn-green"></g:actionSubmit>
                        </p>
                        <g:if test="${flash.message}">
                            <div class="message">
                                ${flash.message}
                            </div>
                        </g:if>
                    </g:form>
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
