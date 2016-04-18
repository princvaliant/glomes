
<%@ page import="com.glo.ndo.Bom" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'bom.label', default: 'Bom')}" />
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
                            <td valign="top" class="name"><g:message code="bom.id.label" default="Id" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: bomInstance, field: "id")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="bom.assemblyProduct.label" default="Assembly Product" /></td>
                            
                            <td valign="top" class="value"><g:link controller="product" action="show" id="${bomInstance?.assemblyProduct?.id}">${bomInstance?.assemblyProduct?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="bom.note.label" default="Note" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: bomInstance, field: "note")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="bom.revision.label" default="Revision" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: bomInstance, field: "revision")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="bom.bomParts.label" default="Bom Parts" /></td>
                            
                            <td valign="top" style="text-align: left;" class="value">
                                <ul>
                                <g:each in="${bomInstance.bomParts}" var="b">
                                    <li><g:link controller="bomPart" action="show" id="${b.id}">${b?.encodeAsHTML()}</g:link></li>
                                </g:each>
                                </ul>
                            </td>
                            
                        </tr>
                    
                    </tbody>
                </table>
            </div>
            <p class="box">
                <g:form>
                    <g:hiddenField name="id" value="${bomInstance?.id}" />
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
