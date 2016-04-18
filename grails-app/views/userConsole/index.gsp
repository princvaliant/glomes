<html>
<head>
<meta name="layout" content="mainext" />
<link rel="stylesheet" href="${resource(dir:'js/ext/resources/css',file:'ext-all.css')}" />
<link rel="stylesheet" href="${resource(dir:'js/ext/resources/css',file:'icons.css')}" />
<g:javascript src="ext/ext-all.js"></g:javascript>
<g:javascript src="ext/src/ux/Message.js"></g:javascript>
<script>

Ext.Loader.setConfig({enabled: true});

Ext.Loader.setPath('glo', '<g:createLink url="js/app" />');
Ext.Loader.setPath('Ext.ux', '<g:createLink url="js/ext/src/ux" />');
fullAppFolder = '${grailsApplication.config.webdomain ?: "http://localhost:8080/glo"}/js/app';
rootFolder = '${grailsApplication.config.webdomain ?: "http://localhost:8080/glo"}/';
isReportGenerator = false;
</script>

</head>
<body>
<g:javascript src="canvas2image.js"></g:javascript>
<g:javascript src="rgbcolor.js"></g:javascript>
<g:javascript src="canvg.js"></g:javascript>
<g:javascript src="app/Application.js"></g:javascript>
<canvas id="canvas" width="1000px" height="600px" style="visible:false;"></canvas> 
</body>

</html>

