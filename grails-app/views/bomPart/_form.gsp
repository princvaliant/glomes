<%@ page import="com.glo.ndo.BomPart" %>



<div class="fieldcontain ${hasErrors(bean: bomPartInstance, field: 'partProduct', 'error')} required">
	<label for="partProduct">
		<g:message code="bomPart.partProduct.label" default="Part Product" />
		<span class="required-indicator">*</span>
	</label>
	<g:select name="partProduct.id" from="${com.glo.ndo.Product.list()}" optionKey="id" value="${bomPartInstance?.partProduct?.id}"  />
</div>

<div class="fieldcontain ${hasErrors(bean: bomPartInstance, field: 'quantity', 'error')} required">
	<label for="quantity">
		<g:message code="bomPart.quantity.label" default="Quantity" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="quantity" value="${fieldValue(bean: bomPartInstance, field: 'quantity')}" />
</div>

<div class="fieldcontain ${hasErrors(bean: bomPartInstance, field: 'bom', 'error')} required">
	<label for="bom">
		<g:message code="bomPart.bom.label" default="Bom" />
		<span class="required-indicator">*</span>
	</label>
	<g:select name="bom.id" from="${com.glo.ndo.Bom.list()}" optionKey="id" value="${bomPartInstance?.bom?.id}"  />
</div>

