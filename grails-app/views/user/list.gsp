
<%@ page import="com.glo.security.User" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'user.label', default: 'User')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>

    </head>
    <body>
        <!-- Page commands -->
		<div id="pagetitle">
			<div class="wrapper">
				<div class="nav">
		           	 <g:link class="cmdlink" action="create" ><g:message code="default.new.label" args="[entityName]" /></g:link>
		        </div>
		        <g:form action="list" name="search" ><g:textField name="q" value="${search}"/></g:form>
			</div>
		</div>
		<!-- End of Page Commands -->
    	
     	<div id="pageBody">
     	<div class="wrapper">     
	        <div class="column width8 first">
	            <g:if test="${flash.message}">
	            <div class="message">${flash.message}</div>
	            </g:if>
	                <table class="stylized display">
	                    <thead>
	                        <tr>	                        
	                            <g:sortableColumn property="username" title="${message(code: 'user.username.label', default: 'Username')}" />
	                        
	                            <g:sortableColumn property="firstName" title="${message(code: 'user.firstName.label', default: 'First Name')}" />
	                        
	                            <g:sortableColumn property="lastName" title="${message(code: 'user.lastName.label', default: 'Last Name')}" />
	                        
	                            <g:sortableColumn property="email" title="${message(code: 'user.email.label', default: 'Email')}" />
	                            
	                            <g:sortableColumn property="cellPhone" title="${message(code: 'user.cellPhone.label', default: 'Cell phone')}" />
	         
	                            <g:sortableColumn property="textTo" title="${message(code: 'user.textTo.label', default: 'Text message to')}" />
	                      
	                        </tr>
	                    </thead>
	                    <tbody>
	                    <g:each in="${userInstanceList}" status="i" var="userInstance">
	                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
	                        
	                            <td><g:link action="edit" id="${userInstance.id}">
	                            ${fieldValue(bean: userInstance, field: "username")}</g:link></td>
	                        
	                            <td>${fieldValue(bean: userInstance, field: "firstName")}</td>
	                        
	                            <td>${fieldValue(bean: userInstance, field: "lastName")}</td>
	                        
	                            <td>${fieldValue(bean: userInstance, field: "email")}</td>
	                            
	                            <td>${fieldValue(bean: userInstance, field: "cellPhone")}</td>
	                            	                            	                            
	                            <td>${fieldValue(bean: userInstance, field: "textTo")}</td>
	                        
	                        </tr>
	                    </g:each>
	                    </tbody>
	                </table>

	            <div class="paginateButtons">
	                <g:paginate total="${userInstanceTotal}" />
	            </div>
	        </div>
        </div>
        </div>
    </body>
</html>
