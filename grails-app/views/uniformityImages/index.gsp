
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
	<p style="margin: 5; font-weight: bold; color: #ff0036;">
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
							<b>UNIFORMITY TEST VISUALIZATION</b>
						</p>
					</td>
				</tr>
			</tbody>
		</table>
	</p>

    <g:each in="${devices}" var="device">
        <div style="position:absolute; top:${device.value.y}px; left:${device.value.x}px;">
            <img  src="${createLink(controller:'uniformityImages', action:'view', params:[code:code, dev:device.key, path:device.value.path])}" alt="No images for ${code}_${device.key}" />
        </div>
    </g:each>
</body>
</html>
