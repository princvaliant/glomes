<html>
<head>
  <title>GLO MES</title>
  		<g:javascript src="extm/sencha-touch.js"></g:javascript>
        <link rel="stylesheet" href="${resource(dir:'js/extm/resources/css',file:'sencha-touch.css')}" />
</head>
<body>

<script type="text/javascript">

Ext.Loader.setConfig({enabled: true});
Ext.Loader.setPath('Ext', '<g:createLink url="js/extm/src" />');
fullAppFolder = '${grailsApplication.config.webdomain ?: "http://localhost:8080/glo"}/js/app';
rootFolder = '${grailsApplication.config.webdomain ?: "http://localhost:8080/glo"}/';

</script> 

<g:javascript  src="app/ApplicationM.js"/>
     
</body>
</html>
