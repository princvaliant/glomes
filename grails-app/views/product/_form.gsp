<%@ page import="com.glo.ndo.Product" %>



<div class="fieldcontain ${hasErrors(bean: productInstance, field: 'code', 'error')} required">
	<label for="code">
		<g:message code="product.code.label" default="Code" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="code" maxlength="100" value="${productInstance?.code}" style="width: 260px;" />
</div>

<div class="fieldcontain ${hasErrors(bean: productInstance, field: 'name', 'error')} required">
	<label for="name">
		<g:message code="product.name.label" default="Name" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="name" maxlength="100" value="${productInstance?.name}" style="width: 260px;" />
</div>

<div class="fieldcontain ${hasErrors(bean: productInstance, field: 'revision', 'error')} ">
	<label for="revision">
		<g:message code="product.revision.label" default="Revision" />
		
	</label>
	<g:textField name="revision" value="${productInstance?.revision}" style="width: 260px;" />
</div>

<div class="fieldcontain ${hasErrors(bean: productInstance, field: 'comment', 'error')} ">
	<label for="comment">
		<g:message code="product.comment.label" default="Comment" />
		
	</label>
	<g:textArea name="comment" cols="70" rows="5" value="${productInstance?.comment}" style="width: 340px;"/>
</div>

<div class="fieldcontain ${hasErrors(bean: productInstance, field: 'isBulk', 'error')} ">
	<label for="isBulk">
		<g:message code="product.isBulk.label" default="Is Bulk" />
		
	</label>
	<g:checkBox name="isBulk" value="${productInstance?.isBulk}" />
</div>

<div class="fieldcontain ${hasErrors(bean: productInstance, field: 'disabled', 'error')} ">
	<label for="disabled">
		<g:message code="product.disabled.label" default="Disabled" />
		
	</label>
	<g:checkBox name="disabled" value="${productInstance?.disabled}" />
</div>

<div class="fieldcontain ${hasErrors(bean: productInstance, field: 'startProcess', 'error')} ">
	<label for="startProcess">
		<g:message code="product.startProcess.label" default="Start Process" />
		
	</label>
	<g:select name="startProcess.id" from="${com.glo.ndo.Process.list()}" optionKey="id" value="${productInstance?.startProcess?.id}" noSelection="['null': '']" />
</div>

<div class="fieldcontain ${hasErrors(bean: productInstance, field: 'productFamily', 'error')} required">
	<label for="productFamily">
		<g:message code="product.productFamily.label" default="Product Family" />
		<span class="required-indicator">*</span>
	</label>
	<g:select name="productFamily.id" from="${com.glo.ndo.ProductFamily.list()}" optionKey="id" value="${productInstance?.productFamily?.id}"  />
</div>

<div class="fieldcontain ${hasErrors(bean: productInstance, field: 'category', 'error')} ">
	<label for="category">
		<g:message code="product.category.label" default="Category" />
		
	</label>
	<g:textField name="category" value="${productInstance?.category}" style="width: 260px;" />
</div>

<div class="fieldcontain ${hasErrors(bean: productInstance, field: 'uom', 'error')} ">
	<label for="uom">
		<g:message code="product.uom.label" default="Uom" />
		
	</label>
	<g:textField name="uom" value="${productInstance?.uom}" style="width: 260px;" />
</div>

<div class="fieldcontain ${hasErrors(bean: productInstance, field: 'minQty', 'error')} ">
	<label for="minQty">
		<g:message code="product.minQty.label" default="Min Qty" />
		
	</label>
	<g:textField name="minQty" value="${fieldValue(bean: productInstance, field: 'minQty')}" />
</div>

<div class="fieldcontain ${hasErrors(bean: productInstance, field: 'maxQty', 'error')} ">
	<label for="maxQty">
		<g:message code="product.maxQty.label" default="Max Qty" />
		
	</label>
	<g:textField name="maxQty" value="${fieldValue(bean: productInstance, field: 'maxQty')}" />
</div>

<div class="fieldcontain ${hasErrors(bean: productInstance, field: 'usedFor', 'error')} ">
	<label for="usedFor">
		<g:message code="product.usedFor.label" default="Used For" />
		
	</label>
	<g:textField name="usedFor" value="${productInstance?.usedFor}" style="width: 260px;" />
</div>

<div class="fieldcontain ${hasErrors(bean: productInstance, field: 'bomParts', 'error')} ">
	<label for="bomParts">
		<g:message code="product.bomParts.label" default="Bom Parts" />
		
	</label>
	
<ul>
<g:each in="${productInstance?.bomParts?}" var="b">
    <li><g:link controller="bomPart" action="edit" id="${b.id}">${b?.encodeAsHTML()}</g:link></li>
</g:each>
</ul>
<g:link class="btn btn-green small" controller="bomPart" action="create" params="['product.id': productInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'bomPart.label', default: 'BomPart')])}</g:link>

</div>

<div class="fieldcontain ${hasErrors(bean: productInstance, field: 'boms', 'error')} ">
	<label for="boms">
		<g:message code="product.boms.label" default="Boms" />
		
	</label>
	
<ul>
<g:each in="${productInstance?.boms?}" var="b">
    <li><g:link controller="bom" action="edit" id="${b.id}">${b?.encodeAsHTML()}</g:link></li>
</g:each>
</ul>
<g:link class="btn btn-green small" controller="bom" action="create" params="['product.id': productInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'bom.label', default: 'Bom')])}</g:link>

</div>

<div class="fieldcontain ${hasErrors(bean: productInstance, field: 'productCompanies', 'error')} ">
	<label for="productCompanies">
		<g:message code="product.productCompanies.label" default="Product Companies" />
		
	</label>
	
<ul>
<g:each in="${productInstance?.productCompanies?}" var="p">
    <li><g:link controller="productCompany" action="edit" id="${p.id}">${p?.encodeAsHTML()}</g:link></li>
</g:each>
</ul>
<g:link class="btn btn-green small" controller="productCompany" action="create" params="['product.id': productInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'productCompany.label', default: 'ProductCompany')])}</g:link>

</div>

<div class="fieldcontain ${hasErrors(bean: productInstance, field: 'productMasks', 'error')} ">
	<label for="productMasks">
		<g:message code="product.productMasks.label" default="Product Masks" />
		
	</label>
	
<ul>
<g:each in="${productInstance?.productMasks?}" var="p">
    <li><g:link controller="productMask" action="edit" id="${p.id}">${p?.encodeAsHTML()}</g:link></li>
</g:each>
</ul>
<g:link class="btn btn-green small" controller="productMask" action="create" params="['product.id': productInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'productMask.label', default: 'ProductMask')])}</g:link>

</div>

