
<%@ page import="com.glo.ndo.Company" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'company.label', default: 'Company')}" />
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
	                        
	                            <g:sortableColumn property="name" title="${message(code: 'company.name.label', default: 'Name')}" />
	                        
	                            <g:sortableColumn property="comment" title="${message(code: 'company.comment.label', default: 'Comment')}" />
	                            
	                            <th></th>
	                        
	                        </tr>
	                    </thead>
	                    <tbody>
	                    <g:each in="${companyInstanceList}" status="i" var="companyInstance">
	                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
	                        
	                            <td>
								<g:link action="edit" id="${companyInstance.id}">${fieldValue(bean: companyInstance, field: "name")}</g:link></td>
	                        
	                            <td>${fieldValue(bean: companyInstance, field: "comment")}</td>
	                            
	                            <td><a href="${createLink(controller:'product',action: 'list',params:['companyId':companyInstance.id,'companyName':companyInstance.name])}">${companyInstance?.productCompanies ? companyInstance?.productCompanies?.size() + ' products': ''} </a></td>
	                        
	                        </tr>
	                    </g:each>
	                    </tbody>
	                </table>

	            <div class="paginateButtons">
	                <g:paginate total="${companyInstanceTotal}" />
	            </div>
	        </div>
        </div>
        </div>
    </body>
</html>
