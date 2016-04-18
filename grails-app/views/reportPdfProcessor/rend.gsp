<html>
<head>
<link rel="stylesheet" href="${resource(dir:'js/ext/resources/css',file:'ext-all.css')}" />
<link rel="stylesheet" href="${resource(dir:'js/ext/resources/css',file:'icons.css')}" />
    <g:javascript src="ext/ext-all.js"></g:javascript>
    <g:javascript src="ext/src/ux/Message.js"></g:javascript>
    <script>
        Ext.Loader.setConfig({enabled: true});
        Ext.Loader.setPath('glo', '<g:createLink url="/glo/js/app" />');
        Ext.Loader.setPath('Ext.ux', '<g:createLink url="/glo/js/ext/src/ux" />');
        fullAppFolder = '${grailsApplication.config.webdomain ?: "http://localhost:8080/glo"}/js/app';
        rootFolder = '${grailsApplication.config.webdomain ?: "http://localhost:8080/glo"}/';
        isReportGenerator = true;
    </script>
    <g:javascript src="app/Application.js"></g:javascript>
</head>
<body>
<script>
    Ext.onReady(function(){

        var getParams = document.URL.split("?");
        var parms = Ext.urlDecode(getParams[getParams.length - 1]);

        var panelChart = Ext.create("Ext.panel.Panel", {
            x: 0,
            y: 0,
            width: (parms.type === 'SPC' ? 1250 : 1000),
            height: (parms.type === 'SPC' ? 2000 : 1000),
            layout: 'fit',
            renderTo : Ext.getBody()
        });

        setTimeout(function(){
            alert("Hello");
        }, 300000);

        var date  =  parms.date;
        if (parms.type == "SPC") {
            panelChart.spcId = parms.id;
            gloApp.getController('SpcPanel').loadCharts(false, panelChart, date);
        } else {
            gloApp.getController('DashboardPanel').refreshData(panelChart, parms.id, 'load', date);
        }

        gloApp.on('chartloaded',  function(id, date, panelChart, chart, type) {

            var svg = Ext.draw.engine.SvgExporter.generate(chart.surface);
            svg = svg.substring(svg.indexOf('<svg'));

            Ext.Ajax.request({
                url : rootFolder + 'reportPdfProcessor/save',
                method : 'POST',
                params : {
                    id: id,
                    type: type,
                    date: date,
                    svg: svg
                },
                success : function(response) {
                    alert('alert');
                }
            });
        });
    });
</script>

</body>
</html>

