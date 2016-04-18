
<%@ page import="com.glo.ndo.BomPart" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'bomPart.label', default: 'BomPart')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
    <!-- Page commands -->
		<div id="pagetitle">
			<div class="wrapper">
				<div class="nav">
		        	<g:link class="cmdlink" action="list"><g:message code="default.list.label" args="[entityName]" /></g:link>
            		<g:link class="cmdlink" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link>
		        </div>
			</div>
		</div>
	<!-- End of Page Commands -->

    <div id="pageBody">
      	<div class="wrapper">
        	<section class="column width8 first">
        	     <g:if test="${flash.message}">
            		<div class="message">${flash.message}</div>
           		 </g:if>
            <fieldset>
            	<legend>
					<g:message code="default.show.label" args="[entityName]" />
				</legend>
            <div class="dialog">
                <table>
                    <tbody>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="bomPart.id.label" default="Id" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: bomPartInstance, field: "id")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="bomPart.partProduct.label" default="Part Product" /></td>
                            
                            <td valign="top" class="value"><g:link controller="product" action="show" id="${bomPartInstance?.partProduct?.id}">${bomPartInstance?.partProduct?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="bomPart.quantity.label" default="Quantity" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: bomPartInstance, field: "quantity")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="bomPart.bom.label" default="Bom" /></td>
                            
                            <td valign="top" class="value"><g:link controller="bom" action="show" id="${bomPartInstance?.bom?.id}">${bomPartInstance?.bom?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                    </tbody>
                </table>
            </div>
            <p class="box">
                <g:form>
                    <g:hiddenField name="id" value="${bomPartInstance?.id}" />
                    <span class="button"><g:actionSubmit class="btn btn-green big" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>
                    <span class="button"><g:actionSubmit class="btn btn-red" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
                </g:form>
            </p>
            </fieldset>
        	</section>
        </div>
        </div>
    </body>
</html>
