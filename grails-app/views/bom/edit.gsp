


<%@ page import="com.glo.ndo.Bom" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'bom.label', default: 'Bom')}" />
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
 <g:hasErrors bean="${bomInstance}" field="">
	<div class="errors"><g:renderErrors bean="${bomInstance}"
		as="list" field="" /></div>
</g:hasErrors>
<fieldset><legend> <g:message
	code="default.edit.label" args="[entityName]" /> </legend> 
 <g:form method="post"
	>
	<g:hiddenField name="id" value="${bomInstance?.id}" />
	<g:hiddenField name="version" value="${bomInstance?.version}" />
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
                                    <g:textField name="revision" value="${bomInstance?.revision}" style="width: 260px;" required="true" />
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
                        

                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="bomParts"><g:message code="bom.bomParts.label" default="Bom Parts" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: bomInstance, field: 'bomParts', 'errors')}">
                                    
<ul>
<g:each in="${bomInstance?.bomParts?}" var="b">
    <li><g:link controller="bomPart" action="edit" id="${b.id}">${b?.encodeAsHTML()}</g:link></li>
</g:each>
</ul>
<g:link class="btn btn-green small" controller="bomPart" action="create" params="['bom.id': bomInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'bomPart.label', default: 'BomPart')])}</g:link>

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
