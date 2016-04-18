
<%@ page import="com.glo.security.User" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'user.label', default: 'User')}" />
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
 <g:hasErrors bean="${userInstance}" field="">
	<div class="errors"><g:renderErrors bean="${userInstance}"
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
                                    <label for="username"><g:message code="user.username.label" default="Username" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'username', 'errors')}">
                                    <g:textField name="username" value="${userInstance?.username}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="password"><g:message code="user.password.label" default="Password" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'tempPassword', 'errors')}">
                                    <g:textField name="tempPassword" value="${userInstance?.tempPassword}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="firstName"><g:message code="user.firstName.label" default="First Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'firstName', 'errors')}">
                                    <g:textField name="firstName" value="${userInstance?.firstName}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="lastName"><g:message code="user.lastName.label" default="Last Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'lastName', 'errors')}">
                                    <g:textField name="lastName" value="${userInstance?.lastName}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="email"><g:message code="user.email.label" default="Email" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'email', 'errors')}">
                                    <g:textField name="email" value="${userInstance?.email}" />
                                </td>
                            </tr>
                            
                         	<tr class="prop">
								<td valign="top" class="name"><label for="textTo"><g:message
										code="user.cellPhone.label" default="Text message to" />
								</label>
								</td>
								<td valign="top"
									class="value ${hasErrors(bean: userInstance, field: 'textTo', 'errors')}">
									<g:textField name="textTo"
										value="${userInstance?.textTo}" />
								</td>
							</tr>
                            
                            <tr class="prop">
								<td valign="top" class="name"><label for="cellPhone"><g:message
									code="user.cellPhone.label" default="Cell phone" /></label></td>
								<td valign="top"
									class="value ${hasErrors(bean: userInstance, field: 'cellPhone', 'errors')}">
								<g:textField name="cellPhone" value="${userInstance?.cellPhone}" /></td>
							</tr>
							
							<tr class="prop">
								<td valign="top" class="name"><label for="workPhone"><g:message
									code="user.workPhone.label" default="Work phone" /></label></td>
								<td valign="top"
									class="value ${hasErrors(bean: userInstance, field: 'workPhone', 'errors')}">
								<g:textField name="workPhone" value="${userInstance?.workPhone}" /></td>
							</tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="accountExpired"><g:message code="user.accountExpired.label" default="Account Expired" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'accountExpired', 'errors')}">
                                    <g:checkBox name="accountExpired" value="${userInstance?.accountExpired}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="accountLocked"><g:message code="user.accountLocked.label" default="Account Locked" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'accountLocked', 'errors')}">
                                    <g:checkBox name="accountLocked" value="${userInstance?.accountLocked}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="enabled"><g:message code="user.enabled.label" default="Enabled" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'enabled', 'errors')}">
                                    <g:checkBox name="enabled" value="${userInstance?.enabled}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="passwordExpired"><g:message code="user.passwordExpired.label" default="Password Expired" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: userInstance, field: 'passwordExpired', 'errors')}">
                                    <g:checkBox name="passwordExpired" value="${userInstance?.passwordExpired}" />
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
