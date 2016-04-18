
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
						<b>Growth#:</b>
						${runNumber}
					</p>
				</td>
				<td valign="top" nowrap=true>
					<p>
						<b>Wafer#:</b>
						${code}
					</p>
				</td>
				<td valign="top" nowrap=true>
					<p>
						<b>Pitch:</b>
						${pitch}
					</p>
				</td>
				<td valign="top" nowrap=true>
					<p><b>Description:</b></p>
				</td>
				<td valign="top" nowrap=true>
					<p>${note}</p>
				</td>
			</tr>

		</tbody>
	</table>
	</p>


	<table>
		<tbody>

			<tr class="prop">
				<td valign="top"><img
					src="${createLink(controller:'photo', action:'viewSem', params:[orient:'CENTER'])}" /><br />
					SEM center</td>
				<td valign="top"><img
					src="${createLink(controller:'photo', action:'viewSem', params:[orient:'TOP'])}" /><br />
					SEM top</td>
				<td valign="top"><img
					src="${createLink(controller:'photo', action:'viewSem', params:[orient:'BOTTOM'])}" /><br />
					SEM bottom</td>
				<td valign="top"></td>
			</tr>
		</tbody>
	</table>
	<table>
		<tbody>
			<tr class="prop">
				<td valign="top"><img
					src="${createLink(controller:'photo', action:'viewPl', params:[sz:400, orient:'.BMP'])}" /><br />
					PL spectrum center</td>
				<td valign="top"><img
					src="${createLink(controller:'photo', action:'viewPl', params:[sz:202, orient:'_spm_2.BMP'])}" /><br />
					PL MAP int</td>
				<td valign="top"><img
					src="${createLink(controller:'photo', action:'viewPl', params:[sz:202, orient:'_spm_1.BMP'])}" /><br />
					PL map WL</td>
				<td valign="top"><img
					src="${createLink(controller:'photo', action:'viewPl', params:[sz:300, orient:'_03_spm_5.BMP'])}" /><br />
					PL bright spot</td>
			</tr></tbody></table><table>
		<tbody>
			<tr class="prop">
				<td valign="top"><img
					src="${createLink(controller:'photo', action:'viewCl', params:[orient:'1'])}" /><br />
					Serial CL</td>
				<td valign="top"><img
					src="${createLink(controller:'photo', action:'viewCl', params:[orient:'2'])}" /><br />
					Mono CL 1</td>
				<td valign="top"><img
					src="${createLink(controller:'photo', action:'viewCl', params:[orient:'3'])}" /><br />
					Mono CL 2</td>
				<td valign="top"><img
					src="${createLink(controller:'photo', action:'viewCl', params:[orient:'4'])}" /><br />
					Mono CL 3</td>
			</tr></tbody>
	</table>
</body>
</html>
