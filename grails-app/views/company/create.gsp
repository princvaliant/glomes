


<%@ page import="com.glo.ndo.Company" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'company.label', default: 'Company')}" />
<title><g:message code="default.create.label"
	args="[entityName]" /></title>
</head>
<body>
<!-- Page commands -->
<div id="pagetitle">
<div class="wrapper">
<div class="nav"><g:link class="cmdlink" action="list">
	<g:message code="default.list.label" args="[entityName]" />
</g:link></div>
</div>
</div>
<!-- End of Page Commands -->

<div id="pageBody">
<div class="wrapper"><section class="column width8 first">
<g:if
	test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if>
 <g:hasErrors bean="${companyInstance}" field="">
	<div class="errors"><g:renderErrors bean="${companyInstance}"
		as="list" field="" /></div>
</g:hasErrors>
<fieldset><legend> <g:message
	code="default.create.label" args="[entityName]" /> </legend> 
	 <g:form action="save"
	>
	<div class="dialog">
	<table>
		<tbody>
			
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name"><g:message code="company.name.label" default="Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: companyInstance, field: 'name', 'errors')}">
                                    <g:textField name="name" maxlength="100" value="${companyInstance?.name}" style="width: 260px;" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="comment"><g:message code="company.comment.label" default="Comment" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: companyInstance, field: 'comment', 'errors')}">
                                    <g:textArea name="comment" cols="70" rows="5" value="${companyInstance?.comment}" style="width: 340px;"/>
                                </td>
                            </tr>
                        
		</tbody>
	</table>
	</div>
	<p class="box"><g:submitButton name="create"
		class="btn btn-green big"
		value="${message(code: 'default.button.create.label', default: 'Create')}" />
	</p>
</g:form></fieldset>
<section></section></div>
</div>
</body>
</html>
