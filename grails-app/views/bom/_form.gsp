<%@ page import="com.glo.ndo.Bom" %>



<div class="fieldcontain ${hasErrors(bean: bomInstance, field: 'assemblyProduct', 'error')} required">
	<label for="assemblyProduct">
		<g:message code="bom.assemblyProduct.label" default="Assembly Product" />
		<span class="required-indicator">*</span>
	</label>
	<g:select name="assemblyProduct.id" from="${com.glo.ndo.Product.list()}" optionKey="id" value="${bomInstance?.assemblyProduct?.id}"  />
</div>

<div class="fieldcontain ${hasErrors(bean: bomInstance, field: 'note', 'error')} ">
	<label for="note">
		<g:message code="bom.note.label" default="Note" />
		
	</label>
	<g:textField name="note" value="${bomInstance?.note}" style="width: 260px;" />
</div>

<div class="fieldcontain ${hasErrors(bean: bomInstance, field: 'revision', 'error')} ">
	<label for="revision">
		<g:message code="bom.revision.label" default="Revision" />
		
	</label>
	<g:textField name="revision" value="${bomInstance?.revision}" style="width: 260px;" />
</div>

<div class="fieldcontain ${hasErrors(bean: bomInstance, field: 'bomParts', 'error')} ">
	<label for="bomParts">
		<g:message code="bom.bomParts.label" default="Bom Parts" />
		
	</label>
	
<ul>
<g:each in="${bomInstance?.bomParts?}" var="b">
    <li><g:link controller="bomPart" action="edit" id="${b.id}">${b?.encodeAsHTML()}</g:link></li>
</g:each>
</ul>
<g:link class="btn btn-green small" controller="bomPart" action="create" params="['bom.id': bomInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'bomPart.label', default: 'BomPart')])}</g:link>

</div>

