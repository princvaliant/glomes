
<%@ page import="com.glo.ndo.Bom" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'bom.label', default: 'BOM')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
        <script>
        	  
  		</script>
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
	                            <g:sortableColumn property="id" title="${message(code: 'bom.id.label', default: 'ID')}" />    
	                                                
	                            <g:sortableColumn property="assemblyCode" title="${message(code: 'bom.assemblyCode.label', default: 'Assembly code')}" />
	                            
	                            <g:sortableColumn property="assemblyRevision" title="${message(code: 'bom.assemblyRevision.label', default: 'Assembly revision')}" />
	                        
	                            <g:sortableColumn property="assemblyName" title="${message(code: 'bom.assemblyName.label', default: 'Assembly name')}" />
	                            
	                            <g:sortableColumn property="note" title="${message(code: 'bom.note.label', default: 'Note')}" />
	                             
	                            <g:sortableColumn property="revision" title="${message(code: 'bom.revision.label', default: 'BOM Revision')}" />
	                        
  	                            <g:sortableColumn property="partsCount" title="${message(code: 'bom.quantity.label', default: 'Parts')}" />
	         
	                        </tr>
	                    </thead>
	                    <tbody>
	                    <g:each in="${bomInstanceList}" status="i" var="bomInstance">
	                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
	                        
	                            <td><g:link controller="bom" action="edit" id="${bomInstance.id}">
	                            ${fieldValue(bean: bomInstance, field: "id")}</g:link></td>
	                            
	                            <td><g:link controller="product" action="edit" id="${bomInstance.assemblyProduct.id}">${bomInstance.assemblyProduct.code}</g:link></td>
	                            
	                             <td>${bomInstance.assemblyProduct.revision}</td>
	                        
	                            <td>${bomInstance.assemblyProduct.name}</td>
	                            
	                             <td>${bomInstance.note}</td>
	                             
	                              <td>${bomInstance.revision}</td>
                        
	                            <td>${bomInstance.bomParts?.size()}</td>
	                        
	                        </tr>
	                    </g:each>
	                    </tbody>
	                </table>

	            <div class="paginateButtons">
	                <g:paginate total="${bomInstanceTotal}" />
	            </div>
	        </div>
        </div>
        </div>
    </body>
</html>
