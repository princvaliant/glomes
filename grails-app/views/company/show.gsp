
<%@ page import="com.glo.ndo.Company" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'company.label', default: 'Company')}" />
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
                            <td valign="top" class="name"><g:message code="company.id.label" default="Id" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: companyInstance, field: "id")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="company.name.label" default="Name" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: companyInstance, field: "name")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="company.comment.label" default="Comment" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: companyInstance, field: "comment")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="company.equipments.label" default="Equipments" /></td>
                            
                            <td valign="top" style="text-align: left;" class="value">
                                <ul>
                                <g:each in="${companyInstance.equipments}" var="e">
                                    <li><g:link controller="equipment" action="show" id="${e.id}">${e?.encodeAsHTML()}</g:link></li>
                                </g:each>
                                </ul>
                            </td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="company.locations.label" default="Locations" /></td>
                            
                            <td valign="top" style="text-align: left;" class="value">
                                <ul>
                                <g:each in="${companyInstance.locations}" var="l">
                                    <li><g:link controller="location" action="show" id="${l.id}">${l?.encodeAsHTML()}</g:link></li>
                                </g:each>
                                </ul>
                            </td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="company.processSteps.label" default="Process Steps" /></td>
                            
                            <td valign="top" style="text-align: left;" class="value">
                                <ul>
                                <g:each in="${companyInstance.processSteps}" var="p">
                                    <li><g:link controller="processStep" action="show" id="${p.id}">${p?.encodeAsHTML()}</g:link></li>
                                </g:each>
                                </ul>
                            </td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="company.productCompanies.label" default="Product Companies" /></td>
                            
                            <td valign="top" style="text-align: left;" class="value">
                                <ul>
                                <g:each in="${companyInstance.productCompanies}" var="p">
                                    <li><g:link controller="productCompany" action="show" id="${p.id}">${p?.encodeAsHTML()}</g:link></li>
                                </g:each>
                                </ul>
                            </td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="company.users.label" default="Users" /></td>
                            
                            <td valign="top" style="text-align: left;" class="value">
                                <ul>
                                <g:each in="${companyInstance.users}" var="u">
                                    <li><g:link controller="user" action="show" id="${u.id}">${u?.encodeAsHTML()}</g:link></li>
                                </g:each>
                                </ul>
                            </td>
                            
                        </tr>
                    
                    </tbody>
                </table>
            </div>
            <p class="box">
                <g:form>
                    <g:hiddenField name="id" value="${companyInstance?.id}" />
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
