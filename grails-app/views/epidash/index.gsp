
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
	<table>
		<tbody>

			<tr class="prop">
				<td valign="top"><img style="width:350;"
					src="${createLink(controller:'epidash', action:'viewMap', params:[f:'M_10MA_EQE'])}" /><br />
					Wafer map EQE @ 10mA</td>
				<td valign="top"><img style="width:350;"
					src="${createLink(controller:'epidash', action:'viewMap', params:[f:'M_20MA_EQE'])}" /><br />
					Wafer map EQE @ 20mA</td>
				<td valign="top">
				<table>
					<tbody>
						<tr>
							<td valign="top" nowrap=true>
								<p>
									<b>Growth#:</b>
									${runNumber}
								</p>
							</td>
						</tr>
						<tr>
							<td valign="top" nowrap=true>
								<p>
									<b>Exp#:</b>
									${experimentId}
								</p>
							</td>
						</tr>
						<tr>
							<td valign="top" nowrap=true>
								<p>
									<b>FAB split:</b>
									${fabProcess}
								</p>
							</td>
						</tr>
						<tr>
							<td valign="top" nowrap=true>
								<p>
									<b>Wafer#:</b>
									${code}
								</p>
							</td>
						</tr>
						<tr>
							<td valign="top" nowrap=true>
								<p>
									<b>Pitch:</b>
									${pitch}
								</p>
							</td>
						</tr>
					</tbody>
				</table>
			</td>
			</tr>
		</tbody>
	</table>
	<table>
		<tbody>

			<tr class="prop">
				<td valign="top"><img style="width:370;"
					src="${createLink(controller:'epidash', action:'viewMap', params:[f:'O_10MA_EQE_PEAK'])}" /><br />
					EQE vs PEAK (nm) @ 10 mA</td>
				<td valign="top"><img style="width:370;"
					src="${createLink(controller:'epidash', action:'viewMap', params:[f:'O_20MA_EQE_PEAK'])}" /><br />
					EQE vs PEAK (nm) @ 20 mA</td>
				<td valign="top"><img style="width:350;"
					src="${createLink(controller:'epidash', action:'viewMap', params:[f:'V_20MA_EQE'])}" /><br />
					Voltage sweep @ 20mA</td>
			</tr>
		</tbody>
	</table>
	<p class="breakhere"/>
	<table>
		<tbody>

			<tr class="prop">
				<td valign="top"><img style="width:350;"
					src="${createLink(controller:'epidash', action:'viewMap', params:[f:'S_10MA_EQE'])}" /><br />
					Spectrum chart @ 10 mA</td>
				<td valign="top"><img style="width:350;"
					src="${createLink(controller:'epidash', action:'viewMap', params:[f:'S_20MA_EQE'])}" /><br />
					Spectrum chart @ 20 mA</td>
				<td valign="top"><img style="width:350;"
					src="${createLink(controller:'epidash', action:'viewMap', params:[f:'C_20MA_EQE'])}" /><br />
					Current dependence</td>
				<td valign="top"></td>
			</tr>
		</tbody>
	</table>
	<table>
		<tbody>
			<tr class="prop">
				<td valign="top"><img style="width:350;"
					src="${createLink(controller:'epidash', action:'viewMap', params:[f:'M_10MA_EQE_TOP5'])}" /><br />
					Wafer map Top 5 EQE @ 10 mA</td>
				<td valign="top"><img style="width:350;"
					src="${createLink(controller:'epidash', action:'viewMap', params:[f:'M_20MA_EQE_TOP5'])}" /><br />
					Wafer map Top 5 EQE @ 20 mA</td>
				<td valign="top"><img style="width:350;"
					src="${createLink(controller:'epidash', action:'viewMap', params:[f:'M_100MA_EQE_TOP5'])}" /><br />
					Wafer map Top 5 EQE @ 100 mA</td>
				<td valign="top"></td>
			</tr>
		</tbody>
	</table>
	<p class="breakhere"/>
	<table>
		<tbody>
			<tr class="prop">
				<td valign="top"><img style="width:350;"
					src="${createLink(controller:'epidash', action:'viewMap', params:[f:'S_10MA_EQE_TOP5'])}" /><br />
					Spectrum Top 5 EQE @ 10 mA</td>
				<td valign="top"><img style="width:350;"
					src="${createLink(controller:'epidash', action:'viewMap', params:[f:'S_20MA_EQE_TOP5'])}" /><br />
					Spectrum Top 5 EQE @ 20 mA</td>
				<td valign="top"><img style="width:350;"
					src="${createLink(controller:'epidash', action:'viewMap', params:[f:'S_100MA_EQE_TOP5'])}" /><br />
					Spectrum Top 5 EQE @ 100 mA</td>
				<td valign="top"></td>
			</tr>
		</tbody>
	</table>
	<table>
		<tbody>
			<tr class="prop">
				<td valign="top"><img style="width:350;"
					src="${createLink(controller:'epidash', action:'viewMap', params:[f:'V_20MA_EQE_TOP5'])}" /><br />
					Voltage sweep Top 5 EQE @ 20 mA</td>
				<td valign="top"><img style="width:350;"
					src="${createLink(controller:'epidash', action:'viewMap', params:[f:'C_20MA_EQE_TOP5'])}" /><br />
					Current dependence Top 5</td>
				<td valign="top"><img style="width:350;">
					</td>
				<td valign="top"></td>
			</tr>
		</tbody>
	</table>
</body>
</html>
