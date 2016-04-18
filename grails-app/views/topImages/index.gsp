
<%@ page import="com.glo.security.User"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="mainext" />
<link rel="stylesheet" href="${resource(dir:'css',file:'style.css')}" />
<link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />
<style TYPE="text/css">
     p.breakhere {page-break-before: always}
</style>
</head>
<body>
	<!-- Page commands -->
	<p style="margin: 5; font-weight: bold; color: red;">
		<g:message code="${message}" />
	</p>
	<p class="box">
		<table>
			<tbody>
				<tr>
					<td valign="top" nowrap=true>
						<p>
							<b>Wafer#:</b>
							${code}
						</p>
					</td>

					<td valign="top" nowrap=true>
						<p>
							<b>TOP TEST VISUALIZATION</b>
						</p>
					</td>
				</tr>
			</tbody>
		</table>
	</p>

 	<h2>Images of top devices</h2>
	<table>
		<tbody>
    		<g:each in="${tops}" var="t">
    		<tr class="prop">
            <td><img
					src="${createLink(controller:'topImages', action:'view', params:[code:code, dev:t])}" alt="No images for ${code}_${t}" /></td>
            </tr>
     		 </g:each>
     	</tbody>
	</table>		 
    <h2>MASK6 PCM images</h2>
	<table>
		<tbody>
     		 <g:each in="${pcms}" var="pp">
	    		<tr class="prop">
	            <td>       
	            <img
						src="${createLink(controller:'topImages', action:'view', params:[code:code, dev:pp])}" alt="No images for ${code}_${pp}" /></td>
	            </tr>
     		 </g:each>
		</tbody>
	</table>
</body>
</html>
