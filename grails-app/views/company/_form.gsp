<%@ page import="com.glo.ndo.Company" %>



<div class="fieldcontain ${hasErrors(bean: companyInstance, field: 'name', 'error')} required">
	<label for="name">
		<g:message code="company.name.label" default="Name" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="name" maxlength="100" value="${companyInstance?.name}" style="width: 260px;" />
</div>

<div class="fieldcontain ${hasErrors(bean: companyInstance, field: 'comment', 'error')} ">
	<label for="comment">
		<g:message code="company.comment.label" default="Comment" />
		
	</label>
	<g:textArea name="comment" cols="70" rows="5" value="${companyInstance?.comment}" style="width: 340px;"/>
</div>

<div class="fieldcontain ${hasErrors(bean: companyInstance, field: 'equipments', 'error')} ">
	<label for="equipments">
		<g:message code="company.equipments.label" default="Equipments" />
		
	</label>
	
<ul>
<g:each in="${companyInstance?.equipments?}" var="e">
    <li><g:link controller="equipment" action="edit" id="${e.id}">${e?.encodeAsHTML()}</g:link></li>
</g:each>
</ul>
<g:link class="btn btn-green small" controller="equipment" action="create" params="['company.id': companyInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'equipment.label', default: 'Equipment')])}</g:link>

</div>

<div class="fieldcontain ${hasErrors(bean: companyInstance, field: 'locations', 'error')} ">
	<label for="locations">
		<g:message code="company.locations.label" default="Locations" />
		
	</label>
	
<ul>
<g:each in="${companyInstance?.locations?}" var="l">
    <li><g:link controller="location" action="edit" id="${l.id}">${l?.encodeAsHTML()}</g:link></li>
</g:each>
</ul>
<g:link class="btn btn-green small" controller="location" action="create" params="['company.id': companyInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'location.label', default: 'Location')])}</g:link>

</div>

<div class="fieldcontain ${hasErrors(bean: companyInstance, field: 'processSteps', 'error')} ">
	<label for="processSteps">
		<g:message code="company.processSteps.label" default="Process Steps" />
		
	</label>
	
</div>

<div class="fieldcontain ${hasErrors(bean: companyInstance, field: 'productCompanies', 'error')} ">
	<label for="productCompanies">
		<g:message code="company.productCompanies.label" default="Product Companies" />
		
	</label>
	
<ul>
<g:each in="${companyInstance?.productCompanies?}" var="p">
    <li><g:link controller="productCompany" action="edit" id="${p.id}">${p?.encodeAsHTML()}</g:link></li>
</g:each>
</ul>
<g:link class="btn btn-green small" controller="productCompany" action="create" params="['company.id': companyInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'productCompany.label', default: 'ProductCompany')])}</g:link>

</div>

<div class="fieldcontain ${hasErrors(bean: companyInstance, field: 'users', 'error')} ">
	<label for="users">
		<g:message code="company.users.label" default="Users" />
		
	</label>
	
<ul>
<g:each in="${companyInstance?.users?}" var="u">
    <li><g:link controller="user" action="edit" id="${u.id}">${u?.encodeAsHTML()}</g:link></li>
</g:each>
</ul>
<g:link class="btn btn-green small" controller="user" action="create" params="['company.id': companyInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'user.label', default: 'User')])}</g:link>

</div>

