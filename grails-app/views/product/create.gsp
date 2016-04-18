


<%@ page import="com.glo.ndo.Product" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'product.label', default: 'Product')}" />
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
 <g:hasErrors bean="${productInstance}" field="">
	<div class="errors"><g:renderErrors bean="${productInstance}"
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
                                    <label for="code"><g:message code="product.code.label" default="Code" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'code', 'errors')}">
                                    <g:textField name="code" maxlength="100" value="${productInstance?.code}" style="width: 260px;" />
                                </td>
                            </tr>
                             
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="revision"><g:message code="product.revision.label" default="Revision" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'revision', 'errors')}">
                                    <g:textField name="revision" value="${productInstance?.revision}" style="width: 260px;" />
                                </td>
                            </tr>
                         
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name"><g:message code="product.name.label" default="Name" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'name', 'errors')}">
                                    <g:textField name="name" maxlength="100" value="${productInstance?.name}" style="width: 260px;" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="comment"><g:message code="product.comment.label" default="Comment" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'comment', 'errors')}">
                                    <g:textArea name="comment" cols="70" rows="5" value="${productInstance?.comment}" style="width: 340px;"/>
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="isBulk"><g:message code="product.isBulk.label" default="Is Bulk" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'isBulk', 'errors')}">
                                    <g:checkBox name="isBulk" value="${productInstance?.isBulk}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="disabled"><g:message code="product.disabled.label" default="Disabled" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'disabled', 'errors')}">
                                    <g:checkBox name="disabled" value="${productInstance?.disabled}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="startProcess"><g:message code="product.startProcess.label" default="Start Process" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'startProcess', 'errors')}">
                                    <g:select name="startProcess.id" from="${com.glo.ndo.Process.list().sort()}" optionKey="id" value="${productInstance?.startProcess?.id}" noSelection="['null': '']" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="productFamily"><g:message code="product.productFamily.label" default="Product Family" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'productFamily', 'errors')}">
                                    <g:select name="productFamily.id" from="${com.glo.ndo.ProductFamily.list().sort()}" optionKey="id" value="${productInstance?.productFamily?.id}"  />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="category"><g:message code="product.category.label" default="Category" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'category', 'errors')}">
                                    <g:textField name="category" value="${productInstance?.category}" style="width: 260px;" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="uom"><g:message code="product.uom.label" default="Uom" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'uom', 'errors')}">
                                    <g:textField name="uom" value="${productInstance?.uom}" style="width: 260px;" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="minQty"><g:message code="product.minQty.label" default="Min Qty" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'minQty', 'errors')}">
                                    <g:textField name="minQty" value="${fieldValue(bean: productInstance, field: 'minQty')}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="maxQty"><g:message code="product.maxQty.label" default="Max Qty" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'maxQty', 'errors')}">
                                    <g:textField name="maxQty" value="${fieldValue(bean: productInstance, field: 'maxQty')}" />
                                </td>
                            </tr>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="usedFor"><g:message code="product.usedFor.label" default="Used For" /></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: productInstance, field: 'usedFor', 'errors')}">
                                    <g:textField name="usedFor" value="${productInstance?.usedFor}" style="width: 260px;" />
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
