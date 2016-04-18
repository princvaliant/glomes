
<% import grails.persistence.Event %>
<% import org.codehaus.groovy.grails.plugins.PluginManagerHolder %>
<%=packageName%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="\${message(code: '${domainClass.propertyName}.label', default: '${className}')}" />
<title><g:message code="default.edit.label" args="[entityName]" /></title>
</head>
<body>
<!-- Page commands -->
<div id="pagetitle">
<div class="wrapper">
<div class="nav"><g:link class="cmdlink" action="list">
	<g:message code="default.list.label" args="[entityName]" />
</g:link> <g:link class="cmdlink" action="create">
	<g:message code="default.new.label" args="[entityName]" />
</g:link></div>
</div>
</div>
<!-- End of Page Commands -->
<div id="pageBody">
<div class="wrapper"><section class="column width8 first">
<g:if
	test="\${flash.message}">
	<div class="message">\${flash.message}</div>
</g:if>
 <g:hasErrors bean="\${${propertyName}}" field="">
	<div class="errors"><g:renderErrors bean="\${${propertyName}}"
		as="list" field="" /></div>
</g:hasErrors>
<fieldset><legend> <g:message
	code="default.edit.label" args="[entityName]" /> </legend> 
 <g:form method="post"
	<%= multiPart ? ' enctype="multipart/form-data"' : '' %>>
	<g:hiddenField name="id" value="\${${propertyName}?.id}" />
	<g:hiddenField name="version" value="\${${propertyName}?.version}" />
	<div class="dialog">
	<table>
		<tbody>
			<%  excludedProps = Event.allEvents.toList() << 'version' << 'id' << 'dateCreated' << 'lastUpdated'
                            persistentPropNames = domainClass.persistentProperties*.name
                            props = domainClass.properties.findAll { persistentPropNames.contains(it.name) && !excludedProps.contains(it.name) }
                            Collections.sort(props, comparator.constructors[0].newInstance([domainClass] as Object[]))
                            display = true
                            boolean hasHibernate = PluginManagerHolder.pluginManager.hasGrailsPlugin('hibernate')
                            props.each { p ->
                                if (hasHibernate) {
                                    cp = domainClass.constrainedProperties[p.name]
                                    display = (cp?.display ?: true)
                                }
                                if (display) { %>
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="${p.name}"><g:message code="${domainClass.propertyName}.${p.name}.label" default="${p.naturalName}" /></label>
                                </td>
                                <td valign="top" class="value \${hasErrors(bean: ${propertyName}, field: '${p.name}', 'errors')}">
                                    ${renderEditor(p)}
                                </td>
                            </tr>
                        <%  }   } %>
		</tbody>
	</table>
	</div>
	<p class="box"><span class="button"><g:actionSubmit
		class="btn btn-green big" action="update"
		value="\${message(code: 'default.button.update.label', default: 'Update')}" /></span>
	<span class="button"><g:actionSubmit class="btn btn-red"
		action="delete"
		value="\${message(code: 'default.button.delete.label', default: 'Delete')}"
		onclick="return confirm('\${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
	</p>
</g:form></fieldset>
</section></div>
</div>
</body>
</html>
