<%@ page import="com.glo.ndo.Company" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'company.label', default: 'Company')}" />
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
 <g:hasErrors bean="${companyInstance}" field="">
	<div class="errors"><g:renderErrors bean="${companyInstance}"
		as="list" field="" /></div>
</g:hasErrors>
<fieldset><legend> <g:message
	code="default.edit.label" args="[entityName]" /> </legend> 
 <g:form method="post"
	>
	<g:hiddenField name="id" value="${companyInstance?.id}" />
	<g:hiddenField name="version" value="${companyInstance?.version}" />
	<div class="dialog">
	<table>
		<tbody>
			
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="name"><g:message code="company.name.label" default="Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: companyInstance, field: 'name', 'errors')}">
                                    <g:textField name="name" maxlength="100" value="${companyInstance?.name}" style="width: 260px;" disabled="true" />
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
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="equipments"><g:message code="company.equipments.label" default="Equipments" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: companyInstance, field: 'equipments', 'errors')}">
                                    
<ul>
<g:each in="${companyInstance?.equipments?}" var="e">
    <li><g:link controller="equipment" action="edit" id="${e.id}">${e?.encodeAsHTML()}</g:link></li>
</g:each>
</ul>
<g:link class="btn btn-green small" controller="equipment" action="create" params="['company.id': companyInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'equipment.label', default: 'Equipment')])}</g:link>

                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="locations"><g:message code="company.locations.label" default="Locations" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: companyInstance, field: 'locations', 'errors')}">
                                    
<ul>
<g:each in="${companyInstance?.locations?}" var="l">
    <li><g:link controller="location" action="edit" id="${l.id}">${l?.encodeAsHTML()}</g:link></li>
</g:each>
</ul>
<g:link class="btn btn-green small" controller="location" action="create" params="['company.id': companyInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'location.label', default: 'Location')])}</g:link>

                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="processSteps"><g:message code="company.processSteps.label" default="Process Steps" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: companyInstance, field: 'processSteps', 'errors')}">
                                    
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="productCompanies"><g:message code="company.productCompanies.label" default="Product Companies" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: companyInstance, field: 'productCompanies', 'errors')}">
                                    
<ul>
<g:each in="${companyInstance?.productCompanies?}" var="p">
    <li><g:link controller="productCompany" action="edit" id="${p.id}">${p?.encodeAsHTML()}</g:link></li>
</g:each>
</ul>
<g:link class="btn btn-green small" controller="productCompany" action="create" params="['company.id': companyInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'productCompany.label', default: 'ProductCompany')])}</g:link>

                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="users"><g:message code="company.users.label" default="Users" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: companyInstance, field: 'users', 'errors')}">
                                    
<ul>
<g:each in="${companyInstance?.users?}" var="u">
    <li><g:link controller="user" action="edit" id="${u.id}">${u?.encodeAsHTML()}</g:link></li>
</g:each>
</ul>
<g:link class="btn btn-green small" controller="user" action="create" params="['company.id': companyInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'user.label', default: 'User')])}</g:link>

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
