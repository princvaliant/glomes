
<%@ page import="com.glo.ndo.Product" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'product.label', default: 'Product')}" />
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
                            <td valign="top" class="name"><g:message code="product.id.label" default="Id" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: productInstance, field: "id")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="product.code.label" default="Code" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: productInstance, field: "code")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="product.name.label" default="Name" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: productInstance, field: "name")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="product.revision.label" default="Revision" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: productInstance, field: "revision")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="product.comment.label" default="Comment" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: productInstance, field: "comment")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="product.isBulk.label" default="Is Bulk" /></td>
                            
                            <td valign="top" class="value"><g:formatBoolean boolean="${productInstance?.isBulk}" /></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="product.disabled.label" default="Disabled" /></td>
                            
                            <td valign="top" class="value"><g:formatBoolean boolean="${productInstance?.disabled}" /></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="product.startProcess.label" default="Start Process" /></td>
                            
                            <td valign="top" class="value"><g:link controller="process" action="show" id="${productInstance?.startProcess?.id}">${productInstance?.startProcess?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="product.productFamily.label" default="Product Family" /></td>
                            
                            <td valign="top" class="value"><g:link controller="productFamily" action="show" id="${productInstance?.productFamily?.id}">${productInstance?.productFamily?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="product.category.label" default="Category" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: productInstance, field: "category")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="product.uom.label" default="Uom" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: productInstance, field: "uom")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="product.minQty.label" default="Min Qty" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: productInstance, field: "minQty")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="product.maxQty.label" default="Max Qty" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: productInstance, field: "maxQty")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="product.usedFor.label" default="Used For" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: productInstance, field: "usedFor")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="product.bomParts.label" default="Bom Parts" /></td>
                            
                            <td valign="top" style="text-align: left;" class="value">
                                <ul>
                                <g:each in="${productInstance.bomParts}" var="b">
                                    <li><g:link controller="bomPart" action="show" id="${b.id}">${b?.encodeAsHTML()}</g:link></li>
                                </g:each>
                                </ul>
                            </td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="product.boms.label" default="Boms" /></td>
                            
                            <td valign="top" style="text-align: left;" class="value">
                                <ul>
                                <g:each in="${productInstance.boms}" var="b">
                                    <li><g:link controller="bom" action="show" id="${b.id}">${b?.encodeAsHTML()}</g:link></li>
                                </g:each>
                                </ul>
                            </td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="product.productCompanies.label" default="Product Companies" /></td>
                            
                            <td valign="top" style="text-align: left;" class="value">
                                <ul>
                                <g:each in="${productInstance.productCompanies}" var="p">
                                    <li><g:link controller="productCompany" action="show" id="${p.id}">${p?.encodeAsHTML()}</g:link></li>
                                </g:each>
                                </ul>
                            </td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="product.productMasks.label" default="Product Masks" /></td>
                            
                            <td valign="top" style="text-align: left;" class="value">
                                <ul>
                                <g:each in="${productInstance.productMasks}" var="p">
                                    <li><g:link controller="productMask" action="show" id="${p.id}">${p?.encodeAsHTML()}</g:link></li>
                                </g:each>
                                </ul>
                            </td>
                            
                        </tr>
                    
                    </tbody>
                </table>
            </div>
            <p class="box">
                <g:form>
                    <g:hiddenField name="id" value="${productInstance?.id}" />
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
