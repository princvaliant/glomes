


<%@ page import="com.glo.ndo.BomPart" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'bomPart.label', default: 'BomPart')}" />
<title><g:message code="default.edit.label" args="[entityName]" /></title>
</head>
<body>
<!-- Page commands -->
<div id="pagetitle">
<div class="wrapper">
<div class="nav"><g:link class="cmdlink" action="list">
	<g:message code="default.list.label" args="[entityName]" />
</g:link> <g:link class="cmdlink" action="create">
	<g:message code="default.new.label" args="[entityName]" />
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
 <g:hasErrors bean="${bomPartInstance}" field="">
	<div class="errors"><g:renderErrors bean="${bomPartInstance}"
		as="list" field="" /></div>
</g:hasErrors>
<fieldset><legend> <g:message
	code="default.edit.label" args="[entityName]" /> </legend> 
 <g:form method="post"
	>
	<g:hiddenField name="id" value="${bomPartInstance?.id}" />
	<g:hiddenField name="version" value="${bomPartInstance?.version}" />
	<div class="dialog">
	<table>
		<tbody>
			
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="partProduct"><g:message code="bomPart.partProduct.label" default="Part Product" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: bomPartInstance, field: 'partProduct', 'errors')}">
                                    <g:select name="partProduct.id" from="${com.glo.ndo.Product.list().sort{it.code}}" optionKey="id" optionValue="${{it.code + ' - ' + it.name}}" value="${bomPartInstance?.partProduct?.id}"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="quantity"><g:message code="bomPart.quantity.label" default="Quantity" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: bomPartInstance, field: 'quantity', 'errors')}">
                                    <g:textField name="quantity" value="${fieldValue(bean: bomPartInstance, field: 'quantity')}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="bom"><g:message code="bomPart.bom.label" default="Bom" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: bomPartInstance, field: 'bom', 'errors')}">
                                    <g:select name="bom.id" from="${com.glo.ndo.Bom.list()}" optionKey="id" value="${bomPartInstance?.bom?.id}"  />
                                </td>
                            </tr>
                        
		</tbody>
	</table>
	</div>
	<p class="box"><span class="button"><g:actionSubmit
		class="btn btn-green big" action="update"
		value="${message(code: 'default.button.update.label', default: 'Update')}" /></span>
	<span class="button"><g:actionSubmit class="btn btn-red"
		action="delete"
		value="${message(code: 'default.button.delete.label', default: 'Delete')}"
		onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
	</p>
</g:form></fieldset>
</section></div>
</div>
</body>
</html>
