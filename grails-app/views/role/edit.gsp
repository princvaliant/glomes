<%@ page import="com.glo.security.User"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'user.label', default: 'Role')}" />
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
<g:if test="${flash.message}">
	<div class="message">
	${flash.message}
	</div>
</g:if> <g:hasErrors bean="${roleInstance}" field="">
	<div class="errors"><g:renderErrors bean="${roleInstance}"
		as="list" field="" /></div>
</g:hasErrors>
<fieldset><legend> <g:message
	code="default.edit.label" args="[entityName]" /> </legend>
<div class="colgroup">
<div class="column width3 first"><g:form method="post">
	<g:hiddenField name="id" value="${roleInstance?.id}" />
	<g:hiddenField name="version" value="${roleInstance?.version}" />
	<div class="dialog">
	<table>
		<tbody>

			<tr class="prop">
				<td valign="top" class="name"><label for=authority><g:message
					code="role.authority.label" default="Group code" /></label></td>
				<td valign="top"
					class="value ${hasErrors(bean: roleInstance, field: 'authority', 'errors')}">
				<g:textField name="authority" value="${roleInstance?.authority}" /></td>
			</tr>

			<tr class="prop">
				<td valign="top" class="name"><label for=name><g:message
					code="role.authority.label" default="Group name" /></label></td>
				<td valign="top"
					class="value ${hasErrors(bean: roleInstance, field: 'name', 'errors')}">
				<g:textField name="name" value="${roleInstance?.name}" /></td>
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
</g:form></div>
<div class="column width5" style="margin-top: -6px"><g:form
	controller="role" action="addUser" id="${roleInstance.id}" style="overflow:hidden;">
	<table>
		<tbody>
			<tr class="prop">
				<td valign="top" class="name" height="1px"><label for="user"><g:message
					code="user.users.label" default="Assign Users" /></label></td>
			</tr>
			<tr>
				<td valign="top" height="1px">
				<div class="colgroup">
				<div class="column width1 first">
								
						<g:select name="addUserAutoComplete"
				          from="${users}"
				          optionKey="id"
				          optionValue="username" />
					
					</div>
				<div class="column"><g:submitButton
					style="padding:3px 15px 4px 15px;margin:0 0 0 -15px;position:relative;left:230px;top:00px;"
					class="btn btn-green small" name="addUser" value="Add" /></div>
				</div>
				</td>
			</tr>
			<tr>
				<td valign="top"
					class="value ${hasErrors(bean: roleInstance, field: 'id', 'errors')}">
				<g:each in="${roleUsers}" var="v">
					<g:link action="removeRole"
						params="${[roleId:roleInstance.id,userId:v.id]}">
						<img src="${resource(dir:'img',file:'delete.png')}" />
					</g:link>
					${v}
					<br />
				</g:each></td>
			</tr>
		</tbody>
	</table>
</g:form><br />

</div>
</div>
</fieldset>
</section></div>
</div>
</body>
</html>
