


<%@ page import="com.glo.ndo.Bom" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'bom.label', default: 'Bom')}" />
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
 <g:hasErrors bean="${bomInstance}" field="">
	<div class="errors"><g:renderErrors bean="${bomInstance}"
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
                                    <label for="assemblyProduct"><g:message code="bom.assemblyProduct.label" default="Assembly Product" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: bomInstance, field: 'assemblyProduct', 'errors')}">
                                    <g:select name="assemblyProduct.id" from="${com.glo.ndo.Product.list().sort{it.code}}" optionKey="id" optionValue="${{it.code + (it.revision ? ' [rev. ' + it.revision + '] ' : '') + it.name}}" value="${bomInstance?.assemblyProduct?.id}"  />
                                </td>
                            </tr>
                            
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="revision"><g:message code="bom.revision.label" default="Revision" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: bomInstance, field: 'revision', 'errors')}">
                                    <g:textField name="revision" value="${bomInstance?.revision}" style="width: 260px;" />
                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name"><label
                                        for="toValidate"><g:message
                                            code="user.toValidate.label"
                                            default="Validate?" />
                                </label>
                                </td>
                                <td valign="top"
                                    class="value ${hasErrors(bean: bomInstance, field: 'toValidate', 'errors')}">
                                    <g:checkBox name="toValidate"
                                                value="${bomInstance?.toValidate}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="note"><g:message code="bom.note.label" default="Note" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: bomInstance, field: 'note', 'errors')}">
                                    <g:textField name="note" value="${bomInstance?.note}" style="width: 260px;" />
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
