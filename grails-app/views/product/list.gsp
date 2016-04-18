
<%@ page import="com.glo.ndo.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'product.label', default: 'Product')}" />
        <title><g:message code="default.list.label" args="[entityName]" /></title>
    </head>
    <body>
        <!-- Page commands -->
		<div id="pagetitle">
			<div class="wrapper">
				<div class="nav">
		           	 <g:link class="cmdlink" action="create" ><g:message code="default.new.label" args="[entityName]" /></g:link>
		           	 <g:link class="cmdlink" controller="company" action="list">${params.companyName}</g:link>
		        </div>
		        
		       
		        <g:form action="list" name="search" ><g:hiddenField name="companyName" value="${params.companyName}" /><g:hiddenField name="companyId" value="${params.companyId}" /><g:textField name="q" value="${search}"/></g:form>
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
	                            <g:sortableColumn property="code" title="${message(code: 'product.code.label', default: 'Code')}" params="[q: params.q]" />
	                        
	                            <g:sortableColumn property="revision" title="${message(code: 'product.revision.label', default: 'Revision')}"  />
	                         
	                            <g:sortableColumn property="name" title="${message(code: 'product.name.label', default: 'Name')}"  />
	                        
	                            <g:sortableColumn property="comment" title="${message(code: 'product.comment.label', default: 'Comment')}" params="[q: params.q]" />
	                        
	                            <g:sortableColumn property="category" title="${message(code: 'product.category.label', default: 'Category')}" params="[q: params.q]" />
	                            
	                            <g:sortableColumn property="startProcess" title="${message(code: 'product.startProcess.label', default: 'Start process')}" />
	         
	                            <g:sortableColumn property="productFamily" title="${message(code: 'product.productFamily.label', default: 'Product group')}" />
	                      
	                      		<th>${message(code: 'product.bom.label', default: 'Boms')} </th>
	                        </tr>
	                    </thead>
	                    <tbody>
	                    <g:each in="${productInstanceList}" status="i" var="productInstance">
	                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
	                        
	                            <td><g:link action="edit" id="${productInstance.id}">
	                            ${fieldValue(bean: productInstance, field: "code")}</g:link></td>
	                            
	                            <td>${fieldValue(bean: productInstance, field: "revision")}</td>
	                        
	                            <td>${fieldValue(bean: productInstance, field: "name")}</td>
	                        
	                            <td>${fieldValue(bean: productInstance, field: "comment")}</td>
	                        
	                            <td>${fieldValue(bean: productInstance, field: "category")}</td>
	                            
	                            <td>${fieldValue(bean: productInstance, field: "startProcess")}</td>
	                            	                            	                            
	                            <td>${fieldValue(bean: productInstance, field: "productFamily")}</td>
	                            
	                           <td>${productInstance?.boms?.size()}</td> 
	                        
	                        </tr>
	                    </g:each>
	                    </tbody>
	                </table>

	            <div class="paginateButtons">
	                <g:paginate total="${productInstanceTotal}" params="[q: params.q, companyId:params.companyId, companyName:params.companyName]"  />
	            </div>
	        </div>
        </div>
        </div>
    </body>
</html>
