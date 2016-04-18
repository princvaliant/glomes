
<%@ page import="com.glo.ndo.BomPart" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'bomPart.label', default: 'BomPart')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <!-- Page commands -->
		<div id="pagetitle">
			<div class="wrapper">
				<div class="nav">
		           	 <g:link class="cmdlink" action="create" ><g:message code="default.new.label" args="[entityName]" /></g:link>
		        </div>
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
	                        
	                            <th><g:message code="bomPart.partProduct.label" default="Part Product" /></th>
	                        
	                            <g:sortableColumn property="quantity" title="${message(code: 'bomPart.quantity.label', default: 'Quantity')}" />
	                        
	                            <th><g:message code="bomPart.bom.label" default="Bom" /></th>
	                        
	                        </tr>
	                    </thead>
	                    <tbody>
	                    <g:each in="${bomPartInstanceList}" status="i" var="bomPartInstance">
	                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
	                        
	                            <td>
								<g:link action="edit" id="${bomPartInstance.id}">${fieldValue(bean: bomPartInstance, field: "partProduct")}</g:link></td>
	                        
	                            <td>${fieldValue(bean: bomPartInstance, field: "quantity")}</td>
	                        
	                            <td>${fieldValue(bean: bomPartInstance, field: "bom")}</td>
	                        
	                        </tr>
	                    </g:each>
	                    </tbody>
	                </table>

	            <div class="paginateButtons">
	                <g:paginate total="${bomPartInstanceTotal}" />
	            </div>
	        </div>
        </div>
        </div>
    </body>
</html>
