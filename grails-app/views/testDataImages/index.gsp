<html>
<head>
<meta name="layout" content="mainext" />
<link rel="stylesheet" href="${resource(dir:'js/ext/resources/css',file:'ext-all.css')}" />
<link rel="stylesheet" href="${resource(dir:'js/ext/resources/css',file:'icons.css')}" />
    <g:javascript src="ext/ext-all.js"></g:javascript>
    <g:javascript src="ext/src/ux/Message.js"></g:javascript>
    <script>
        Ext.Loader.setConfig({enabled: true});
        fullAppFolder = '${grailsApplication.config.webdomain ?: "http://localhost:8080/glo"}/js/app';
        rootFolder = '${grailsApplication.config.webdomain ?: "http://localhost:8080/glo"}/';

        Ext.onReady(function(){

            // separating the GET parameters from the current URL
            var getParams = document.URL.split("?");
            // transforming the GET parameters into a dictionnary
            var params = Ext.urlDecode(getParams[getParams.length - 1]);

            var drawComponent = Ext.create('Ext.draw.Component', {
                width: 780,
                height: 700,
                viewBox: false,
                autoShow: true,
                autoRender: true,
                renderTo: Ext.getBody()
            });

            // If response not received in 5mins ignore and continue
            setTimeout(function(){
                alert("Hello");
            }, 300000);

            Ext.Ajax.request({
                method: 'GET',
                params: {
                    source: 'LIST',
                    code: params.code,
                    tkey: params.tkey,
                    testId: params.testId,
                    propertyName: '',
                    level1: params.level1,
                    level2: params.level2,
                    minSpec: '',
                    maxSpec: '',
                    act: 'U',
                    spec: '',
                    top5: false,
                    bypassAdminFilter: false
                },
                url: rootFolder + 'wafer/map',
                success: function (response) {

                    Ext.suspendLayouts();
                    var obj = Ext.decode(response.responseText);
                    Ext.Array.each(obj.devices, function(device, index, itSelf) {
                        drawComponent.surface.add(device).show(true);
                    });
                    Ext.resumeLayouts();

                    var svg = Ext.draw.engine.SvgExporter.generate(drawComponent.surface);
                    svg = svg.substring(svg.indexOf('<svg'));

                    Ext.Ajax.request({
                        scope : this,
                        method : 'POST',
                        params : {
                            image: params.image,
                            svg: svg
                        },
                        url : rootFolder + 'testDataImages/save',
                        success : function(response) {
                            alert('alert');
                        }
                    });
                }
            });
        });

</script>

</head>
<body style="margin-top: 0px; margin-left: 0px; margin-right: 0px;">

</body>
</html>

